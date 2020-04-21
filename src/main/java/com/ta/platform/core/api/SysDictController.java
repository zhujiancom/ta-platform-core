package com.ta.platform.core.api;

import com.ey.tax.toolset.core.exceptions.ExceptionUtil;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.modules.system.service.ISysDictService;
import com.ta.platform.common.system.model.DictModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Creator: zhuji
 * Date: 4/21/2020
 * Time: 4:03 PM
 * Description:
 */
@Slf4j
@RestController
@RequestMapping(value = "/sys/dict")
public class SysDictController {

    @Autowired
    private ISysDictService dictService;

    @RequestMapping(value = "/item-list", method = RequestMethod.GET)
    public Result<Object> getDictItemList(@RequestParam("dictCode") String dictCode){
        try {
            List<DictModel> dictModelList = dictService.queryDictItemsByCode(dictCode);
            return Result.ok(dictModelList);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Throwable cause = ExceptionUtil.getRootCause(e);
            if(cause != null){
                errMsg = cause.getMessage();
            }
            return Result.error(errMsg);
        }
    }
}
