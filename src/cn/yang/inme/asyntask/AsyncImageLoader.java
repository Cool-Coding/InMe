package cn.yang.inme.asyntask;

/**
 * Created by Administrator on 14-5-28.
 */

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import cn.yang.inme.utils.ImageLoader;

public class AsyncImageLoader {

    private final static HandlerThread handlerThread = new HandlerThread("getImage");

    static {
        handlerThread.start();
    }

    /**
     * 对图片进行管理的工具类
     */

    public AsyncImageLoader() {

    }

    public static Bitmap loadDrawable(final String imageUrl, final ImageCallback imageCallback, final String tag, final int imageSize) {
        //图片处理工具类
        final ImageLoader imageLoader = ImageLoader.getInstance();

        Bitmap imageBitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);
        if (imageBitmap != null) {
            return imageBitmap;
        }

        final Handler handler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message message) {
                imageCallback.imageLoaded((Bitmap) message.obj, tag);
            }
        };
        new Thread() {
            @Override
            public void run() {
                Bitmap drawable = imageLoader.loadImageFromUrl(imageUrl, imageSize);
                Message message = handler.obtainMessage(0, drawable);
                handler.sendMessage(message);
            }
        }.start();
        return null;
    }

    public interface ImageCallback {
        public void imageLoaded(Bitmap imageDrawable, String tag);
    }
}

