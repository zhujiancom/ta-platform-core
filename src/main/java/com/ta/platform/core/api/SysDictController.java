package com.ta.platform.core.api;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ey.tax.toolset.core.NumberUtil;
import com.ey.tax.toolset.core.exceptions.ExceptionUtil;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.modules.system.entity.SysDict;
import com.ta.platform.common.modules.system.service.ISysDictService;
import com.ta.platform.common.system.model.DictModel;
import com.ta.platform.core.query.SearchableQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

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
            List<Object> dictList = dictModelList.stream().map(model -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", model.getText());
                jsonObject.put("title", model.getTitle());
                if(model.getType() == 1){
                    jsonObject.put("value", Integer.valueOf(model.getValue()));
                }else{
                    jsonObject.put("value", model.getValue());
                }
                return jsonObject;
            }).collect(Collectors.toList());
            return Result.ok(dictList);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Throwable cause = ExceptionUtil.getRootCause(e);
            if(cause != null){
                errMsg = cause.getMessage();
            }
            return Result.error(errMsg);
        }
    }

    @RequestMapping(value = "/item-label", method = RequestMethod.GET)
    public Result<String> getDictText(@RequestParam("dictCode") String dictCode, @RequestParam("itemCode") String itemCode){
        Result<String> result = new Result<>();
        String text = dictService.queryDictTextByKey(dictCode, itemCode);
        result.setResult(text);
        result.setSuccess(true);
        return result;
    }

    @GetMapping(value="/page-list")
    public Result<Object> dictPageList(SysDict dict, @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(value="pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest request){
        Result<Object> result = new Result<>();
        QueryWrapper<SysDict> queryWrapper = SearchableQueryWrapper.buildQueryWrapper(dict, request.getParameterMap());
        Page<SysDict> page = new Page(pageNo, pageSize);
        IPage<SysDict> pageList = dictService.page(page, queryWrapper);
        log.debug("查询当前页："+pageList.getCurrent());
        log.debug("查询当前页数量："+pageList.getSize());
        log.debug("查询结果数量："+pageList.getRecords().size());
        log.debug("数据总数："+pageList.getTotal());
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
}
