package com.hanclouds.model;

/**
 * 设备通过mqtt登陆成功后服务端返回的设备信息
 * @author szl
 * @date 2018/7/15
 */
public class DeviceInfo {
    /**
     * deviceKey
     */
    private String deviceKey;
    /**
     * 设备上传数据的uploadToken
     */
    private String uploadToken;
    /**
     * 查询次设备数据的token
     */
    private String queryToken;
    /**
     * 给此设备下发命令的token
     */
    private String cmdToken;
    /**
     * 设备的静态秘钥，设备创建时生成的秘钥
     */
    private String deviceSecret;
    /**
     * 设备的动态秘钥，只对此链接有效
     */
    private String sessionSecret;
    /**
     * 当前服务端的时间戳，设备侧可以用此时间进行简单的时间同步校正
     */
    private Long time;

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public String getUploadToken() {
        return uploadToken;
    }

    public void setUploadToken(String uploadToken) {
        this.uploadToken = uploadToken;
    }

    public String getQueryToken() {
        return queryToken;
    }

    public void setQueryToken(String queryToken) {
        this.queryToken = queryToken;
    }

    public String getCmdToken() {
        return cmdToken;
    }

    public void setCmdToken(String cmdToken) {
        this.cmdToken = cmdToken;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getSessionSecret() {
        return sessionSecret;
    }

    public void setSessionSecret(String sessionSecret) {
        this.sessionSecret = sessionSecret;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
