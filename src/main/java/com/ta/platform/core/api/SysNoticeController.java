package com.ta.platform.core.api;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ey.tax.toolset.core.BeanUtil;
import com.ey.tax.toolset.core.StrUtil;
import com.ey.tax.toolset.core.exceptions.ExceptionUtil;
import com.ta.platform.common.api.ApiCode;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.constant.CommonConstant;
import com.ta.platform.common.tool.DictHelper;
import com.ta.platform.common.tool.JwtUtil;
import com.ta.platform.core.entity.SysNotice;
import com.ta.platform.core.entity.SysNoticeAction;
import com.ta.platform.core.model.SysNoticeModel;
import com.ta.platform.core.service.impl.SysNoticeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.ta.platform.common.constant.CommonConstant.CATEGORY_ANNOUNCE;
import static com.ta.platform.common.constant.CommonConstant.CATEGORY_NOTICE;
import static com.ta.platform.common.constant.CommonConstant.DEL_FLAG_0;
import static com.ta.platform.common.constant.CommonConstant.UNPUBLISHED_STATE;

/**
 * Creator: zhuji
 * Date: 4/15/2020
 * Time: 12:25 PM
 * Description:
 */
@Slf4j
@RestController
@RequestMapping("/sys/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeServiceImpl noticeService;

    /**
     * 分页获取通知公告
     * @param sysNoticeModel
     * @param pageNo
     * @param pageSize
     * @param request
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<Object> noticePageList(SysNoticeModel sysNoticeModel, @RequestParam(name="pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name="pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest request){
        Result<Object> result = new Result<>();
        SysNotice sysNotice = new SysNotice();
        BeanUtil.copyProperties(sysNoticeModel, sysNotice);
        sysNotice.setDelFlag(DEL_FLAG_0.toString());
        QueryWrapper<SysNotice> queryWrapper = new QueryWrapper<>(sysNotice);
        Page<SysNotice> page = new Page(pageNo, pageSize);
        // 设置排序
        String column = request.getParameter("column");
        String order = request.getParameter("order");
        if(StrUtil.isNotEmpty(column) && StrUtil.isNotEmpty(order)){
            if("asc".equals(order)){
                queryWrapper.orderByAsc(StrUtil.toUnderlineCase(column));
            }else{
                queryWrapper.orderByDesc(StrUtil.toUnderlineCase(column));
            }
        }
        IPage<SysNotice> pageList = noticeService.page(page, queryWrapper);

        log.info("查询当前页："+pageList.getCurrent());
        log.info("查询当前页数量："+pageList.getSize());
        log.info("查询结果数量："+pageList.getRecords().size());
        log.info("数据总数："+pageList.getTotal());

        List<JSONObject> realPageList = pageList.getRecords().stream().map(item -> DictHelper.parseDictField(item)).collect(Collectors.toList());
        IPage newPageList = pageList;
        newPageList.setRecords(realPageList);
        return Result.ok(JSONObject.toJSON(newPageList));
    }

    /**
     * 获取用户的头部通知， 包括通知公告和系统消息
     * @param userId
     * @return
     */
    @RequestMapping(value = "/fetch_header_notice", method = RequestMethod.GET)
    public Result<JSONObject> fetchNotice(@RequestParam("userId") String userId){
        try {
            Page<SysNotice> noticePage = new Page(0,5);
            noticePage = noticeService.fetchHeaderNotice(noticePage,userId, CATEGORY_NOTICE);

            Page<SysNotice> announcePage = new Page(0,5);
            announcePage = noticeService.fetchHeaderNotice(announcePage,userId, CATEGORY_ANNOUNCE);

            JSONObject json = new JSONObject();
            json.put("noticeList", noticePage.getRecords());
            json.put("noticeCount", noticePage.getTotal());
            json.put("announceList", announcePage.getRecords());
            json.put("announceCount", announcePage.getTotal());

            return Result.ok(json, "头部通知数据获取成功");
        } catch (Exception e) {
            log.error("获取头部通知数据失败！",e);
            String errorMsg = e.getMessage();
            if(ExceptionUtil.getRootCause(e)!=null){
                errorMsg = ExceptionUtil.getRootCause(e).getMessage();
            }
            return Result.result(ApiCode.FAIL,errorMsg,null);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<Boolean> add(@RequestBody SysNoticeModel sysNoticeModel){
        try {
            sysNoticeModel.setDelFlag(DEL_FLAG_0.toString());
            sysNoticeModel.setPublishState(UNPUBLISHED_STATE);
            noticeService.saveNotice(sysNoticeModel);
            return Result.ok();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping(value = "/delete")
    public Result<Boolean> delete(@RequestParam(value = "id") String id){
        SysNotice sysNotice = noticeService.getById(id);
        if(sysNotice == null){
            return Result.error("未找到对应实体！");
        }
        sysNotice.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
        boolean ok = noticeService.updateById(sysNotice);
        if(ok){
            return Result.ok("删除成功！");
        }else{
            return Result.error("删除失败！");
        }
    }

    @DeleteMapping(value = "/deleteBatch")
    public Result<Boolean> deleteBatch(@RequestParam(value = "ids") String ids){
        if(StrUtil.isBlank(ids)){
            return Result.error("参数不识别！");
        }
        String[] idArr = ids.split(",");
        for(int i=0;i<idArr.length;i++){
            SysNotice sysNotice = noticeService.getById(idArr[i]);
            sysNotice.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
            noticeService.updateById(sysNotice);
        }
        return Result.ok("批量删除成功！");
    }

    @GetMapping(value = "/publish")
    public Result<Boolean> publishAnnounce(@RequestParam(value = "id") String id,HttpServletRequest request){
        String token = JwtUtil.getToken(request);
        String currentUser = JwtUtil.getUsername(token);
        SysNotice sysNotice = noticeService.getById(id);
        sysNotice.setPublisher(currentUser);
        noticeService.publishNotice(sysNotice);
        return Result.ok("公告发布成功");
    }

    @GetMapping(value = "/revoke")
    public Result<Boolean> revokeAnnounce(@RequestParam(value = "id") String id){
        SysNotice sysNotice = noticeService.getById(id);
        noticeService.revokeNotice(sysNotice);
        return Result.ok("公告撤销成功");
    }

    @PutMapping(value = "/read")
    public Result<Boolean> readNotice(@RequestBody JSONObject jsonObject){
        String id = jsonObject.getString("id");
        String userId = jsonObject.getString("userId");
        noticeService.readNotice(id, userId);
        return Result.ok();
    }
}
