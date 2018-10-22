package cn.yang.inme.utils.imagepicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import cn.yang.inme.R;

/**
 * Created by Administrator on 14-7-25.
 * 可选择的图片
 */
public class SingleImageViewSelector extends ImageView implements View.OnTouchListener {

    private boolean selectAble = true;//是否启用标识功能
    private boolean checkAble = true;//是否起用打勾
    private boolean checked = false;

    /**
     * 图片选择器的回调事件
     */
    private SingleImageSelectorDialog singleImageSelectorDialog;

    public SingleImageViewSelector(Context context, SingleImageSelectorDialog singleImageSelectorDialog) {
        super(context);
        setOnTouchListener(this);
        this.singleImageSelectorDialog = singleImageSelectorDialog;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (selectAble) {

            Paint paint = new Paint();
            int r;
            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(2);

            r = getResources().getInteger(R.integer.image_selector_circle_radius);

            float x = getWidth() - r;
            float y = r;

            canvas.drawCircle(x, y, r, paint);

            if (checked && checkAble) {
                int color = getResources().getColor(R.color.image_check);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(color);
                canvas.drawCircle(x, y, r, paint);

                //绘对勾
                paint.setStrokeWidth(3);
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                Path path = new Path();
                path.moveTo(x - r / 2, y);
                path.lineTo(x, r / 2 + y);
                path.lineTo(x + r / 2, r / 2);
                canvas.drawPath(path, paint);
            }
        }
    }

    /**
     * @return 该图片是否选取
     */
    public boolean isChecked() {
        return checked;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                if (checkAble) {
                    //得到按下时的X，Y坐标值
                    float x = event.getX();//点击点相对于图片的坐标
                    float y = event.getY();

                    int r = getResources().getInteger(R.integer.image_selector_circle_radius);
                    float circle_x = v.getWidth();

                    if (x > circle_x - 2 * r && x < circle_x && y < 2 * r) {
                        if (checkAble) {
                            checked = !checked;
                            invalidate();

                            SingleImageSelectorDialog.OnImagePickedFinished onImagePickedFinished = singleImageSelectorDialog.getOnImagePickedFinished();
                            if (onImagePickedFinished != null)
                                onImagePickedFinished.imagePicked(this.getTag().toString());
                            singleImageSelectorDialog.dismiss();
                        }
                    }
                }
                break;
        }
        return false;
    }


    public void clear() {
        selectAble = false;
    }

    /**
     * 清除选择圆圈标识
     */
    public void clearChecked() {
        checkAble = false;
    }
}
