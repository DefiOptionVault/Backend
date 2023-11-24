package com.DefiOptionVault.DOV.Option;

import com.DefiOptionVault.DOV.Strike.CurrentPriceResponse;
import com.DefiOptionVault.DOV.Strike.StrikeService;
import com.DefiOptionVault.DOV.Strike.Web3jService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.DefiOptionVault.DOV.Notification.NotificationService;

import java.util.Optional;
import java.util.List;
import org.springframework.web.client.RestTemplate;

@Service
public class OptionService {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private NotificationService notificationService;

    private static final String DERIBIT_CURRENT_PRICE_API_URL = "https://www.deribit.com/api/v2/public/get_index_price?index_name=eth_usdc";
    private static final BigInteger UNIT = new BigInteger("1000000000000000000");

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
        return Objects.requireNonNull(response.getBody()).getResult().getPrice();
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
                BigInteger settlementPrice = new BigInteger(String.valueOf(getCurrentAssetPrice()));
                web3jService.expire(settlementPrice.multiply(UNIT));
                Option newOption = generateNextRoundOption(option.getOptionId());
                BigInteger[] strikes = new BigInteger[4];
                BigInteger base = new BigInteger(String.valueOf(getCurrentAssetPrice()));
                strikes[0] = base.subtract(new BigInteger("100"));
                strikes[1] = base.subtract(new BigInteger("50"));
                strikes[2] = base.add(new BigInteger("50"));
                strikes[3] = base.add(new BigInteger("100"));

                for(int i = 0; i < 4; i++) {
                    strikes[i] = strikes[i].multiply(UNIT);
                }

                web3jService.bootstrap(strikes,
                        BigInteger.valueOf(newOption.getExpiry().getTime()),
                        newOption.getSymbol());
            }
            /*
            try {
                String userDeviceToken = notificationRequest.getDeviceToken();
                if (userDeviceToken != null && !userDeviceToken.isEmpty()) {
                    notificationService.sendNotification(
                            userDeviceToken,
                            "알림 : 옵션 상품 만기",
                            "현재 옵션 상품이 만기되었습니다. 정산 내역을 확인해주세요.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
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