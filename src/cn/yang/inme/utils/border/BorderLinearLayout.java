package cn.yang.inme.utils.border;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class BorderLinearLayout extends LinearLayout {
    protected Paint paint;

    //上边框
    private int top_color;
    private int top_width;
    //左边框
    private int left_color;
    private int left_width;
    //下边框
    private int bottom_color;
    private int bottom_width;
    //右边框
    private int right_color;
    private int right_width;

    private int alpha;

    private boolean overLay = false;

    public void setTopBorder(int color, int width) {
        this.top_color = color;
        this.top_width = width;
    }

    public void setLeftBorder(int color, int width) {
        this.left_color = color;
        this.left_width = width;
    }

    public void setBottomBorder(int color, int width) {
        this.bottom_color = color;
        this.bottom_width = width;
    }

    public void setRightBorder(int color, int width) {
        this.right_color = color;
        this.right_width = width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paint == null) paint = new Paint();
        //  画TextView的4个边
        //top
        paint.setColor(top_color);
        paint.setStrokeWidth(top_width);
        canvas.drawLine(0, 0, this.getWidth() - 1, 0, paint);
        //left
        paint.setColor(left_color);
        paint.setStrokeWidth(left_width);
        canvas.drawLine(0, 0, 0, this.getHeight() - 1, paint);
        //right
        paint.setColor(right_color);
        paint.setStrokeWidth(right_width);
        canvas.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1, paint);
        //bottom
        paint.setColor(bottom_color);
        paint.setStrokeWidth(bottom_width);
        canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1, paint);

        if (overLay) {
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(alpha);//此方法必须放在Color后面才能实现透明作用
            canvas.drawRect(new RectF(0, 0, this.getWidth(), this.getHeight()), paint);
        }
    }

    //设置边框的颜色与亮度
    public void setBorders(int color, int width, int alpha) {
        this.top_color = color;
        this.top_width = width;

        this.left_color = color;
        this.left_width = width;

        this.bottom_color = color;
        this.bottom_width = width;

        this.right_color = color;
        this.right_width = width;

        this.alpha = alpha;
    }

    public void drawOverLayRect(int color, int width, int alpha) {
        setBorders(color, width, alpha);
        overLay = true;
        invalidate();
    }

    public BorderLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BorderLinearLayout(Context context) {
        super(context);
    }

    public BorderLinearLayout(Context context, int top_color, int top_width, int left_color, int left_width, int bottom_color, int bottom_width, int right_color, int right_width) {
        super(context);

        this.top_color = top_color;
        this.top_width = top_width;

        this.left_color = left_color;
        this.left_width = left_width;

        this.bottom_color = bottom_color;
        this.bottom_width = bottom_width;

        this.right_color = right_color;
        this.right_width = right_width;
    }

}
