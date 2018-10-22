/**
 *
 */
package cn.yang.inme.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.yang.inme.R;

public class ToastUtil {

    public static void show(Context context, String info) {
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.toast_item_border));
        linearLayout.getBackground().setAlpha(200);

        TextView textView=new TextView(context);
        textView.setText(info);
        textView.setPadding(20,15,20,15);

        float size=context.getResources().getDimension(R.dimen.toast_text_size);
        textView.setTextSize(size);
        linearLayout.addView(textView);

        Toast toast=new Toast(context);
        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM,0,Constants.footerHeight+50);
        toast.show();
    }

    public static void show(Context context, int info) {
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.toast_item_border));
        linearLayout.getBackground().setAlpha(200);

        TextView textView=new TextView(context);
        textView.setText(info);
        textView.setPadding(20,15,20,15);

        float size=context.getResources().getDimension(R.dimen.toast_text_size);
        textView.setTextSize(size);
        linearLayout.addView(textView);

        Toast toast=new Toast(context);
        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM,0,Constants.footerHeight+50);
        toast.show();
    }

    public static void showShort(Context context, String info) {
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.toast_item_border));
        linearLayout.getBackground().setAlpha(200);

        TextView textView=new TextView(context);
        textView.setText(info);
        textView.setPadding(20,15,20,15);

        float size=context.getResources().getDimension(R.dimen.toast_text_size);
        textView.setTextSize(size);
        linearLayout.addView(textView);

        Toast toast=new Toast(context);
        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM,0,Constants.footerHeight+50);
        toast.show();
    }
}
