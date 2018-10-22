package cn.yang.inme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.yang.inme.activity.AlarmMessageActivity;

/**
 * Created by Administrator on 14-7-11.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        Intent intent2 = new Intent(context, AlarmMessageActivity.class);
        intent2.putExtras(intent);
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent2);
    }

}