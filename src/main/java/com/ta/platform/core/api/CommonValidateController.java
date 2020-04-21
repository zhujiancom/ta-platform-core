package com.ta.platform.core.api;

import com.ey.tax.toolset.core.StrUtil;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.modules.system.mapper.SysDictMapper;
import com.ta.platform.common.modules.system.model.DuplicateBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Creator: zhuji
 * Date: 4/20/2020
 * Time: 5:39 PM
 * Description:
 */
@Slf4j
@RestController
@RequestMapping(value = "/sys/validate")
@Api(tags="通用校验逻辑接口")
public class CommonValidateController {
    @Resource
    private SysDictMapper dictMapper;

    @RequestMapping(value = "/duplicate/check", method = RequestMethod.GET)
    @ApiOperation("重复校验接口")
    public Result<Object> duplicateCheck(DuplicateBean duplicateBean){
        Long num = null;
        log.info("----duplicate check------："+ duplicateBean.toString());

        if(StrUtil.isNotBlank(duplicateBean.getDataId())){
            num = dictMapper.duplicateCheckCountSql(duplicateBean);
        }else{
            num = dictMapper.duplicateCheckCountSqlNoDataId(duplicateBean);
        }

        if(num == null || num == 0){
            return Result.ok("该值可用！");
        }else{
            log.info("该值不可用，系统中已存在！");
            return Result.error("该值不可用，系统中已存在！");
        }
    }
}
