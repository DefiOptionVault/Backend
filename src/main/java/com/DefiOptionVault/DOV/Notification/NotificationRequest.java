package com.DefiOptionVault.DOV.Notification;


public class NotificationRequest {
    private String deviceToken;
    private String title;
    private String body;

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
