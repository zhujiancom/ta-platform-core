package com.ta.platform.core.convert;

import com.ta.platform.common.module.entity.SysLog;
import com.ta.platform.common.system.model.SysLogModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Creator: zhuji
 * Date: 5/20/2020
 * Time: 6:26 PM
 * Description:
 */
@Mapper
public interface SysLogConvert {
    SysLogConvert INSTANCE = Mappers.getMapper(SysLogConvert.class);

    SysLog sysLogModelToSysLogEntity(SysLogModel sysLogModel);
}
