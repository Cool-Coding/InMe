package cn.yang.inme.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import cn.yang.inme.bean.Memo;
import cn.yang.inme.bean.MsgAndPathFrequency;
import cn.yang.inme.sqlite.MemoDBManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yang on 2014/6/28.
 */
public class PhoneUtils {

    private Logger log = Logger.getLogger(PhoneUtils.class);

    private Context context;
    private String smsjson;
    private HashMap<String, List<String>> smsMap;

    private String calllogjson;
    private HashMap<String, List<String>> calllogMap;

    public HashSet<String> phoneNumbers=new HashSet<String>();

    public  PhoneUtils(Context context) {
        this.context = context;
    }

    /**
     * 返回短信各类别的条目数
     *
     * @return
     */
    public String getSmsjson() {
        return smsjson;
    }

    /**
     * 返回短信类别的详细内容,在方法getSmsInPhone之后调用
     *
     * @return
     */
    public HashMap<String, List<String>> getSmsMap() {
        return smsMap;
    }

    public void clearMessageMap() {
        if (smsMap != null) smsMap.clear();
        if (calllogMap != null) calllogMap.clear();
        smsjson = null;
        calllogjson = null;
    }

    /**
     * 返回通话记录各类型的条目数，在getCalllogInPhone()之后调用
     *
     * @return
     */
    public String getCalllogjson() {
        return calllogjson;
    }

    /**
     * 返回通话记录各类别的详细记录,在getCalllogInPhone()之后调用
     *
     * @return
     */
    public HashMap<String, List<String>> getCalllogMap() {
        return calllogMap;
    }

