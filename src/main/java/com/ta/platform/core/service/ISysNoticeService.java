package com.ta.platform.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ta.platform.core.entity.SysNotice;
import com.ta.platform.core.model.SysNoticeModel;

/**
 * Creator: zhuji
 * Date: 4/16/2020
 * Time: 11:58 AM
 * Description:
 */
public interface ISysNoticeService extends IService<SysNotice> {
    void saveNotice(SysNoticeModel sysNoticeModel);

    void updateNotice(SysNoticeModel sysNoticeModel);

    Page<SysNotice> fetchHeaderNotice(Page<SysNotice> page, String userId, String category);

    void publishNotice(SysNotice sysNotice);

    void revokeNotice(SysNotice sysNotice);

    void readNotice(String noticeId, String userId);
}
