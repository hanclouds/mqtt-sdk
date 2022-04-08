package com.hanclouds.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanclouds.HancloudsClient;
import com.hanclouds.model.*;
import com.hanclouds.util.CmdTopicWrapper;
import com.hanclouds.util.CryptoUtils;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

/**
 * HanCloudsClient实现类
 *
 * @author szl
 * @date 2018/7/15
 */
public class HancloudsClientImpl implements HancloudsClient {

    /**
     * 向HanClouds请求设备侧接入地址的restul API地址
     */
    private String API_URL_HANCLOUDS_GATEWAY_IP = "http://api.hanclouds.com/api/v1/mqttGatewayIps";
    /**
     * HTTP请求响应成功
     */
    private final static int HTTP_RESPONSE_OK = 200;

    private String mqttIp;

    /**
     * 设备注册HanClouds时，HanClouds反馈的topic
     */
    private final static String TOPIC_WELCOME = "rsp/welcome";
    /**
     * 下发命令的topic前缀
     */
    private final static String TOPIC_CMD_PREFIX = "cmd/";
    /**
     * 下发模板命令的topic前缀
     */
    private final static String TOPIC_CTL_PREFIX = "ctl/";
    /**
     * ERROR的topic前缀
     */
    private final static String TOPIC_ERROR_PREFIX = "$error/";
    /**
     * rsp/proxy/notify/的topic前缀
     */
    private final static String TOPIC_PROXY_PREFIX = "rsp/proxy/notify/";
    /**
     * sync的topic前缀
     */
    private final static String TOPIC_SYNC_PREFIX = "sync/";
    /**
     * direct的topic前缀
     */
    private final static String TOPIC_DIRECT_PREFIX = "direct/";
    /**
     * version的topic前缀
     */
    private final static String TOPIC_VERSION_PREFIX = "version/";

    private static Logger logger = LoggerFactory.getLogger(HancloudsClientImpl.class);
    private final Object waitWelcome = new Object();
    private MqttClient mqttClient = null;
    private String productKey;
    private String accessKey;
    private String accessSecret;
    private String deviceKey;
    private String sessionSecret;
    private boolean signMode = false;
    private AbstractHancloudsCallback callback;
    private ExecutorService executorService;

    public HancloudsClientImpl() {
    }

    public HancloudsClientImpl(String API_URL_HANCLOUDS_GATEWAY_IP) {
        this.API_URL_HANCLOUDS_GATEWAY_IP = API_URL_HANCLOUDS_GATEWAY_IP;
    }

    public HancloudsClientImpl(String API_URL_HANCLOUDS_GATEWAY_IP, String mqttIp) {
        this.API_URL_HANCLOUDS_GATEWAY_IP = API_URL_HANCLOUDS_GATEWAY_IP;
        this.mqttIp = mqttIp;
    }

    @Override
    public void init(String productKey, String accessKey, String accessSecret, AbstractHancloudsCallback callback) {
        this.productKey = productKey;
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
        this.callback = callback;
        if (executorService != null) {
            executorService.shutdownNow();
        }
        executorService = Executors.newFixedThreadPool(1);
        if (callback != null) {
            callback.setClient(this);
        }
    }

