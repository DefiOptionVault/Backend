package com.DefiOptionVault.DOV.Option;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
/*
@Service
public class OptionPricingService {
    private static final String DERIBIT_VOLATILITY_API_URL = "https://www.deribit.com/api/v2/public/get_historical_volatility?currency=ETH";
    private static final String DERIBIT_CURRENT_PRICE_API_URL = "https://www.deribit.com/api/v2/public/get_index_price?index_name=eth_usdc";

    public BigDecimal calculatePutOptionPrice(BigDecimal S, BigDecimal K, BigDecimal T, BigDecimal r) {
        BigDecimal sigma = getLastVolatilityFromDeribit();
        return BlackScholesBigDecimal.putOptionPrice(S, K, T, r, sigma);
    }

    private BigDecimal getLastVolatilityFromDeribit() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<HistoricalVolatilityResponse> response = restTemplate.getForEntity(DERIBIT_VOLATILITY_API_URL, HistoricalVolatilityResponse.class);

        List<BigDecimal> volatilities = response.getBody().getResult();
        BigDecimal lastVolatility = volatilities.get(volatilities.size() - 1);

        BigDecimal divisor = BigDecimal.valueOf(Math.sqrt(365));
        return lastVolatility.divide(divisor, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getCurrentAssetPrice() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CurrentPriceResponse> response = restTemplate.getForEntity(DERIBIT_CURRENT_PRICE_API_URL, CurrentPriceResponse.class);
        return response.getBody().getResult().getPrice();
    }

    @Scheduled(cron = "0 50 23 * * ?")
    public void scheduledUpdatePutOptionPrice() {
        BigDecimal S = getCurrentAssetPrice();
        BigDecimal K = new BigDecimal("1700");
        BigDecimal T = new BigDecimal("7");
        BigDecimal r = new BigDecimal("0.05");

        calculatePutOptionPrice(S, K, T, r);
    }
}
*/