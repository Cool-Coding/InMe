package cn.yang.inme.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.yang.inme.bean.Memo;
import cn.yang.inme.bean.MemoFrequency;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 14-7-4.
 * 操作备忘录的数据库类
 */
public class MemoDBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public MemoDBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getReadableDatabase();
    }

    /**
     * 增加备忘录
     *
     * @param memos 多个备忘录
     */
    public void add(Memo... memos) {
        db.beginTransaction();  //开始事务
        try {
            for (Memo memo : memos) {
                db.execSQL("INSERT INTO memo VALUES(null,?,?,?,?,?,?,?,?)", new Object[]{memo.content, memo.frequency, memo.month, memo.day, memo.week, memo.time, memo.createTime, memo.backgroundcolor});
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public long add(Memo memo) {
        db.beginTransaction();  //开始事务
        long id = -1;
        try {
            ContentValues cv = new ContentValues();
            cv.put("content", memo.content);
            MemoFrequency frequency = memo.frequency;
            cv.put("frequency", frequency == null ? null : frequency.toString());
            cv.put("month", memo.month);
            cv.put("day", memo.day);
            cv.put("week", memo.week);
            cv.put("time", memo.time);
            cv.put("createtime", memo.createTime);
            cv.put("bgcolor", memo.backgroundcolor);
            id = db.insert("memo", "_id", cv);
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
        return id;
    }

    /**
     * 更新备忘录的内容
     *
     * @param memos 多个备忘录
     */
    public void updateContent(Memo... memos) {
        for (Memo memo : memos) {
            ContentValues cv = new ContentValues();
            cv.put("content", memo.content);
            MemoFrequency frequency = memo.frequency;
            cv.put("frequency", frequency == null ? null : frequency.toString());
            cv.put("month", memo.month);
            cv.put("day", memo.day);
            cv.put("week", memo.week);
            cv.put("time", memo.time);
            cv.put("createtime", memo.createTime);
            cv.put("bgcolor", memo.backgroundcolor);
            db.update("memo", cv, "_id = ?", new String[]{String.valueOf(memo._id)});
        }
    }

    /**
     * 删除备忘录
     *
     * @param memos 多个备忘录
     */
    public void delete(Memo... memos) {
        for (Memo memo : memos) {
            db.delete("memo", "_id = ?", new String[]{String.valueOf(memo._id)});
        }
    }

    /**
     * 获取查询所有备忘录的游标
     *
     * @return Cursor
     */
    private Cursor queryTheCursorAll() {
        Cursor c = db.rawQuery("SELECT * FROM memo", null);
        return c;
    }


    /**
     * 获取根据ID查询备忘录的游标
     *
     * @param ids
     * @return
     */
    private Cursor queryTheCursorById(String... ids) {
        String where = "_id in ( ";
        int len = ids.length;
        for (int i = 1; i <= len; i++) {
            where += " ?" + i + " ,";
        }
        where = where.substring(0, where.length() - 1);
        where += " ) ";

        Cursor c = db.query("memo", new String[]{"_id", "content", "frequency", "month", "day", "week", "time", "createtime", "bgcolor"}, where, ids, null, null, null);
        return c;
    }

    /**
     * 把查询结果封装成Memo
     *
     * @param c
     * @return
     */
    private List<Memo> query(Cursor c) {
        if (c == null) return null;
        ArrayList<Memo> memos = new ArrayList<Memo>();
        while (c.moveToNext()) {
            Memo memo = new Memo();
            memo._id = c.getInt(c.getColumnIndex("_id"));
            memo.content = c.getString(c.getColumnIndex("content"));
            String frequency = c.getString(c.getColumnIndex("frequency"));
            memo.frequency = frequency == null || "".equals(frequency) || "null".equals(frequency) ? null : Enum.valueOf(MemoFrequency.class, frequency);
            String col = c.getString(c.getColumnIndex("month"));
            memo.month = col == null || "null".equals(col) || "".equals(col) ? null : Integer.valueOf(col);
            col = c.getString(c.getColumnIndex("day"));
            memo.day = col == null || "null".equals(col) || "".equals(col) ? null : Integer.valueOf(col);
            col = c.getString(c.getColumnIndex("week"));
            memo.week = col == null || "null".equals(col) || "".equals(col) ? null : Integer.valueOf(col);
            memo.time = c.getString(c.getColumnIndex("time"));
            memo.createTime = c.getString(c.getColumnIndex("createtime"));
            memo.backgroundcolor = c.getInt(c.getColumnIndex("bgcolor"));
            memos.add(memo);
        }
        c.close();
        return memos;
    }

    /**
     * 查询所有的备忘录
     *
     * @return List<Memo>
     */
    public List<Memo> queryAll() {
        Cursor c = queryTheCursorAll();
        return query(c);
    }

    public List<Memo> queryTodayMemosCursor() {
        Calendar calendar = Calendar.getInstance();
        String createtime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (week == 0) week = 7;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;

        //判断是否应该加入到今日待办中
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, month - 1);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Cursor c = null;
        if (day != 31 && day == maxDays) {
            switch (day) {
                case 28:
                    c = db.rawQuery("select * from memo where (frequency is null and createtime = '" + createtime + "') or frequency = 'EVERYDAY' or (frequency='ONCE' and createtime='" + createtime + "') or (frequency='PERWEEK' and week=" + week + ") or (frequency='PERMONTH' and day in ( 28,29,30,31 ) ) or (frequency='PERYEAR' and month=" + month + " and day in (28,29 ) )", null);
                    break;
                case 29:
                    c = db.rawQuery("select * from memo where (frequency is null and createtime = '" + createtime + "') or frequency = 'EVERYDAY' or (frequency='ONCE' and createtime='" + createtime + "') or (frequency='PERWEEK' and week=" + week + ") or (frequency='PERMONTH' and day in (29,30,31) ) or (frequency='PERYEAR' and month=" + month + " and day=" + day + ")", null);
                    break;
                case 30:
                    c = db.rawQuery("select * from memo where (frequency is null and createtime = '" + createtime + "') or frequency = 'EVERYDAY' or (frequency='ONCE' and createtime='" + createtime + "') or (frequency='PERWEEK' and week=" + week + ") or (frequency='PERMONTH' and day in (30,31)) or (frequency='PERYEAR' and month=" + month + " and day=" + day + ")", null);
                    break;
            }
        } else
            c = db.rawQuery("select * from memo where (frequency is null and createtime = '" + createtime + "') or frequency = 'EVERYDAY' or (frequency='ONCE' and createtime='" + createtime + "') or (frequency='PERWEEK' and week=" + week + ") or (frequency='PERMONTH' and day=" + day + ") or (frequency='PERYEAR' and month=" + month + " and day=" + day + ")", null);
        return query(c);
    }

    /**
     * 根据Id查询备忘录
     *
     * @param id
     * @return
     */
    public Memo queryMemo(String id) {
        List<Memo> memos = query(queryTheCursorById(id));
        if (memos.size() == 0) return null;
        else return memos.get(0);
    }

    /**
     * 断开数据库连接
     */
    public void closeDB() {
        db.close();
    }

}
