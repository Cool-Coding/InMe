package cn.yang.inme.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import cn.yang.inme.R;

/**
 * Created by Administrator on 14-10-17.
 */
public class InMeTextProgressDialog {
    /**
     * 当前指针的位置
     */
    private int currentIndex=0;

    /**
     * 内容个数
     */
    private int count;

    /**
     * TextView
     */
    private TextView tip;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(tip!=null && tip.getParent()!=null){
                int[] content=(int[])msg.obj;
                tip.setText(content[currentIndex++]);
                if(currentIndex+1>count)currentIndex=0;

                Message msg2=new Message();
                msg2.obj=content;
                handler.sendMessageDelayed(msg2,1000);
            }
        }
    };

    public static TextView instance(Context context,int... content){
         if (content.length==0) return null;
         InMeTextProgressDialog inMeTextProgressDialog=new InMeTextProgressDialog();
         return inMeTextProgressDialog.getTextProgressDialog(context,content);
    }

    public TextView getTextProgressDialog(Context context,int... content){
        tip = new TextView(context);
        count=content.length;
        tip.setText(content[currentIndex++]);
        if(currentIndex+1>count)currentIndex=0;

        Message msg=new Message();
        msg.obj=content;
        handler.sendMessageDelayed(msg, 1000);
        return tip;
    }
}
