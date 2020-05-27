package com.ta.platform.core.endpoint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Creator: zhuji
 * Date: 5/26/2020
 * Time: 4:55 PM
 * Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ApiModel("WebSocket消息实体")
public class MessageModel implements Serializable {

    @ApiModelProperty(value = "消息内容")
    private String message;

    @ApiModelProperty(value = "接收人Id")
    private String userId;
}
