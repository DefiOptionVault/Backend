package com.DefiOptionVault.DOV.Option;

import com.DefiOptionVault.DOV.Strike.CurrentPriceResponse;
import com.DefiOptionVault.DOV.Strike.StrikeService;
import com.DefiOptionVault.DOV.Strike.Web3jService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import org.springframework.web.client.RestTemplate;

@Service
public class OptionService {

    @Autowired
    private OptionRepository optionRepository;

    private static final String DERIBIT_CURRENT_PRICE_API_URL = "https://www.deribit.com/api/v2/public/get_index_price?index_name=eth_usdc";
    private static final BigDecimal UNIT = new BigDecimal("1000000000000000000");

    // Create
    public Option saveOption(Option option) {
        return optionRepository.save(option);
    }

    // Read
    public Optional<Option> getOptionById(int id) {
        return optionRepository.findById(id);
    }

    public BigDecimal getCurrentAssetPrice() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CurrentPriceResponse> response = restTemplate.getForEntity(DERIBIT_CURRENT_PRICE_API_URL, CurrentPriceResponse.class);
        return Objects.requireNonNull(response.getBody()).getResult().getIndex_price();
    }

    public Option generateNextRoundOption(Integer optionId) {
        Option existingOption = optionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option not found with id: " + optionId));

        Option newOption = new Option();

        newOption.setOptionAddress(existingOption.getOptionAddress());
        newOption.setBaseAsset(existingOption.getBaseAsset());
        newOption.setCollateralAsset(existingOption.getCollateralAsset());
        newOption.setSymbol(existingOption.getSymbol());
        newOption.setRound(existingOption.getRound() + 1);

        ZonedDateTime nextSunday = ZonedDateTime.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59);

        newOption.setExpiry(Timestamp.from(nextSunday.toInstant()));

        return optionRepository.save(newOption);
    }

    @Scheduled(cron = "0 59 23 * * SUN")
    public void createNextRoundOptionInContract() {
        Web3jService web3jService = new Web3jService();
        List<Option> allOptions = optionRepository.findAll();
        for (Option option : allOptions) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC)
                    .withSecond(59)
                    .withNano(0);

            LocalDateTime expiry = option
                    .getExpiry()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            if (expiry.equals(now)) {
                BigDecimal settlementPrice = getCurrentAssetPrice();
                web3jService.expire(settlementPrice
                        .multiply(UNIT)
                        .setScale(0, RoundingMode.DOWN)
                        .toBigInteger());
                Option newOption = generateNextRoundOption(option.getOptionId());
                
                BigDecimal[] strikes = new BigDecimal[4];
                BigDecimal base = getCurrentAssetPrice();
                strikes[0] = base.subtract(new BigDecimal("100"));
                strikes[1] = base.subtract(new BigDecimal("50"));
                strikes[2] = base.add(new BigDecimal("50"));
                strikes[3] = base.add(new BigDecimal("100"));

                BigInteger[] strikesForBootstrap = new BigInteger[4];
                for(int i = 0; i < 4; i++){
                    strikesForBootstrap[i] = strikes[i]
                            .multiply(UNIT)
                            .setScale(0, RoundingMode.DOWN)
                            .toBigInteger();
                }

                web3jService.bootstrap(strikesForBootstrap,
                        BigInteger.valueOf(newOption.getExpiry().getTime()),
                        newOption.getSymbol());
            }

        }

    }

    public List<Option> getValidOptions() {
        List<Option> allOptions = getAllOptions();
        List<Option> result = new ArrayList<>();
        Map<String, Integer> validOptions = new HashMap<>();
        for(Option option : allOptions) {
            String symbol = option.getSymbol();
            int round = option.getRound();
            if (!validOptions.containsKey(symbol)) {
                validOptions.put(symbol, round);
                result.add(option);
            }
            if (validOptions.get(symbol) < round) {
                validOptions.replace(symbol, round);
                Option tmp = findOptionBySymbol(symbol, result);
                int index = result.indexOf(tmp);
                result.add(index, option);
                result.remove(tmp);
            }
        }
        return result;
    }

    private Option findOptionBySymbol(String symbol, List<Option> options) {
        for(Option option : options) {
            if (option.getSymbol().equals(symbol)) {
                return option;
            }
        }
        throw new NoSuchElementException();
    }

    // Update
    public Option updateOption(Option option) {
        return optionRepository.save(option);
    }

    // Delete
    public void deleteOption(int id) {
        optionRepository.deleteById(id);
    }

    public List<Option> getAllOptions() {
        return optionRepository.findAll();
    }

    public Optional<Option> findById(Integer optionId) {
        return optionRepository.findById(optionId);
    }
}