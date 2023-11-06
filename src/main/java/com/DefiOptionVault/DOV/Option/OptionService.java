package com.DefiOptionVault.DOV.Option;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import org.springframework.beans.factory.annotation.Autowired;
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