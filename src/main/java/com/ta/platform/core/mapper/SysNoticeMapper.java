package com.ta.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ta.platform.core.entity.SysNotice;
import com.ta.platform.core.model.SysNoticeModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creator: zhuji
 * Date: 4/15/2020
 * Time: 11:25 AM
 * Description:
 */
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
    List<SysNotice> queryUnreadNoticeListByCategory(Page<SysNotice> page, @Param("userId") String userId, @Param("category") String category);

    List<SysNotice> querySysNoticeListByUserId(Page<SysNotice> page, @Param("noticeModel") SysNoticeModel sysNoticeModel);
}
