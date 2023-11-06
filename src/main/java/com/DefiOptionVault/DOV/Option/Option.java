package com.DefiOptionVault.DOV.Option;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "OPTIONS")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTION_ID")
    private int optionId;

    @Column(name = "OPTION_ADDRESS", length = 42, nullable = false)
    private String optionAddress;

    @Column(name = "BASE_ASSET", length = 10, nullable = false)
    private String baseAsset;

    @Column(name = "COLLATERAL_ASSET", length = 10, nullable = false)
    private String collateralAsset;

    @Column(name = "EXPIRY")
    private Timestamp expiry;

    @Column(name = "SYMBOL", length = 20)
    private String symbol;

    @Column(name = "ROUND", nullable = false)
    private int round;

    // Getters
    public int getOptionId() {
        return optionId;
    }

    public String getOptionAddress() {
        return optionAddress;
    }

    public String getBaseAsset() {
        return baseAsset;
    }

    public String getCollateralAsset() {
        return collateralAsset;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getRound() {
        return round;
    }

    // Setters
    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public void setOptionAddress(String optionAddress) {
        this.optionAddress = optionAddress;
    }

    public void setBaseAsset(String baseAsset) {
        this.baseAsset = baseAsset;
    }

    public void setCollateralAsset(String collateralAsset) {
        this.collateralAsset = collateralAsset;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setRound(int round) {
        this.round = round;
    }
}

