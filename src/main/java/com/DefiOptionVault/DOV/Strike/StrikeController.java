package com.DefiOptionVault.DOV.Strike;
import com.DefiOptionVault.DOV.Option.OptionRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


@RestController
@RequestMapping("/api/strikes")
public class StrikeController {
    @Autowired
    private StrikeService strikeService;

    @Autowired
    private OptionRepository optionRepository;

    // Create
    @PostMapping("/bulk")
    public ResponseEntity<List<Strike>> createStrikes(@RequestBody List<Strike> strikes) {
        List<Strike> savedStrikes = strikeService.saveAllStrikes(strikes);
        return new ResponseEntity<>(savedStrikes, HttpStatus.CREATED);
    }

    ///api/strikes/updateOptionPrices?optionId=YOUR_OPTION_ID
    @PostMapping("/updateOptionPrices")
    public ResponseEntity<Void> updateOptionPricesForGivenOptionId(@RequestParam int optionId) {
        strikeService.updateStrikeOptionPricesForOptionId(optionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    ///api/strikes/byOption?optionId=YOUR_OPTION_ID
    @GetMapping("/byOptionId")
    public ResponseEntity<List<Strike>> getStrikesByOptionId(@RequestParam int optionId) {
        List<Strike> strikes = strikeService.getStrikesByOptionId(optionId);
        if(strikes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(strikes, HttpStatus.OK);
    }

    @GetMapping("/currentOptionPrice")
    public BigDecimal showCurrentPrice() {
        BigDecimal test = strikeService.getCurrentAssetPrice();
        if (test == null) return BigDecimal.ZERO;
        return strikeService.getCurrentAssetPrice();
    }

    /*//옵션 가격 테스트
    @GetMapping("/calcPutOptionPrice")
    public BigDecimal showOptionPrice() {
        return strikeService.calcPutOptionPrice(
                strikeService.getCurrentAssetPrice(),
                new BigDecimal("2000"),
                new BigDecimal("7"),
                new BigDecimal("0.0525")
        );
    }
    */
}