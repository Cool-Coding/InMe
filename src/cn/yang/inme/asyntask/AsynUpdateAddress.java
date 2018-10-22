package cn.yang.inme.asyntask;

import android.content.Context;
import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.activity.around.AroundShop;
import cn.yang.inme.activity.around.AroundTuangou;
import cn.yang.inme.utils.ToastUtil;
import cn.yang.inme.utils.network.LocationSource;

/**
 * Created by Administrator on 14-6-10.
 */
public class AsynUpdateAddress extends AsyncTask<Void, Void, String> {

    private ImageView imageView;//刷新图标
    private TextView textView;//地址文字
    private Context context;
    private LocationSource locate;

    public AsynUpdateAddress(ImageView imageView, TextView textView, Context context) {

        this.imageView = imageView;
        this.textView = textView;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {

        Animation anim = AnimationUtils.loadAnimation(context, R.anim.address_refresh);
        imageView.startAnimation(anim);

        locate = new LocationSource(context);
        locate.locate();
    }

    @Override
    protected String doInBackground(Void... voids) {

        while (locate.getResult() == ' ') ;
        if (locate.getResult() == 'A') {
            return context.getResources().getString(R.string.locate_fail);//前面打个‘X’表示错误
        }
        return locate.getAddress();
    }

    @Override
    protected void onPostExecute(String s) {

        //如果有错误出现
        if (s == null) s = context.getResources().getString(R.string.locate_fail);
        if (s.charAt(0) == 'X') {
            ToastUtil.show(context, s.substring(1));
        } else {
            textView.setText(s.replaceAll(" ", ""));
            if (context instanceof AroundTuangou) {
                ((AroundTuangou) context).getmPullScrollView().doPullRefreshing(true, 0);
            } else if (context instanceof AroundShop) {
                ((AroundShop) context).getmPullScrollView().doPullRefreshing(true, 0);
            }
        }
        imageView.clearAnimation();
    }
}
