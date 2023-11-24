package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Option.OptionRepository;
import com.DefiOptionVault.DOV.Option.OptionService;
import com.DefiOptionVault.DOV.Strike.StrikeService;
import com.DefiOptionVault.DOV.Strike.Web3jService;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    private OptionRepository optionRepository;
    @Autowired
    private OptionService optionService;
    @Autowired
    private Web3jService web3jService;
    @Autowired
    private StrikeService strikeService;

    private static final BigInteger UNIT = new BigInteger("1000000000000000000");

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
    public BigInteger getAllPnl(@RequestParam String address) {
        List<Order> historicalPosition = getHistoricalPosition(address);
        BigInteger sum = BigInteger.valueOf(0);
        for (Order order : historicalPosition) {
            sum = sum.add(new BigInteger(order.getPnl()));
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
        tmpBD = tmpBD.multiply(new BigDecimal("1e18"));
        BigInteger bigSettlePrice = tmpBD.toBigInteger();
        web3jService.expire(bigSettlePrice);

        List<Order> orders = orderService.getAllOrders();

        for (Order order : orders) {
            if (order.getOption().getOptionId() == optionId) {
                order.setSettled(true);
                order.setSettlementPrice(bigSettlePrice.toString());
                BigInteger pnl = orderService.calcPnl(order);
                order.setPnl(pnl.toString());
            }
        }
        BigDecimal nowPrice = strikeService.getCurrentAssetPrice();
        Option option = optionService.getOptionById(optionId).orElseThrow(NoSuchElementException::new);;
        String expirySymbol = option.getSymbol();

        ZonedDateTime nextSunday = ZonedDateTime.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59);
        Timestamp tmp = Timestamp.from(nextSunday.toInstant());
        BigInteger newExpiry = BigInteger.valueOf(tmp.getTime());

        BigInteger[] strikes = new BigInteger[4];
        BigInteger base = nowPrice.toBigInteger();
        strikes[0] = base.subtract(new BigInteger("100"));
        strikes[1] = base.subtract(new BigInteger("50"));
        strikes[2] = base.add(new BigInteger("50"));
        strikes[3] = base.add(new BigInteger("100"));

        web3jService.bootstrap(strikes, newExpiry, expirySymbol);
    }

    @Transactional
    @PostMapping("/updateSettled/{orderId}")
    public void updateSetteled(@PathVariable int orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(NoSuchElementException::new);
        System.out.println(order.getSettled());
        order.setSettled(true);
        System.out.println(order.getSettled());
    }
}