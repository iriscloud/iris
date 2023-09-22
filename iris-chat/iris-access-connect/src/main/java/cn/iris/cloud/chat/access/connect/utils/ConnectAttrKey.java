package cn.iris.cloud.chat.access.connect.utils;

import io.netty.util.AttributeKey;


public class ConnectAttrKey {

    private ConnectAttrKey() {
    }

    public static final AttributeKey<String> USER_ID = AttributeKey.valueOf(
        "CON.userId");
    public static final AttributeKey<String> CLIENT_ID = AttributeKey.valueOf(
        "CON.clientId");


}
