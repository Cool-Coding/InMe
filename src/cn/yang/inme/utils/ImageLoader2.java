package cn.yang.inme.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.LruCache;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 对图片进行管理的工具类。
 *
 * @author Tony
 */
public class ImageLoader2 {

    /**
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
     */
    private static LruCache<String, Bitmap> mMemoryCache;

    /**
     * ImageLoader的实例。
     */
    private static ImageLoader2 mImageLoader;

    private ImageLoader2() {
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        // 设置图片缓存大小为程序最大可用内存的1/8
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    /**
     * 获取ImageLoader的实例。
     *
     * @return ImageLoader的实例。
     */
    public static ImageLoader2 getInstance() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader2();
        }
        return mImageLoader;
    }

    /**
     * 将一张图片存储到LruCache中。
     *
     * @param key    LruCache的键，这里传入图片的URL地址。
     * @param bitmap LruCache的键，这里传入从网络上下载的Bitmap对象。
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     *
     * @param key LruCache的键，这里传入图片的URL地址。
     * @return 对应传入键的Bitmap对象，或者null。
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth) {
        // 源图片的宽度
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            // 计算出实际宽度和目标宽度的比率
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathName,
                                                         int reqWidth) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(pathName, options);

        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;// 同时设置才会有效
        options.inInputShareable = true;//。当系统内存不够时候图片自动被回收

        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
        return bitmap;
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathName) {
        Bitmap bitmap = BitmapFactory.decodeFile(pathName);
        return bitmap;
    }

    public Bitmap loadImageFromUrl(String imageUrl, int imageSize) {
        if (hasSDCard()) {
            File imageFile = new File(getImagePath(imageUrl));
            if (!imageFile.exists()) {
                downloadImageAndSaveSD(imageUrl, imageSize);
                //下载完后，直接从缓存中取
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                if (bitmap != null) return bitmap;
            }

            if (imageUrl != null) {
                Bitmap bitmap = ImageLoader2.decodeSampledBitmapFromResource(
                        imageFile.getPath(), imageSize);
                if (bitmap != null) {
                    addBitmapToMemoryCache(imageUrl, bitmap);
                    return bitmap;
                }
            }
        } else {
            Bitmap bitmap = downloadImage(imageUrl, imageSize);
            addBitmapToMemoryCache(imageUrl, bitmap);
            return bitmap;
        }
        return null;
    }

    /**
     * 直接从网上下载图片
     *
     * @param url
     * @return
     */
    private Bitmap downloadImage(String url, int imageSize) {
        URL m;
        HttpURLConnection con = null;
        InputStream i = null;
        try {
            m = new URL(url);
            con = (HttpURLConnection) m.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(15 * 1000);
            con.setDoInput(true);
            i = con.getInputStream();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap d = resize(BitmapFactory.decodeStream(i), imageSize);
        return d;
    }

    public static Bitmap resize(Bitmap bitmap, int newWidth) {
        //获取这个图片的宽和高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) newWidth) / width;
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleWidth);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                width, height, matrix, true);
        return resizedBitmap;
    }

    /**
     * 将图片下载到SD卡缓存起来。
     *
     * @param imageUrl 图片的URL地址。
     */
    private void downloadImageAndSaveSD(String imageUrl, int imageSize) {
        HttpURLConnection con = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        File imageFile = null;
        try {
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(15 * 1000);
            con.setDoInput(true);
            //con.setDoOutput(true);//此方法将导致以HTTP以OST方式提交，取不到图片
            bis = new BufferedInputStream(con.getInputStream());
            imageFile = new File(getImagePath(imageUrl));
            fos = new FileOutputStream(imageFile);
            bos = new BufferedOutputStream(fos);
            byte[] b = new byte[1024];
            int length;
            while ((length = bis.read(b)) != -1) {
                bos.write(b, 0, length);
                bos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (imageFile != null) {
            Bitmap bitmap = ImageLoader2.decodeSampledBitmapFromResource(
                    imageFile.getPath(), imageSize);
            if (bitmap != null) {
                addBitmapToMemoryCache(imageUrl, bitmap);
            }
        }
    }

    /**
     * 获取图片的本地存储路径。
     *
     * @param imageUrl 图片的URL地址。
     * @return 图片的本地存储路径。
     */
    public String getImagePath(String imageUrl) {
        int lastSlashIndex = imageUrl.indexOf("/", 6);
        String imageName = imageUrl.substring(lastSlashIndex + 1);
        imageName = imageName.replaceAll("/", "-");
        String imageDir = Environment.getExternalStorageDirectory()
                .getPath() + "/inme/imagecaches/";
        File file = new File(imageDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String imagePath = imageDir + imageName;
        return imagePath;
    }

    /**
     * 判断手机是否有SD卡。
     *
     * @return 有SD卡返回true，没有返回false。
     */
    public boolean hasSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }
}
