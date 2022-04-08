package com.hanclouds;

import com.hanclouds.impl.HancloudsClientImpl;

/**
 * HanClouds客户端工厂
 *
 * @author szl
 * @date 2018/7/15
 */
public class HanCloudsClientFactory {

    public HanCloudsClientFactory() {
    }

    /**
     * 构造一个新的HanCloudsClient
     *
     * @return HancloudsClient 对象
     */
    public static HancloudsClient getClient() {
        return new HancloudsClientImpl();
    }

    public static HancloudsClient getClient(String url) {
        return new HancloudsClientImpl(url);
    }

    public static HancloudsClient getClient(String url, String mqttIp) {
        return new HancloudsClientImpl(url, mqttIp);
    }
}
