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
import java.util.ArrayList;
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

    public BigInteger[] createNewStrikes(Option option) {
        BigInteger[] strikes = new BigInteger[4];
        BigInteger base = getCurrentAssetPrice().toBigInteger();
        strikes[0] = base.subtract(new BigInteger("100"));
        strikes[1] = base.subtract(new BigInteger("50"));
        strikes[2] = base.add(new BigInteger("50"));
        strikes[3] = base.add(new BigInteger("100"));

        int i = 1;
        for(BigInteger strike : strikes) {
            Strike newStrike = createNewStrike(option, strike, i);
            strikeRepository.save(newStrike);
            i += 1;
        }
        return strikes;
    }

    public Strike createNewStrike(Option option, BigInteger strikePrice, int index) {
        Strike newStrike = new Strike();
        BigDecimal currentPrice = getCurrentAssetPrice();
        BigDecimal optionPrice = calcPutOptionPrice(
                currentPrice,
                new BigDecimal(strikePrice),
                new BigDecimal(7),
                new BigDecimal("0.0525"));

        newStrike.setOption(option);
        newStrike.setStrikePrice(strikePrice.toString());
        newStrike.setOptionPrice(optionPrice.toString());
        newStrike.setStrikeIndex(index);

        return newStrike;
    }

    public BigDecimal calcPutOptionPrice(BigDecimal S, BigDecimal K, BigDecimal T, BigDecimal r) {
        BigDecimal sigma = getLastVolatilityFromDeribit();
        return BlackScholes.putOptionPrice(S, K, T, r, sigma);
    }

    private BigDecimal getLastVolatilityFromDeribit() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<HistoricalVolatilityResponse> response = restTemplate.getForEntity(DERIBIT_VOLATILITY_API_URL, HistoricalVolatilityResponse.class);

        List<List<BigDecimal>> volatilities = Objects.requireNonNull(response.getBody()).getResult();
        List<BigDecimal> lastVolatilityList = volatilities.get(volatilities.size() - 1);
        BigDecimal lastVolatility = lastVolatilityList.get(lastVolatilityList.size() - 1);

        BigDecimal divisor = BigDecimal.valueOf(Math.sqrt(365));
        return lastVolatility.divide(divisor, 64, RoundingMode.HALF_UP);
    }

    public BigDecimal getCurrentAssetPrice() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CurrentPriceResponse> response = restTemplate.getForEntity(DERIBIT_CURRENT_PRICE_API_URL, CurrentPriceResponse.class);
        return Objects.requireNonNull(response.getBody()).getResult().getIndex_price();
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
        Web3jService web3jService = new Web3jService();
        List<Strike> strikes = getStrikesByOptionId(optionId);
        BigInteger[] strikesForContract = new BigInteger[4];
        int i = 0;
        for (Strike strike : strikes) {
            //BigInteger updatedPrice = new BigInteger(String.valueOf(setStrikeOptionPrice(strike)));
            BigDecimal updatedPrice = setStrikeOptionPrice(strike).multiply(UNIT_MULTIPLIER);
            strikesForContract[i++] = new BigInteger(String.valueOf(updatedPrice));
            strike.setOptionPrice(updatedPrice.toString());
            strikeRepository.save(strike);
        }
        web3jService.updateOptionPrices(
                optionRepository.findById(optionId).get().getOptionAddress(),
                strikesForContract);
        return strikes;
    }
/*
    @Scheduled(cron = "0 0 0 * * ?")
    public void BigIntegersForAllOptions() {
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
            if (expiry.isAfter(now)) {
                List<Strike> strikes = updateStrikeOptionPricesForOptionId(option.getOptionId());
                String address = option.getOptionAddress();
                BigInteger[] strikePrices = new BigInteger[4];
                int i = 0;
                for (Strike strike : strikes) {
                    strikePrices[i] = new BigInteger(strike.getOptionPrice());
                    i += 1;
                }
                web3jService.updateOptionPrices(address, strikePrices);
            }
        }
    }

}