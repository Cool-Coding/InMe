package cn.yang.inme.activity.tabs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import cn.yang.inme.R;
import cn.yang.inme.utils.*;
import cn.yang.inme.utils.alarm.AlarmUtil;
import cn.yang.inme.utils.colorpallet.ColorPalletDialog;
import cn.yang.inme.utils.imagepicker.SingleImageSelectorDialog;

import java.io.File;
import java.net.URI;
import java.util.Calendar;

/**
 * Created by yang on 2014/6/1.
 */
public class MeActivity extends Activity implements SingleImageSelectorDialog.OnImagePickedFinished {

    private Uri imageUri;//裁剪图片保存的URI

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.melayout);
        LinearLayout meSet = (LinearLayout) findViewById(R.id.meSet);
        ApplicationUtil.setThemeBackground(meSet);


        Switch switcher = (Switch) findViewById(R.id.pathswitch);
        final Spinner spinner = (Spinner) findViewById(R.id.location_interval);
        final ImageButton imageButton = (ImageButton) findViewById(R.id.setthemecolor);
        final ImageButton bkImageButton = (ImageButton) findViewById(R.id.setthemebackground);
        final TextView caches_size = (TextView) findViewById(R.id.caches_size);
        final Button clearCachesButton = (Button) findViewById(R.id.setcaches);
        final Button clearBackgroundButton = (Button) findViewById(R.id.setbackground);
        TextView version=(TextView)findViewById(R.id.version_explain);
//-----------------------------------------------------------------------------------------
        String enable = PropertiesUtil.instance().read(Constants.PATH_LOCATION_ENABLE);
        if ("true".equals(enable)) {
            switcher.setChecked(true);
            spinner.setEnabled(true);
        } else {
            switcher.setChecked(false);
            spinner.setEnabled(false);
        }

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PropertiesUtil.instance().add(Constants.PATH_LOCATION_ENABLE, "true");
                    AlarmUtil.startLocateUser(MeActivity.this);
                    spinner.setEnabled(true);
                } else {
                    PropertiesUtil.instance().add(Constants.PATH_LOCATION_ENABLE, "false");
                    AlarmUtil.stopLocateUser(MeActivity.this);
                    spinner.setEnabled(false);
                }
            }
        });

//---------------------------------------------------------------------------------------------
        int color = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));
        imageButton.setBackgroundColor(color);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ColorPalletDialog dialog = new ColorPalletDialog(MeActivity.this);
                dialog.setOnColorSelected(new ColorPalletDialog.OnColorSelected() {
                    @Override
                    public void select(int color) {

                        PropertiesUtil.instance().add(Constants.SET_THEME_COLOR, String.valueOf(color));
                        imageButton.setBackgroundColor(color);
                        dialog.dismiss();

                        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                dialog.show();
            }
        });
