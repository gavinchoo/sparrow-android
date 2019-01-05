package com.sparrow.bundle.framework.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by WEI on 2018/7/11.
 */

public class DateUtils {

    /**
     * 年-月-日 时:分:秒 显示格式
     */
    // 备注:如果使用大写HH标识使用24小时显示格式,如果使用小写hh就表示使用12小时制格式。
    public static String DATE_TO_STRING_DETAIAL_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 年-月-日 显示格式
     */
    public static String DATE_TO_STRING_SHORT_PATTERN = "yyyy-MM-dd";

    public static String dateToString(Date source, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(source);
    }

    /**
     * 日期加或减
     *
     * @param date
     * @param field  1、年；2、月5、天
     * @param value
     * @return 返回相加后的日期，格式为yyyy-MM-dd
     */
    public static String dateToStringAdd(String date, int field, int value) {
        Date date2 = parseDate(date);
        Calendar c = Calendar.getInstance();
        c.setTime(date2);
        c.add(field, value);
        return formatDate(c.getTime());
    }

    /**
     * @param dateStr
     * @return
     * @function：parse时间，格式yyyy-MM-dd
     * @returnType：java.util.Date
     * @author：xupeng
     * @createTime：Jul 7, 2014
     */
    public static java.util.Date parseDate(String dateStr) {
        return parseDate(dateStr, DATE_TO_STRING_SHORT_PATTERN);
    }

    /**
     * 格式化日期（字符串）
     *
     * @param dateStr 字符型日期
     * @param format  格式
     * @return 返回日期
     */
    public static Date parseDate(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param date
     * @return
     * @function：格式化日期，格式yyyy-MM-dd
     * @returnType：String
     */
    public static String formatDate(java.util.Date date) {
        return format(date, DATE_TO_STRING_SHORT_PATTERN);
    }

    /**
     * 格式化输出日期
     *
     * @param date   日期
     * @param format 格式
     * @return 返回字符型日期
     */
    public static String format(Date date, String format) {
        try {
            if (date == null) {
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } catch (Exception e) {
            return null;
        }
    }
}
