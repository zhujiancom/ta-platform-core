package com.ta.platform.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Creator: zhuji
 * Date: 4/15/2020
 * Time: 11:40 AM
 * Description: 通知模型类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@AllArgsConstructor
public class SysNoticeModel implements Serializable {

    private static final long serialVersionUID = 6538871140518972824L;

    private String id;

    private String noticeId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 发布人
     */
    private String publisher;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 消息类型： 1-通知公告； 2-系统消息
     */
    private String category;

    /**
     * 接收者类型： 1-指定用户；2-指定角色； 3-所有用户
     */
    private String receiverType;

    /**
     * 消息发布状态： 0-未发布；1-已发布；2-已撤销
     */
    private String publishState;

    /**
     * 消息发布时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /**
     * 消息撤销时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;

    /**
     * 删除状态
     */
    private String delFlag;

    /**
     * 阅读状态
     */
    private String readState;

    /**
     * 阅读时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private String readTime;

    private String userIds;

    private String roleIds;
}
