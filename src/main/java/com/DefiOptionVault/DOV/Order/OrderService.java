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

    private static final BigInteger UNIT = new BigInteger("1000000000000000000");

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
                BigInteger pnl;
                try {
                    pnl = new BigInteger(order.getPnl());
                } catch (NumberFormatException e) {
                    pnl = BigInteger.ZERO;
                }
                if (order.getSettlementPrice().equals("0")) {
                    result.add(order);
                } else if (!order.getSettled()) {
                    if (order.getPosition().equals("write")) {
                        result.add(order);
                    }
                    if (order.getPosition().equals("purchase")
                            && pnl.compareTo(BigInteger.ZERO) > 0) {
                        result.add(order);
                    }
                }
            }
        }

        return result;
    }

    @Transactional
    public BigInteger calcPnl(Order order) {
        BigInteger orderSettle = new BigInteger(order.getSettlementPrice());
        BigInteger orderStrike = new BigInteger(order.getStrikePrice());
        BigInteger amount = new BigInteger(String.valueOf(order.getAmount()));
        BigInteger newPnl = BigInteger.ZERO;
        if (order.getPosition().equals("purchase")) {
            newPnl = (orderStrike.subtract(orderSettle)).multiply(amount);
            if (newPnl.signum() == -1) {
                newPnl = BigInteger.ZERO;
            }
        }
        if (order.getPosition().equals("write")) {
            newPnl = (orderSettle.subtract(orderStrike)).multiply(amount);
            if (newPnl.signum() == -1) {
                newPnl = BigInteger.ZERO;
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

                    BigInteger price = new BigInteger(String.valueOf(strikeService.getCurrentAssetPrice()));
                    order.setSettlementPrice(String.valueOf(price.multiply(UNIT)));

                    BigInteger strike = new BigInteger(order.getStrikePrice());
                    BigInteger pnl = price.subtract(strike);

                    if (pnl.signum() == -1) {
                        order.setPnl(String.valueOf(0));
                    } else {
                        order.setPnl(String.valueOf(pnl));
                    }
                }
                if (symbol.substring(symbol.length() - 4).equals("CALL")) {
                    BigInteger settlement = new BigInteger(order.getSettlementPrice());
                    BigInteger strike = new BigInteger(order.getStrikePrice());
                    order.setPnl(String.valueOf(strike.subtract(settlement)));
                }
            }
        }
    }
}


//openedOrder.get().
//settlementPrice : 만기 시 가격으로 업뎃
//Pnl : strikePrice 에서 빼서 넣기
//strikePrice - settelmentPrice(put옵션 이익)
//큰 경우는 Pnl 0