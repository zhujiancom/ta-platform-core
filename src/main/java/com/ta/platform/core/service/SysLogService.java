package com.ta.platform.core.service;

import com.ta.platform.common.system.model.SysLogModel;
import com.ta.platform.core.mapper.SysLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creator: zhuji
 * Date: 4/9/2020
 * Time: 7:14 PM
 * Description: 操作日志服务类
 */
@Service
public class SysLogService {
    @Autowired
    private SysLogMapper sysLogMapper;

    //TODO
    public void addLog(SysLogModel sysLogModel){

    }
}
