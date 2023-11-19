package com.DefiOptionVault.DOV.Strike;

import com.DefiOptionVault.DOV.Option.OptionRepository;
import com.DefiOptionVault.DOV.Option.OptionService;
import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Strike.Strike;
import com.DefiOptionVault.DOV.Strike.StrikeRepository;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.web3j.protocol.Web3j;

@Service
public class StrikeService {

    private static final String DERIBIT_VOLATILITY_API_URL = "https://www.deribit.com/api/v2/public/get_historical_volatility?currency=ETH";
    private static final String DERIBIT_CURRENT_PRICE_API_URL = "https://www.deribit.com/api/v2/public/get_index_price?index_name=eth_usdc";
    private static final BigDecimal UNIT_MULTIPLIER = new BigDecimal("1e18");

    @Autowired
    private StrikeRepository strikeRepository;

    @Autowired
    private OptionService optionService;

    @Autowired
    private OptionRepository optionRepository;

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

    public BigDecimal setStrikeOptionPrice(Strike strike) {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime expiry = strike
                .getOption()
                .getExpiry()
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
        long dayDiff = ChronoUnit.DAYS.between(now, expiry);

        if (dayDiff == 0L) {
            dayDiff = 1L;
        }

        BigDecimal S = getCurrentAssetPrice();
        BigDecimal K = new BigDecimal(strike.getStrikePrice());
        BigDecimal T = BigDecimal.valueOf(Math.abs(dayDiff));
        BigDecimal r = BigDecimal.valueOf(0.0525);
        return calcPutOptionPrice(S, K, T, r).multiply(UNIT_MULTIPLIER);
    }

    public List<Strike> updateStrikeOptionPricesForOptionId(int optionId) {
        List<Strike> strikes = getStrikesByOptionId(optionId);
        for (Strike strike : strikes) {
            BigDecimal updatedPrice = setStrikeOptionPrice(strike);
            strike.setOptionPrice(updatedPrice.toString());
            strikeRepository.save(strike);
        }
        return strikes;
    }
/*
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateOptionPricesForAllOptions() {
        List<Option> allOptions = optionRepository.findAll();
        for (Option option : allOptions) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime expiry = option
                    .getExpiry()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            if(expiry.isAfter(now)) {
                updateStrikeOptionPricesForOptionId(option.getOptionId());
            }
        }
    }
*/
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateOptionPricesByAddress() {
        Web3jService web3jService = new Web3jService();
        List<Option> allOptions = optionRepository.findAll();
        for (Option option : allOptions) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime expiry = option
                    .getExpiry()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            if(expiry.isAfter(now)) {
                List<Strike> strikes = updateStrikeOptionPricesForOptionId(option.getOptionId());
                String address = option.getOptionAddress();
                BigInteger[] strikePrices = new BigInteger[4];
                int i = 0;
                for(Strike strike : strikes) {
                    strikePrices[i] = new BigInteger(strike.getOptionPrice());
                    i += 1;
                }
                web3jService.updateOptionPrices(address, strikePrices);
            }
        }
    }

}