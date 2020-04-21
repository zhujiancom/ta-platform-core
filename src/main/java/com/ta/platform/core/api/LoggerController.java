package com.ta.platform.core.api;

import com.ta.platform.common.system.model.SysLogModel;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Creator: zhuji
 * Date: 4/9/2020
 * Time: 7:25 PM
 * Description:
 */
@RestController
@RequestMapping("/api/syslog")
public class LoggerController {

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public void addLog(@RequestBody SysLogModel model){
        //TODO
    }
}
