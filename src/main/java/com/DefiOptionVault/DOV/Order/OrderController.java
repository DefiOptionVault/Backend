package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Order.OrderService;
import java.math.BigInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private Web3jService web3jService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Optional<Order> getOrderById(@PathVariable Integer id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
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
    public void bootstrap(@Value("${ethereum.rpc.url}") String rpcUrl) {
        web3jService.bootstrap();
    }

    @GetMapping("/getBalance")
    public BigInteger getBalance() {
        return web3jService.BalanceOf();
    }
}