package com.ta.platform.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ey.tax.toolset.core.BeanUtil;
import com.ey.tax.toolset.core.collection.CollectionUtil;
import com.ta.platform.common.constant.CommonConstant;
import com.ta.platform.core.constant.BizConstant;
import com.ta.platform.core.convert.SysNoticeConvert;
import com.ta.platform.core.endpoint.WebSocketEndPoint;
import com.ta.platform.core.entity.SysNotice;
import com.ta.platform.core.entity.SysNoticeAction;
import com.ta.platform.core.mapper.SysNoticeActionMapper;
import com.ta.platform.core.mapper.SysNoticeMapper;
import com.ta.platform.core.model.SysNoticeModel;
import com.ta.platform.core.service.ISysNoticeService;
import com.ta.platform.core.service.notify.IReceiverSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Creator: zhuji
 * Date: 4/15/2020
 * Time: 12:45 PM
 * Description:
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {
    @Resource
    private SysNoticeMapper noticeMapper;

    @Resource
    private SysNoticeActionMapper noticeActionMapper;

    @Autowired
    private List<IReceiverSelector> receiverSelectors;

    @Autowired
    private WebSocketEndPoint webSocketEndPoint;

    @Transactional
    @Override
    public void saveNotice(SysNoticeModel sysNoticeModel) {
        SysNotice sysNotice = new SysNotice();
        BeanUtil.copyProperties(sysNoticeModel, sysNotice);
        noticeMapper.insert(sysNotice);
        String noticeId = sysNotice.getId();
        Optional<IReceiverSelector> receiverSelector = receiverSelectors.stream().filter(s -> s.support(sysNoticeModel.getReceiverType())).findAny();
        Map<String, String> parameter = new HashMap<>();
        parameter.put("userIds",sysNoticeModel.getUserIds());
        parameter.put("roleIds",sysNoticeModel.getRoleIds());
        List<String> userIds = receiverSelector.get().getReceiverList(parameter);
        for(String userId : userIds){
            SysNoticeAction sysNoticeAction = new SysNoticeAction();
            sysNoticeAction.setNoticeId(noticeId);
            sysNoticeAction.setUserId(userId);
            sysNoticeAction.setReadState(CommonConstant.NO_READ_FLAG);
            noticeActionMapper.insert(sysNoticeAction);
        }
    }

    @Override
    public void updateNotice(SysNoticeModel sysNoticeModel) {
        SysNotice sysNotice = SysNoticeConvert.INSTANCE.toSysNotice(sysNoticeModel);
        noticeMapper.updateById(sysNotice);
        final String noticeId = sysNotice.getId();
        Optional<IReceiverSelector> receiverSelector = receiverSelectors.stream().filter(s -> s.support(sysNoticeModel.getReceiverType())).findAny();
        Map<String, String> parameter = new HashMap<>();
        parameter.put("userIds",sysNoticeModel.getUserIds());
        parameter.put("roleIds",sysNoticeModel.getRoleIds());
        List<String> userIds = receiverSelector.get().getReceiverList(parameter);
        for(String userId : userIds){
            LambdaQueryWrapper<SysNoticeAction> queryWrapper = new LambdaQueryWrapper<SysNoticeAction>();
            queryWrapper.eq(SysNoticeAction::getNoticeId, noticeId);
            queryWrapper.eq(SysNoticeAction::getUserId, userId);
            List<SysNoticeAction> noticeActionList=noticeActionMapper.selectList(queryWrapper);
            if(CollectionUtil.isEmpty(noticeActionList)){
                SysNoticeAction sysNoticeAction = new SysNoticeAction();
                sysNoticeAction.setNoticeId(noticeId);
                sysNoticeAction.setUserId(userId);
                sysNoticeAction.setReadState(CommonConstant.NO_READ_FLAG);
                noticeActionMapper.insert(sysNoticeAction);
            }
        }
    }

    @Override
    public Page<SysNotice> fetchHeaderNotice(Page<SysNotice> page, String userId,String category){
       return page.setRecords(noticeMapper.queryUnreadNoticeListByCategory(page,userId, category));
    }

    /**
     * 发布通知
     * @param sysNotice
     */
    @Override
    public void publishNotice(SysNotice sysNotice) {
        LambdaQueryWrapper<SysNoticeAction> queryWrapper = new LambdaQueryWrapper<SysNoticeAction>();
        queryWrapper.eq(SysNoticeAction::getNoticeId, sysNotice.getId());
        List<SysNoticeAction> noticeActionList = noticeActionMapper.selectList(queryWrapper);
        List<String> receiverUserIds = noticeActionList.stream().map(a -> a.getUserId()).collect(Collectors.toList());
        sysNotice.setPublishTime(new Date());
        sysNotice.setPublishState(BizConstant.PUBLISH_STATE_DEPLOY);
        noticeMapper.updateById(sysNotice);
        webSocketEndPoint.sendMoreMessage(receiverUserIds, "发布通知公告！");
    }

    /**
     * 发布通知
     * @param sysNotice
     */
    @Override
    public void revokeNotice(SysNotice sysNotice) {
        LambdaQueryWrapper<SysNoticeAction> queryWrapper = new LambdaQueryWrapper<SysNoticeAction>();
        queryWrapper.eq(SysNoticeAction::getNoticeId, sysNotice.getId());
        List<SysNoticeAction> noticeActionList = noticeActionMapper.selectList(queryWrapper);
        List<String> receiverUserIds = noticeActionList.stream().map(a -> a.getUserId()).collect(Collectors.toList());
        sysNotice.setPublishState(BizConstant.PUBLISH_STATE_REVOKE);
        sysNotice.setCancelTime(new Date());
        noticeMapper.updateById(sysNotice);
        webSocketEndPoint.sendMoreMessage(receiverUserIds, "撤销通知公告！");
    }

    @Override
    public void readNotice(String noticeId, String userId) {
        LambdaQueryWrapper<SysNoticeAction> queryWrapper = new LambdaQueryWrapper<SysNoticeAction>();
        queryWrapper.eq(SysNoticeAction::getNoticeId, noticeId);
        queryWrapper.eq(SysNoticeAction::getUserId, userId);
        SysNoticeAction noticeAction = noticeActionMapper.selectOne(queryWrapper);
        if(noticeAction != null){
            noticeAction.setReadState(CommonConstant.HAS_READ_FLAG);
            noticeAction.setReadTime(new Date());
            noticeActionMapper.updateById(noticeAction);
        }
    }
}
