import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        WebSocketClient client = new WebSocketClient(new URI("ws://127.0.0.1:8888/action/callback/B3563580-3029-44C3-B132-XXXXXXXXXX")) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("onOpen");
            }
            @Override
            public void onMessage(String message) {
                JSONObject actionJsonMsg = JSON.parseObject(message);
            //    System.out.println("onMessage:"+JSON.toJSONString(actionJsonMsg,SerializerFeature.PrettyFormat));

                System.out.println("=================收到消息====================");
                String actionType = actionJsonMsg.getString("type");
                System.out.println(String.format("-- 收到客户端事件 类型：%s",actionType));
                System.out.println(String.format("-- 事件来源：%s",actionJsonMsg.getString("machineCode")));
                System.out.println(String.format("-- 所属微信客户端：%s",actionJsonMsg.getString("clientId")));
                switch (actionType){
                    case "on_user_msg":{
                        //用户消息
                        System.out.println("  -- 目标用户："+actionJsonMsg.getJSONObject("action").getString("targetUserWxid"));
                        System.out.println("  -- 是否为群消息："+actionJsonMsg.getJSONObject("action").getBoolean("isGroupMsg"));
                        System.out.println("  -- 所属群wxid："+actionJsonMsg.getJSONObject("action").getString("groupWxid"));
                        System.out.println("  -- @的用户："+actionJsonMsg.getJSONObject("action").getString("atUserListWxids"));
                        System.out.println("  -- 消息流向："+(actionJsonMsg.getJSONObject("action").getInteger("msgFrom") == 0 ?"我发出去的":"发给我的"));
                        System.out.println("  -- 消息内容："+ Base64.decodeStr(actionJsonMsg.getJSONObject("action").getString("msgBody")));

                        String toUserWxid = actionJsonMsg.getJSONObject("action").getBoolean("isGroupMsg") ? actionJsonMsg.getJSONObject("action").getString("groupWxid"):actionJsonMsg.getJSONObject("action").getString("targetUserWxid");
                        sendMsg(
                                actionJsonMsg.getString("machineCode"),
                                actionJsonMsg.getJSONObject("action").getString("accountWxid"),
                                toUserWxid,
                                "自动回复:" + Base64.decodeStr(actionJsonMsg.getJSONObject("action").getString("msgBody")));
                    }
                }
                System.out.println("=============================================");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("onClose");
            }

            @Override
            public void onError(Exception ex) {
                System.out.println("onError");
            }
        };
        client.connect();
    }

    /**
     * 发送文本消息
     * @param machineCode
     * @param accountWxid
     * @param toUserWxid
     * @param msg
     */
    private static void sendMsg(String machineCode,String accountWxid,String toUserWxid,String msg){
        HttpUtil.createPost("http://127.0.0.1:8888/api/sendTextMsg")
                .contentType("application/json")
                .charset("utf-8")
                .body(JSON
                        .toJSONString(MapUtil.builder()
                                .put("machineCode",machineCode)
                                .put("accountWxid",accountWxid)
                                .put("toUserWxid",toUserWxid)
                                .put("msg",msg).build()))
                .execute();

    }
}