    @Override
    public void close() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnectForcibly(2000, 2000);
                mqttClient.close(true);
                mqttClient = null;
                executorService = null;
            } catch (Exception e) {
                logger.error("close error. {}", e);
            }
        }
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        this.productKey = null;
        this.accessKey = null;
        this.accessSecret = null;
        this.deviceKey = null;
        this.sessionSecret = null;
        this.signMode = false;
    }

    @Override
    public DeviceInfo connect(String deviceType, String sn, boolean signMode, String deviceSecret) {
        if (this.mqttClient != null && this.mqttClient.isConnected()) {
            return null;
        }
        String userName;
        String password;
        String clientId;
        String address = getMqttGatewayIp(productKey, sn);
        if (signMode) {
            clientId = buildClientIdWithSign(deviceType, sn);
            userName = buildUserNameWithSign();
            password = buildPasswordWithSign(deviceType, sn);
            this.signMode = true;
        } else {
            clientId = buildClientId(deviceType, sn);
            userName = buildUserName();
            password = buildPassword();
            this.signMode = false;
        }
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(userName);
        connOpts.setPassword(password.toCharArray());
        connOpts.setConnectionTimeout(30);
        connOpts.setKeepAliveInterval(120);
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(false);
        connOpts.setMqttVersion(MQTT_VERSION_3_1_1);
        DeviceInfo deviceInfo = new DeviceInfo();

        try {
            mqttClient = new MqttClient(address, clientId, null);
            mqttClient.setManualAcks(false);
            mqttClient.setCallback(new MqttCallback() {
                private String dynamicSecret;

                @Override
                public void connectionLost(Throwable cause) {
                    executorService.execute(() -> callback.onConnectionLost());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // receive welcome message
                    if (TOPIC_WELCOME.equals(topic)) {
                        String data = new String(message.getPayload());
                        WelcomeInfo welcomeInfo = JSON.parseObject(data, WelcomeInfo.class);
                        deviceInfo.setDeviceKey(welcomeInfo.getDeviceKey());
                        deviceInfo.setCmdToken(welcomeInfo.getCmdToken());
                        deviceInfo.setQueryToken(welcomeInfo.getQueryToken());
                        deviceInfo.setUploadToken(welcomeInfo.getUploadToken());
                        deviceInfo.setDeviceSecret(welcomeInfo.getDeviceSecret());
                        deviceInfo.setTime(welcomeInfo.getTime());
                        String initDeviceSecret = deviceSecret;
                        if (initDeviceSecret == null) {
                            initDeviceSecret = welcomeInfo.getDeviceSecret();
                        }

                        if (welcomeInfo.getDynamicSecret() != null) {
                            if (initDeviceSecret == null) {
                                throw new Exception("the deviceSecret is missing. please provide the deviceSecret when connect at signMode");
                            } else {
                                dynamicSecret = CryptoUtils.decodeWithAesCbc(initDeviceSecret, welcomeInfo.getDynamicSecret());
                                if (dynamicSecret == null) {
                                    throw new Exception("decode the sessionSecret failed! maybe the deviceSecret is wrong");
                                }
                                deviceInfo.setSessionSecret(dynamicSecret);
                            }
                        }
                        notifyWelcome();
                    } else if (topic.startsWith(TOPIC_CMD_PREFIX)) {
                        byte[] rcvData = message.getPayload();
                        if (signMode) {
                            // recieve command message
                            if (sessionSecret == null) {
                                throw new Exception("the sessionSecret is null. maybe when you connect in signMode but you don't provide the deviceSecret");
                            }
                            logger.debug("receive enc cmd: {}", new String(Base64.encodeBase64(rcvData)));
                            rcvData = CryptoUtils.decodeWithAesCbc(sessionSecret, rcvData);
                            if (rcvData == null) {
                                logger.warn("receive data, but decrypt failed! maybe the sessionSecret is wrong");
                                return;
                            }
                        }
                        byte[] decData = rcvData;
                        CmdTopicWrapper cmdTopicWrapper = new CmdTopicWrapper();
                        if (!cmdTopicWrapper.init(topic)) {
                            logger.warn("the topic is error.");
                        }
                        switch (cmdTopicWrapper.dataType()) {
                            case "bin": {
                                logger.info("receive cmd: {}", new String(Base64.encodeBase64(decData)));
                                if (callback != null) {
                                    executorService.execute(() ->
                                            callback.onRecvCommandBin(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), decData)
                                    );
                                }
                            }
                            break;
                            case "json": {
                                String data = new String(decData);
                                logger.info("receive cmd: {}", data);
                                if (callback != null) {
                                    executorService.execute(() ->
                                            callback.onRecvCommandJson(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), data)
                                    );
                                }
                            }
                            break;
                            case "int": {
                                ByteBuffer byteBuffer = ByteBuffer.wrap(decData);
                                int value = byteBuffer.asIntBuffer().get();
                                logger.info("receive cmd: {}", value);
                                if (callback != null) {
                                    executorService.execute(() ->
                                            callback.onRecvCommandInt(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), value)
                                    );
                                }
                            }
                            break;
                            case "double": {
                                ByteBuffer byteBuffer = ByteBuffer.wrap(decData);
                                double value = byteBuffer.asDoubleBuffer().get();
                                logger.info("receive cmd: {}", value);
                                if (callback != null) {
                                    executorService.execute(() ->
                                            callback.onRecvCommandDouble(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), value)
                                    );
                                }
                            }
                            break;
                            case "string": {
                                String data = new String(decData);
                                logger.info("receive cmd: {}", data);
                                if (callback != null) {
                                    executorService.execute(() ->
                                            callback.onRecvCommandString(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), data)
                                    );
                                }
                            }
                            break;
                            default: {
                            }
                        }
                    }else if(topic.startsWith(TOPIC_CTL_PREFIX)){
                        messageArrivedCtl(topic, message);
                    }else if(topic.startsWith(TOPIC_ERROR_PREFIX)){
                        messageArrivedError(topic,message);
                    }else if(topic.startsWith(TOPIC_SYNC_PREFIX)){
                        messageArrivedSync(topic,message);
                    }else if(topic.startsWith(TOPIC_DIRECT_PREFIX)){
                        messageArrivedError(topic,message);
                    }else if(topic.startsWith(TOPIC_VERSION_PREFIX)){
                        messageArrivedError(topic,message);
                    }else if (topic.startsWith(TOPIC_PROXY_PREFIX)){
                        messageArrivedProxy(topic,message);
                    }

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            mqttClient.connect(connOpts);
            waitWelcome();
            if (mqttClient.isConnected()) {
                if (deviceInfo.getDeviceKey() != null) {
                    this.deviceKey = deviceInfo.getDeviceKey();
                    if (this.sessionSecret == null) {
                        this.sessionSecret = deviceInfo.getSessionSecret();
                    }
                    return deviceInfo;
                } else {
                    mqttClient.disconnectForcibly();
                    mqttClient.close(true);
                }
            } else {
                mqttClient.close(true);
                mqttClient = null;
                return null;
            }
        } catch (Exception e) {
            logger.error("error occur when connect. {}", e);
        }
        return null;
    }
    private void messageArrivedError(String topic, MqttMessage message) throws Exception{
        byte[] rcvData = message.getPayload();
        if (signMode) {
            // recieve command message
            if (sessionSecret == null) {
                throw new Exception("the sessionSecret is null. maybe when you connect in signMode but you don't provide the deviceSecret");
            }
            logger.debug("receive enc ctl: {}", new String(Base64.encodeBase64(rcvData)));
            rcvData = CryptoUtils.decodeWithAesCbc(sessionSecret, rcvData);
            if (rcvData == null) {
                logger.warn("receive data, but decrypt failed! maybe the sessionSecret is wrong");
                return;
            }
        }

        String decData = new String(rcvData);
        logger.info("receive error: {}", new String(decData));

    }

    private void messageArrivedCtl(String topic, MqttMessage message) throws Exception{
        byte[] rcvData = message.getPayload();
        if (signMode) {
            // recieve command message
            if (sessionSecret == null) {
                throw new Exception("the sessionSecret is null. maybe when you connect in signMode but you don't provide the deviceSecret");
            }
            logger.debug("receive enc ctl: {}", new String(Base64.encodeBase64(rcvData)));
            rcvData = CryptoUtils.decodeWithAesCbc(sessionSecret, rcvData);
            if (rcvData == null) {
                logger.warn("receive data, but decrypt failed! maybe the sessionSecret is wrong");
                return;
            }
        }
        CmdTopicWrapper cmdTopicWrapper = new CmdTopicWrapper();
        if (!cmdTopicWrapper.init(topic)) {
            logger.warn("the topic is error.");
        }
        String rcv = new String(rcvData);
        logger.info("receive ctl: {}", rcv);
        if (callback != null) {
            executorService.execute(() ->
                    callback.onRecvCommandTemplate(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), rcv)
            );
        }
    }

    private void messageArrivedSync(String topic, MqttMessage message) throws Exception{
        byte[] rcvData = message.getPayload();
        if (signMode) {
            // recieve command message
            if (sessionSecret == null) {
                throw new Exception("the sessionSecret is null. maybe when you connect in signMode but you don't provide the deviceSecret");
            }
            logger.debug("receive enc ctl: {}", new String(Base64.encodeBase64(rcvData)));
            rcvData = CryptoUtils.decodeWithAesCbc(sessionSecret, rcvData);
            if (rcvData == null) {
                logger.warn("receive data, but decrypt failed! maybe the sessionSecret is wrong");
                return;
            }
        }
        CmdTopicWrapper cmdTopicWrapper = new CmdTopicWrapper();
        if (!cmdTopicWrapper.init(topic)) {
            logger.warn("the topic is error.");
        }
        String rcv = new String(rcvData);
        List<StructureInfo> structureInfoList = JSONArray.parseArray(rcv,StructureInfo.class);
        logger.info("receive ctl: {}", rcv);
        if (callback != null) {
            executorService.execute(() ->
                    callback.onRecvCommandSync(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), structureInfoList)
            );
        }
    }

    private void messageArrivedProxy(String topic, MqttMessage message) throws Exception{
        byte[] rcvData = message.getPayload();
        if (signMode) {
            // recieve command message
            if (sessionSecret == null) {
                throw new Exception("the sessionSecret is null. maybe when you connect in signMode but you don't provide the deviceSecret");
            }
            logger.debug("receive enc ctl: {}", new String(Base64.encodeBase64(rcvData)));
            rcvData = CryptoUtils.decodeWithAesCbc(sessionSecret, rcvData);
            if (rcvData == null) {
                logger.warn("receive data, but decrypt failed! maybe the sessionSecret is wrong");
                return;
            }
        }
        CmdTopicWrapper cmdTopicWrapper = new CmdTopicWrapper();
        if (!cmdTopicWrapper.init(topic)) {
            logger.warn("the topic is error.");
        }
        String rcv = new String(rcvData);

        ProxyNotifyInfo proxyNotifyInfo = JSON.parseObject(rcv, ProxyNotifyInfo.class);
        /*List<ProxyDeviceInfo> proxyDeviceInfos = proxyNotifyInfo.getLoginSuc();
        List<LoginFail> loginFails = proxyNotifyInfo.getLoginFail();
        List<OfflineSuc> offlineSuc = proxyNotifyInfo.getOfflineSuc();
        List<OfflineFail> offlineFail = proxyNotifyInfo.getOfflineFail();
        Map<String, Object> map = new HashMap<>();*/


        logger.info("receive ctl: {}", rcv);
        if (callback != null) {
            executorService.execute(() ->
                    callback.onRecvCommandProxy(cmdTopicWrapper.commandId(), cmdTopicWrapper.deviceKey(), proxyNotifyInfo)
            );
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return mqttClient != null && mqttClient.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnectForcibly(2000, 2000);
                mqttClient.close(true);
                mqttClient = null;
                this.deviceKey = null;
                this.sessionSecret = null;
                this.signMode = false;
            } catch (Exception e) {
                logger.error("disconnect error");
            }
        }
    }

    @Override
    public boolean uploadInt(String stream, int data) {
        String topic = "data/" + deviceKey + "/" + stream + "/int";
        byte[] d = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asIntBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean uploadString(String stream, String data) {
        String topic = "data/" + deviceKey + "/" + stream + "/string";
        byte[] d = data.getBytes();
        return publish(topic, d, 0);
    }

    @Override
    public boolean uploadJson(String stream, String data) {
        String topic = "data/" + deviceKey + "/" + stream + "/json";
        byte[] d = data.getBytes();
        return publish(topic, d, 0);
    }

    @Override
    public boolean uploadDouble(String stream, double data) {
        String topic = "data/" + deviceKey + "/" + stream + "/double";
        byte[] d = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asDoubleBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean uploadBin(String stream, byte[] data) {
        String topic = "data/" + deviceKey + "/" + stream + "/bin";
        return publish(topic, data, 0);
    }

    @Override
    public boolean uploadFloat(String stream, float data) {
        String topic = "data/" + deviceKey + "/" + stream ;
        byte[] d = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asFloatBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean uploadEnum(String stream, int data) {
        return uploadInt(stream, data);
    }

    @Override
    public boolean uploadDate(String stream, long data) {
        return uploadLong(stream, data);
    }

    @Override
    public boolean uploadLong(String stream, long data) {
        String topic = "data/" + deviceKey + "/" + stream ;
        byte[] d = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asLongBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean uploadArray(String stream, String data) {
        return uploadString(stream, data);
    }

    @Override
    public boolean uploadGps(String stream, String data) {
        return uploadJson(stream, data);
    }

    @Override
    public boolean uploadBoolean(String stream, String data) {
        return uploadString(stream, data);
    }

    @Override
    public void publishInitAck() {
        String topic = "initack/" + deviceKey;
        publish(topic, null, 0);
    }

    @Override
    public void publishCmdAck(String commandId) {
        String topic = "cmdack/" + deviceKey + "/" + commandId;
        publish(topic, null, 0);
    }

    @Override
    public boolean publishEvent(String identifier, String data) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("event/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(identifier);
        String topic = builder.toString();
        return publish(topic, data.getBytes(), 0);
    }

    @Override
    public boolean proxyUploadStructure(List<String> snList) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("proxy/");
        builder.append("structureUp");
        String topic = builder.toString();
        JSONObject object = new JSONObject();
        object.put("slaveDeviceSn", snList);
        return publish(topic, object.toJSONString().getBytes(), 0);
    }

    @Override
    public boolean proxyUploadStructureOnly(List<String> snList) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("proxy/");
        builder.append("structureUpOnly");
        String topic = builder.toString();
        JSONObject object = new JSONObject();
        object.put("slaveDeviceSn", snList);
        return publish(topic, object.toJSONString().getBytes(), 0);
    }

    @Override
    public boolean proxyGetStructure() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("proxy/");
        builder.append("structureGet");
        String topic = builder.toString();
        JSONObject object = new JSONObject();
        object.put("connDeviceKey", deviceKey);
        return publish(topic, object.toJSONString().getBytes(), 0);
    }

    @Override
    public boolean proxyStatusOnline(List<String> onlineList) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("proxy/");
        builder.append("structureStatus");
        String topic = builder.toString();
        JSONObject object = new JSONObject();
        object.put("onlineSns", onlineList);
        return publish(topic, object.toJSONString().getBytes(), 0);
    }

    @Override
    public boolean proxyStatusOffline(List<String> offlineList) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("proxy/");
        builder.append("structureStatus");
        String topic = builder.toString();
        JSONObject object = new JSONObject();
        object.put("offlineSns", offlineList);
        return publish(topic, object.toJSONString().getBytes(), 0);
    }

    @Override
    public boolean proxyUploadInt(String stream, int data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("data/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(stream);
        builder.append("/int");
        String topic = builder.toString();
        byte[] d = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asIntBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean proxyUploadDouble(String stream, double data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("data/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(stream);
        builder.append("/double");
        String topic = builder.toString();
        byte[] d = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asDoubleBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean proxyUploadBin(String stream, byte[] data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("data/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(stream);
        builder.append("/bin");
        String topic = builder.toString();
        return publish(topic, data, 0);
    }

    @Override
    public boolean proxyUploadString(String stream, String data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("data/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(stream);
        builder.append("/string");
        String topic = builder.toString();
        return publish(topic, data.getBytes(), 0);
    }

    @Override
    public boolean proxyUploadFloat(String stream, float data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("data/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(stream);
        String topic = builder.toString();
        byte[] d = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asFloatBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean proxyUploadJson(String stream, String data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("data/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(stream);
        builder.append("/json");
        String topic = builder.toString();
        return publish(topic, data.getBytes(), 0);
    }

    @Override
    public boolean proxyUploadEnum(String stream, int data, String deviceKey) {
        return proxyUploadInt(stream, data, deviceKey);
    }

    @Override
    public boolean proxyUploadDate(String stream, long data, String deviceKey) {
        return proxyUploadLong(stream, data, deviceKey);
    }

    @Override
    public boolean proxyUploadLong(String stream, long data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("data/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(stream);
        String topic = builder.toString();
        byte[] d = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(d);
        byteBuffer.asLongBuffer().put(data);
        return publish(topic, d, 0);
    }

    @Override
    public boolean proxyUploadArray(String stream, String data, String deviceKey) {
        return proxyUploadString(stream, data, deviceKey);
    }

    @Override
    public boolean proxyUploadGps(String stream, String data, String deviceKey) {
        return proxyUploadJson(stream, data, deviceKey);
    }

    @Override
    public boolean proxyUploadBoolean(String stream, String data, String deviceKey) {
        return proxyUploadString(stream, data, deviceKey);
    }

    @Override
    public boolean proxyPublishCmdAck(String commandId, String data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("cmdack/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(commandId);
        String topic = builder.toString();
        return publish(topic, data.getBytes(), 0);
    }

    @Override
    public boolean proxyPublishEvent(String identifier, String data, String deviceKey) {
        StringBuilder builder = new StringBuilder(128);
        builder.append("event/");
        builder.append(deviceKey);
        builder.append("/");
        builder.append(identifier);
        String topic = builder.toString();
        return publish(topic, data.getBytes(), 0);
    }

    private String getMqttGatewayIp(String productKey, String sn) {
        if (this.mqttIp != null && this.mqttIp.length() > 0) {
            return this.mqttIp;
        }
        StringBuilder builder = new StringBuilder(128);
        builder.append(API_URL_HANCLOUDS_GATEWAY_IP);
        builder.append("?productKey=");
        builder.append(productKey);
        builder.append("&sn=");
        builder.append(sn);

        String urlString = builder.toString();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.connect();
            int rspCode = connection.getResponseCode();
            String ip;
            if (rspCode == HTTP_RESPONSE_OK) {
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                StringBuilder response = new StringBuilder();
                String line;
                while (null != (line = reader.readLine())) {
                    response.append(line);
                }
                ip = response.toString();
                //return "tcp://118.178.153.178:1883";
                return "tcp://" + ip + ":1883";
            } else {
                logger.error("get mqtt address failed. http response code = {}", rspCode);
            }
            return null;
        } catch (Exception e) {
            logger.error("error occur when query the ip. {}", e);
        }
        return null;
    }

    private String buildClientId(String deviceType, String sn) {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append("d:");
        stringBuilder.append(productKey);
        stringBuilder.append(":");
        stringBuilder.append(deviceType);
        stringBuilder.append(":");
        stringBuilder.append(sn);
        return stringBuilder.toString();
    }

    private String buildUserName() {
        return productKey;
    }

    private String buildPassword() {
        return accessKey + ":" + accessSecret;
    }

    private String buildClientIdWithSign(String deviceType, String sn) {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append("ds:");
        stringBuilder.append(productKey);
        stringBuilder.append(":");
        stringBuilder.append(deviceType);
        stringBuilder.append(":");
        stringBuilder.append(sn);
        return stringBuilder.toString();
    }

    private String buildUserNameWithSign() {
        return productKey;
    }

    private String buildPasswordWithSign(String deviceType, String sn) {
        // {accessKey}:{timestamp}:nonce:{signature}
        // signature = HMacSha1(accessSecret, productKey:accessKey:nonce:deviceType:sn:timestamp)
        StringBuilder builder = new StringBuilder(128);
        String nonce = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis() / 1000;
        builder.append(productKey);
        builder.append(":");
        builder.append(accessKey);
        builder.append(":");
        builder.append(nonce);
        builder.append(":");
        builder.append(deviceType);
        builder.append(":");
        builder.append(sn);
        builder.append(":");
        builder.append(timestamp);
        String content = builder.toString();
        String signature = CryptoUtils.signWithHmacsha1(accessSecret, content);

        builder.setLength(0);
        builder.append(accessKey);
        builder.append(":");
        builder.append(timestamp);
        builder.append(":");
        builder.append(nonce);
        builder.append(":");
        builder.append(signature);
        return builder.toString();
    }

    private boolean publish(String topic, byte[] data, int qos) {
        if (mqttClient == null) {
            return false;
        }
        try {
            if (data == null) {
                data = new byte[0];
            }
            if (data.length > 0 && signMode) {
                data = CryptoUtils.encodeWithAesCbc(sessionSecret, data);
            }
            mqttClient.publish(topic, data, qos, false);
            logger.info("publish data on topic {}", topic);
            return true;
        } catch (Exception e) {
            logger.error("error occur when publish! {}", e);
            return false;
        }
    }

    /**
     * 等待服务端返回rsp/welcome消息
     */
    private void waitWelcome() {
        try {
            synchronized (waitWelcome) {
                waitWelcome.wait(10000);
            }
        } catch (InterruptedException e) {
            logger.error("waitWelcome error! e={}", e);
        }
    }

    private void notifyWelcome() {
        try {
            synchronized (waitWelcome) {
                waitWelcome.notify();
            }
        } catch (Exception e) {
            logger.error("notifyWelcome error! e={}", e);
        }
    }
}
