package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Option.Option;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_ID", referencedColumnName = "OPTION_ID", nullable = false)
    private Option option;

    @Column(name = "SYMBOL", length = 20)
    private String symbol;

    @Column(name = "POSITION", length = 10)
    private String position;

    @Column(name = "STRIKE_PRICE", length = 64)
    private String strikePrice;

    @Column(name = "SETTLEMENT_PRICE", length = 64)
    private String settlementPrice;

    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @Column(name = "PNL", length = 64)
    private String pnl;

    @Column(name = "ORDER_TIME")
    private Timestamp orderTime;

    @Column(name = "CLIENT_ADDRESS", length = 42)
    private String clientAddress;
}