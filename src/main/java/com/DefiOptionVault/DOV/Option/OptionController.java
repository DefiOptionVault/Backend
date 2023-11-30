package com.DefiOptionVault.DOV.Option;

import com.DefiOptionVault.DOV.Order.OrderService;
import com.DefiOptionVault.DOV.Strike.StrikeService;
import com.DefiOptionVault.DOV.Strike.Web3jService;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import org.bouncycastle.util.test.FixedSecureRandom;
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
    
    @Autowired
    private StrikeService strikeService;

    @Autowired
    private OrderService orderService;

    private static final BigDecimal UNIT = new BigDecimal("1000000000000000000");

    // Create
    @PostMapping("/set_option_info")
    public ResponseEntity<Option> createOption(@RequestBody Option option) {
        Option savedOption = optionService.saveOption(option);
        return new ResponseEntity<>(savedOption, HttpStatus.CREATED);
    }


    @PostMapping("/{optionId}/generateNextRoundIfExpired")
    public ResponseEntity<Option> generateNextRoundOption(@PathVariable Integer optionId) {
        Web3jService web3jService = new Web3jService();
        Option newOption = optionService.generateNextRoundOption(optionId);
        BigDecimal[] strikes = strikeService.createNewStrikes(newOption);
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
        return new ResponseEntity<>(newOption, HttpStatus.CREATED);
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

    @GetMapping("/getValidOptionInfo")
    public List<Option> getValidOptionInfo() {
        return optionService.getValidOptions();
    }
}
