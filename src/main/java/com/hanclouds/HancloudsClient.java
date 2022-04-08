package com.hanclouds;

import com.hanclouds.impl.AbstractHancloudsCallback;
import com.hanclouds.model.DeviceInfo;

import java.util.List;

/**
 * 设备侧接入HanClouds时的Client
 *
 * @author szl
 * @date 2018/7/15
 */
public interface HancloudsClient {

    /**
     * 初始化Client
     *
     * @param productKey   设备接入的产品的标识
     * @param accessKey    产品接入鉴权AccessKey
     * @param accessSecret 产品接入鉴权秘钥AccessSecret
     * @param callback     收到下行消息时的回调对象
     */
    void init(String productKey, String accessKey, String accessSecret, AbstractHancloudsCallback callback);

    /**
     * 关闭本client内部的资源
     */
    void close();

    /**
     * 以某个设备的身份连接HanClouds，本方法会阻塞，如果鉴权成功返回设备的deviceKey和鉴权加密参数；如果失败返回null
     *
     * @param isGateAway   设备类型是否为网关
     * @param sn           设备序列号，为字母数字下划线组成的字符串
     * @param signMode     是否为签名模式。签名模式则后续数据加密传输
     * @param deviceSecret 本sn对应的设备的deviceSecret，在初次创建时服务端返回
     * @return 成功则返回非NULL，失败返回NULL
     */
    DeviceInfo connect(boolean isGateAway, String sn, boolean signMode, String deviceSecret);

    /**
     * 判断设备当前是否和服务端建立连接
     *
     * @return true标识建立已经建立
     */
    boolean isConnected();

    /**
     * 断开和服务端的mqtt网络连接
     */
    void disconnect();

    /**
     * 向HanClouds上传一个整数
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   整数值
     * @return 成功时返回true
     */
    boolean uploadInt(String stream, int data);

    /**
     * 向HanClouds上传一个字符串数据
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   字符串值
     * @return 成功返回true，失败返回false
     */
    boolean uploadString(String stream, String data);

    /**
     * 向HanClouds上传一个JSON对象
     * 如果不符合JSON格式，则服务端会断开连接
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   字符串形式的json对象
     * @return 成功返回true，失败返回false
     */
    boolean uploadJson(String stream, String data);

    /**
     * 向HanClouds上传一个双精度浮点数对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   浮点数
     * @return 成功返回true，失败返回false
     */
    boolean uploadDouble(String stream, double data);

    /**
     * 向HanClouds上传一个二进制对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   二进制字节数组
     * @return 成功返回true，失败返回false
     */
    boolean uploadBin(String stream, byte[] data);

    /**
     * 向HanClouds上传一个双精度浮点数对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   二进制字节数组
     * @return 成功返回true，失败返回false
     */
    boolean uploadFloat(String stream, float data);

    /**
     * 向HanClouds上传一个enum类型数据对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   二进制字节数组
     * @return 成功返回true，失败返回false
     */
    boolean uploadEnum(String stream, int data);

    /**
     * 向HanClouds上传一个date类型数据对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   二进制字节数组
     * @return 成功返回true，失败返回false
     */
    boolean uploadDate(String stream, long data);

    /**
     * 向HanClouds上传一个long类型数据对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   长整型值
     * @return 成功返回true，失败返回false
     */
    boolean uploadLong(String stream, long data);

    /**
     * 向HanClouds上传一个array类型数据对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   二进制字节数组
     * @return 成功返回true，失败返回false
     */
    boolean uploadArray(String stream, String data);

    /**
     * 向HanClouds上传一个Gps类型数据对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   二进制字节数组
     * @return 成功返回true，失败返回false
     */
    boolean uploadGps(String stream, String data);

    /**
     * 向HanClouds上传一个boolean类型数据对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   boolean类型
     * @return 成功返回true，失败返回false
     */
    boolean uploadBoolean(String stream, boolean data);

    /**
     * 向 HanClouds 发送InitAck消息，告知HanClouds设备侧已经收到设备及其鉴权数据
     */
    void publishInitAck();

    /**
     * 向HanClouds发送命令完成相应
     *
     * @param commandId 命令标识码
     */
    void publishCmdAck(String commandId);

    /**
     * 向HanClouds发送event类型数据
     *
     * @param identifier 事件标识
     * @param data 事件数据
     */
    boolean publishEvent(String identifier, String data);

    /**
     * 网关向HanClouds代理上报子设备拓扑关系，平台返回全量子设备信息列表
     *
     * @param snList   子设备sn号列表
     * @return 成功返回true，失败返回false
     */
    boolean proxyUploadStructure(List<String> snList);

    /**
     * 网关向HanClouds代理上报子设备拓扑关系，平台只返回上报的子设备列表
     *
     * @param snList   子设备sn号列表
     * @return 成功返回true，失败返回false
     */
    boolean proxyUploadStructureOnly(List<String> snList);

    /**
     * 网关向HanClouds请求子设备拓扑关系
     *
     * @return 成功返回true，失败返回false
     */
    boolean proxyGetStructure();

    /**
     * 网关向HanClouds同步子设备上线
     *
     * @param onlineList   子设备sn号列表
     * @return 成功返回true，失败返回false
     */
    boolean proxyStatusOnline(List<String> onlineList);

    /**
     * 网关向HanClouds同步子设备下线
     *
     * @param offlineList   子设备sn号列表
     * @return 成功返回true，失败返回false
     */
    boolean proxyStatusOffline(List<String> offlineList);

    /**
     * 网关代理子设备向HanClouds上传一个整数
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   整数值
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadInt(String stream, int data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个浮点数
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   浮点数
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadDouble(String stream, double data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个二进制对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   二进制字节数组
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadBin(String stream, byte[] data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个字符串
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   字符串
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadString(String stream, String data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个浮点数
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   浮点数
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadFloat(String stream, float data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个JSON对象
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   字符串形式的json对象
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadJson(String stream, String data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个enum类型数据
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   整形
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadEnum(String stream, int data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个date类型数据
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   长整形
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadDate(String stream, long data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个long类型数据
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   长整形
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadLong(String stream, long data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个数组类型数据
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   字符串
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadArray(String stream, String data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个gps数据
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   字符串
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadGps(String stream, String data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds上传一个布尔类型数据
     *
     * @param stream 数据流名字，为字母、数字、下划线组成的字符串
     * @param data   布尔类型数据
     * @param deviceKey   被代理的子设备的deviceKey
     * @return 成功时返回true
     */
    boolean proxyUploadBoolean(String stream, boolean data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds发送event类型数据
     *
     * @param commandId 命令标识符
     * @param data 命令回复数据
     * @param deviceKey   被代理的子设备的deviceKey
     */
    boolean proxyPublishCmdAck(String commandId, String data, String deviceKey);

    /**
     * 网关代理子设备向HanClouds发送event类型数据
     *
     * @param identifier 事件标识
     * @param data 事件数据
     * @param deviceKey   被代理的子设备的deviceKey
     */
    boolean proxyPublishEvent(String identifier, String data, String deviceKey);
}
