package cn.yang.inme.utils.colorpallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.*;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 14-7-9.
 */
public class ColorPalletDialog {

    private Activity context;
    private OnColorSelected onColorSelected;
    private AlertDialog dialog;

    public ColorPalletDialog(Activity context) {
        this.context = context;
    }

    public void show() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        int[] colors = new int[]{Color.parseColor("#FFFF00"), Color.parseColor("#FF9912"), Color.parseColor("#D2691E"), Color.parseColor("#1E90FF"),
                Color.parseColor("#B03060"), Color.parseColor("#336666"), Color.parseColor("#A020F0"), Color.parseColor("#7FFF00"),
                Color.parseColor("#D2B48C"), Color.parseColor("#A39480"), Color.parseColor("#00C78C"), Color.parseColor("#6A5ACD"),
                Color.parseColor("#385E0F"), Color.parseColor("#228B22"), Color.parseColor("#6B8E23"), Color.parseColor("#FF00CC"),
        };
        int len = colors.length;
        int rows = len / 4;
        if (len % 4 != 0) rows += 1;

        LinearLayout pallet = new LinearLayout(context);
        pallet.setOrientation(LinearLayout.VERTICAL);
        pallet.setLayoutParams(params);

        int i = 0;
        while (i < rows) {
            LinearLayout line = new LinearLayout(context);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setLayoutParams(params);

            params.setMargins(10, 10, 10, 10);
            for (int j = 4 * i; j < 4 * i + 4; j++) {
                ColorpalletView color = new ColorpalletView(context, colors[j]);
                color.setLayoutParams(params);
                color.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorpalletView colorpalletView = ((ColorpalletView) v);
                        colorpalletView.checkRight();
                        onColorSelected.select(colorpalletView.color);
                    }
                });
                line.addView(color);
            }
            pallet.addView(line);

            i++;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请选择颜色").setView(pallet);

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用

        dialog = builder.show();
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.5
        lp.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
//        lp.alpha = 0.3f; // 透明度
        dialogWindow.setAttributes(lp);
    }

    public interface OnColorSelected {
        public void select(int color);
    }

    public void setOnColorSelected(OnColorSelected onColorSelected) {
        this.onColorSelected = onColorSelected;
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
