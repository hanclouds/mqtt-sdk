package com.hanclouds.model;

/**
 * 修改设备信息
 *
 * @author ljy
 * @date 2022/9/22
 */
public class UpdateDeviceInfo {
    /**
     * sn
     */
    private String sn;
    /**
     * deviceName
     */
    private String deviceName;
    /**
     * descr
     */
    private String descr;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
}
