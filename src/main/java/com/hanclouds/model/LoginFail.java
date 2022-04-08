package com.hanclouds.model;

import java.util.List;

/**
 * 设备代理错误信息
 * 2019.11.25
 * clx
 */
public class LoginFail {
    private List<String> snSet;
    private String code;

    public List<String> getSnSet() {
        return snSet;
    }

    public void setSnSet(List<String> snSet) {
        this.snSet = snSet;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
