package com.dashlabs.hermes;

import com.notnoop.apns.ApnsService;

/**
 * User: blangel
 * Date: 8/1/13
 * Time: 11:19 AM
 */
public class IOSTransport implements Transport<String> {

    private final String deviceId;

    private final ApnsService apnsService;

    public IOSTransport(String deviceId, ApnsService apnsService) {
        this.deviceId = deviceId;
        this.apnsService = apnsService;
    }

    @Override public String getId() {
        return deviceId;
    }

    @Override public String send(String message, int retries) {
        apnsService.push(deviceId, message);
        return message;
    }
}
