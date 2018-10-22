package cn.yang.inme.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 14-7-4.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inme.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("DROP TABLE IF EXISTS memo");
        db.execSQL("CREATE TABLE IF NOT EXISTS memo" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, content VARCHAR, frequency VARCHAR, month INTEGER,day INTEGER,week INTEGER,time TIME,createtime DATE,bgcolor INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS location" +
                "(userid INTEGER ,createtime DATETIME,latitude VARCHAR,longitude VARCHAR,PRIMARY KEY (userid,createtime))");
    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
    }
}
