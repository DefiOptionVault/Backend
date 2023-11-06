package com.DefiOptionVault.DOV.Option;

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