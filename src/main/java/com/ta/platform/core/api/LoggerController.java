package com.ta.platform.core.api;

import com.ta.platform.core.api.vo.SysLogModel;
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
@RequestMapping("/logger")
public class LoggerController {

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public void addLog(@RequestBody SysLogModel model){
        //TODO
    }
}
