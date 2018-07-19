package com.hanclouds;

import com.hanclouds.impl.AbstractHancloudsCallback;
import com.hanclouds.model.DeviceInfo;

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
     * @param deviceType   设备类型，为字母数字下划线组成的字符串，不能有空格和特殊字符
     * @param sn           设备序列号，为字母数字下划线组成的字符串
     * @param signMode     是否为签名模式。签名模式则后续数据加密传输
     * @param deviceSecret 本sn对应的设备的deviceSecret，在初次创建时服务端返回
     * @return 成功则返回非NULL，失败返回NULL
     */
    DeviceInfo connect(String deviceType, String sn, boolean signMode, String deviceSecret);

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
     * 向 HanClouds 发送InitAck消息，告知HanClouds设备侧已经收到设备及其鉴权数据
     */
    void publishInitAck();

    /**
     * 向HanClouds发送命令完成相应
     *
     * @param commandId 命令标识码
     */
    void publishCmdAck(String commandId);
}
