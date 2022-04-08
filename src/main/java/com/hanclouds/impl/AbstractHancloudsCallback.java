package com.hanclouds.impl;


import com.hanclouds.HancloudsClient;
import com.hanclouds.model.ProxyNotifyInfo;
import com.hanclouds.model.StructureInfo;

import java.util.List;

/**
 * HanCloudsCallback 回调抽象类，外部可以实现
 *
 * @author szl
 * @date 2018/7/15
 */
public abstract class AbstractHancloudsCallback {

    /**
     * 基于Paho mqtt client的封装
     */
    private HancloudsClient client;

    /**
     * 当本设备从服务端断开时，会回调此方法
     */
    abstract public void onConnectionLost();

    /**
     * 当收到一个整形命令时，会回调此方法
     *
     * @param commandId 命令标识
     * @param deviceKey 设备标识
     * @param data      数值
     */
    abstract public void onRecvCommandInt(String commandId, String deviceKey, int data);

    /**
     * 当收到一个浮点数命令时，会回调此方法
     *
     * @param commandId 命令标识
     * @param deviceKey 设备标识
     * @param data      否点数的值
     */
    abstract public void onRecvCommandDouble(String commandId, String deviceKey, double data);

    /**
     * 当收到一个字符串命令时，会回调此方法
     *
     * @param commandId 命令标识
     * @param deviceKey 设备标识
     * @param data      命令字符串
     */
    abstract public void onRecvCommandString(String commandId, String deviceKey, String data);

    /**
     * 当收到一个json对象时，会回调此方法
     *
     * @param commandId 命令标识
     * @param deviceKey 设备标识
     * @param data      命令json字符串
     */
    abstract public void onRecvCommandJson(String commandId, String deviceKey, String data);

    /**
     * 当收到一个二进制对象时，会回调此方法
     *
     * @param commandId 命令标识
     * @param deviceKey 设备标识
     * @param data      二进制命令内容
     */
    abstract public void onRecvCommandBin(String commandId, String deviceKey, byte[] data);

    /**
     * 当收到一个ctl回复时，会回调此方法
     *
     * @param commandId 命令标识
     * @param deviceKey 设备标识
     * @param data      命令字符串
     */
    abstract public void onRecvCommandTemplate(String commandId, String deviceKey, String data);
    /**
     * 当收到一个Sync回复时，会回调此方法
     *
     * @param structureInfoList      list
     */
    abstract public void onRecvStructureSync(List<StructureInfo> structureInfoList);
    /**
     * 当收到一个proxy回复时，会回调此方法
     *
     * @param proxyNotifyInfo      listonRecv
     */
    abstract public void onRecvProxyNotify(ProxyNotifyInfo proxyNotifyInfo);
    /**
     * 当收到一个Error回复时，会回调此方法
     *
     * @param errorMsg      errorMsg
     */
    abstract public void onRecvError(String errorMsg);

    /**
     * 当收到一个Direct回复时，会回调此方法
     *
     * @param direct      direct
     */
    abstract public void onRecvDirect(String direct);

    /**
     * 当收到一个Error回复时，会回调此方法
     *
     * @param version      version
     */
    abstract public void onRecvVersion(String version);

    /**
     * 向HanClouds发送某个命令响应
     *
     * @param commandId 命令标识
     */
    protected void sendCommandAck(String commandId) {
        if (client != null) {
            client.publishCmdAck(commandId);
        }
    }

    protected void setClient(HancloudsClient client) {
        this.client = client;
    }
}
