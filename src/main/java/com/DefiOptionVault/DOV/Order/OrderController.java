package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Option.OptionRepository;
import com.DefiOptionVault.DOV.Option.OptionService;
import com.DefiOptionVault.DOV.Strike.Strike;
import com.DefiOptionVault.DOV.Strike.StrikeService;
import com.DefiOptionVault.DOV.Strike.Web3jService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private OptionService optionService;
    @Autowired
    private Web3jService web3jService;
    @Autowired
    private StrikeService strikeService;

    private static final BigDecimal UNIT = new BigDecimal("1000000000000000000");

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    ///api/orders/openedPosition?address={}
    @GetMapping("/openedPosition")
    public List<Order> getOpenedPosition(@RequestParam String address) {
        return orderService.showOpenedPosition(address);
    }

    ///api/orders/historicalPosition?address={}
    @GetMapping("/historicalPosition")
    public List<Order> getHistoricalPosition(@RequestParam String address) {
        List<Order> orders = orderService.getAllOrders();
        List<Order> opened = orderService.showOpenedPosition(address);
        List<Order> result = new ArrayList<>();

        for(Order order : orders) {
            if (!opened.contains(order)
                    && order.getClientAddress().equals(address)) {
                result.add(order);
            }
        }

        return result;
    }

    @GetMapping("/{id}")
    public Optional<Order> getOrderById(@PathVariable Integer id) {
        return orderService.getOrderById(id);
    }

    ///api/orders/allPnl?address={}
    @GetMapping("/allPnl")
    public BigDecimal getAllPnl(@RequestParam String address) {
        List<Order> historicalPosition = getHistoricalPosition(address);
        BigDecimal sum = BigDecimal.valueOf(0);
        for (Order order : historicalPosition) {
            sum = sum.add(new BigDecimal(order.getPnl()));
        }
        return sum;
    }

    @PostMapping("/sendPosition")
    public Order createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        Order order = new Order();

        Option option = optionRepository.findById(orderRequestDTO.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));
        order.setOption(option);

        int strikeIndex = orderService
                .findStrikeIndexByStrikePrice(option, orderRequestDTO.getStrikePrice());

        order.setAmount(orderRequestDTO.getAmount());
        order.setPosition(orderRequestDTO.getPosition());
        order.setStrikePrice(orderRequestDTO.getStrikePrice());
        order.setStrikeIndex(strikeIndex);
        order.setClientAddress(orderRequestDTO.getClientAddress());
        order.setSymbol(option.getSymbol());
        order.setOrderTime(new Timestamp(System.currentTimeMillis()));
        order.setSettlementPrice("0");
        order.setPnl("0");
        order.setSettled(false);
        order.setTokenId(orderRequestDTO.getTokenId());

        return orderService.saveOrder(order);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
    }

    /*
    @GetMapping("/bootstrap")
    public void bootstrap() {
        web3jService.bootstrap();
    }
    */

    @GetMapping("/getBalance")
    public BigInteger getBalance() {
        return web3jService.BalanceOf();
    }

    @Transactional
    @PostMapping("/expire/{settlementPrice}/{optionId}")
    public void expire(@PathVariable double settlementPrice, @PathVariable int optionId) {
        BigDecimal tmpBD = BigDecimal.valueOf(settlementPrice);
        tmpBD = tmpBD.multiply(new BigDecimal("1000000000000000000"))
                .setScale(0, RoundingMode.DOWN);
        BigInteger bigSettlePrice = tmpBD.toBigInteger();
        web3jService.expire(bigSettlePrice);

        List<Order> orders = orderService.getAllOrders();
        for (Order order : orders) {
            if (order.getOption().getOptionId() == optionId) {
                order.setSettlementPrice(String.valueOf(settlementPrice));
                BigDecimal pnl = orderService.calcPnl(order);
                order.setPnl(pnl.toString());
                orderRepository.save(order);
            }
        }

        Option newOption = optionService.generateNextRoundOption(optionId);
        BigDecimal[] strikes = strikeService.createNewStrikes(newOption);
        BigInteger[] strikesForBootstrap = new BigInteger[4];
        for (int i = 0; i < 4; i++) {
            strikesForBootstrap[i] = strikes[i]
                    .multiply(UNIT)
                    .setScale(0, RoundingMode.DOWN)
                    .toBigInteger();
        }

        web3jService.bootstrap(
                strikesForBootstrap,
                BigInteger.valueOf(newOption.getExpiry().getTime()),
                newOption.getSymbol());
    }

    @Transactional
    @PostMapping("/updateSettled/{orderId}")
    public void updateSettled(@PathVariable int orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(NoSuchElementException::new);
        order.setSettled(true);
        orderRepository.save(order);
    }
}