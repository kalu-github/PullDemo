package lib.kalu.pull.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PullUtil {

    // 标准日期时间格式
    /**
     * yyyy-MM
     */
    public static final String FORMAT_MONTH = "yyyy-MM";
    /**
     * yyyy
     */
    public static final String FORMAT_DATE_YEAR = "yyyy";
    /**
     * yyyy-MM-dd
     */
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    /**
     * yyyy-MM-dd HH:mm
     */
    public static final String FORMAT_DATE_MIN = "yyyy-MM-dd HH:mm";
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyy-MM-dd E
     */
    public static final String FORMAT_DATE_WEEK = "yyyy年MM月dd日   E";

    /**
     * 将标准时间转成时间格式
     *
     * @param date 标准时间
     * @return 时间格式 yyyy-MM-dd HH:mm:ss
     */
    public static String format(Date date) {
        return format(date, FORMAT_DATE_TIME);
    }

    /**
     * 按指定格式格式化时期时间
     *
     * @param date   date
     * @param format format
     * @return string.
     */
    public static String format(Date date, String format) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(format);
            return df.format(date);
        } else {
            return "";
        }
    }

    /**
     * 将时间的字符串格式转成Date
     *
     * @param str str
     * @return Date
     */
    public static Date parse(String str) {
        return parse(str, FORMAT_DATE_TIME);
    }

    /**
     * 按指定格式解析字符串，将字符串转为日期时间格式
     *
     * @param str    string
     * @param format format
     * @return date
     */
    public static Date parse(String str, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            return df.parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转化成容易阅读的日期格式（简化版）：
     * 去年以前 ----> 2016-09-10
     * 今年 ----> 09-10
     * 昨天 ----> 昨天
     * 今天 ----> 14:23
     */
    public static String toDisplaySimpleDatetime(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.set(Calendar.HOUR_OF_DAY, 0);
        temp.set(Calendar.MINUTE, 0);
        temp.set(Calendar.SECOND, 0);
        temp.set(Calendar.MILLISECOND, 0);

        temp.add(Calendar.DATE, 1);

        String formatStr;
        if (date.getTime() >= temp.getTimeInMillis()) {
            // 明天或以后...
            formatStr = "yy-MM-dd";
        } else {
            temp.add(Calendar.DATE, -1);
            if (date.getTime() >= temp.getTimeInMillis()) {
                // 今天
                formatStr = "HH:mm";
            } else {
                temp.add(Calendar.DATE, -1);
                if (date.getTime() >= temp.getTimeInMillis()) {
                    // 昨天
                    formatStr = "昨天";
                } else {
                    temp.set(Calendar.DAY_OF_YEAR, 1);
                    if (date.getTime() >= temp.getTimeInMillis()) {
                        // 今年
                        formatStr = "M-d";
                    } else {
                        // 很久以前...
                        formatStr = "yy-M-d";
                    }
                }
            }
        }

        return format(date, formatStr);
    }

    /**
     * 时间戳格式化 21:15:21
     */
    public static String foamatTimestamp(long timestamp) {
        String ms, ss;

        long m, s;
        m = timestamp / 60;
        s = timestamp % 60;

        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }

        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }

        return ms + ":" + ss;
    }

    /**
     * 转化成容易阅读的日期格式（标准版）：
     * 去年以前 ----> 16-09-10 14:23
     * 今年 ----> 09-10 14:23
     * 昨天 ----> 昨天 14:23
     * 今天 ----> 14:23
     */
    public static String foamatTimestampDisplay(long timestamp) {

        Date date = new Date(timestamp);

        Calendar temp = Calendar.getInstance();
        temp.set(Calendar.HOUR_OF_DAY, 0);
        temp.set(Calendar.MINUTE, 0);
        temp.set(Calendar.SECOND, 0);
        temp.set(Calendar.MILLISECOND, 0);

        temp.add(Calendar.DATE, 1);

        String formatStr;
        if (date.getTime() >= temp.getTimeInMillis()) {
            // 明天或以后...
            formatStr = "yy-MM-dd HH:mm";
        } else {
            temp.add(Calendar.DATE, -1);
            if (date.getTime() >= temp.getTimeInMillis()) {
                // 今天
                formatStr = "今天 HH:mm";
            } else {
                temp.add(Calendar.DATE, -1);
                if (date.getTime() >= temp.getTimeInMillis()) {
                    // 昨天
                    formatStr = "昨天 HH:mm";
                } else {
                    temp.set(Calendar.DAY_OF_YEAR, 1);
                    if (date.getTime() >= temp.getTimeInMillis()) {
                        // 今年
                        formatStr = "MM-dd HH:mm";
                    } else {
                        // 很久以前...
                        formatStr = "yy-MM-dd HH:mm";
                    }
                }
            }
        }

        return format(date, formatStr);
    }

    public static String getHH() {

        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String str = df.format(date);
        int a = Integer.parseInt(str);
        if (a >= 0 && a <= 6) {
            return "凌晨";
        } else if (a > 6 && a <= 12) {
            return "上午";
        } else if (a > 12 && a <= 13) {
            return "中午";
        } else if (a > 13 && a <= 18) {
            return "下午";
        } else {
            return "晚上";
        }
//        if (a > 18 && a <= 24) {
//            view.setText(view, R.id.fingerprint_date, "晚上好");
//        }

    }
}
