package com.ta.platform.core.service.notify.impl;

import com.ey.tax.toolset.core.collection.CollectionUtil;
import com.ta.platform.common.constant.CommonConstant;
import com.ta.platform.core.service.notify.IReceiverSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Creator: zhuji
 * Date: 4/16/2020
 * Time: 1:50 PM
 * Description:
 */
@Slf4j
@Service
public class ByUserSelectorImpl implements IReceiverSelector {
    @Override
    public List<String> getReceiverList(Map<String, String> parameter) {
        String userIds = parameter.get("userIds");
        String[] userIdArr = userIds.substring(0, (userIds.length()-1)).split(",");
        return CollectionUtil.newArrayList(userIdArr);
    }

    @Override
    public boolean support(String receiverType) {
        return CommonConstant.RECEIVER_TYPE_USER.equals(receiverType);
    }
}
