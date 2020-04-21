package com.ta.platform.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ey.tax.toolset.core.BeanUtil;
import com.ey.tax.toolset.core.collection.CollectionUtil;
import com.ta.platform.common.constant.CommonConstant;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        SysNotice sysNotice = new SysNotice();
        BeanUtil.copyProperties(sysNoticeModel, sysNotice);
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
}
