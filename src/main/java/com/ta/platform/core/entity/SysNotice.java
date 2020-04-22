package com.ta.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ta.platform.common.aspect.annotation.Dict;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Creator: zhuji
 * Date: 4/15/2020
 * Time: 10:18 AM
 * Description: 通知公告实体
 */
@TableName("t_sys_notice")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysNotice implements Serializable {

    private static final long serialVersionUID = -5707579871657984881L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

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
    @Dict(dictCode = "priority")
    private String priority;

    /**
     * 消息类型： 1-通知公告； 2-系统消息
     */
    @Dict(dictCode = "notice_type")
    private String category;

    /**
     * 接收者类型： 1-指定用户；2-指定角色； 3-所有用户
     */
    @Dict(dictCode = "receiver_type")
    private String receiverType;

    /**
     * 消息发布状态： 0-未发布；1-已发布；2-已撤销
     */
    @Dict(dictCode = "publish_state")
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
    @Dict(dictCode = "del_flag")
    private String delFlag;

    /**创建人*/
    private String createBy;
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**更新人*/
    private String updateBy;
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
