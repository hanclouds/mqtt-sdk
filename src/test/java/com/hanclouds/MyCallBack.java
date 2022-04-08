package com.hanclouds;

import com.alibaba.fastjson.JSON;
import com.hanclouds.impl.AbstractHancloudsCallback;
import com.hanclouds.impl.HancloudsClientImpl;
import com.hanclouds.model.ProxyNotifyInfo;
import com.hanclouds.model.StructureInfo;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class MyCallBack extends AbstractHancloudsCallback {

    private static Logger logger = LoggerFactory.getLogger(MyCallBack.class);
    @Override
    public void onConnectionLost() {
        System.out.println("the connection lost!");
    }

    @Override
    public void onRecvCommandInt(String commandId, String deviceKey, int data) {
        System.out.printf("receive command, commandId= %s, data=%d\n", commandId, data);
        System.out.println();
        sendCommandAck(commandId);
    }

    @Override
    public void onRecvCommandDouble(String commandId, String deviceKey, double data) {
        System.out.printf("receive command, commandId= %s, data=%f", commandId, data);
        System.out.println();
        sendCommandAck(commandId);
    }

    @Override
    public void onRecvCommandString(String commandId, String deviceKey, String data) {
        System.out.printf("receive command, commandId= %s, data=%s", commandId, data);
        System.out.println();
        sendCommandAck(commandId);
    }

    @Override
    public void onRecvCommandJson(String commandId, String deviceKey, String data) {
        System.out.printf("receive command, commandId= %s, data=%s", commandId, data);
        System.out.println();
        sendCommandAck(commandId);
    }

    @Override
    public void onRecvCommandBin(String commandId, String deviceKey, byte[] data) {
        System.out.printf("receive command, commandId= %s, data=%s", commandId, Base64.encodeBase64String(data));
        System.out.println();
        sendCommandAck(commandId);
    }

    @Override
    public void onRecvCommandTemplate(String commandId, String deviceKey, String data) {
        System.out.printf("receive command, commandId= %s, data=%s", commandId, data);
    }

    @Override
    public void onRecvStructureSync(List<StructureInfo> structureInfoList) {
        System.out.println(JSON.toJSONString(structureInfoList));
    }

    @Override
    public void onRecvProxyNotify(ProxyNotifyInfo proxyNotifyInfo) {
        System.out.println(JSON.toJSONString(proxyNotifyInfo));
    }

    @Override
    public void onRecvError(String errorMsg) {
        System.out.println(errorMsg);
    }

    @Override
    public void onRecvDirect(String direct) {
        logger.info(direct);
    }

    @Override
    public void onRecvVersion(String version) {
        logger.info(version);
    }


}
