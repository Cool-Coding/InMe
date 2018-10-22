package cn.yang.inme.utils;

import android.graphics.*;

/**
 * Created by yang on 2014/10/11.
 */
public class ImageUtil {
    //空图片
    public static Bitmap emptyImage;
    public static Bitmap tuanImage;
    public static Bitmap quanImage;

    public static Bitmap getRoundRect(Bitmap source,int radius){
        Bitmap target = Bitmap.createBitmap(source.getWidth(),source.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas=new Canvas(target);
        Paint paint=new Paint();
        paint.setAntiAlias(true);

        RectF rect=new RectF();
        rect.set(0, 0, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rect,radius,radius,paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);

        source.recycle();
        return target;
    }
}
