package cn.yang.inme.activity.around;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;
import cn.yang.inme.utils.ToastUtil;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by Administrator on 14-5-26.
 */
public class PurchaseGroupDetail extends Activity {

    private WebView luntanListview;
    private Logger log = Logger.getLogger(PurchaseGroupDetail.class);
    private String title;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuangou_item);
        setHeaderColor();
        setFooterColor();

        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        String url = (String) bundle.get("detail_url");
        title = (String) bundle.get("detail_title");
        if (url == null && "".equals(url)) {
            log.warn("网页连接地址为空");
            return;
        }

        luntanListview = new WebView(this);
        luntanListview.getSettings().setJavaScriptEnabled(true); //设置支持Javascript
        luntanListview.getSettings().setDomStorageEnabled(true);
        luntanListview.requestFocus(); //触摸焦点起作用.如果不设置，则在点击网页文本输入框时，不能弹出软键盘及不响应其他的一些事件。
        luntanListview.loadUrl(url);
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                if (url.startsWith("tel:")) {
                    popupListDialog(url);
                } else {
                    view.loadUrl(url);
                }

                return true;
            }


        };

        luntanListview.setWebViewClient(webViewClient);
        //设置WebChromeClient
        luntanListview.setWebChromeClient(new WebChromeClient() {
            //处理javascript中的alert
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                //构建一个Builder来显示网页中的对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(PurchaseGroupDetail.this);
                builder.setTitle("警告");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        });
                builder.setCancelable(false);
                builder.create();
                builder.show();
                return true;
            }

            ;

            //处理javascript中的confirm
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                /*AlertDialog.Builder builder = new AlertDialog.Builder(PurchaseGroupDetail.this);
                builder.setTitle("确认");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        });
                builder.setCancelable(false);
                builder.create();
                builder.show();*/
                result.confirm();
                return true;
            }

            ;


            //设置网页加载的进度条
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

//                PurchaseGroupDetail.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
                showWebloading(newProgress);
                super.onProgressChanged(view, newProgress);
            }

            //设置应用程序的标题title
            @Override
            public void onReceivedTitle(WebView view, String title) {
//                PurchaseGroupDetail.this.setTitle(title);
                TextView title_view = ((TextView) PurchaseGroupDetail.this.findViewById(R.id.tuangou_title));
                title_view.setText(PurchaseGroupDetail.this.title);
                title_view.setSingleLine(true);
                title_view.setEllipsize(TextUtils.TruncateAt.END);
                super.onReceivedTitle(view, title);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                super.onReceivedTouchIconUrl(view, url, precomposed);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }
        });

        ScrollView scrollView = (ScrollView) this.findViewById(R.id.myScrollView);
        scrollView.removeAllViews();
        scrollView.addView(luntanListview);

/*        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String html=getHtml(url);
        luntanListview= new WebView(this);
        luntanListview.getSettings().setJavaScriptEnabled(true); //设置支持Javascript
        luntanListview.getSettings().setDomStorageEnabled(true);
        // 设置WevView要显示的网页
        luntanListview.getSettings().setDefaultTextEncodingName("GBK") ;
        luntanListview.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
        luntanListview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        luntanListview.getSettings().setJavaScriptEnabled(true); //设置支持Javascript
        luntanListview.requestFocus(); //触摸焦点起作用.如果不设置，则在点击网页文本输入框时，不能弹出软键盘及不响应其他的一些事件。
        luntanListview.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                //设置点击网页里面的链接还是在当前的webview里跳转
                String html=getHtml(url);
                luntanListview.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
                return true;
            }
        });
        ScrollView scrollView=(ScrollView)this.findViewById(R.id.myScrollView);
        scrollView.addView(luntanListview);*/
    }

    public void setHeaderColor() {
        LinearLayout header = (LinearLayout) findViewById(R.id.header);

        int startcolor = Color.parseColor("#EBEBEB");
        int endcolor = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{startcolor, endcolor});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setGradientRadius(1);
        header.setBackgroundDrawable(gradientDrawable);
    }

    public void setFooterColor() {
        LinearLayout radioGroup = (LinearLayout) findViewById(R.id.footer2);
        int startcolor = Color.parseColor("#EBEBEB");
        int endcolor = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{startcolor, endcolor});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setGradientRadius(3);
        radioGroup.setBackgroundDrawable(gradientDrawable);
    }

    public void showWebloading(int progress) {

        ImageView webloading = (ImageView) PurchaseGroupDetail.this.findViewById(R.id.webLoading_image);
        LinearLayout radioGroup = (LinearLayout) findViewById(R.id.footer2);
        if (progress == 0) {
            radioGroup.setAlpha(1f);
            radioGroup.setVisibility(View.VISIBLE);
            webloading.setVisibility(View.VISIBLE);
        }

        if (webloading.getAnimation() == null) {
            webloading.setBackgroundResource(R.drawable.webloading);
            // 加载旋转动画
            Animation webloading_animation = AnimationUtils.loadAnimation(this,
                    R.anim.webloading);
            webloading.startAnimation(webloading_animation);
        }

        TextView loading_text = (TextView) PurchaseGroupDetail.this.findViewById(R.id.webloading_text);

        if (loading_text != null) {
            if (progress == 100) {
                loading_text.setText("InMe祝您购物愉快!");
                if (webloading != null) {
                    webloading.clearAnimation();
                    webloading.setVisibility(View.INVISIBLE);
                }
                radioGroup.setAlpha(1f);
                radioGroup.setVisibility(View.VISIBLE);
                radioGroup.animate()
                        .alpha(0f)
                        .setDuration(2000);

            } else {
                loading_text.setText("InMe正在努力为您加载" + progress + "%");
            }
        }

    }

    public String getHtml(String url) {
        String html = "";
        try {
            // 定义获取文件内容的URL
            URL myURL = new URL(url);
            // 打开URL链接
            HttpURLConnection ucon = (HttpURLConnection) myURL.openConnection();
            // 使用InputStream，从URLConnection读取数据
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            // 用ByteArrayBuffer缓存
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            // 将缓存的内容转化为String,用UTF-8编码
            html = EncodingUtils.getString(baf.toByteArray(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            html = e.getMessage();
        }

        return html;
    }

    public void popupListDialog(String url) {

        String telephone;
        try {
            telephone = URLDecoder.decode(url, "UTF-8");
            final String[] items = telephone.split("[，,、;；]");
            if (items.length > 1) {
                for (int i = 0; i < items.length; i++) {
                    if (items[i].startsWith("tel:")) {
                        items[i] = items[i].substring(4);
                        break;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + items[which]));
                                PurchaseGroupDetail.this.startActivity(intent);
                            }
                        }
                );
                builder.show();
            } else {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
                PurchaseGroupDetail.this.startActivity(intent);
            }
        } catch (Exception e) {
            ToastUtil.show(PurchaseGroupDetail.this, "不好意思，拨打电话功能暂时不可用!");
        }
    }

    public void back_to_pgitem(View view) {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && luntanListview.canGoBack()) {
            luntanListview.goBack();//返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}