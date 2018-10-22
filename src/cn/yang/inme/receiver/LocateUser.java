package cn.yang.inme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.yang.inme.bean.UserLocation;
import cn.yang.inme.sqlite.LocationDBManager;
import cn.yang.inme.utils.network.LocationSource;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 14-7-15.
 */
public class LocateUser extends BroadcastReceiver {

    private Logger log = Logger.getLogger(LocateUser.class);

    public void onReceive(final Context context, Intent intent) {
        log.info("LocateUser被触发");

        final LocationSource locate = new LocationSource(context);
        locate.locate();
        if (locate != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (locate.getResult() == ' ') ;
                    if (locate.getResult() == 'A') {
                        return;
                    }
                    UserLocation userLocation = new UserLocation();
                    userLocation.userid = 1;
                    userLocation.latitude = locate.getLatitude();
                    userLocation.longitude = locate.getLongitude();
                    Log.d("LocateUser:", userLocation.latitude + ":" + userLocation.longitude);
                    if (userLocation.latitude != null) {
                        LocationDBManager db = new LocationDBManager(context);
                        if (!db.isExisted(userLocation, locate.getAccuracy())) {
                            db.add(userLocation);
                        }
                        db.closeDB();
                    }
                }
            }).start();
        }
    }
}