    /**
     * 返回通话或短信中所有电话号码
     * @return
     */
    public HashSet<String> getHistoryNumbers(){
        return phoneNumbers;
    }
    /**
     * 取得短信内容
     */
    public void getSmsInPhone() {

        try {
            ContentResolver cr = context.getContentResolver();
            String[] projection = new String[]{"address", "person",
                    "body", "date", "type", "read"};
            final Uri SMS_URI_ALL = Uri.parse("content://sms/");

            String frequency = PropertiesUtil.instance().read(Constants.MESSAGE_FREQUENCY);
            long timestamp = getTimeStamp(frequency);

            Cursor cur;
            if (timestamp != -1 && timestamp != 0) {
                String where = " date > '" + timestamp + "'";
                cur = cr.query(SMS_URI_ALL, projection, where, null, "date desc");
            } else {
                cur = cr.query(SMS_URI_ALL, projection, null, null, "date desc");
            }
            if (cur != null && cur.moveToFirst()) {
                String name;
                String phoneNumber;
                String smsbody;
                String type = "";
                String read = "";

                int nameColumn = cur.getColumnIndex("person");
                int phoneNumberColumn = cur.getColumnIndex("address");
                int smsbodyColumn = cur.getColumnIndex("body");
                int dateColumn = cur.getColumnIndex("date");
                int typeColumn = cur.getColumnIndex("type");
                int readColumn = cur.getColumnIndex("read");
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");

                smsMap = new HashMap<String, List<String>>();
                do {
                    name = cur.getString(nameColumn);
                    phoneNumber = cur.getString(phoneNumberColumn);
                    smsbody = cur.getString(smsbodyColumn);

                    Date d = new Date(Long.parseLong(cur.getString(dateColumn)));
                    String date = dateFormat.format(d);

                    int typeId = cur.getInt(typeColumn);
                    if (typeId == 1) {
                        type = "接收";
                    } else if (typeId == 2) {
                        type = "发送";
                    } else if (typeId == 3) {
                        type = "草稿";
                    }

                    List<String> list = smsMap.get(type);
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    phoneNumbers.add(phoneNumber);
                    list.add(name + "-;" + phoneNumber + "-;" + date + "-;" + smsbody);
                    smsMap.put(type, list);


                    int readId = cur.getInt(readColumn);
                    if (readId == 0) {
                        read = "未读";
                    } else if (readId == 1) {
                        read = "已读";
                    }

                    if ("未读".equals(read)) {
                        list = smsMap.get(read);
                        if (list == null) {
                            list = new ArrayList<String>();
                        }
                        phoneNumbers.add(phoneNumber);
                        list.add(name + "-;" + phoneNumber + "-;" + date + "-;" + smsbody);
                        smsMap.put(read, list);
                    }
                } while (cur.moveToNext());
            }


            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
                jsonObject.put("category", "短信:");

                //短信：接收，发送，未读必须要有
                if (smsMap.get("接收") == null) {
                    smsMap.put("接收", null);
                }
                if (smsMap.get("发送") == null) {
                    smsMap.put("发送", null);
                }
                if (smsMap.get("未读") == null) {
                    smsMap.put("未读", null);
                }
                int index = 0;

                for (Map.Entry<String, List<String>> entry : smsMap.entrySet()) {
                    List<String> list = entry.getValue();
                    JSONObject item = new JSONObject();
                    item.put("name", entry.getKey());
                    item.put("count", list == null ? 0 : list.size());
                    jsonArray.put(index++, item);
                }
                jsonObject.put("details", jsonArray);
                smsjson = jsonObject.toString();
            } catch (JSONException e) {
                log.error("JSONException in parse 短信", e.getCause());
            }

            if (cur != null) cur.close();
        } catch (SQLiteException ex) {
            log.error("SQLiteException in getSmsInPhone", ex.getCause());
        }

    }

    /**
     * 查询通话记录
     */
    public void getCallogInPhone() {
        try {
            ContentResolver cr = context.getContentResolver();
            String[] projection = new String[]{CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION};

           /*String date=getFirstDayOfThisMonth();
           date=date+" 00:00:00";

           SimpleDateFormat dateFormat = new SimpleDateFormat(
                   "yyyy-MM-dd HH:mm:ss");
           long timestamp=0;
           Date d;
           try {
               d = dateFormat.parse(date);
               timestamp=d.getTime();
           }catch(ParseException e){
               log.error("ParseException In parse date",e.getCause());
               return;
           }*/

            String frequency = PropertiesUtil.instance().read(Constants.MESSAGE_FREQUENCY);
            long timestamp = getTimeStamp(frequency);

            Cursor cur;
            if (timestamp != -1 && timestamp != 0) {
                String where = " date > '" + timestamp + "'";
                cur = cr.query(CallLog.Calls.CONTENT_URI, projection, where, null, "date desc");
            } else {
                cur = cr.query(CallLog.Calls.CONTENT_URI, projection, null, null, "date desc");
            }

            if (cur != null && cur.moveToFirst()) {
                int numberIndex = cur.getColumnIndex(CallLog.Calls.NUMBER);
                int cachednameIndex = cur.getColumnIndex(CallLog.Calls.CACHED_NAME);
                int typeIndex = cur.getColumnIndex(CallLog.Calls.TYPE);
                int dateIndex = cur.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cur.getColumnIndex(CallLog.Calls.DURATION);
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                calllogMap = new HashMap<String, List<String>>();

                do {
                    String number = cur.getString(numberIndex);
                    String cachedname = cur.getString(cachednameIndex);
                    String duration = cur.getString(durationIndex);

                    String type;
                    switch (Integer.parseInt(cur.getString(typeIndex))) {
                        case CallLog.Calls.INCOMING_TYPE:
                            type = "已接";
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            type = "拨打";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            type = "未接";
                            break;
                        default:
                            type = "挂断";
                            break;
                    }

                    Date d = new Date(Long.parseLong(cur.getString(dateIndex)));
                    String date = dateFormat.format(d);

                    List<String> list = calllogMap.get(type);
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    phoneNumbers.add(number);
                    list.add(cachedname + "-;" + number + "-;" + date + "-;" + duration);
                    calllogMap.put(type, list);
                } while (cur.moveToNext());
            }

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
                jsonObject.put("category", "通话:");

                //通话：已接，未接，拨打三部分必须有
                if (calllogMap.get("已接") == null) {
                    calllogMap.put("已接", null);
                }
                if (calllogMap.get("未接") == null) {
                    calllogMap.put("未接", null);
                }
                if (calllogMap.get("拨打") == null) {
                    calllogMap.put("拨打", null);
                }
                int index = 0;
                for (Map.Entry<String, List<String>> entry : calllogMap.entrySet()) {
                    List<String> list = entry.getValue();
                    JSONObject item = new JSONObject();
                    item.put("name", entry.getKey());
                    item.put("count", list == null ? 0 : list.size());
                    jsonArray.put(index++, item);
                }
                jsonObject.put("details", jsonArray);

                calllogjson = jsonObject.toString();
            } catch (JSONException e) {
                log.error("JSONException in parse 通话", e.getCause());
            }

            if (cur != null) cur.close();
        } catch (SQLiteException ex) {
            log.error("SQLiteException in getCalllogInPhone", ex.getCause());
        }
    }

    /**
     * 查询今日待办
     * @return
     */
    public JSONArray getTodayMemo() {
        MemoDBManager db = new MemoDBManager(context);
        List<Memo> memos = db.queryTodayMemosCursor();
        db.closeDB();
        JSONArray jsonArray = new JSONArray();
        for (Memo memo : memos) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("_id", memo._id);
                jsonObject.put("action", memo.content);
                if (memo.time == null || "null".equals(memo.time) || "".equals(memo.time)) {
                    memo.time = "";
                }
                jsonObject.put("time", memo.time);
                jsonObject.put("color", memo.backgroundcolor);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {

            }
        }
        return jsonArray;
    }


    /**
     * 通过联系人ID查询姓名
     *
     * @param context View环境
     * @param ids     ID数组
     * @return
     */
    public static HashMap<String, String> queryNameById(Context context, String[] ids) {
        int len = ids.length;
        String where = ContactsContract.Contacts._ID + " in ( ";
        for (int i = 1; i <= len; i++) {
            where += " ?" + i + " ,";
        }
        where = where.substring(0, where.length() - 1);
        where += " )";

        Cursor cursorOriginal =
                context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME},
                        where, ids, null);

        if (null != cursorOriginal) {
            int idIndex = cursorOriginal.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIndex = cursorOriginal.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            HashMap<String, String> id_name = new HashMap<String, String>();

            if (cursorOriginal.moveToFirst()) {
                do {
                    String idstr = cursorOriginal.getString(idIndex);
                    String name = cursorOriginal.getString(nameIndex);
                    id_name.put(idstr, name);
                }
                while (cursorOriginal.moveToNext());
                cursorOriginal.close();
                return id_name;
            }
        } else {
            cursorOriginal.close();
            return null;
        }

        return null;
    }

    /**
     * 通过电话号码，查询联系人姓名
     *
     * @param context
     * @param num
     * @return
     */
    public static HashMap<String, String> queryNameByNum(Context context, String... num) {

        //对电话号码进行处理(空格和前置+86)
        ArrayList<String> appendNumber = new ArrayList<String>();
        for (String n : num) {
            appendNumber.add(n);
            int index = n.indexOf(" ");
            if (index != -1) {
                String number_2 = n.replaceAll(" ", "");
                appendNumber.add(number_2);
                if (number_2.startsWith("+86")) {
                    appendNumber.add(number_2.substring(3));
                } else if (number_2.length() == 11) {
                    appendNumber.add("+86" + number_2);
                }
            }
            if (n.startsWith("+86")) {
                appendNumber.add(n.substring(3));
            } else if (n.length() == 11) {
                appendNumber.add("+86" + n);
            }
        }

        num = appendNumber.toArray(new String[0]);
        int len = num.length;

        String where;
        if (len > 900) {

            int start = 1;
            int end = 0;
            HashMap<String, String> num_name = new HashMap<String, String>();
            do {
                end = start + 899;
                if (end > len) {
                    end = len;
                }

                String[] numbers = new String[end - start + 1];

                where = ContactsContract.CommonDataKinds.Phone.NUMBER + " in ( ";
                for (int i = start; i <= end; i++) {
                    where += " ?" + (i - start + 1) + " ,";
                    numbers[i - start] = num[i - start];
                }
                if (where.indexOf("?") == -1) break;

                where = where.substring(0, where.length() - 1);
                where += " ) ";
                HashMap<String, String> sub_num_name = findNamebyNum(context, where, numbers);
                if (sub_num_name != null) {
                    num_name.putAll(sub_num_name);
                }

                start = end + 1;
            } while (1 == 1);

            return num_name;

        } else {
            where = ContactsContract.CommonDataKinds.Phone.NUMBER + " in ( ";
            for (int i = 1; i <= len; i++) {
                where += " ?" + i + " ,";
            }
            where = where.substring(0, where.length() - 1);
            where += " ) ";

            return findNamebyNum(context, where, num);
        }

    }


    private static HashMap<String, String> findNamebyNum(Context context, String where, String[] args) {
        Cursor cursorOriginal =
                context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                        where, args, null);
        if (null != cursorOriginal) {
            int numberIndex = cursorOriginal.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursorOriginal.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            HashMap<String, String> num_name = new HashMap<String, String>();

            if (cursorOriginal.moveToFirst()) {
                do {
                    String number = cursorOriginal.getString(numberIndex);
                    String name = cursorOriginal.getString(nameIndex);
                    num_name.put(number, name);

                }
                while (cursorOriginal.moveToNext());

                cursorOriginal.close();
                return num_name;
            }
        } else {
            return null;
        }
        return null;
    }

    /**
     * 得到周一
     *
     * @return
     */
    public static String getMondayOfThisWeek() {
        Calendar c = Calendar.getInstance();
        int dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayofweek == 0)
            dayofweek = 7;
        c.add(Calendar.DATE, -dayofweek + 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(c.getTime());
    }

    /**
     * 得到当月第一天
     *
     * @return
     */

    public static String getFirstDayOfThisMonth() {
        //获取当月的第一天
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal_1 = Calendar.getInstance();//获取当前日期
        cal_1.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        return format.format(cal_1.getTime());
    }

    /**
     * 根据频率得到时间戳
     *
     * @param frequency
     * @return
     */
    public static long getTimeStamp(String frequency) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        MsgAndPathFrequency frequency_obj = MsgAndPathFrequency.valueOf(frequency);
        if (frequency_obj == MsgAndPathFrequency.TODAY) {
            return c.getTimeInMillis();
        } else if (frequency_obj == MsgAndPathFrequency.WEEK) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            return c.getTimeInMillis();
        } else if (frequency_obj == MsgAndPathFrequency.MONTH) {
            c.set(Calendar.DAY_OF_MONTH, 1);
            return c.getTimeInMillis();
        } else if (frequency_obj == MsgAndPathFrequency.YEAR) {
            c.set(Calendar.DAY_OF_YEAR, 1);
            return c.getTimeInMillis();
        } else if (frequency_obj == MsgAndPathFrequency.ALL) {
            return 0;
        }
        return -1;
    }
}

