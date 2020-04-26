package com.ta.platform.core.configuration.interceptor;

import com.ey.tax.toolset.core.ReflectUtil;
import com.ey.tax.toolset.core.StrUtil;
import com.ta.platform.common.api.vo.Result;
import com.ta.platform.common.clientapi.GatewayAPIClient;
import com.ta.platform.common.constant.CommonConstant;
import com.ta.platform.common.system.model.LoginUser;
import com.ta.platform.common.tool.ApplicationContextProvider;
import com.ta.platform.core.DefConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

/**
 * mybatis拦截器，自动注入创建人、创建时间、修改人、修改时间
 *
 */
@Slf4j
@Component
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class MybatisInterceptor implements Interceptor {

	@Autowired
	private GatewayAPIClient gatewayAPIClient;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
		String sqlId = mappedStatement.getId();
		log.debug("------sqlId------" + sqlId);
		SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
		Object parameter = invocation.getArgs()[1];
		log.debug("------sqlCommandType------" + sqlCommandType);

		if (parameter == null) {
			return invocation.proceed();
		}

		String loginUserName = getLoginUserName();
		if (SqlCommandType.INSERT == sqlCommandType) {
			Field[] fields = ReflectUtil.getFields(parameter.getClass());
			for (Field field : fields) {
				log.debug("------field.name------" + field.getName());
				try {
					if ("createBy".equals(field.getName())) {
						field.setAccessible(true);
						Object local_createBy = field.get(parameter);
						field.setAccessible(false);
						if (local_createBy == null || local_createBy.equals("")) {
							if (StrUtil.isNotEmpty(loginUserName)) {
								// 登录人账号
								field.setAccessible(true);
								field.set(parameter, loginUserName);
								field.setAccessible(false);
							}
						}
					}
					// 注入创建时间
					if ("createTime".equals(field.getName())) {
						field.setAccessible(true);
						Object local_createDate = field.get(parameter);
						field.setAccessible(false);
						if (local_createDate == null || local_createDate.equals("")) {
							field.setAccessible(true);
							field.set(parameter, new Date());
							field.setAccessible(false);
						}
					}
					//注入部门编码
					if ("sysOrgCode".equals(field.getName())) {
						field.setAccessible(true);
						Object local_sysOrgCode = field.get(parameter);
						field.setAccessible(false);
						if (local_sysOrgCode == null || local_sysOrgCode.equals("")) {
							// 获取登录用户信息
							if (StrUtil.isNotEmpty(loginUserName)) {
								field.setAccessible(true);
								field.set(parameter, loginUserName);
								field.setAccessible(false);
							}
						}
					}
				} catch (Exception e) {
				}
			}
		}
		if (SqlCommandType.UPDATE == sqlCommandType) {
			Field[] fields = null;
			if (parameter instanceof ParamMap) {
				ParamMap<?> p = (ParamMap<?>) parameter;
				//update-begin-author:scott date:20190729 for:批量更新报错issues/IZA3Q--
				if (p.containsKey("et")) {
					parameter = p.get("et");
				} else {
					parameter = p.get("param1");
				}
				//update-end-author:scott date:20190729 for:批量更新报错issues/IZA3Q-

				//update-begin-author:scott date:20190729 for:更新指定字段时报错 issues/#516-
				if (parameter == null) {
					return invocation.proceed();
				}
				//update-end-author:scott date:20190729 for:更新指定字段时报错 issues/#516-

				fields = ReflectUtil.getFields(parameter.getClass());
			} else {
				fields = ReflectUtil.getFields(parameter.getClass());
			}

			for (Field field : fields) {
				log.debug("------field.name------" + field.getName());
				try {
					if ("updateBy".equals(field.getName())) {
						//获取登录用户信息
						if (StrUtil.isNotEmpty(loginUserName)) {
							// 登录账号
							field.setAccessible(true);
							field.set(parameter, loginUserName);
							field.setAccessible(false);
						}
					}
					if ("updateTime".equals(field.getName())) {
						field.setAccessible(true);
						field.set(parameter, new Date());
						field.setAccessible(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
	}

	private String getLoginUserName() {
//		LoginUser sysUser = null;
//		try {
//			sysUser = SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser) SecurityUtils.getSubject().getPrincipal() : null;
//		} catch (Exception e) {
//			sysUser = null;
//		}
//		return sysUser;
		HttpServletRequest request = ApplicationContextProvider.getHttpServletRequest();
		String token = request.getHeader(DefConstants.X_ACCESS_TOKEN);
		Result<Object> result = gatewayAPIClient.getLoginUser(token);
		if(StrUtil.isNotBlank((String)result.getResult())){
			return result.getResult().toString();
		}
		return null;
	}

}
