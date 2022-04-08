package com.hanclouds;

import com.hanclouds.impl.AbstractHancloudsCallback;
import com.hanclouds.model.ProxyNotifyInfo;
import com.hanclouds.model.StructureInfo;
import org.apache.commons.codec.binary.Base64;

import java.util.List;


public class MyCallBack extends AbstractHancloudsCallback {
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

    }

    @Override
    public void onRecvCommandSync(String commandId, String deviceKey, List<StructureInfo> structureInfoList) {

    }

    @Override
    public void onRecvCommandProxy(String commandId, String deviceKey, ProxyNotifyInfo proxyNotifyInfo) {

    }


}
