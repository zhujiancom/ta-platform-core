package com.ta.platform.core.api;

import com.alibaba.fastjson.JSONObject;
import com.ey.tax.toolset.core.RandomUtil;
import com.ey.tax.toolset.core.StrUtil;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.core.endpoint.MessageModel;
import com.ta.platform.core.endpoint.WebSocketEndPoint;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Creator: zhuji
 * Date: 5/26/2020
 * Time: 4:49 PM
 * Description:
 */
@RestController
@RequestMapping("/api/websocket")
@Api(tags = "websocket发送消息接口")
public class MockWebSocketController {

    @Autowired
    private WebSocketEndPoint webSocketEndPoint;

    @PostMapping("/sendAll")
    @ApiOperation("广播消息")
    public Result<Boolean> sendAll(@RequestBody MessageModel messageModel){
        String message = messageModel.getMessage();
        webSocketEndPoint.sendAllMessage(message);
        return Result.ok("群发成功!");
    }

    @PostMapping("/sendUser")
    @ApiOperation("发送给单个用户")
    public Result<Boolean> sendToUser(@RequestBody MessageModel messageModel){
        String userId = messageModel.getUserId();
        String message = messageModel.getMessage();

        JSONObject obj = new JSONObject();
        obj.put("cmd", "user");
        obj.put("userId", userId);
        obj.put("msgId", RandomUtil.getUUIDBaseOnV1());
        obj.put("msgTxt",message);

        webSocketEndPoint.sendOneMessage(userId, obj);
        return Result.ok(StrUtil.format("发送消息给用户{}成功", userId));
    }
}
