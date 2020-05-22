package com.ta.platform.core.api;

import com.ey.tax.toolset.http.HttpUtil;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.clientapi.GatewayAPIClient;
import com.ta.platform.common.module.entity.SysLog;
import com.ta.platform.common.module.mapper.SysLogMapper;
import com.ta.platform.common.system.model.SysLogModel;
import com.ta.platform.common.tool.ApplicationContextProvider;
import com.ta.platform.common.tool.JwtUtil;
import com.ta.platform.common.vo.LoginUserRedisVo;
import com.ta.platform.core.convert.SysLogConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Creator: zhuji
 * Date: 4/9/2020
 * Time: 7:25 PM
 * Description:
 */
@RestController
@RequestMapping("/api/syslog")
public class LoggerController {
    @Autowired
    private GatewayAPIClient gatewayAPIClient;

    @Autowired
    private SysLogMapper sysLogMapper;


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public void addLog(@RequestBody SysLogModel model) {

        SysLog sysLog = SysLogConvert.INSTANCE.sysLogModelToSysLogEntity(model);
        try {
            //获取request
            HttpServletRequest request = ApplicationContextProvider.getHttpServletRequest();
            //设置IP地址
            sysLog.setIp(HttpUtil.getClientIP(request));
        //获取登录用户信息
            Result<LoginUserRedisVo> result = gatewayAPIClient.getLoginUser(JwtUtil.getToken(request));
            if (result.isSuccess()) {
                LoginUserRedisVo loginUser = result.getData();
                sysLog.setUserid(loginUser.getUsername());
                sysLog.setUsername(loginUser.getRealname());
            }
        } catch (Exception e) {
            sysLog.setIp("127.0.0.1");
        }

        sysLog.setCreateTime(new Date());
        //保存系统日志
        sysLogMapper.insert(sysLog);
    }
}
