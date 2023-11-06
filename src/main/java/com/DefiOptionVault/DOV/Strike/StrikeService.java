package com.DefiOptionVault.DOV.Strike;

import com.DefiOptionVault.DOV.Option.OptionService;
import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Strike.Strike;
import com.DefiOptionVault.DOV.Strike.StrikeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.web.client.RestTemplate;

@Service
public class StrikeService {

    private static final String DERIBIT_VOLATILITY_API_URL = "https://www.deribit.com/api/v2/public/get_historical_volatility?currency=ETH";
    private static final String DERIBIT_CURRENT_PRICE_API_URL = "https://www.deribit.com/api/v2/public/get_index_price?index_name=eth_usdc";


    @Autowired
    private StrikeRepository strikeRepository;

    @Autowired
    private OptionService optionService;

    public List<Strike> getAllStrikes() {
        return strikeRepository.findAll();
    }

    public List<Strike> saveAllStrikes(List<Strike> strikes) {
        for (Strike strike : strikes) {
            Option option = optionService.findById(strike.getOption().getOptionId())
                    .orElseThrow(() -> new RuntimeException("Option not found!"));
            strike.setOption(option);
        }

        return strikeRepository.saveAll(strikes);
    }

    public List<Strike> getStrikesByOptionId(int optionId) {
        return strikeRepository.findByOption_OptionId(optionId);
    }

    public Optional<Strike> getStrikeById(Integer id) {
        return strikeRepository.findById(id);
    }

    public Strike saveStrike(Strike strike) {
        return strikeRepository.save(strike);
    }

    public void deleteStrike(Integer id) {
        strikeRepository.deleteById(id);
    }

    public BigDecimal calcPutOptionPrice(BigDecimal S, BigDecimal K, BigDecimal T, BigDecimal r) {
        BigDecimal sigma = getLastVolatilityFromDeribit();
        return BlackScholes.putOptionPrice(S, K, T, r, sigma);
    }

    private BigDecimal getLastVolatilityFromDeribit() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<HistoricalVolatilityResponse> response = restTemplate.getForEntity(DERIBIT_VOLATILITY_API_URL, HistoricalVolatilityResponse.class);

        List<BigDecimal> volatilities = Objects.requireNonNull(response.getBody()).getResult();
        BigDecimal lastVolatility = volatilities.get(volatilities.size() - 1);

        BigDecimal divisor = BigDecimal.valueOf(Math.sqrt(365));
        return lastVolatility.divide(divisor, 64, RoundingMode.HALF_UP);
    }

    private BigDecimal getCurrentAssetPrice() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CurrentPriceResponse> response = restTemplate.getForEntity(DERIBIT_CURRENT_PRICE_API_URL, CurrentPriceResponse.class);
        return Objects.requireNonNull(response.getBody()).getResult().getPrice();
    }

    @Scheduled(cron = "0 00 00 * * ?")
    public BigDecimal setStrikeOptionPrice(Strike strike) {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime expiry = strike.getOption().getExpiry().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
        long dayDiff = ChronoUnit.DAYS.between(now, expiry);

        if (dayDiff == 0L) {
            dayDiff = 1L;
        }

        BigDecimal S = getCurrentAssetPrice();
        BigDecimal K = new BigDecimal(strike.getStrikePrice());
        BigDecimal T = BigDecimal.valueOf(Math.abs(dayDiff));
        BigDecimal r = BigDecimal.valueOf(0.0525);

        return calcPutOptionPrice(S, K, T, r);
    }
}