package com.ta.platform.core.api;

import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.clientapi.GatewayAPIClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Creator: zhuji
 * Date: 4/24/2020
 * Time: 5:11 PM
 * Description:
 */
@Slf4j
@RestController
@Api(tags = "测试其他接口服务的接口")
@RequestMapping(value = "/api/test")
public class TestAPIController {

    @Autowired
    private GatewayAPIClient gatewayAPIClient;

    @ApiOperation(value = "测试用户鉴权接口")
    @RequestMapping(value = "/auth/login-user")
    public Result<Object> testAuthApi(@RequestParam("token") String token){
        Result<Object> result = gatewayAPIClient.getLoginUser(token);
        log.info("username = "+result.getResult());
        return result;
    }
}
