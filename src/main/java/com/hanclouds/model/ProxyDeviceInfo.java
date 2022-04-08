package com.hanclouds.model;

import java.util.Objects;

/**
 * proxy deviceInfo
 * author clx
 */
public class ProxyDeviceInfo {
    //设备sn号
    private String sn;

    private String deviceKey;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getDevice_Key() {
        return deviceKey;
    }

    public void setDevice_Key(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        ProxyDeviceInfo proxyDeviceInfo = (ProxyDeviceInfo) obj;
        if (this == proxyDeviceInfo) {
            return true;
        } else {
            return (this.sn.equals(proxyDeviceInfo.sn) && this.deviceKey.equals(proxyDeviceInfo.deviceKey));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, deviceKey);
    }
}
