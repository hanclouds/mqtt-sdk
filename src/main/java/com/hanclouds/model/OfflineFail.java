package com.hanclouds.model;

public class OfflineFail {
    private int reasonCode;
    private String childDeviceKey;

    public int getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getChildDeviceKey() {
        return childDeviceKey;
    }

    public void setChildDeviceKey(String childDeviceKey) {
        this.childDeviceKey = childDeviceKey;
    }
}
