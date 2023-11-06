package com.DefiOptionVault.DOV.Option;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

import java.util.Optional;

@RestController
@RequestMapping("/api/options")
public class OptionController {

    @Autowired
    private OptionService optionService;

    /*
    @Autowired
    private OptionPricingService optionPricingService;

    @GetMapping("/putPrice")
    public BigDecimal getPutOptionPrice(
            @RequestParam BigDecimal S,
            @RequestParam BigDecimal K,
            @RequestParam BigDecimal T,
            @RequestParam BigDecimal r) {
        return optionPricingService.calculatePutOptionPrice(S, K, T, r);
    }
    */

    // Create
    @PostMapping("/set_option_info")
    public ResponseEntity<Option> createOption(@RequestBody Option option) {
        Option savedOption = optionService.saveOption(option);
        return new ResponseEntity<>(savedOption, HttpStatus.CREATED);
    }

    // Read
    @GetMapping("/{id}")
    public ResponseEntity<Option> getOptionById(@PathVariable int id) {
        Optional<Option> option = optionService.getOptionById(id);
        return option.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update
    /*
    @PutMapping("/{id}")
    public ResponseEntity<Option> updateOption(@PathVariable int id, @RequestBody Option option) {
        if (optionService.getOptionById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        option.setOptionId(id);
        Option updatedOption = optionService.updateOption(option);
        return new ResponseEntity<>(updatedOption, HttpStatus.OK);
    }
    */

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOption(@PathVariable int id) {
        if (optionService.getOptionById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        optionService.deleteOption(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/get_option_info")
    public List<Option> getAllOptionById() {
        return optionService.getAllOptions();
    }
}
