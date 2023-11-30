package com.DefiOptionVault.DOV.Strike;

import java.math.BigDecimal;
import java.util.List;

public class HistoricalVolatilityResponse {

    private List<List<BigDecimal>> result;

    public List<List<BigDecimal>> getResult() {
        return result;
    }

    public void setResult(List<List<BigDecimal>> result) {
        this.result = result;
    }
}
