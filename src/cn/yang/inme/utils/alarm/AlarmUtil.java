package cn.yang.inme.utils.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import cn.yang.inme.bean.Memo;
import cn.yang.inme.bean.MemoFrequency;
import cn.yang.inme.receiver.AlarmReceiver;
import cn.yang.inme.receiver.LocateUser;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 14-7-11.
 */
public class AlarmUtil {

    private static Logger log = Logger.getLogger(AlarmUtil.class);

   /* **
            *闹钟三种设置模式（dateMode）：
            *1、DATE_MODE_FIX：指定日期，如20120301,参数dateValue格式：2012-03-01
            *2、DATE_MODE_WEEK：按星期提醒，如星期一、星期三,参数dateValue格式：1,3
            *3、DATE_MODE_MONTH：按月提醒，如3月2、3号，4月2、3号,参数dateValue格式：3,4|2,3
            *
            *startTime:为当天开始时间，如上午9点,参数格式为09:00
            */

    public static long getNextAlarmTime(Task dateMode, String dateValue,
                                        String startTime) {
        final SimpleDateFormat fmt = new SimpleDateFormat();
        final Calendar c = Calendar.getInstance();
        final long now = System.currentTimeMillis();

//        c.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 设置开始时间
        try {
            if (Task.DATE_MODE_FIX == dateMode) {
                fmt.applyPattern("yyyy-MM-dd");
                Date d = fmt.parse(dateValue);
                c.setTimeInMillis(d.getTime());
            }

            fmt.applyPattern("HH:mm");
            Date d = fmt.parse(startTime);
            c.set(Calendar.HOUR_OF_DAY, d.getHours());
            c.set(Calendar.MINUTE, d.getMinutes());
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long nextTime = 0;
        if (Task.DATE_MODE_FIX == dateMode) { // 按指定日期
            nextTime = c.getTimeInMillis();
            // 指定日期已过
            if (now >= nextTime) nextTime = 0;
        } else if (Task.DATE_MODE_DAY == dateMode) {
            long triggerAtTime = c.getTimeInMillis();
            if (triggerAtTime < now) {//每天
                triggerAtTime += AlarmManager.INTERVAL_DAY;
            }
            // 保存最近闹钟时间
            if (0 == nextTime) {
                nextTime = triggerAtTime;
            } else {
                nextTime = Math.min(triggerAtTime, nextTime);
            }
        } else if (Task.DATE_MODE_WEEK == dateMode) { // 按周
            final long[] checkedWeeks = parseDateWeeks(dateValue);
            if (null != checkedWeeks) {
                for (long week : checkedWeeks) {
                    week = week + 1;
                    if (week == 8) week = 1;
                    c.set(Calendar.DAY_OF_WEEK, (int) week);

                    long triggerAtTime = c.getTimeInMillis();
                    if (triggerAtTime <= now) { // 下周
                        triggerAtTime += AlarmManager.INTERVAL_DAY * 7;
                    }
                    // 保存最近闹钟时间
                    if (0 == nextTime) {
                        nextTime = triggerAtTime;
                    } else {
                        nextTime = Math.min(triggerAtTime, nextTime);
                    }
                }
            }
        } else if (Task.DATE_MODE_MONTH == dateMode) {
            final long[] days = parseDateWeeks(dateValue);
            if (null != days) {
                boolean isAdd = false;
                for (long day : days) {
                    c.set(Calendar.DAY_OF_MONTH, (int) day);

                    long triggerAtTime = c.getTimeInMillis();
                    if (triggerAtTime <= now) { // 下各月
                        c.add(Calendar.MONTH, 1);
                        triggerAtTime = c.getTimeInMillis();
                        isAdd = true;
                    } else {
                        isAdd = false;
                    }

                    if (isAdd) {
                        c.add(Calendar.MONTH, -1);
                    }

                    // 保存最近闹钟时间
                    if (0 == nextTime) {
                        nextTime = triggerAtTime;
                    } else {
                        nextTime = Math.min(triggerAtTime, nextTime);
                    }
                }
            }
        } else if (Task.DATE_MODE_YEAR == dateMode) { // 按年
            final long[][] items = parseDateMonthsAndDays(dateValue);
            final long[] checkedMonths = items[0];
            final long[] checkedDays = items[1];

            if (null != checkedDays && null != checkedMonths) {
                boolean isAdd = false;
                for (long month : checkedMonths) {
                    c.set(Calendar.MONTH, (int) (month - 1));
                    for (long day : checkedDays) {
                        c.set(Calendar.DAY_OF_MONTH, (int) day);

                        long triggerAtTime = c.getTimeInMillis();
                        if (triggerAtTime <= now) { // 下一年
                            c.add(Calendar.YEAR, 1);
                            triggerAtTime = c.getTimeInMillis();
                            isAdd = true;
                        } else {
                            isAdd = false;
                        }
                        if (isAdd) {
                            c.add(Calendar.YEAR, -1);
                        }
                        // 保存最近闹钟时间
                        if (0 == nextTime) {
                            nextTime = triggerAtTime;
                        } else {
                            nextTime = Math.min(triggerAtTime, nextTime);
                        }
                    }
                }
            }
        }
        return nextTime;
    }

    private static long[] parseDateWeeks(String value) {
        long[] weeks = null;
        try {
            final String[] items = value.split(",");
            weeks = new long[items.length];
            int i = 0;
            for (String s : items) {
                weeks[i++] = Long.valueOf(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weeks;
    }

    private static long[][] parseDateMonthsAndDays(String value) {
        long[][] values = new long[2][];
        try {
            final String[] items = value.split("\\|");
            final String[] monthStrs = items[0].split(",");
            final String[] dayStrs = items[1].split(",");
            values[0] = new long[monthStrs.length];
            values[1] = new long[dayStrs.length];

            int i = 0;
            for (String s : monthStrs) {
                values[0][i++] = Long.valueOf(s);
            }
            i = 0;
            for (String s : dayStrs) {
                values[1][i++] = Long.valueOf(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 设置闹钟
     *
     * @param context 环境
     * @param memo    备忘录
     */
    public static void setAlarm(Context context, Memo memo) {
        if (memo.frequency == null) return;
        if (memo.content == null || "".equals(memo.content)) return;


        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("memoid", memo._id);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, memo._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        if (memo.frequency == MemoFrequency.ONCE) {
            long nextTime = getNextAlarmTime(Task.DATE_MODE_FIX, memo.createTime, memo.time);
            am.set(AlarmManager.RTC_WAKEUP, nextTime, pIntent);
        } else if (memo.frequency == MemoFrequency.EVERYDAY) {
            long nextTime = getNextAlarmTime(Task.DATE_MODE_DAY, null, memo.time);
            am.setRepeating(AlarmManager.RTC_WAKEUP, nextTime, AlarmManager.INTERVAL_DAY, pIntent);
        } else if (memo.frequency == MemoFrequency.PERWEEK) {
            long nextTime = getNextAlarmTime(Task.DATE_MODE_WEEK, memo.week.toString(), memo.time);
            am.setRepeating(AlarmManager.RTC_WAKEUP, nextTime, AlarmManager.INTERVAL_DAY * 7, pIntent);
        } else if (memo.frequency == MemoFrequency.PERMONTH) {
            long nextTime = getNextAlarmTime(Task.DATE_MODE_MONTH, memo.day.toString(), memo.time);
            am.set(AlarmManager.RTC_WAKEUP, nextTime, pIntent);//每月只能设置单次提醒，下次还需等下次再提醒
        } else if (memo.frequency == MemoFrequency.PERYEAR) {
            long nextTime = getNextAlarmTime(Task.DATE_MODE_YEAR, memo.month.toString() + "|" + memo.day.toString(), memo.time);
            am.set(AlarmManager.RTC_WAKEUP, nextTime, pIntent);
        }
    }

    public static void delayAlarm(Context context, Memo memo, long time) {
        if (memo.frequency == null) return;
        if (memo.content == null || "".equals(memo.content)) return;


        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("memoid", memo._id);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, memo._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, time, pIntent);
    }

    /**
     * 取消闹钟
     *
     * @param context 环境
     * @param memo    备忘录
     */
    public static void cancelAlarm(Context context, Memo memo) {
        if (memo.frequency == null) return;
        if (memo.content == null || "".equals(memo.content)) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("memoid", memo._id);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, memo._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pIntent);
    }

    /**
     * 开始定位
     *
     * @param context
     */
    public static void startLocateUser(Context context) {
        //读取时间间隔
        try {
            Intent intent = new Intent(context, LocateUser.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            //每次启动InMe应用程序，都先取消系统中有的定时任务，开始新的任务
            AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            am.cancel(pIntent);
            long interval = Long.valueOf(PropertiesUtil.instance().read(Constants.PATH_LOCATION_INTERVAL));
            am.setRepeating(AlarmManager.RTC_WAKEUP, 0, interval * 1000, pIntent);
        } catch (NumberFormatException e) {
            log.error("读取路径记录时间间隔", e.getCause());
        }

    }

    /**
     * 停止定位
     *
     * @param context
     */
    public static void stopLocateUser(Context context) {
        Intent intent = new Intent(context, LocateUser.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pIntent);
    }
}
