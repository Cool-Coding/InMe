package cn.yang.inme.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.yang.inme.R;

/**
 * Created by Administrator on 14-10-17.
 */
public class InMeProgressDialog {

    private  ImageView waitingIn;
    private  ImageView waitingMe;
    private LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private  Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // 加载旋转动画
            switch (msg.what){
                case 0: {
                    Context context = (Context) msg.obj;
                    Animation loading_animation = AnimationUtils.loadAnimation(context,
                            R.anim.webloading_in_2);
                    Animation loading_animation2 = AnimationUtils.loadAnimation(context,
                            R.anim.webloading_me_2);
                    if (waitingIn!=null)
                    waitingIn.startAnimation(loading_animation);
                    if(waitingMe!=null)
                    waitingMe.startAnimation(loading_animation2);

                    Message msg2 = new Message();
                    msg2.what = 1;
                    msg2.obj = context;
                    if(waitingIn!=null && waitingMe!=null)
                    handler.sendMessageDelayed(msg2, 3000);
                }
                    break;
                case 1: {
                    Context context = (Context) msg.obj;
                    Animation loading_animation = AnimationUtils.loadAnimation(context,
                            R.anim.webloading_in_1);
                    Animation loading_animation2 = AnimationUtils.loadAnimation(context,
                            R.anim.webloading_me_1);
                    if (waitingIn!=null)
                        waitingIn.startAnimation(loading_animation);
                    if(waitingMe!=null)
                        waitingMe.startAnimation(loading_animation2);

                    Message msg2 = new Message();
                    msg2.what = 0;
                    msg2.obj = context;
                    if(waitingIn!=null && waitingMe!=null)
                    handler.sendMessageDelayed(msg2, 3000);
                }
                    break;
            }
        }
    };

    public static ProgressDialog instance(Context context){
        InMeProgressDialog inMeProgressDialog=new InMeProgressDialog();
        return inMeProgressDialog.getProgressDialog(context);
    }

    public ProgressDialog getProgressDialog(Context context){
        ProgressDialog progressDialog = new ProgressDialog(context);
        LinearLayout background = new LinearLayout(context);
        background.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                ShapeDrawable shapeDrawable = new ShapeDrawable();
                Shape shape = new Shape() {
                    int color=Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));
                     @Override
                    public void draw(Canvas canvas, Paint paint) {
                        paint = new Paint();
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(color);
                        paint.setAlpha(160);

                        RectF rect = new RectF();
                        rect.set(0, 0, canvas.getWidth(), canvas.getHeight());
                        canvas.drawRoundRect(rect, 15, 15, paint);
                    }
                };

        shapeDrawable.setShape(shape);
        background.setBackgroundDrawable(shapeDrawable);
/*        background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tuangou_item_border));
        background.getBackground().setAlpha(230);*/

        background.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);

        waitingIn = new ImageView(context);
        waitingIn.setImageResource(R.drawable.webloading_in);

        waitingMe = new ImageView(context);
        waitingMe.setImageResource(R.drawable.webloading_me);

        // 加载旋转动画
        Animation loading_animation = AnimationUtils.loadAnimation(context,
                R.anim.webloading_in_1);
        Animation loading_animation2 = AnimationUtils.loadAnimation(context,
                R.anim.webloading_me_1);

        background.addView(waitingIn);
        background.addView(waitingMe);

        progressDialog.show();
        progressDialog.setContentView(background);

        Window dialogWindow = progressDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        lp.height = 180;
        lp.width = 400;
        lp.alpha = 0.8f; // 透明度
        dialogWindow.setAttributes(lp);
        waitingIn.startAnimation(loading_animation);
        waitingMe.startAnimation(loading_animation2);

        Message msg=new Message();
        msg.what=0;
        msg.obj=context;

        handler.sendMessageDelayed(msg,3000);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                waitingIn=null;
                waitingMe=null;
            }
        });
        return progressDialog;
    }
}
