package com.hanclouds.util;

/**
 * cmdack 消息响应topic的解析器
 * <p>
 * cmd/{deviceKey}/{cmdId}/{datatype}
 *
 * @author szl
 * @date 2018/3/28
 */
public class CmdTopicWrapper {

    private final static String TOPIC_CMDACK_PREFIX = "cmd/";
    private final static int ITEM_COUNT_CMDACK_TOPIC = 4;

    private String topic;
    private String deviceKey;
    private String commandId;
    private String dataType;

    public boolean init(String topic) {
        if (topic.startsWith(TOPIC_CMDACK_PREFIX)) {
            this.topic = topic;
            String[] result = topic.split("/");
            if (result.length == ITEM_COUNT_CMDACK_TOPIC) {
                deviceKey = result[1];
                commandId = result[2];
                dataType = result[3];
                return true;
            }
        }
        return false;
    }

    public String deviceKey() {
        return deviceKey;
    }

    public String commandId() {
        return commandId;
    }

    public String dataType() {
        return dataType;
    }

}
