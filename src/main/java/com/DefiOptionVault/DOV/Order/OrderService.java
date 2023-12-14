package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Order.Order;
import com.DefiOptionVault.DOV.Order.OrderRepository;
import com.DefiOptionVault.DOV.Strike.Strike;
import com.DefiOptionVault.DOV.Strike.StrikeService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StrikeService strikeService;

    private static final BigDecimal UNIT = new BigDecimal("1000000000000000000");

    public BigDecimal getUNIT() {
        return UNIT;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    public int findStrikeIndexByStrikePrice(Option option, String strikePrice) {
        List<Strike> strikes = strikeService.getStrikesByOptionId(option.getOptionId());
        int index = -1;
        for(Strike strike : strikes) {
            if (strike.getStrikePrice().equals(strikePrice)) {
                index = strike.getStrikeIndex();
            }
        }
        if (index == -1){
            throw new NoSuchElementException("There's no Strike Price");
        }
        return index;
    }

    public List<Order> showOpenedPosition(String client) {
        List<Order> orders = getAllOrders();
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getClientAddress().equals(client)) {
                BigDecimal pnl;
                try {
                    pnl = new BigDecimal(order.getPnl());
                } catch (NumberFormatException e) {
                    pnl = BigDecimal.ZERO;
                }
                if (order.getSettlementPrice().equals("0")) {
                    result.add(order);
                } else if (!order.getSettled()) {
                    if (order.getPosition().equals("write")) {
                        result.add(order);
                    }
                    if (order.getPosition().equals("purchase")
                            && pnl.compareTo(BigDecimal.ZERO) > 0) {
                        result.add(order);
                    }
                }
            }
        }

        return result;
    }

    @Transactional
    public BigDecimal calcPnl(Order order) {
        BigDecimal orderSettle = new BigDecimal(order.getSettlementPrice());
        BigDecimal orderStrike = new BigDecimal(order.getStrikePrice());
        BigDecimal amount = new BigDecimal(String.valueOf(order.getAmount()));
        BigDecimal newPnl = BigDecimal.ZERO;
        if (order.getPosition().equals("purchase")) {
            newPnl = (orderStrike.subtract(orderSettle)).multiply(amount);
            if (newPnl.signum() == -1) {
                newPnl = BigDecimal.ZERO;
            }
        }
        if (order.getPosition().equals("write")) {
            newPnl = (orderSettle.subtract(orderStrike)).multiply(amount);
            if (newPnl.signum() == -1) {
                newPnl = BigDecimal.ZERO;
            }
        }
        return newPnl;
    }

    @Transactional
    @Scheduled(cron = "0 59 23 * * SUN")
    public void setAllPnl() {
        List<Order> orders = getAllOrders();
        for (Order order : orders) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime expiry = order.getOption()
                    .getExpiry()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            if (expiry.isBefore(now) && order.getPnl().equals("0")) {
                String symbol = order.getSymbol();
                if (symbol.substring(symbol.length() - 3).equals("PUT")) {

                    BigDecimal price = new BigDecimal(String.valueOf(strikeService.getCurrentAssetPrice()));
                    order.setSettlementPrice(String.valueOf(price));

                    BigDecimal strike = new BigDecimal(order.getStrikePrice());
                    BigDecimal pnl = price.subtract(strike);

                    if (pnl.signum() == -1) {
                        order.setPnl(String.valueOf(0));
                    } else {
                        order.setPnl(String.valueOf(pnl));
                    }
                }
                if (symbol.substring(symbol.length() - 4).equals("CALL")) {
                    BigDecimal settlement = new BigDecimal(order.getSettlementPrice());
                    BigDecimal strike = new BigDecimal(order.getStrikePrice());
                    order.setPnl(String.valueOf(strike.subtract(settlement)));
                }
            }
            orderRepository.save(order);
        }
    }
}
