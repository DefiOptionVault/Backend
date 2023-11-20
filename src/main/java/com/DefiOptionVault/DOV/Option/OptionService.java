package com.DefiOptionVault.DOV.Option;

import com.DefiOptionVault.DOV.Strike.Strike;
import com.DefiOptionVault.DOV.Strike.StrikeService;
import com.DefiOptionVault.DOV.Strike.Web3jService;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public class OptionService {

    @Autowired
    private OptionRepository optionRepository;

    // Create
    public Option saveOption(Option option) {
        return optionRepository.save(option);
    }

    // Read
    public Optional<Option> getOptionById(int id) {
        return optionRepository.findById(id);
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
        StrikeService strikeService = new StrikeService();
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
                //web3jService.expire();
                Option newOption = generateNextRoundOption(option.getOptionId());
                BigInteger[] strikes = new BigInteger[4];
                BigInteger base = new BigInteger(String.valueOf(strikeService.getCurrentAssetPrice()));
                strikes[0] = base.subtract(new BigInteger("100"));
                strikes[1] = base.subtract(new BigInteger("50"));
                strikes[2] = base.add(new BigInteger("50"));
                strikes[3] = base.add(new BigInteger("100"));

                for(int i = 0; i < 4; i++) {
                    strikes[i] = strikes[i].multiply(new BigInteger("1000000000000000000"));
                }

                web3jService.bootstrap(strikes,
                        BigInteger.valueOf(newOption.getExpiry().getTime()),
                        newOption.getSymbol());
            }
        }
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