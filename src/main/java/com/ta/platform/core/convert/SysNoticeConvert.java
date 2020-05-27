package com.ta.platform.core.convert;

import com.ta.platform.core.entity.SysNotice;
import com.ta.platform.core.model.SysNoticeModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Creator: zhuji
 * Date: 5/26/2020
 * Time: 6:09 PM
 * Description:
 */
@Mapper
public interface SysNoticeConvert {
    SysNoticeConvert INSTANCE = Mappers.getMapper(SysNoticeConvert.class);

    SysNotice toSysNotice(SysNoticeModel sysNoticeModel);
}
