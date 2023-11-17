package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Order.Order;
import com.DefiOptionVault.DOV.Order.OrderRepository;
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

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

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

    public List<Order> addOpenedPosition() {
        List<Order> orders = getAllOrders();
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            BigInteger pnl;
            try {
                pnl = new BigInteger(order.getPnl());
            } catch (NumberFormatException e) {
                pnl = BigInteger.ZERO;
            }
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime expiry = order.getOption()
                    .getExpiry()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            if (expiry.isAfter(now)) {
                result.add(order);
            } else if(!order.getSettled()) {
                if (order.getPosition().equals("write")) {
                    result.add(order);
                }
                if (order.getPosition().equals("purchase")
                        && pnl.compareTo(BigInteger.ZERO) > 0) {
                    result.add(order);
                }
            }
        }

        return result;
    }
/*
    public void popOpenedPosition(Order order) {
        Optional<Order> openedOrder = getOrderById(order.getOrderId());

        if (openedOrder.isPresent() && openedPosition.contains(openedOrder.get())) {
            openedPosition.remove(openedOrder.get());
            //openedOrder.get().
            //settlementPrice : 만기 시 가격으로 업뎃
            //Pnl : strikePrice 에서 빼서 넣기
            //strikePrice - settelmentPrice(put옵션 이익)
            //큰 경우는 Pnl 0
            historicalPosition.add(openedOrder.get());
        }
    }
*/
}