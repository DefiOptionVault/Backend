package com.DefiOptionVault.DOV.Strike;
import com.DefiOptionVault.DOV.Option.OptionRepository;
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

    ///api/strikes/byOption?optionId=YOUR_OPTION_ID
    @GetMapping("/byOptionId")
    public ResponseEntity<List<Strike>> getStrikesByOptionId(@RequestParam int optionId) {
        List<Strike> strikes = strikeService.getStrikesByOptionId(optionId);
        if(strikes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(strikes, HttpStatus.OK);
    }


}