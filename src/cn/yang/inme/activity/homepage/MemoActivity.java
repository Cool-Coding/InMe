package cn.yang.inme.activity.homepage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import cn.yang.inme.R;
import cn.yang.inme.bean.Memo;
import cn.yang.inme.bean.MemoFrequency;
import cn.yang.inme.sqlite.MemoDBManager;
import cn.yang.inme.utils.ApplicationUtil;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;
import cn.yang.inme.utils.ToastUtil;
import cn.yang.inme.utils.alarm.AlarmUtil;
import cn.yang.inme.utils.border.BorderTextView;
import cn.yang.inme.utils.border.TopBorderLinearLayout;
import cn.yang.inme.utils.colorpallet.ColorPalletDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yang on 2014/7/6.
 */
public class MemoActivity extends Activity {

    private TextView remindMe;//"提醒我"按钮
    private int memoBackgroundColor = Color.WHITE;
    private String _id;//memoid
    private Memo memo;//编辑模式下的备忘录

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creatememo);
        getActionBar().setDisplayShowTitleEnabled(false);
        //设置ActionBar颜色
        ApplicationUtil.setActionBarColor(this);


        //接收传递过来的数据
        Intent intent = getIntent();
        _id = intent.getStringExtra(getResources().getString(R.string.memoId));
        if (_id != null) {
            MemoDBManager db = new MemoDBManager(MemoActivity.this);
            memo = db.queryMemo(_id);
            db.closeDB();
        }

        final TextView content = (TextView) findViewById(R.id.content);

        remindMe = (TextView) findViewById(R.id.remindMe);
        Button save = (Button) findViewById(R.id.addMemoBt);
        final TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) remindMe.getParent();

        remindMe.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        remindMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //下拉频率选择框
                final BorderTextView frequency = new BorderTextView(MemoActivity.this);
                frequency.setBottomBorder(Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR)), 3);
                frequency.setPadding(10, 3, 10, 0);
                frequency.setText("一次");
                frequency.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                remindMeParent.removeView(remindMe);
                remindMeParent.addView(frequency);
                frequency.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dropList((TextView) v, MemoActivity.this);
                    }
                });
                remindMeParent.addView(addRemind(MemoActivity.this, "time"));
                remindMeParent.addView(addCloseBt(MemoActivity.this));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content_text = content.getText().toString();
                if (!"".equals(content_text.trim())) {

                    MemoDBManager db = new MemoDBManager(MemoActivity.this);
                    Memo memo = new Memo();
                    memo.content = content_text;
                    memo.backgroundcolor = memoBackgroundColor;
                    memo.createTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    //有提醒时间
                    if (remindMe == null || remindMe.getParent() == null) {
                        TextView frequency = (TextView) remindMeParent.getChildAt(1);
                        memo.frequency = MemoFrequency.getFrequency((String) frequency.getText());

                        //月
                        TextView month = (TextView) remindMeParent.findViewWithTag("month");
                        if (month != null) {
                            String text = (String) month.getText();
                            memo.month = Integer.valueOf(text.substring(0, text.length() - 1));
                        }
                        //日
                        TextView day = (TextView) remindMeParent.findViewWithTag("day");
                        if (day != null) {
                            String text = (String) day.getText();
                            memo.day = Integer.valueOf(text.substring(0, text.length() - 1));
                        }
                        //周
                        TextView week = (TextView) remindMeParent.findViewWithTag("week");
                        if (week != null) {
                            memo.week = getWeekInt(week.getText().toString());
                        }
                        //时间
                        TextView time = (TextView) remindMeParent.findViewWithTag("time");
                        if (time != null) {
                            memo.time = time.getText().toString();
                        }
                    }

                    long id = -1;
                    if (_id != null) {
                        memo._id = Integer.valueOf(_id);
                        db.updateContent(memo);
                    } else {
                        id = db.add(memo);
                        memo._id = (int) id;
                    }
                    db.closeDB();

                    ToastUtil.showShort(MemoActivity.this, getString(R.string.memo_save));

                    //判断是否应该加入到今日待办中
                    Calendar calendar = Calendar.getInstance();
                    int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    if (week == 0) week = 7;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, month - 1);
                    int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (day == maxDays && memo.day!=null) {
                        day = Math.max(day, memo.day);
                    }
                    if (memo.frequency == null ||
                            memo.frequency.equals(MemoFrequency.ONCE) ||
                            memo.frequency.equals(MemoFrequency.EVERYDAY) ||
                            memo.frequency.equals(MemoFrequency.PERWEEK) && memo.week == week ||
                            memo.frequency.equals(MemoFrequency.PERMONTH) && memo.day == day ||
                            memo.frequency.equals(MemoFrequency.PERYEAR) && memo.month == month && memo.day == day) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("_id", memo._id);
                            jsonObject.put("action", memo.content);
                            if (memo.time == null) memo.time = "";
                            jsonObject.put("time", memo.time);
                            jsonObject.put("color", memo.backgroundcolor);

                            Intent intent = new Intent();
                            intent.putExtra("memo", jsonObject.toString());
                            setResult(20, intent);
                        } catch (JSONException e) {

                        }
                    } else if (_id != null) {//编辑模式：修改时间不在原来的时间内
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("_id", memo._id);

                            Intent intent = new Intent();
                            intent.putExtra("memo", jsonObject.toString());
                            setResult(400, intent);
                        } catch (JSONException e) {
                        }
                    }

                    //设置闹钟
                    if (MemoActivity.this.memo == null) {
                        AlarmUtil.setAlarm(MemoActivity.this, memo);
                    } else {
                        if (MemoActivity.this.memo.frequency != null || memo.frequency != null) {
                            if (MemoActivity.this.memo.frequency != memo.frequency
                                    || MemoActivity.this.memo.month != memo.month
                                    || MemoActivity.this.memo.day != memo.day
                                    || MemoActivity.this.memo.week != memo.week
                                    || !MemoActivity.this.memo.time.equals(memo.time)) {
                                //添加本次注册
                                AlarmUtil.setAlarm(MemoActivity.this, memo);
                            }
                        }
                    }
                } else {
                    if (_id != null) {//编辑模式：将内容去掉的话
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("_id", _id);

                            Intent intent = new Intent();
                            intent.putExtra("memo", jsonObject.toString());
                            setResult(400, intent);
                        } catch (JSONException e) {

                        }

                        //去掉之前定过的闹钟
                        AlarmUtil.cancelAlarm(MemoActivity.this, MemoActivity.this.memo);
                        ToastUtil.showShort(MemoActivity.this, getString(R.string.memo_empty_delete));
                    } else {
                        ToastUtil.showShort(MemoActivity.this, getString(R.string.memo_empty_nosave));
                    }


                }
                finish();
            }
        });

        //如果是修改
        if (memo != null) {
            content.setText(memo.content);
            content.setBackgroundColor(memo.backgroundcolor);
            memoBackgroundColor = memo.backgroundcolor;
            //设置频率
            if (memo.frequency != null) {
                //下拉频率选择框
                final BorderTextView frequency = new BorderTextView(MemoActivity.this);
                frequency.setBottomBorder(Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR)), 3);
                frequency.setPadding(10, 3, 10, 0);
                frequency.setText(memo.frequency.getLabel());
                frequency.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                remindMeParent.removeView(remindMe);
                remindMeParent.addView(frequency);
                frequency.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dropList((TextView) v, MemoActivity.this);
                    }
                });

                //加载对应重复频率的视图
                addFrequencyView(this, memo.frequency.ordinal(), false);
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.creatememo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addColor:
                final ColorPalletDialog dialog = new ColorPalletDialog(this);
                dialog.setOnColorSelected(new ColorPalletDialog.OnColorSelected() {
                    @Override
                    public void select(int color) {
                        memoBackgroundColor = color;
                        MemoActivity.this.findViewById(R.id.content).setBackgroundColor(color);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void dropList(final TextView frequency, final Context context) {
        final ArrayList<String> frequency_list = new ArrayList<String>();
        frequency_list.add(MemoFrequency.ONCE.getLabel());
        frequency_list.add(MemoFrequency.EVERYDAY.getLabel());
        frequency_list.add(MemoFrequency.PERWEEK.getLabel());
        frequency_list.add(MemoFrequency.PERMONTH.getLabel());
        frequency_list.add(MemoFrequency.PERYEAR.getLabel());

        final ListPopupWindow menu = new ListPopupWindow(context);
        menu.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        menu.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return frequency_list.size();
            }

            @Override
            public Object getItem(int position) {
                return frequency_list.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView frequency_text = new TextView(context);
                frequency_text.setText(frequency_list.get(position));
                frequency_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                frequency_text.setMinHeight(60);
                frequency_text.setGravity(Gravity.CENTER);
                return frequency_text;
            }
        });

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                menu.dismiss();
                frequency.setText(frequency_list.get(position));
                addFrequencyView(context, position, true);

            }
        });
        menu.setWidth(180);
        menu.setAnchorView(frequency);
        menu.show();
    }

    private void addFrequencyView(Context context, int position, boolean showMenu) {
        //对于每一种不同的频率会有不同的响应
        TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
        remindMeParent.removeViews(2, remindMeParent.getChildCount() - 2);
        switch (position) {
            case 0:
                //时间
                remindMeParent.addView(addRemind(context, "time"));
                if (showMenu) showTimePicker(context);
                break;
            case 1:
                //时间
                remindMeParent.addView(addRemind(context, "time"));
                if (showMenu) showTimePicker(context);
                break;
            case 2:
                //周几
                remindMeParent.addView(addRemind(context, "week"));
                //时间
                remindMeParent.addView(addRemind(context, "time"));
                if (showMenu) showWeekTimePicker(context);
                break;
            case 3:
                //哪一天
                remindMeParent.addView(addRemind(context, "day"));
                //时间
                remindMeParent.addView(addRemind(context, "time"));
                if (showMenu) showDayTimePicker(context);
                break;
            case 4:
                //哪一个月
                remindMeParent.addView(addRemind(context, "month"));
                //哪一天
                remindMeParent.addView(addRemind(context, "day"));
                //时间
                remindMeParent.addView(addRemind(context, "time"));
                if (showMenu) showMonthDayTimePicker(context);
                break;
        }
        remindMeParent.addView(addCloseBt(context));
    }

    /**
     * 在提醒我处添加关闭按钮
     *
     * @param context
     * @return
     */
    public LinearLayout addCloseBt(Context context) {
        //添加关闭按钮
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout close_layout = new LinearLayout(context);
        close_layout.setLayoutParams(params);
        close_layout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        ImageView close = new ImageView(context);
        close.setImageResource(R.drawable.ic_action_close);
        close_layout.addView(close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                remindMeParent.removeViews(1, remindMeParent.getChildCount() - 1);
                remindMeParent.addView(remindMe);
            }
        });
        return close_layout;
    }

    public TextView addRemind(final Context context, String str) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param1.leftMargin = 7;

        final BorderTextView textView = new BorderTextView(context);
        textView.setBottomBorder(Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR)), 3);
        textView.setPadding(1, 3, 1, 0);
        textView.setLayoutParams(param1);

        textView.setTag(str);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        if ("time".equals(str)) {
            //默认时间是一小时之后
            String time;
            if (memo != null && memo.time != null) {
                time = memo.time;
            } else time = new SimpleDateFormat("HH:mm").format(System.currentTimeMillis() + 60 * 60 * 1000);
            textView.setText(time);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePicker(context);
                }
            });
        } else if ("week".equals(str)) {
            //得到当天在一周中为周几
            if (memo != null && memo.week != null) textView.setText(getWeekLabel(memo.week));
            else textView.setText(getWeekLabel(-1));
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showWeekPicker(context);
                }
            });
        } else if ("day".equals(str)) {
            String text;
            if (memo != null && memo.day != null) text = String.valueOf(memo.day);
            else text = new SimpleDateFormat("dd").format(new Date());
            if (text.startsWith("0")) text = text.substring(1);
            textView.setText(text + "日");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDayPicker(context);
                }
            });
        } else if ("month".equals(str)) {
            if (memo != null && memo.month != null) textView.setText(memo.month + "月");
            else textView.setText(getNowMonth());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMonthPicker(context);
                }
            });
        }
        return textView;
    }

    /**
     * 日期选择器
     *
     * @param context
     */
    public void showTimePicker(Context context) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);

        final TimePicker timePicker = getTimePicker();
        linearLayout.addView(timePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                TextView time = (TextView) remindMeParent.findViewWithTag("time");
                int hourOfDay = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String hour;
                String minu;
                if (hourOfDay < 10) {
                    hour = "0" + hourOfDay;
                } else hour = "" + hourOfDay;

                if (minute < 10) {
                    minu = "0" + minute;
                } else {
                    minu = "" + minute;
                }
                time.setText(hour + ":" + minu);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("请选择时间");
        builder.create().show();
    }

    public void showWeekPicker(Context context) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);

        final NumberPicker weekPicker = getWeekPicker();

        linearLayout.addView(weekPicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                TextView day = (TextView) remindMeParent.findViewWithTag("week");
                day.setText(getWeekLabel(weekPicker.getValue()));
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("请选择周几");
        builder.create().show();
    }

    /**
     * 星期及日期选择器
     *
     * @param context
     */
    public void showWeekTimePicker(Context context) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);

        final NumberPicker weekPicker = getWeekPicker();
        final TimePicker timePicker = getTimePicker();

        linearLayout.addView(weekPicker);
        linearLayout.addView(timePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                TextView day = (TextView) remindMeParent.findViewWithTag("week");
                day.setText(getWeekLabel(weekPicker.getValue()));
                TextView time = (TextView) remindMeParent.findViewWithTag("time");
                int hourOfDay = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String hour;
                String minu;
                if (hourOfDay < 10) {
                    hour = "0" + hourOfDay;
                } else hour = "" + hourOfDay;

                if (minute < 10) {
                    minu = "0" + minute;
                } else {
                    minu = "" + minute;
                }
                time.setText(hour + ":" + minu);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("请选择星期时间");
        builder.create().show();

    }

    public void showDayPicker(Context context) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);
        final NumberPicker dayPicker = getDayPicker();

        TextView month_textView = (TextView) findViewById(R.id.remindMeParent).findViewWithTag("month");
        if (month_textView != null) {
            String text = (String) month_textView.getText();
            text = text.substring(0, text.length() - 1);
            int curmonth = Integer.valueOf(text);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, curmonth - 1);
            int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            dayPicker.setMaxValue(maxDays);
        }

        linearLayout.addView(dayPicker);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                TextView day = (TextView) remindMeParent.findViewWithTag("day");
                day.setText(dayPicker.getValue() + "日");
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("请选择哪一天");
        builder.create().show();
    }

    public void showDayTimePicker(final Context context) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);
        final NumberPicker dayPicker = getDayPicker();
        final TimePicker timePicker = getTimePicker();

        linearLayout.addView(dayPicker);
        linearLayout.addView(timePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                TextView day = (TextView) remindMeParent.findViewWithTag("day");
                day.setText(dayPicker.getValue() + "日");
                TextView time = (TextView) remindMeParent.findViewWithTag("time");
                int hourOfDay = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String hour;
                String minu;
                if (hourOfDay < 10) {
                    hour = "0" + hourOfDay;
                } else hour = "" + hourOfDay;

                if (minute < 10) {
                    minu = "0" + minute;
                } else {
                    minu = "" + minute;
                }
                time.setText(hour + ":" + minu);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("请选择日期");
        builder.create().show();
    }

    public void showMonthPicker(Context context) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);

        final NumberPicker monthPicker = getMonthPicker();
        linearLayout.addView(monthPicker);

        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, newVal - 1);
                int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                TextView day_textView = (TextView) findViewById(R.id.remindMeParent).findViewWithTag("day");
                String text = (String) day_textView.getText();
                int curday = Integer.valueOf(text.substring(0, text.length() - 1));

                int minDay = Math.min(maxDays, curday);
                day_textView.setText(minDay + "日");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                TextView month = (TextView) remindMeParent.findViewWithTag("month");
                month.setText(monthPicker.getValue() + "月");
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("请选择哪一月");
        builder.create().show();
    }

    public void showMonthDayTimePicker(Context context) {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);

        final NumberPicker monthPicker = getMonthPicker();
        final NumberPicker dayPicker = getDayPicker();
        final TimePicker timePicker = getTimePicker();

        linearLayout.addView(monthPicker);
        linearLayout.addView(dayPicker);
        linearLayout.addView(timePicker);

        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, newVal - 1);
                int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                int curDay = Math.min(maxDays, dayPicker.getValue());
                dayPicker.setMaxValue(maxDays);
                dayPicker.setValue(curDay);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopBorderLinearLayout remindMeParent = (TopBorderLinearLayout) findViewById(R.id.remindMeParent);
                TextView month = (TextView) remindMeParent.findViewWithTag("month");
                month.setText(monthPicker.getValue() + "月");
                TextView day = (TextView) remindMeParent.findViewWithTag("day");
                day.setText(dayPicker.getValue() + "日");
                TextView time = (TextView) remindMeParent.findViewWithTag("time");
                int hourOfDay = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String hour;
                String minu;
                if (hourOfDay < 10) {
                    hour = "0" + hourOfDay;
                } else hour = "" + hourOfDay;

                if (minute < 10) {
                    minu = "0" + minute;
                } else {
                    minu = "" + minute;
                }
                time.setText(hour + ":" + minu);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("请选择月日时间");
        builder.create().show();
    }

    public TimePicker getTimePicker() {
        TextView time_textView = (TextView) findViewById(R.id.remindMeParent).findViewWithTag("time");
        String time;
        if (time_textView == null)
            time = new SimpleDateFormat("HH:mm").format(System.currentTimeMillis() + 60 * 60 * 1000);
        else time = (String) time_textView.getText();

        String[] times = time.split(":");

        int hour = Integer.valueOf(times[0]);
        int minute = Integer.valueOf(times[1]);

        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        TimePicker timePicker = new TimePicker(this);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        return timePicker;
    }

    public NumberPicker getMonthPicker() {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(dip2px(70), LinearLayout.LayoutParams.WRAP_CONTENT);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        TextView month_textView = (TextView) findViewById(R.id.remindMeParent).findViewWithTag("month");
        int curmonth;
        if (month_textView == null) curmonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        else {
            String text = (String) month_textView.getText();
            text = text.substring(0, text.length() - 1);
            curmonth = Integer.valueOf(text);
        }
        final NumberPicker monthPicker = new NumberPicker(this);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setLayoutParams(param1);
        monthPicker.setValue(curmonth);
        monthPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return value + "月";
            }
        });

        return monthPicker;
    }

    public NumberPicker getDayPicker() {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(dip2px(70), LinearLayout.LayoutParams.WRAP_CONTENT);
        param1.leftMargin = 5;
        param1.rightMargin = 5;

        TextView day_textView = (TextView) findViewById(R.id.remindMeParent).findViewWithTag("day");
        int curday;
        if (day_textView == null) curday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        else {
            String text = (String) day_textView.getText();
            curday = Integer.valueOf(text.substring(0, text.length() - 1));
        }

        final NumberPicker dayPicker = new NumberPicker(this);
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setLayoutParams(param1);
        dayPicker.setValue(curday);
        dayPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return value + "日";
            }
        });

        return dayPicker;
    }

    public NumberPicker getWeekPicker() {
        NumberPicker.LayoutParams param1 = new NumberPicker.LayoutParams(dip2px(70), NumberPicker.LayoutParams.WRAP_CONTENT);
        param1.leftMargin = 5;
        param1.rightMargin = 5;


        int dayofweek;

        TextView week_textView = (TextView) findViewById(R.id.remindMeParent).findViewWithTag("week");
        if (week_textView == null) {
            Calendar c = Calendar.getInstance();
            dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayofweek == 0)
                dayofweek = 7;
        } else {
            dayofweek = getWeekInt((String) week_textView.getText());
        }
        final NumberPicker dayPicker = new NumberPicker(this);
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(7);
        dayPicker.setLayoutParams(param1);
        dayPicker.setValue(dayofweek);
        dayPicker.cancelLongPress();
        dayPicker.setLongClickable(false);

        dayPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return getWeekLabel(value);
            }
        });

        return dayPicker;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public int px2dip(float pxValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 得到现在是周几
     *
     * @return
     */
    public static String getWeekLabel(int day) {
        int dayofweek;
        if (day == -1) {
            Calendar c = Calendar.getInstance();
            dayofweek = c.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayofweek == 0)
                dayofweek = 7;
        } else dayofweek = day;

        String week = null;
        switch (dayofweek) {
            case 1:
                week = "周一";
                break;
            case 2:
                week = "周二";
                break;
            case 3:
                week = "周三";
                break;
            case 4:
                week = "周四";
                break;
            case 5:
                week = "周五";
                break;
            case 6:
                week = "周六";
                break;
            case 7:
                week = "周日";
                break;
        }
        return week;
    }

    public int getWeekInt(String label) {
        int value = -1;

        if ("周一".equals(label)) value = 1;
        else if ("周二".equals(label)) value = 2;
        else if ("周三".equals(label)) value = 3;
        else if ("周四".equals(label)) value = 4;
        else if ("周五".equals(label)) value = 5;
        else if ("周六".equals(label)) value = 6;
        else if ("周日".equals(label)) value = 7;

        return value;
    }

    /**
     * 得到几月
     *
     * @return
     */
    public static String getNowMonth() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;

        String month_text = null;
        switch (month) {
            case 1:
                month_text = "1月";
                break;
            case 2:
                month_text = "2月";
                break;
            case 3:
                month_text = "3月";
                break;
            case 4:
                month_text = "4月";
                break;
            case 5:
                month_text = "5月";
                break;
            case 6:
                month_text = "6月";
                break;
            case 7:
                month_text = "7月";
                break;
            case 8:
                month_text = "8月";
                break;
            case 9:
                month_text = "9月";
                break;
            case 10:
                month_text = "10月";
                break;
            case 11:
                month_text = "11月";
                break;
            case 12:
                month_text = "12月";
                break;
        }
        return month_text;
    }
}