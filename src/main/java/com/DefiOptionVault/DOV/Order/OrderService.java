package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Order.Order;
import com.DefiOptionVault.DOV.Order.OrderRepository;
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

    private List<Order> openedPosition;
    private List<Order> historicalPosition;

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

    public void addOpenedPosition(Order order) {
        openedPosition.add(order);
    }

    public void addHistoricalPosition(Order order) {
        historicalPosition.add(order);
    }

    public void popOpenedPosition(Order order) {
        Optional<Order> openedOrder = getOrderById(order.getOrderId());

        if (openedOrder.isPresent() && openedPosition.contains(openedOrder.get())) {
            openedPosition.remove(openedOrder.get());
            historicalPosition.add(openedOrder.get());
        }
    }



    @Scheduled(cron = "59 59 23 * * SUN")
    public void processExpiredOrders() {
        List<Order> expiredOrders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        for (Order order : openedPosition) {
            LocalDateTime expiry = order.getOption()
                    .getExpiry()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            if (expiry.isBefore(now)) {
                expiredOrders.add(order);
            }
        }

        openedPosition.removeAll(expiredOrders);
        historicalPosition.addAll(expiredOrders);
    }

    public List<Order> getOpenedPosition() {
        return openedPosition;
    }

    public List<Order> getHistoricalPosition() {
        return historicalPosition;
    }
}