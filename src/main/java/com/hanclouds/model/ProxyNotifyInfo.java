package com.hanclouds.model;

import java.util.List;

/**
 * proxy连接rsp/proxy/welcome 的payload
 * 2019.11.25
 * clx
 */
public class ProxyNotifyInfo {
    private List<ProxyDeviceInfo> loginSuc;
    private List<LoginFail> loginFail;
    private List<OfflineSuc> offlineSuc;
    private List<OfflineFail> offlineFail;

    public List<ProxyDeviceInfo> getLoginSuc() {
        return loginSuc;
    }

    public void setLoginSuc(List<ProxyDeviceInfo> loginSuc) {
        this.loginSuc = loginSuc;
    }

    public List<LoginFail> getLoginFail() {
        return loginFail;
    }

    public void setLoginFail(List<LoginFail> loginFail) {
        this.loginFail = loginFail;
    }

    public List<OfflineSuc> getOfflineSuc() {
        return offlineSuc;
    }

    public void setOfflineSuc(List<OfflineSuc> offlineSuc) {
        this.offlineSuc = offlineSuc;
    }

    public List<OfflineFail> getOfflineFail() {
        return offlineFail;
    }

    public void setOfflineFail(List<OfflineFail> offlineFail) {
        this.offlineFail = offlineFail;
    }
}
