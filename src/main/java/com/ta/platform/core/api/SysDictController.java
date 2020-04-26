package com.ta.platform.core.api;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ey.tax.toolset.core.StrUtil;
import com.ey.tax.toolset.core.exceptions.ExceptionUtil;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.constant.CacheConstant;
import com.ta.platform.common.constant.CommonConstant;
import com.ta.platform.common.modules.system.entity.SysDict;
import com.ta.platform.common.modules.system.entity.SysDictItem;
import com.ta.platform.common.modules.system.service.ISysDictItemService;
import com.ta.platform.common.modules.system.service.ISysDictService;
import com.ta.platform.common.system.model.DictModel;
import com.ta.platform.common.tool.DictHelper;
import com.ta.platform.core.query.SearchableQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private ISysDictItemService dictItemService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

    @PostMapping(value="/add")
    public Result<Object> add(@RequestBody SysDict sysDict){
        Result<Object> result = new Result<>();
        try {
            sysDict.setDelFlag(CommonConstant.DEL_FLAG_0);
            dictService.save(sysDict);
            result.success("操作成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败！");
        }
        return result;
    }

    @PutMapping(value="/edit")
    public Result<Object> edit(@RequestBody SysDict sysDict) {
        Result<Object> result = new Result<>();
        SysDict dbDict = dictService.getById(sysDict.getId());
        if(dbDict == null){
            result.error500("未找到对应的实体！");
        }else{
            boolean ok = dictService.updateById(sysDict);
            if(ok){
                result.success("修改成功！");
            }
        }
        return result;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @CacheEvict(value= CacheConstant.SYS_DICT_CACHE, allEntries=true)
    public Result<Object> delete(@RequestParam(name="id",required=true) String id) {
        Result<Object> result = new Result<Object>();
        boolean ok = dictService.removeById(id);
        if(ok) {
            result.success("删除成功!");
        }else{
            result.error500("删除失败!");
        }
        return result;
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    @CacheEvict(value= CacheConstant.SYS_DICT_CACHE, allEntries=true)
    public Result<Object> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        Result<Object> result = new Result<Object>();
        if(StrUtil.isEmpty(ids)) {
            result.error500("参数不识别！");
        }else {
            dictService.removeByIds(Arrays.asList(ids.split(",")));
            result.success("删除成功!");
        }
        return result;
    }

    @PutMapping(value="/refreshCache")
    public Result<Object> refreshCache(){
        Result<Object> result = new Result<>();
        // 清空字典缓存
        try {
            Set keys= redisTemplate.keys(CacheConstant.SYS_DICT_CACHE);
            Set keys2 = redisTemplate.keys(CacheConstant.SYS_DICT_TABLE_CACHE);
            Set keys3 = redisTemplate.keys(CacheConstant.SYS_DEPARTS_CACHE + "*");
            Set keys4 = redisTemplate.keys(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
            redisTemplate.delete(keys);
            redisTemplate.delete(keys2);
            redisTemplate.delete(keys3);
            redisTemplate.delete(keys4);
            result.success("字典缓存刷新成功！");
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            result.error500("字典缓存刷新失败！");
        }
        return result;
    }

    //---------------- dict items -------------------------------

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

    @GetMapping(value = "/item/page-list")
    public Result<Object> itemsPageList(SysDictItem dictItem, @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize, HttpServletRequest request){
        Result<Object> result = new Result<>();
        QueryWrapper<SysDictItem> queryWrapper = SearchableQueryWrapper.buildQueryWrapper(dictItem,request.getParameterMap());
        Page<SysDictItem> page = new Page<>(pageNo, pageSize);
        IPage<SysDictItem> pageList = dictItemService.page(page, queryWrapper);
        List<JSONObject> realPageList = pageList.getRecords().stream().map(item -> DictHelper.parseDictField(item)).collect(Collectors.toList());
        IPage newPageList = pageList;
        newPageList.setRecords(realPageList);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    @PostMapping(value = "/item/add")
    @CacheEvict(value= CacheConstant.SYS_DICT_CACHE, allEntries=true)
    public Result<Object> addItem(@RequestBody SysDictItem dictItem){
        Result<Object> result = new Result<>();
        try {
            dictItemService.save(dictItem);
            result.success("操作成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败！");
        }
        return result;
    }

    @PutMapping(value="/item/edit")
    @CacheEvict(value= CacheConstant.SYS_DICT_CACHE, allEntries=true)
    public Result<Object> editItem(@RequestBody SysDictItem dictItem) {
        Result<Object> result = new Result<>();
        SysDictItem dbDictItem = dictItemService.getById(dictItem.getId());
        if(dbDictItem == null){
            result.error500("未找到对应的实体！");
        }else{
            boolean ok = dictItemService.updateById(dictItem);
            if(ok){
                result.success("修改成功！");
            }
        }
        return result;
    }

    @RequestMapping(value = "/item/delete", method = RequestMethod.DELETE)
    @CacheEvict(value=CacheConstant.SYS_DICT_CACHE, allEntries=true)
    public Result<SysDictItem> deleteItem(@RequestParam(name="id",required=true) String id) {
        Result<SysDictItem> result = new Result<SysDictItem>();
        SysDictItem dbDictItem = dictItemService.getById(id);
        if(dbDictItem==null) {
            result.error500("未找到对应实体");
        }else {
            boolean ok = dictItemService.removeById(id);
            if(ok) {
                result.success("删除成功!");
            }
        }
        return result;
    }

}