//-----------------------------------------------------------------------------------------------------
        final String[] interval = new String[]{"1", "2", "5", "10", "30"};//单位分钟

        spinner.setAdapter(new SpinnerAdapter() {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView text = new TextView(MeActivity.this);
                text.setText(interval[position] + "min");
                text.setGravity(Gravity.CENTER);
                text.setMinWidth(100);
                return text;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return interval.length;
            }

            @Override
            public Object getItem(int position) {
                return interval[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView text = new TextView(MeActivity.this);
                text.setText(interval[position]);
                return text;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String time = interval[position];
                int time_int = Integer.valueOf(time) * 60;
                time = time_int + "";
                //System.out.println(time+"被选中了");
                PropertiesUtil.instance().add(Constants.PATH_LOCATION_INTERVAL, time);
                if ("true".equals(PropertiesUtil.instance().read(Constants.PATH_LOCATION_ENABLE))) {
                    AlarmUtil.startLocateUser(MeActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String time = PropertiesUtil.instance().read(Constants.PATH_LOCATION_INTERVAL);
        if (time != null) {
            int time_int = Integer.valueOf(time);
            time_int = time_int / 60;
            int len = interval.length;
            int i = 0;
            for (; i < len; i++) {
                if (interval[i].equals(time_int + "")) {
                    break;
                }
            }
            spinner.setSelection(i);
        }
//------------------------------------------------------------------------------------
        String background = PropertiesUtil.instance().read(Constants.SET_THEME_BACKGROUND);
        if (background == null || "".equals(background)) {
            bkImageButton.setBackgroundResource(R.drawable.default_image);
            bkImageButton.setTag(R.string.theme_background_indicator, "");
            clearBackgroundButton.setEnabled(false);
        } else {
            Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(background, getResources().getInteger(R.integer.set_theme_background_size));
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
            bkImageButton.setBackgroundDrawable(bitmapDrawable);
            bkImageButton.setTag(R.string.theme_background_indicator, background);
        }

        bkImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/jpeg");
                startActivityForResult(intent, 10);
            }
        });


        clearBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(PropertiesUtil.instance().read(Constants.SET_THEME_BACKGROUND));
                if (file.exists()) file.delete();

                PropertiesUtil.instance().add(Constants.SET_THEME_BACKGROUND, "");
                bkImageButton.setTag(R.string.theme_background_indicator, "");

                //重启应用
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
//----------------------------------------------------------------------------------------------------
        File file = ImageLoader.getDefaultDirectory();
        if (file.exists()) {
            long size = getFileSize(file);
            int temp;
            float size2 = (float) size / 10240;
            if (size2 < 1024) {
                temp = Math.round(size2 * 100);
                size2 = (float) temp / 100;
                caches_size.setText(size2 + "KB");
                if (size2 == 0) clearCachesButton.setEnabled(false);
            } else {
                size2 = size2 / 10240;
                temp = Math.round(size2 * 100);
                size2 = (float) temp / 100;
                caches_size.setText(size2 + "MB");
            }
        }
        clearCachesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File file = ImageLoader.getDefaultDirectory();
                if (file.exists()) {
                    caches_size.setText("0KB");
                    clearCachesButton.setEnabled(false);
                    ToastUtil.showShort(MeActivity.this, "清除成功!");
                    //启动删除缓存线程
                    new Thread(){
                        @Override
                        public void run() {
                            deleteFile(file);
                        }
                    }.start();
                }
            }
        });
        //-----------------------------------------------------------------------
        version.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupWindow popupWindow=new PopupWindow();
                popupWindow.setOutsideTouchable(true);
                popupWindow.setWindowLayoutMode(ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT);
                popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.tuangou_item_border));
                popupWindow.getBackground().setAlpha(230);

                LinearLayout linearLayout=new LinearLayout(MeActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setGravity(Gravity.CENTER);

                TextView header=new TextView(MeActivity.this);
                header.setGravity(Gravity.CENTER);
                header.setText("版本说明");
                header.setPadding(0,15,0,0);
                header.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);

                TextView content=new TextView(MeActivity.this);
                content.setText("   感谢大家选择InMe,今年是InMe诞生的一年，许多功能尚不完善。" +
                                "InMe服务的综旨是为大家提供方便的生活环境，从每天的记事，行程，消息到购物消费，还有" +
                                "随心定义的主题颜色与背景。后期将加入微博互动，周边活动等功能，真正打造属于您的InMe。" +
                                "谢谢大家的关注，希望将您宝贵的意见发送到邮箱:riguang_2007@163.com。在InMe今后的路上，我们将一同前进。" +
                                "此软件的最终解释权归InMe所有！");
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                content.setPadding(10,40,10,20);

                LinearLayout footerLayout=new LinearLayout(MeActivity.this);
                footerLayout.setOrientation(LinearLayout.HORIZONTAL);
                footerLayout.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Button button=new Button(MeActivity.this);
                button.setLayoutParams(params);
                button.setText("确认");
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                linearLayout.addView(header);
                linearLayout.addView(content);

                footerLayout.addView(button);
                linearLayout.addView(footerLayout);
                popupWindow.setContentView(linearLayout);
                popupWindow.setAnimationStyle(R.style.popwin_anim_style);
                popupWindow.showAtLocation(v,Gravity.CENTER,0,100);
            }
        });
    }



    @Override
    public void imagePicked(String path) {
        ImageButton bkImageButton = (ImageButton) findViewById(R.id.setthemebackground);
        Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(path, getResources().getInteger(R.integer.set_theme_background_size));
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        bkImageButton.setBackgroundDrawable(bitmapDrawable);

        if (!bkImageButton.getTag(R.string.theme_background_indicator).toString().equals(path)) {
            //得到屏幕的宽度与高度
            Display display = this.getWindowManager().getDefaultDisplay();
            cropImage(Uri.fromFile(new File(path)), display.getWidth(), display.getHeight(), 20);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == 20) {
                if (imageUri!=null) {
                    String path = imageUri.getPath();
                    ImageButton bkImageButton = (ImageButton) findViewById(R.id.setthemebackground);
                    //Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(path, getResources().getInteger(R.integer.set_theme_background_size));
                    //bkImageButton.setImageBitmap(bitmap);
                    bkImageButton.setImageURI(imageUri);
                    //保存图片路径
                    PropertiesUtil.instance().add(Constants.SET_THEME_BACKGROUND, path);
                    //设置标识
                    bkImageButton.setTag(R.string.theme_background_indicator, path);
                    //加载背景
                    ApplicationUtil.loadBackground();
                    //删除背景之外的图片
                    File file = ImageLoader.getBackgroundDirectory();
                    if (file.exists()) {
                        File[] files = file.listFiles();
                        for (File f : files) {
                            if (!path.equals(f.getAbsolutePath())) {
                                f.delete();
                            }
                        }
                    }

                    //重启应用
                    final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } else if (requestCode == 10) {
                Uri photoUri = data.getData();
                //得到屏幕的宽度与高度
                Display display = this.getWindowManager().getDefaultDisplay();
                cropImage(photoUri, display.getWidth(), display.getHeight(), 20);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 裁剪图片
     *
     * @param uri
     * @param outputX
     * @param outputY
     * @param requestCode
     */
    public void cropImage(Uri uri, int outputX, int outputY, int requestCode) {

        outputY = outputY - Constants.headerHeight - Constants.footerHeight;
        //裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        //裁剪框的比例，1：1
        intent.putExtra("aspectX", outputX);
        intent.putExtra("aspectY", outputY);
        //裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        //图片格式
        File file = new File(ImageLoader.getBackgroundPath(Calendar.getInstance().getTimeInMillis() + ".jpg")); // 以时间秒为文件名)
        imageUri=Uri.fromFile(file);
        intent.putExtra("output", imageUri);  // 传入目标文件
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 得到目录下所有文件的大小(除背景图片外)
     *
     * @param file
     */
    public long getFileSize(File file) {
        if (file.isDirectory()) {
            if (!"background".equals(file.getName())) {
                long size = 0;
                File[] files = file.listFiles();
                for (File f : files) {
                    size += getFileSize(f);
                }
                return size;
            }
        } else {
            return file.length();
        }
        return 0;
    }

    /**
     * 删除文件(目录)
     *
     * @param file
     */
    public void deleteFile(File file) {
        if (file.isDirectory()) {
            if (!"background".equals(file.getName())) {
                File[] files = file.listFiles();
                for (File f : files) {
                    deleteFile(f);
                }
            }
        } else {
            file.delete();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean result = ApplicationUtil.isShouldExit(this);
            if (result) onBackPressed();
            else return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}