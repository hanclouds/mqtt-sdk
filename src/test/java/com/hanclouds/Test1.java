package com.hanclouds;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hanclouds.impl.HancloudsClientImpl;
import com.hanclouds.model.DeviceInfo;
import com.hanclouds.util.CryptoUtils;
import org.junit.Test;

import java.util.*;

public class Test1 {
    @Test
    public void test() {
        HancloudsClient hancloudsClient = HanCloudsClientFactory.getClient();
        MyCallBack callBack = new MyCallBack();
        hancloudsClient.init("X1UoJ1HY", "ygb4jEUb", "vMDZ0Z9D3poaUdfo", callBack);

        DeviceInfo deviceInfo = hancloudsClient.connect(false, "snSzl0", true, null);
        System.out.println("the one round");
        int i = 0;
        Random random = new Random();
        for (i = 0; i < 10; i++) {
            double value = 200.0 + random.nextInt(50);
            String stringValue = String.valueOf(value);
            hancloudsClient.uploadBin("binq", stringValue.getBytes());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hancloudsClient.disconnect();
        System.out.println("the second round");
        deviceInfo = hancloudsClient.connect(false, "snSzl0", false, null);
        for (i = 0; i < 10; i++) {
            double value = 200.0 + random.nextInt(50);
            hancloudsClient.uploadDouble("ddd", value);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hancloudsClient.disconnect();
        hancloudsClient.close();
        System.out.println("completed!");
    }

    @Test
    public void test1() {
        HancloudsClient hancloudsClient = HanCloudsClientFactory.getClient();
        MyCallBack callBack = new MyCallBack();
        //hancloudsClient.init("X1UoJ1HY", "ygb4jEUb", "vMDZ0Z9D3poaUdfo", callBack);

        boolean isConnected = hancloudsClient.connectByMqtt("tcp://broker.emqx.io:1883", "mqttx_5487b6c7", "huyunsen", "link$100");
        System.out.println("the one round");


        Random random = new Random();
        double value = 200.0 + random.nextInt(50);
        String stringValue = String.valueOf(value);
        hancloudsClient.publishCollectData("huyunsen",stringValue);

        hancloudsClient.disconnect();
        hancloudsClient.close();
        System.out.println("completed!");
    }

    @Test
    public void test2() {
        HancloudsClient hancloudsClient = HanCloudsClientFactory.getClient();

        // 第1次连接创建设备
        MyCallBack callBack = new MyCallBack();
        String sn = CryptoUtils.getRandomString(8);
        System.out.printf("sn = %s", sn);
        System.out.println();
        hancloudsClient.init("X1UoJ1HY", "ygb4jEUb", "vMDZ0Z9D3poaUdfo", callBack);
        DeviceInfo deviceInfo1 = hancloudsClient.connect(false, sn, true, null);
        System.out.printf("deviceSecret = %s", deviceInfo1.getDeviceSecret());
        System.out.println();
        // 告知服务端已经收到设备鉴权参数，断开和重置连接，后续服务端不会再次向设备反馈deviceSecret
        hancloudsClient.publishInitAck();
        hancloudsClient.disconnect();
        hancloudsClient.close();

        // 第2 次连接
        hancloudsClient.init("X1UoJ1HY", "ygb4jEUb", "vMDZ0Z9D3poaUdfo", callBack);
        // 提供第一次请求时返回的deviceSecret
        DeviceInfo deviceInfo2 = hancloudsClient.connect(false, sn, true, deviceInfo1.getDeviceSecret());

        System.out.println("the one round upload int");
        int i = 0;
        Random random = new Random();
        for (i = 0; i < 500; i++) {
            int value = 100 + random.nextInt(50);
            hancloudsClient.uploadInt("bbb", value);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hancloudsClient.disconnect();

        // 第3次连接
        System.out.println("the second round， upload double");
        deviceInfo2 = hancloudsClient.connect(false, sn, true, deviceInfo1.getDeviceSecret());
        for (i = 0; i < 500; i++) {
            double value = 100.0 + random.nextInt(50);
            hancloudsClient.uploadDouble("ddd", value);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hancloudsClient.disconnect();

        // 第4次连接
        System.out.println("the second round upload string");
        deviceInfo2 = hancloudsClient.connect(false, sn, true, deviceInfo1.getDeviceSecret());
        for (i = 0; i < 500; i++) {
            double value = 200.0 + random.nextInt(50);
            String stringValue = String.valueOf(value);
            hancloudsClient.uploadString("sss", stringValue);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hancloudsClient.disconnect();

        // 第5次连接
        System.out.println("the five round upload bin");
        deviceInfo2 = hancloudsClient.connect(false, sn, true, deviceInfo1.getDeviceSecret());
        for (i = 0; i < 500; i++) {
            double value = 100.0 + random.nextInt(50);
            String stringValue = String.valueOf(value);
            hancloudsClient.uploadBin("bin", stringValue.getBytes());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hancloudsClient.disconnect();

        // 第6次连接
        System.out.println("the six round upload json");
        deviceInfo2 = hancloudsClient.connect(false, sn, true, deviceInfo1.getDeviceSecret());
        for (i = 0; i < 500; i++) {
            int value = 200 + random.nextInt(50);
            String stringValue = String.valueOf(value);
            TestJson json = new TestJson();
            json.setParam1(stringValue);
            json.setParam2(value);
            hancloudsClient.uploadJson("json", JSON.toJSONString(json));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hancloudsClient.disconnect();

        hancloudsClient.close();
        System.out.println("completed!");
    }

    @Test
    public void test3() throws InterruptedException {
        HancloudsClient hancloudsClient = new HancloudsClientImpl("", "tcp://mqtt-broker-device.test.svc.cluster.local:1883");
        hancloudsClient.init("z7HcRQmZ","FyHdVAUR","qg2VHFPKFre0Gkpe",new MyCallBack());
        hancloudsClient.connect(true, "16492146476839435769481", false, null);
        proxyStatusOnline(hancloudsClient);
        //proxyUploadStructure(hancloudsClient);
        //proxyUploadStructureOnly(hancloudsClient);
        //proxyUploadString(hancloudsClient);
//        proxyUploadInt(hancloudsClient);
//        proxyUploadDouble(hancloudsClient);
//        proxyUploadFloat(hancloudsClient);
        proxyUploadArray(hancloudsClient);
//        proxyUploadBin(hancloudsClient);
       // proxyUploadBoolean(hancloudsClient);
//        proxyUploadDate(hancloudsClient);
//        proxyUploadEnum(hancloudsClient);
//        proxyUploadGps(hancloudsClient);
//        proxyUploadJson(hancloudsClient);
//        proxyUploadLong(hancloudsClient);
//        proxyPublishEvent(hancloudsClient);
        //proxyPublishCmdAck(hancloudsClient);
        //proxyStatusOffline(hancloudsClient);
        Thread.sleep(500);
    }

    /**
     * 获取云端拓扑结构
     */
    void proxyGetStructure(HancloudsClient hancloudsClient){
        hancloudsClient.proxyGetStructure();
    }

    /**
     * 子设备上线
     */
    void proxyStatusOnline(HancloudsClient hancloudsClient){
        List<String> list = Arrays.asList("16494032837884253617428");
        hancloudsClient.proxyStatusOnline(list);
    }

    /**
     * 子设备下线
     */
    void proxyStatusOffline(HancloudsClient hancloudsClient){
        List<String> list = Arrays.asList("16494032837884253617428");
        hancloudsClient.proxyStatusOffline(list);
    }

    /**
     * 上报子设备状态，返回全量子设备信息
     */
    void proxyUploadStructure(HancloudsClient hancloudsClient){
        List<String> list = Arrays.asList("sn123");
        hancloudsClient.proxyUploadStructure(list);
    }

    /**
     * 上报子设备状态，返回上报子设备信息
     */
    void proxyUploadStructureOnly(HancloudsClient hancloudsClient){
        List<String> list = Arrays.asList("snonly1234");
        hancloudsClient.proxyUploadStructureOnly(list);
    }

    void proxyUploadString(HancloudsClient hancloudsClient){
        hancloudsClient.proxyUploadString("string","sss","f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadInt(HancloudsClient hancloudsClient){
        hancloudsClient.proxyUploadInt("int",12,"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadDouble(HancloudsClient hancloudsClient){
        hancloudsClient.proxyUploadDouble("double",13d,"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadFloat(HancloudsClient hancloudsClient){
        hancloudsClient.proxyUploadFloat("float",11f,"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadBoolean(HancloudsClient hancloudsClient){
        boolean b = false;
        hancloudsClient.proxyUploadBoolean("boolean", b,"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadArray(HancloudsClient hancloudsClient){
        //Double[] ints = {11.1,22.2,33.3};
        String[] ints = {"sa","3a","asd"};
        List<String> array = Arrays.asList(ints);
        hancloudsClient.proxyUploadArray("array3", JSON.toJSONString(array),"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadGps(HancloudsClient hancloudsClient){
        JSONObject object = new JSONObject();
        object.put("lng",112);
        object.put("lat",171);
        hancloudsClient.proxyUploadGps("gps",object.toJSONString(),"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadLong(HancloudsClient hancloudsClient){
        hancloudsClient.proxyUploadLong("long",133l,"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadDate(HancloudsClient hancloudsClient){
        hancloudsClient.proxyUploadDate("date",System.currentTimeMillis(),"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadEnum(HancloudsClient hancloudsClient){
        hancloudsClient.proxyUploadEnum("enum",1,"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadJson(HancloudsClient hancloudsClient){
        JSONObject object = new JSONObject();
        object.put("up",1);
        object.put("left",2);
        hancloudsClient.proxyUploadJson("json",object.toJSONString(),"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyUploadBin(HancloudsClient hancloudsClient){
        String s = "21d";
        hancloudsClient.proxyUploadBin("bin",s.getBytes(),"f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyPublishCmdAck(HancloudsClient hancloudsClient){
        String commandId = "73873e80cbcf4689a8d8de9ecde0e10b";
        JSONObject object = new JSONObject();
        object.put("up",1);
        String data = object.toJSONString();
        hancloudsClient.proxyPublishCmdAck(commandId, data, "f6c6fe6eec42443d98a7115bbce89f79");
    }

    void proxyPublishEvent(HancloudsClient hancloudsClient){
        String identifier = "event";
        JSONObject object = new JSONObject();
        object.put("out",1);
        String data = object.toJSONString();
        hancloudsClient.proxyPublishEvent(identifier, data, "f6c6fe6eec42443d98a7115bbce89f79");
    }
}
