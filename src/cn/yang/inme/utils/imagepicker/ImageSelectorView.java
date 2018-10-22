package cn.yang.inme.utils.imagepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import cn.yang.inme.R;
import cn.yang.inme.utils.ImageLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 14-7-24.
 */
public class ImageSelectorView extends ScrollView {

    /**
     * 记录上垂直方向的滚动距离。
     */
    private static int lastScrollY = -1;
    /**
     * 线程池
     */
    private ExecutorService transThread = Executors.newSingleThreadExecutor();
    private Future transPending;

    /**
     * 空图片
     */

    private Drawable emptyImage;

    /**
     * 线程池中的线程
     */
    private ChangeImage changeImage = new ChangeImage();

    public ImageSelectorView(Context context) {
        super(context);
        emptyImage = context.getResources().getDrawable(R.drawable.default_image);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //必须放super之后，这样Image的距离才能确定
        new Thread(changeImage).start();
    }


    /**
     * 监听用户的触屏事件，如果用户手指离开屏幕则开始进行滚动检测。
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Message message = new Message();
            message.what = 1;
            message.obj = this;
            handler.sendMessageDelayed(message, 5);
        }
        return super.onTouchEvent(event);
    }


    /**
     * 在Handler中进行图片可见性检查的判断，以及加载更多图片的操作。
     */
    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    ImageSelectorView myScrollView = (ImageSelectorView) msg.obj;
                    int scrollY = myScrollView.getScrollY();
                    // 如果当前的滚动位置和上次相同，表示已停止滚动
                    if (scrollY == lastScrollY) {
                        if (transPending != null) transPending.cancel(true);
                        changeImage.setScrolly(scrollY);
                        transPending = transThread.submit(changeImage);
                    } else {
                        lastScrollY = scrollY;
                        Message message = new Message();
                        message.what = 1;
                        message.obj = myScrollView;
                        // 5毫秒后再次对滚动位置进行判断
                        handler.sendMessageDelayed(message, 5);
                    }
                    break;
                case 2: {
                    ImageView image = (ImageView) msg.obj;
                    Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource((String) image.getTag(), 150);
                    image.setImageBitmap(bitmap);
                }
                break;
                case 3: {
                    ImageView image = (ImageView) msg.obj;
                    image.setImageDrawable(emptyImage);
                }
                break;
            }
        }

        ;
    };

    /**
     * 将不在可见范围内的图片用空图片代替，可见
     */
    private class ChangeImage implements Runnable {
        private int t;

        public void setScrolly(int t) {
            this.t = t;
        }

        public void run() {
            LinearLayout linearLayout = (LinearLayout) ImageSelectorView.this.getChildAt(0);
            int count = linearLayout.getChildCount();

            for (int i = 0; i < count; i++) {
                LinearLayout item_line = (LinearLayout) linearLayout.getChildAt(i);
                if (item_line == null) continue;
                if (item_line.getTop() + item_line.getHeight() >= t && item_line.getTop() < t + getHeight()) {
                    int line_itemcount = item_line.getChildCount();
                    for (int j = 0; j < line_itemcount; j++) {
                        ImageView image = (ImageView) item_line.getChildAt(j);
                        String status = (String) image.getTag(R.string.wb_image_status);
                        if ("0".equals(status)) {
                            Message message = new Message();
                            message.what = 2;
                            message.obj = image;
                            handler.sendMessage(message);
                            image.setTag(R.string.wb_image_status, "1");
                        }
                    }
                } else {
                    int line_itemcount = item_line.getChildCount();
                    for (int j = 0; j < line_itemcount; j++) {
                        ImageView image = (ImageView) item_line.getChildAt(j);
                        String status = (String) image.getTag(R.string.wb_image_status);
                        if ("1".equals(status)) {
                            Message message = new Message();
                            message.what = 3;
                            message.obj = image;
                            handler.sendMessage(message);

                            image.setTag(R.string.wb_image_status, "0");
                        }
                    }
                }
            }
        }
    }

    ;
}
