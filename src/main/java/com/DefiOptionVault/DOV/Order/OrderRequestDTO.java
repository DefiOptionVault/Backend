package com.DefiOptionVault.DOV.Order;

public class OrderRequestDTO {
    private Integer amount;
    private Integer optionId;
    private String position;
    private String strikePrice;
    private String clientAddress;
    private int tokenId;

    public Integer getAmount() {
        return amount;
    }

    public Integer getOptionId() {
        return optionId;
    }

    public String getPosition() {
        return position;
    }

    public String getStrikePrice() {
        return strikePrice;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setStrikePrice(String strikePrice) {
        this.strikePrice = strikePrice;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }
}
