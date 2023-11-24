package com.DefiOptionVault.DOV.Strike;

import java.math.BigDecimal;

public class IndexPriceResult {

    private BigDecimal index_price;
    private BigDecimal estimated_delivery_price;

    public BigDecimal getIndex_price() {
        return index_price;
    }

    public BigDecimal getEstimated_delivery_price() {
        return estimated_delivery_price;
    }

    public void setIndex_price(BigDecimal price) {
        this.index_price = price;
    }

    public void setEstimated_delivery_price(BigDecimal price) {
        this.estimated_delivery_price = price;
    }
}
