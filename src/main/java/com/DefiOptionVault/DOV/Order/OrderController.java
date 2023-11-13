package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Order.OrderService;
import java.math.BigInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private Web3jService web3jService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/openedPosition")
    public List<Order> getOpenedPosition() {
        return orderService.getOpenedPosition();
    }

    @GetMapping("/historicalPosition")
    public List<Order> getHistoricalPosition() {
        return orderService.getHistoricalPosition();
    }

    @GetMapping("/{id}")
    public Optional<Order> getOrderById(@PathVariable Integer id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/allPnl")
    public BigInteger getAllPnl() {
        List<Order> historicalPosition = getHistoricalPosition();
        BigInteger sum = BigInteger.valueOf(0);
        for (Order order : historicalPosition) {
            sum = sum.add(new BigInteger(order.getPnl()));
        }
        return sum;
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        orderService.addOpenedPosition(order);
        return orderService.saveOrder(order);
    }

    /*
    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Integer id, @RequestBody Order updatedOrder) {
        Order order = orderService.getOrderById(id).orElse(null);
        if (order != null) {
            order.setStrikePrice(updatedOrder.getStrikePrice());
            order.setAmount(updatedOrder.getAmount());
            order.setSettlementPrice(updatedOrder.getSettlementPrice());
            order.setPnl(updatedOrder.getPnl());
            order.setOrderTime(updatedOrder.getOrderTime());
            return orderService.saveOrder(order);
        }
        return null;
    }
    */

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
    }

    @GetMapping("/bootstrap")
    public void bootstrap() {
        web3jService.bootstrap();
    }

    @GetMapping("/getBalance")
    public BigInteger getBalance() {
        return web3jService.BalanceOf();
    }

    @PostMapping("/expire/{settlementPrice}")
    public void expire(@PathVariable BigInteger settlementPrice) {
        web3jService.expire(settlementPrice);
    }
}