package cn.yang.inme.utils.imagepicker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.yang.inme.R;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 14-7-24.
 * 图片浏览器
 */
public class SingleImageSelectorDialog {

    //private String[] regexp={"jpg","jpeg","bmp","png","gif"};//后缀名
    private String[] regexp = {"jpg", "jpeg"};//后缀名
    private Context context;
    private LinearLayout rootLayout;

    /**
     * 线程池(用于后台取图片)
     */
    private ExecutorService transThread = Executors.newSingleThreadExecutor();
    private Future transPending;

    /**
     * 空图片资源
     */
    private Drawable emptyImage;
    /**
     * 对话框对象
     */
    private AlertDialog dialog;
    /**
     * 图片选择器的回调事件
     */
    private OnImagePickedFinished onImagePickedFinished;

    public SingleImageSelectorDialog(Context context) {
        this.context = context;
        emptyImage = context.getResources().getDrawable(R.drawable.default_image);
    }

    public Dialog createDialog() {
        boolean hasSDcard = Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
        if (hasSDcard) {
            //创建View
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(150, 250, 1);
            params2.setMargins(10, 10, 10, 10);

            ImageSelectorView scrollView = new ImageSelectorView(context);
            scrollView.setLayoutParams(params);

            rootLayout = new LinearLayout(context);
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            rootLayout.setLayoutParams(params);
            scrollView.addView(rootLayout);

            //header
            LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(60, 60);
            LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(60, 60);
            params3.setMargins(10, 0, 0, 0);
            params3.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

            params4.setMargins(0, 0, 10, 0);

            //取照片
            transPending = transThread.submit(getImage);

            TextView header = new TextView(context);
            header.setText("请选择照片");
            header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            header.setPadding(10, 15, 0, 5);
            header.setTextColor(Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR)));

            //创建对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCustomTitle(header);
            builder.setView(scrollView);

            dialog = builder.create();
            return dialog;
        }

        return null;
    }

    private boolean isExtendMaxImageLimit(LinearLayout linearLayout, int limitCount) {
        int temp = 0;
        int count = linearLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            LinearLayout item_line = (LinearLayout) linearLayout.getChildAt(i);
            int line_itemcount = item_line.getChildCount();
            for (int j = 0; j < line_itemcount; j++) {
                ImageViewSelector image = (ImageViewSelector) item_line.getChildAt(j);
                if (image.isChecked()) {
                    temp += 1;
                }
            }
        }

        if (temp > limitCount)
            return true;
        else
            return false;
    }

    /**
     * 取图片线程
     */
    private Runnable getImage = new Runnable() {
        @Override
        public void run() {
            File file = Environment.getExternalStorageDirectory();
            getImages(file);
        }
    };

    private void getImages(File file) {
        if (file == null) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                getImages(f);
            }
        } else {
            String fileName = file.getName();
            for (String reg : regexp) {
                if (fileName.toLowerCase().endsWith(reg)) {
                    Message msg = new Message();
                    msg.obj = file;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    public void dismiss() {
        dialog.dismiss();
    }

    /**
     * 添加图片Handler
     */
    private Handler handler = new Handler() {
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(150, 250, 1);

        @Override
        public void handleMessage(Message msg) {
            File file = (File) msg.obj;
            if (file != null) {
                LinearLayout line = (LinearLayout) rootLayout.getChildAt(rootLayout.getChildCount() - 1);
                if (line != null && line.getChildCount() < 2) {
                    String path = file.getAbsolutePath();
                    SingleImageViewSelector imageView = new SingleImageViewSelector(context, SingleImageSelectorDialog.this);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    params2.setMargins(10, 10, 10, 10);
                    imageView.setLayoutParams(params2);
                    imageView.setTag(path);
                    imageView.setImageDrawable(emptyImage);
                    imageView.setTag(R.string.wb_image_status, "0");//0标识空图片
                    line.addView(imageView);
                } else {
                    LinearLayout line2 = new LinearLayout(context);
                    line2.setOrientation(LinearLayout.HORIZONTAL);
                    String path = file.getAbsolutePath();
                    SingleImageViewSelector imageView = new SingleImageViewSelector(context, SingleImageSelectorDialog.this);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    params2.setMargins(10, 10, 10, 10);
                    imageView.setLayoutParams(params2);
                    imageView.setTag(path);
                    imageView.setImageDrawable(emptyImage);
                    imageView.setTag(R.string.wb_image_status, "0");//0标识空图片
                    line2.addView(imageView);
                    rootLayout.addView(line2);
                }
            }
        }
    };

    public OnImagePickedFinished getOnImagePickedFinished() {
        return onImagePickedFinished;
    }

    public void setOnImagePickedFinished(OnImagePickedFinished onImagePickedFinished) {
        this.onImagePickedFinished = onImagePickedFinished;
    }

    public interface OnImagePickedFinished {
        public void imagePicked(String path);
    }

}
