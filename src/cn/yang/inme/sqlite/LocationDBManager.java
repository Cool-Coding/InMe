package cn.yang.inme.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cn.yang.inme.bean.UserLocation;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PhoneUtils;
import cn.yang.inme.utils.PropertiesUtil;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 14-7-15.
 */
public class LocationDBManager {
    private DBHelper helper;
    private SQLiteDatabase db;
    private Logger log = Logger.getLogger(LocationDBManager.class);

    public LocationDBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getReadableDatabase();
    }

    /**
     * 增加位置记录
     *
     * @param userLocations 多个位置
     */
    public void add(UserLocation... userLocations) {
        db.beginTransaction();  //开始事务
        try {
            for (UserLocation userLocation : userLocations) {
                db.execSQL("INSERT INTO location VALUES(?,?,?,?)", new Object[]{userLocation.userid, userLocation.creattime, userLocation.latitude, userLocation.longitude});
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * 获取根据ID查询位置的游标
     *
     * @param ids
     * @return
     */
    private Cursor queryTheCursorById(String... ids) {
        String where = "userid in ( ";
        int len = ids.length;
        for (int i = 1; i <= len; i++) {
            where += " ?" + i + " ,";
        }
        where = where.substring(0, where.length() - 1);
        where += " ) ";

        String frequency = PropertiesUtil.instance().read(Constants.PATH_LOCATION_FREQUENCY);
        long timestamp = PhoneUtils.getTimeStamp(frequency);

        String time = new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
        where += " and createtime >= '" + time + "'";

        Cursor c = db.query("location", new String[]{"userid", "latitude", "longitude", "createtime"}, where, ids, null, null, "createtime asc");
        return c;
    }

    /**
     * 把查询结果封装成Memo
     *
     * @param c
     * @return
     */
    private List<UserLocation> query(Cursor c) {
        if (c == null) return null;
        ArrayList<UserLocation> userLocations = new ArrayList<UserLocation>();
        while (c.moveToNext()) {
            UserLocation userLocation = new UserLocation();
            userLocation.userid = c.getInt(c.getColumnIndex("userid"));
            userLocation.latitude = c.getString(c.getColumnIndex("latitude"));
            userLocation.longitude = c.getString(c.getColumnIndex("longitude"));
            userLocation.creattime = c.getString(c.getColumnIndex("createtime"));
            userLocations.add(userLocation);
        }
        c.close();
        return userLocations;
    }

    public List<UserLocation> queryByIds(Integer... ids) {
        int len = ids.length;
        if (len <= 0) return null;

        String[] ids_str = new String[len];
        for (int i = 0; i < len; i++) {
            ids_str[i] = String.valueOf(ids[i]);
        }

        Cursor c = queryTheCursorById(ids_str);
        return query(c);
    }

    /**
     * 判断当前位置是否存入数据库(与当天最后一条位置比较，如果相近，则不插入数据库)
     *
     * @param userLocation
     * @return
     */
    public boolean isExisted(UserLocation userLocation, float accuracy) {
        String where;
        where = "userid=" + userLocation.userid;

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        where += " and createtime >= '" + today + "'";

        Cursor c = db.query("location", new String[]{"userid,max(createtime) as createtime,latitude,longitude"}, where, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToNext();
            if (c.isNull(c.getColumnIndex("createtime"))) {
                Log.d("exist", "false");
                return false;
            }
            double latitude2 = Double.valueOf(c.getString(c.getColumnIndex("latitude")));
            double longitude2 = Double.valueOf(c.getString(c.getColumnIndex("longitude")));
            LatLng latLng_old = new LatLng(latitude2, longitude2);
            LatLng latLng_now = new LatLng(Double.valueOf(userLocation.latitude), Double.valueOf(userLocation.longitude));

            float distance = AMapUtils.calculateLineDistance(latLng_old, latLng_now);
            log.info("精度:" + accuracy + "-" + "距离:" + distance);

            if (distance <= accuracy) {
                Log.d("exist", "true");
                return true;
            } else {
                Log.d("exist", "false");
                return false;
            }
        } else {
            Log.d("exist", "false");
            return false;
        }
    }

    /**
     * 断开数据库连接
     */
    public void closeDB() {
        db.close();
    }
}
