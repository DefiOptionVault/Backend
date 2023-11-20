package com.DefiOptionVault.DOV.Strike;

import com.DefiOptionVault.DOV.Option.Option;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "STRIKES")
public class Strike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STRIKE_ID", nullable = false)
    private Integer strikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_ID", referencedColumnName = "OPTION_ID", nullable = false)
    private Option option;

    @Column(name = "STRIKE_PRICE", length = 64)
    private String strikePrice;

    @Column(name = "OPTION_PRICE", length = 64)
    private String optionPrice;

    @Column(name = "STRIKE_INDEX", nullable = false)
    private int strikeIndex;

    public Option getOption() {
        return this.option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public Integer getStrikeId() {
        return strikeId;
    }

    public void setStrikeId(Integer strikeId) {
        this.strikeId = strikeId;
    }

    public String getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(String strikePrice) {
        this.strikePrice = strikePrice;
    }

    public String getOptionPrice() {
        return optionPrice;
    }

    public void setOptionPrice(String optionPrice) {
        this.optionPrice = optionPrice;
    }

    public int getStrikeIndex() {
        return strikeIndex;
    }

    public void setStrikeIndex(int strikeIndex) {
        this.strikeIndex = strikeIndex;
    }
}