package com.DefiOptionVault.DOV.Strike;

import java.math.BigDecimal;
import java.util.List;

public class HistoricalVolatilityResponse {

    private List<BigDecimal> result;

    public List<BigDecimal> getResult() {
        return result;
    }

    public void setResult(List<BigDecimal> result) {
        this.result = result;
    }
}
