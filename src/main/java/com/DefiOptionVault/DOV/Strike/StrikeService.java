package com.DefiOptionVault.DOV.Strike;

import com.DefiOptionVault.DOV.Option.OptionService;
import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Strike.Strike;
import com.DefiOptionVault.DOV.Strike.StrikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StrikeService {

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
}