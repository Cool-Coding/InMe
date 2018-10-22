package cn.yang.inme.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.yang.inme.Inme;
import cn.yang.inme.R;
import cn.yang.inme.bean.Memo;
import cn.yang.inme.bean.MemoFrequency;
import cn.yang.inme.sqlite.MemoDBManager;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;
import cn.yang.inme.utils.ToastUtil;
import cn.yang.inme.utils.alarm.AlarmUtil;
import cn.yang.inme.utils.border.BorderLinearLayout;
import cn.yang.inme.view.MyFloatTextView;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by Administrator on 14-7-11.
 */
public class AlarmMessageActivity extends Activity {

    private WindowManager wm = null;
    private WindowManager.LayoutParams wmParams = null;
    private MyFloatTextView myFV = null;
    private Logger log = Logger.getLogger(AlarmMessageActivity.class);
    private MediaPlayer mp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        requestWindowFeature(Window.FEATURE_NO_TITLE);*/
        setContentView(R.layout.alarmmessage);
        setHeaderColor();

        //接收数据
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String _id = String.valueOf(bundle.getInt("memoid"));
        Memo memo = null;

        if (_id != null) {
            MemoDBManager db = new MemoDBManager(AlarmMessageActivity.this);
            memo = db.queryMemo(_id);
            db.closeDB();
        }

        if (memo == null) {
//            ToastUtil.show(this,"出问题啦，请稍后重试!");
            log.error("闹铃(id:" + _id + ")没取到待办项");
            finish();
            return;
        }


        TextView textView = (TextView) findViewById(R.id.content);//提醒的文字
        Button sure = (Button) findViewById(R.id.sure);//确认按钮
        Button delay = (Button) findViewById(R.id.delay);//延迟按钮

        int color = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        //整个提醒对话框
        BorderLinearLayout dialog = (BorderLinearLayout) textView.getParent();
        dialog.setBorders(color, 1, 0);
        dialog.invalidate();

        //确认区域
        BorderLinearLayout button_layout = (BorderLinearLayout) sure.getParent().getParent();
        button_layout.setTopBorder(Color.LTGRAY, 1);
        button_layout.invalidate();

        //迟延区域
        BorderLinearLayout delay_layout = (BorderLinearLayout) delay.getParent();
        delay_layout.setLeftBorder(Color.LTGRAY, 1);
        delay_layout.setTopBorder(Color.LTGRAY, 1);
        delay_layout.invalidate();


        final String content = memo.content;
        //设置消息内容
        textView.setText(content);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView.setTextColor(Color.BLACK);

        //设置"知道了"按钮颜色及事件
        //sure.setTextColor(color);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) mp.stop();
                AlarmMessageActivity.this.finish();
            }
        });

        //设置延迟按钮颜色及事件
        //delay.setTextColor(color);
        delay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Memo memo = new Memo();
                memo.content = content;
                memo.frequency = MemoFrequency.ONCE;

                long time = System.currentTimeMillis() + 10 * 60 * 1000;

                AlarmUtil.delayAlarm(AlarmMessageActivity.this, memo, time);
                ToastUtil.showShort(AlarmMessageActivity.this, "设置成功");
                if (mp != null) mp.stop();
                AlarmMessageActivity.this.finish();
            }
        });

        //播放系统默认闹铃
        mp = new MediaPlayer();
        try {
            mp.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            log.error("闹铃播放失败!");
        }
        //根据不同的频率设置下次闹钟时间
        if (memo.frequency == MemoFrequency.PERMONTH || memo.frequency == MemoFrequency.PERYEAR) {
            AlarmUtil.setAlarm(this, memo);
        }
    }

    private void createView() {
        myFV = new MyFloatTextView(getApplicationContext());
        //获取WindowManager
        wm = getWindowManager();
        //设置LayoutParams(全局变量）相关参数
        wmParams = ((Inme) getApplication()).getMywmParams();

        /**
         *以下都是WindowManager.LayoutParams的相关属性
         * 具体用途可参考SDK文档
         */
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;   //设置window type
//        wmParams.format = PixelFormat.RGBA_8888;   //设置图片格式，效果为背景透明

        //设置Window flag
/*        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                         | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;*/
                    /*
             * 下面的flags属性的效果形同“锁定”。
	         * 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
	         wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL
	                               | LayoutParams.FLAG_NOT_FOCUSABLE
	                               | LayoutParams.FLAG_NOT_TOUCHABLE;
	        */

        wmParams.gravity = Gravity.LEFT | Gravity.TOP;   //调整悬浮窗口至左上角，便于调整坐标
        //以屏幕左上角为原点，设置x、y初始值
   /*     wmParams.x = 0;
        wmParams.y = 0;*/

        //设置悬浮窗口长宽数据
        Display dis = wm.getDefaultDisplay();
        wmParams.width = (int) (dis.getWidth() * 0.8);
        wmParams.height = (int) (dis.getHeight() * 0.5);

        //显示myFloatView图像
        wm.addView(myFV, wmParams);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在程序退出(Activity销毁）时销毁悬浮窗口
//        wm.removeView(myFV);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            wm.removeView(myFV);
//            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setHeaderColor() {
        LinearLayout header = (LinearLayout) findViewById(R.id.alertTitle);

        int startcolor = Color.parseColor("#EBEBEB");
        int endcolor = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{startcolor, endcolor});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setGradientRadius(1);
        header.setBackgroundDrawable(gradientDrawable);
    }
}