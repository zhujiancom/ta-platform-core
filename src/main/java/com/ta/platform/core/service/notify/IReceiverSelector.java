package com.ta.platform.core.service.notify;

import com.ta.platform.core.model.ReceiverModel;

import java.util.List;
import java.util.Map;

/**
 * Creator: zhuji
 * Date: 4/16/2020
 * Time: 12:26 PM
 * Description:
 */
public interface IReceiverSelector {
    List<String> getReceiverList(Map<String,String> parameter);

    boolean support(String receiverType);
}
