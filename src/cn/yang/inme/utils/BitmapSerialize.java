package cn.yang.inme.utils;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Administrator on 14-5-28.
 */
public class BitmapSerialize implements Serializable {
    private static final long serialVersionUID = 1L;
    private Bitmap bitmap;

    public BitmapSerialize(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
