package com.ta.platform.core.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ey.tax.toolset.core.BeanUtil;
import com.ey.tax.toolset.core.StrUtil;
import com.ey.tax.toolset.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Creator: zhuji
 * Date: 4/23/2020
 * Time: 3:29 PM
 * Description:
 */
@Slf4j
public class SearchableQueryWrapper {
    private static final String BEGIN = "_begin";
    private static final String END = "_end";
    private static final String STAR = "*";
    private static final String COMMA = ",";
    private static final String NOT_EQUAL = "!";
    /**页面带有规则值查询，空格作为分隔符*/
    private static final String QUERY_SEPARATE_KEYWORD = " ";

    public static <T> QueryWrapper<T> buildQueryWrapper(T searchObj, Map<String, String[]> parameterMap){
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        try {
            PropertyDescriptor origDescriptors[] = BeanUtil.getPropertyDescriptors(searchObj.getClass());
            String name, type;
            for (int i = 0; i < origDescriptors.length; i++) {
                name = origDescriptors[i].getName();
                type = origDescriptors[i].getPropertyType().toString();
                if(judgedIsUselessField(name) || !PropertyUtils.isReadable(searchObj, name)){
                    continue;
                }

                // 添加 判断是否有区间值
                String endValue = null,beginValue = null;
                if (parameterMap != null && parameterMap.containsKey(name + BEGIN)) {
                    beginValue = parameterMap.get(name + BEGIN)[0].trim();
                    addQueryByRule(queryWrapper, name, type, beginValue, QueryRuleEnum.GE);
                }

                if (parameterMap != null && parameterMap.containsKey(name + END)) {
                    endValue = parameterMap.get(name + END)[0].trim();
                    addQueryByRule(queryWrapper, name, type, endValue, QueryRuleEnum.LE);
                }

                Object value = PropertyUtils.getSimpleProperty(searchObj, name);
                //根据参数值带什么关键字符串判断走什么类型的查询
                QueryRuleEnum rule = convert2Rule(value);
                value = replaceValue(rule,value);
                // add -end 添加判断为字符串时设为全模糊查询
                addEasyQuery(queryWrapper, name, rule, value);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return queryWrapper;
    }

    private static boolean judgedIsUselessField(String name) {
        return "class".equals(name) || "ids".equals(name)
                || "page".equals(name) || "rows".equals(name)
                || "sort".equals(name) || "order".equals(name);
    }

    private static void addQueryByRule(QueryWrapper<?> queryWrapper, String name, String type, String value, QueryRuleEnum rule) throws ParseException {
        if(StrUtil.isNotEmpty(value)) {
            Object temp;
            switch (type) {
                case "class java.lang.Integer":
                    temp =  Integer.parseInt(value);
                    break;
                case "class java.math.BigDecimal":
                    temp =  new BigDecimal(value);
                    break;
                case "class java.lang.Short":
                    temp =  Short.parseShort(value);
                    break;
                case "class java.lang.Long":
                    temp =  Long.parseLong(value);
                    break;
                case "class java.lang.Float":
                    temp =   Float.parseFloat(value);
                    break;
                case "class java.lang.Double":
                    temp =  Double.parseDouble(value);
                    break;
                case "class java.util.Date":
                    temp = getDateQueryByRule(value, rule);
                    break;
                default:
                    temp = value;
                    break;
            }
            addEasyQuery(queryWrapper, name, rule, temp);
        }
    }
    /**
     * 根据规则走不同的查询
     * @param queryWrapper QueryWrapper
     * @param name         字段名字
     * @param rule         查询规则
     * @param value        查询条件值
     */
    private static void addEasyQuery(QueryWrapper<?> queryWrapper, String name, QueryRuleEnum rule, Object value) {
        if (value == null || rule == null || StrUtil.isEmpty(value.toString())) {
            return;
        }
        name = StrUtil.toUnderlineCase(name);
        log.info("--查询规则-->"+name+" "+rule.getValue()+" "+value);
        switch (rule) {
            case GT:
                queryWrapper.gt(name, value);
                break;
            case GE:
                queryWrapper.ge(name, value);
                break;
            case LT:
                queryWrapper.lt(name, value);
                break;
            case LE:
                queryWrapper.le(name, value);
                break;
            case EQ:
                queryWrapper.eq(name, value);
                break;
            case NE:
                queryWrapper.ne(name, value);
                break;
            case IN:
                if(value instanceof String) {
                    queryWrapper.in(name, (Object[])value.toString().split(","));
                }else if(value instanceof String[]) {
                    queryWrapper.in(name, (Object[]) value);
                }else {
                    queryWrapper.in(name, value);
                }
                break;
            case LIKE:
                queryWrapper.like(name, value);
                break;
            case LEFT_LIKE:
                queryWrapper.likeLeft(name, value);
                break;
            case RIGHT_LIKE:
                queryWrapper.likeRight(name, value);
                break;
            default:
                log.info("--查询规则未匹配到---");
                break;
        }
    }

    /**
     * 获取日期类型的值
     * @param value
     * @param rule
     * @return
     * @throws ParseException
     */
    private static Date getDateQueryByRule(String value, QueryRuleEnum rule) throws ParseException {
        Date date = null;
        if(value.length()==10) {
            if(rule==QueryRuleEnum.GE) {
                //比较大于
                value = value + " 00:00:00";
            }else if(rule==QueryRuleEnum.LE) {
                //比较小于
                value = value + " 23:59:59";
            }
        }
        date = DateUtil.parse(value);
        return date;
    }

    /**
     * 根据所传的值 转化成对应的比较方式
     * 支持><= like in !
     * @param value
     * @return
     */
    private static QueryRuleEnum convert2Rule(Object value) {
        // 避免空数据
        if (value == null) {
            return null;
        }
        String val = (value + "").toString().trim();
        if (val.length() == 0) {
            return null;
        }
        QueryRuleEnum rule =null;

        //initQueryWrapper组装sql查询条件错误 #284-------------------
        //TODO 此处规则，只适用于 le lt ge gt
        // step 2 .>= =<
        if (rule == null && val.length() >= 3) {
            if(QUERY_SEPARATE_KEYWORD.equals(val.substring(2, 3))){
                rule = QueryRuleEnum.getByValue(val.substring(0, 2));
            }
        }
        // step 1 .> <
        if (rule == null && val.length() >= 2) {
            if(QUERY_SEPARATE_KEYWORD.equals(val.substring(1, 2))){
                rule = QueryRuleEnum.getByValue(val.substring(0, 1));
            }
        }
        //update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284---------------------

        // step 3 like
        if (rule == null && val.contains(STAR)) {
            if (val.startsWith(STAR) && val.endsWith(STAR)) {
                rule = QueryRuleEnum.LIKE;
            } else if (val.startsWith(STAR)) {
                rule = QueryRuleEnum.LEFT_LIKE;
            } else if(val.endsWith(STAR)){
                rule = QueryRuleEnum.RIGHT_LIKE;
            }
        }
        // step 4 in
        if (rule == null && val.contains(COMMA)) {
            //TODO in 查询这里应该有个bug  如果一字段本身就是多选 此时用in查询 未必能查询出来
            rule = QueryRuleEnum.IN;
        }
        // step 5 !=
        if(rule == null && val.startsWith(NOT_EQUAL)){
            rule = QueryRuleEnum.NE;
        }
        return rule != null ? rule : QueryRuleEnum.EQ;
    }

    /**
     * 替换掉关键字字符
     *
     * @param rule
     * @param value
     * @return
     */
    private static Object replaceValue(QueryRuleEnum rule, Object value) {
        if (rule == null) {
            return null;
        }
        if (! (value instanceof String)){
            return value;
        }
        String val = (value + "").toString().trim();
        if (rule == QueryRuleEnum.LIKE) {
            value = val.substring(1, val.length() - 1);
        } else if (rule == QueryRuleEnum.LEFT_LIKE || rule == QueryRuleEnum.NE) {
            value = val.substring(1);
        } else if (rule == QueryRuleEnum.RIGHT_LIKE) {
            value = val.substring(0, val.length() - 1);
        } else if (rule == QueryRuleEnum.IN) {
            value = val.split(",");
        } else if(val.startsWith(rule.getCondition()+QUERY_SEPARATE_KEYWORD)){
            value = val.replaceFirst(rule.getCondition()+QUERY_SEPARATE_KEYWORD,"").trim();
        }
        return value;
    }
}
