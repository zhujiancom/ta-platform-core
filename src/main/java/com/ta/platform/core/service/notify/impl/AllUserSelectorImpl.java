package com.ta.platform.core.service.notify.impl;

import com.ey.tax.toolset.core.StrUtil;
import com.ta.platform.common.constant.CommonConstant;
import com.ta.platform.core.service.notify.IReceiverSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class AllUserSelectorImpl implements IReceiverSelector {
    @Override
    public List<String> getReceiverList(Map<String, String> parameter) {
        return new ArrayList<>();
    }

    @Override
    public boolean support(String receiverType) {
        return CommonConstant.RECEIVER_TYPE_ALL.equals(receiverType) || StrUtil.isEmpty(receiverType);
    }
}
