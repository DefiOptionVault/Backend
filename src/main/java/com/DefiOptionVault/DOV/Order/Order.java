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

    @Column(name = "SETTLED")
    private boolean settled;

    @Column(name = "TOKEN_ID", length = 64)
    private String tokenId;

    public Integer getOrderId() {
        return orderId;
    }

    public Option getOption() {
        return option;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPosition() {
        return position;
    }

    public String getStrikePrice() {
        return strikePrice;
    }

    public String getSettlementPrice() {
        return settlementPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getPnl() {
        return pnl;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public boolean getSettled() {
        return settled;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setStrikePrice(String strikePrice) {
        this.strikePrice = strikePrice;
    }

    public void setSettlementPrice(String settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setPnl(String pnl) {
        this.pnl = pnl;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}