package com.ta.platform.core.endpoint;

import com.alibaba.fastjson.JSONObject;
import com.ey.tax.toolset.core.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Creator: zhuji
 * Date: 5/26/2020
 * Time: 4:44 PM
 * Description: WebSocket 后端响应节点
 */
@Component
@Slf4j
@ServerEndpoint("/websocket/{userId}") //此注解相当于设置访问URL
public class WebSocketEndPoint {

    private Session session;

    private static CopyOnWriteArraySet<WebSocketEndPoint> webSockets = new CopyOnWriteArraySet<>();
    private static Map<String, Session> sessionPool = new HashMap<String, Session>();

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        try {
            this.session = session;
            webSockets.add(this);
            sessionPool.put(userId, session);
            log.info("【websocket消息】有新的连接，总数为:" + webSockets.size());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            log.info("【websocket消息】连接断开，总数为:" + webSockets.size());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("【websocket消息】收到客户端消息:"+message);
        JSONObject obj = new JSONObject();
        obj.put("cmd", "heartcheck");//业务类型
        obj.put("msgTxt", "服务器心跳响应正常");//消息内容
        session.getAsyncRemote().sendText(obj.toJSONString());
    }

    // 此为广播消息
    public void sendAllMessage(String message) {
        log.info("【websocket消息】广播消息:"+message);
        JSONObject obj = new JSONObject();
        obj.put("cmd", "topic");
        obj.put("msgId", RandomUtil.getUUIDBaseOnV1());
        obj.put("msgTxt",message);
        for(WebSocketEndPoint webSocket : webSockets) {
            try {
                if(webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText(obj.toJSONString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 此为单点消息
    public void sendOneMessage(String userId, JSONObject jsonObject) {
        Session session = sessionPool.get(userId);
        if (session != null&&session.isOpen()) {
            try {
                log.info("【websocket消息】 单点消息:"+jsonObject.toJSONString());
                session.getAsyncRemote().sendText(jsonObject.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 此为单点消息(多人)
    public void sendMoreMessage(List<String> userIds, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cmd","user");
        jsonObject.put("msgTxt", message);
        for(String userId:userIds) {
            sendOneMessage(userId, jsonObject);
        }

    }
}
