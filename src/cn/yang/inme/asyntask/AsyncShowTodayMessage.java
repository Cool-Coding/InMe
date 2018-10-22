package cn.yang.inme.asyntask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import cn.yang.inme.R;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.InMeProgressDialog;
import cn.yang.inme.utils.PhoneUtils;
import cn.yang.inme.utils.PropertiesUtil;
import cn.yang.inme.utils.border.BottomBorderLinearLayout;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 14-7-2.
 */
public class AsyncShowTodayMessage extends AsyncTask<List<String>, Void, List<Map.Entry<String, Integer>>> {
    private Context context;//上下文环境
    private int childposition;//今日消息子列表项的序号(从0开始)
    private String name;//未读，接收等项的名称
    private String[][] items;
    private Handler handler;

    //字体大小
    private float size;

    private ProgressDialog progressDialog;
    private LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT
            , ViewGroup.LayoutParams.MATCH_PARENT, 2);
    private LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT
            , ViewGroup.LayoutParams.MATCH_PARENT);
    private LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT
            , ViewGroup.LayoutParams.MATCH_PARENT, 1);

    public AsyncShowTodayMessage(Context context, int childposition, String name) {
        this.context = context;
        this.childposition = childposition;
        this.name = name;

        size = context.getResources().getDimension(R.dimen.item_size);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = InMeProgressDialog.instance(context);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AsyncShowTodayMessage.this.cancel(true);

            }
        });
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected List<Map.Entry<String, Integer>> doInBackground(List<String>... params) {

        List<String> list = params[0];
        if (list != null) {
            int rowCount = list.size();
            //将文本字符串拆分成文本字符串数组
            items = new String[rowCount][4];//4列，根据短信，通话记录确定
            for (int i = 0; i < rowCount; i++) {
                items[i] = list.get(i).split("-;");
            }

            //分别根据姓名ID与手机号查询对应的姓名
            HashSet<String> ids = new HashSet<String>();
            HashSet<String> nums = new HashSet<String>();

            for (int i = 0; i < rowCount; i++) {
                String name = items[i][0];
                if (name == null || "null".equalsIgnoreCase(name) || "".equals(name)) {
                    nums.add(items[i][1]);
                } else {
                    if (isNoSignInteger(name)) {
                        ids.add(name);
                    }
                }
            }

            HashMap<String, String> id_name = PhoneUtils.queryNameById(context, ids.toArray(new String[0]));
            HashMap<String, String> num_name = PhoneUtils.queryNameByNum(context, nums.toArray(new String[0]));

            final HashMap<String, Integer> nameCount = new HashMap<String, Integer>();
            for (int i = 0; i < rowCount; i++) {
                String name = items[i][0];
                if (name == null || "null".equalsIgnoreCase(name) || "".equals(name)) {
                    String number = items[i][1];
                    if (!number.startsWith("-")) {
                        if (num_name != null) {
                            String nickname = getNameByNum(num_name, number);
                            if (nickname != null) {
                                number = nickname;
                            }
                        }
                        //最后根据短信内容中关键字判断发信人
                        if (isNumber(number)) {
                            String content = items[i][3];

                            int index = content.lastIndexOf("【");
                            if (index != -1) {
                                String str = content.substring(index + 1, content.lastIndexOf("】"));
                                if (!"".equals(str)) number = str;
                            } else {
                                index = content.lastIndexOf("[");
                                if (index != -1) {
                                    String str = content.substring(index + 1, content.lastIndexOf("]"));
                                    if (!"".equals(str)) number = str;
                                }
                            }
                        }
                    } else {//对于电话号码为负数的情况
                        number = "私人号码";
                        items[i][1] = "私人号码";
                    }
                    items[i][0] = number;//为了方便后面比较，直接从索引0取值，且保证了值与nameCount中一致
                    nameCount.put(number, (nameCount.get(number) == null ? 0 : nameCount.get(number)) + 1);
                } else {
                    //如果为ID，则根据ID查询姓名
                    if (isNoSignInteger(name)) {
                        if (id_name != null) {
                            String nickname = id_name.get(name);
                            if (nickname != null) {
                                name = nickname;
                            }
                        }
                        //如果仍为ID号，则显示手机号，并尽可能查询到姓名
                        if (isNoSignInteger(name)) {
                            String number = items[i][1];
                            HashMap<String, String> nameMap = PhoneUtils.queryNameByNum(context, number);
                            if (nameMap != null) {
                                String name2 = getNameByNum(nameMap, number);
                                //如果id_name为空则创建新的
                                if (id_name == null) id_name = new HashMap<String, String>();
                                id_name.put(name, name2);
                                name = name2;
                            }
                            if (isNoSignInteger(name)) name = number;
                        }
                    } else {
                        if (num_name == null) num_name = new HashMap<String, String>();
                        num_name.put(items[i][1], name);//如果姓名不为ID号，则按电话号码存入num_name集合
                    }
                    items[i][0] = name;
                    nameCount.put(name, (nameCount.get(name) == null ? 0 : nameCount.get(name)) + 1);
                }
            }

            final List<Map.Entry<String, Integer>> name_count_layout_list = new ArrayList<Map.Entry<String, Integer>>();
            for (Map.Entry<String, Integer> entry : nameCount.entrySet()) {
                name_count_layout_list.add(entry);
            }
            //两级排序，先汉字(又分在通讯录中和不在通讯录中，在则前)，后数量(先大后小)
            Collections.sort(name_count_layout_list, new Comparator<Map.Entry<String, Integer>>() {

                private HashSet<String> nameSet = new HashSet<String>();

                public Comparator<Map.Entry<String, Integer>> setSet(HashMap<String, String> map1, HashMap<String, String> map2) {
                    if (map1 != null) {
                        for (Map.Entry<String, String> entry : map1.entrySet()) {
                            nameSet.add(entry.getValue());
                        }
                    }
                    if (map2 != null) {
                        for (Map.Entry<String, String> entry : map2.entrySet()) {
                            nameSet.add(entry.getValue());
                        }
                    }
                    return this;
                }

                @Override
                public int compare(Map.Entry<String, Integer> lhs, Map.Entry<String, Integer> rhs) {
                    final String name1 = lhs.getKey();
                    final String name2 = rhs.getKey();

                    //判断是否为电话号码
                    if (isNumber(name1)) {
                        if (isNumber(name2)) {
                            Integer value1 = lhs.getValue();
                            Integer value2 = rhs.getValue();
                            if (value1 > value2) return -1;
                            else if (value1 < value2) return 1;
                            else return 0;
                        } else {
                            return 1;
                        }
                    } else if (isNumber(name2)) {
                        return -1;
                    } else {
                        //对于都有姓名情况，则判断在没有在通讯录中，在则在前
                        if (nameSet.contains(name1)) {
                            if (nameSet.contains(name2)) {
                                Integer value1 = lhs.getValue();
                                Integer value2 = rhs.getValue();
                                if (value1 > value2) return -1;
                                else if (value1 < value2) return 1;
                                else return 0;
                            } else return -1;
                        } else if (nameSet.contains(name2)) return 1;
                        else {
                            Integer value1 = lhs.getValue();
                            Integer value2 = rhs.getValue();
                            if (value1 > value2) return -1;
                            else if (value1 < value2) return 1;
                            else return 0;
                        }

                    }
                }
            }.setSet(id_name, num_name));

            return name_count_layout_list;
        }

        return null;
    }

    //判断是否为正整数
    public boolean isNoSignInteger(String str) {
        return Pattern.compile("^[0-9]*$").matcher(str).matches();
    }

    //判断是否为纯数字
    public boolean isNumber(String str) {
        return Pattern.compile("^[+-]?[0-9]*$").matcher(str).matches();
    }

    @Override
    protected void onPostExecute(final List<Map.Entry<String, Integer>> name_count_layout_list) {

        if (name_count_layout_list == null) return;

        //使用ListView显示
        final ListView listView = new ListView(context);

        int color2 = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));
        int color = Color.argb(153, Color.red(color2), Color.green(color2), Color.blue(color2));

        listView.setDivider(new ColorDrawable(color));
        listView.setDividerHeight(3);
        listView.setAdapter(new BaseAdapter() {

            @Override
            public int getCount() {
                return name_count_layout_list.size();
            }

            @Override
            public Object getItem(int position) {
                return name_count_layout_list.get(position);

            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return super.getDropDownView(position, convertView, parent);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Map.Entry<String, Integer> entry = name_count_layout_list.get(position);
                final ViewFlipper flipper = new ViewFlipper(context);
                flipper.setMinimumHeight(150);
                //添加首页
                LinearLayout title_page = new LinearLayout(context);

                LinearLayout name_layout = new LinearLayout(context);
                name_layout.setOrientation(LinearLayout.VERTICAL);
                name_layout.setLayoutParams(params1);

                TextView nameView = new TextView(context);
                String name = entry.getKey();
                nameView.setText(name);
                nameView.setTextSize(size + 5);
                nameView.setGravity(Gravity.CENTER);
                nameView.setLayoutParams(params1);
                name_layout.addView(nameView);

                LinearLayout count_layout = new LinearLayout(context);
                count_layout.setLayoutParams(params2);
                count_layout.setGravity(Gravity.CENTER);
                params2.rightMargin = 10;

                final Integer count = entry.getValue();
                TextView count_textView = new TextView(context);
                count_textView.setText(String.valueOf(count));
                count_textView.setTextSize(size + 2);
                count_textView.setMinWidth(50);
                count_textView.setMinHeight(50);
                count_textView.setGravity(Gravity.CENTER);
                count_textView.setPadding(5, 5, 5, 5);
                count_textView.setBackgroundResource(R.drawable.homepage_item_message_count);
                count_layout.addView(count_textView);

                title_page.addView(name_layout);
                title_page.addView(count_layout);

                flipper.addView(title_page);

                int sum_duration = 0;
                //添加后面的详细信息
                int len = items.length;
                for (int i = 0; i < len; i++) {
                    if (name.equals(items[i][0])) {
                        flipper.addView(createEmptySmsItem(i));
                        if (childposition == 0) {
                            sum_duration += Integer.valueOf(items[i][3]);
                        }
                    }
                }

                //如果为通话信息
                if (childposition == 0) {
                    nameView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                    TextView sum_duration_textView = new TextView(context);
                    sum_duration_textView.setText("(总时长:" + getDuration(sum_duration) + ")");
                    sum_duration_textView.setTextColor(Color.GRAY);
                    sum_duration_textView.setLayoutParams(params1);
                    sum_duration_textView.setTextSize(size - 3);
                    sum_duration_textView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    name_layout.addView(sum_duration_textView);
                }

                flipper.setOnTouchListener(new View.OnTouchListener() {
                    private float startX = 0;
                    private int index = 0;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = event.getX();
                                break;
                            case MotionEvent.ACTION_UP:
                                if (event.getX() > startX) { // 向右滑动前一个
                                    flipper.setInAnimation(AnimationUtils.makeInAnimation(context, true));
                                    flipper.setOutAnimation(AnimationUtils.makeOutAnimation(context, true));
                                    flipper.showPrevious();

                                    index--;
                                    if (index == -1) index = count;
                                    loadSmsItem(flipper, index, count, true);
                                } else if (event.getX() < startX) { // 向左滑动后一个
                                    flipper.setInAnimation(AnimationUtils.makeInAnimation(context, false));
                                    flipper.setOutAnimation(AnimationUtils.makeOutAnimation(context, false));
                                    flipper.showNext();

                                    index++;
                                    if (index > count) index = 0;
                                    loadSmsItem(flipper, index, count, false);
                                }
                                break;
                            default:
                                return false;
                        }

                        return true;
                    }
                });
                return flipper;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setMinimumHeight(80);

        TextView title = new TextView(context);
        title.setText(name);
        title.setTextSize(size + 5);
        title.setGravity(Gravity.CENTER | Gravity.LEFT);
        title.setTextColor(context.getResources().getColor(R.color.homepage_list_header));
        title.setLayoutParams(params1);

        TextView tip = new TextView(context);
        tip.setText("温馨提示:联系人可左右滑动哦");
        tip.setLayoutParams(params3);
        params3.rightMargin = 10;
        tip.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        tip.setTextSize(size - 3);
        tip.setTextColor(Color.GRAY);

        header.addView(title);
        header.addView(tip);
        builder.setCustomTitle(header)
                .setView(listView)
                .create()
                .show();

        //关闭进度对话框
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void loadSmsItem(ViewFlipper flipper, int index, int count, boolean rightflip) {

        LinearLayout item = (LinearLayout) flipper.getCurrentView();
        Object object = item.getTag();
        if (object != null) {
            if (item.getChildCount() == 0) {
                BottomBorderLinearLayout header = new BottomBorderLinearLayout(context, null);
                header.setOrientation(LinearLayout.HORIZONTAL);
                header.setBackgroundColor(Color.TRANSPARENT);
                header.setColor(Color.LTGRAY);
                header.setWidth(1);
                header.setPadding(0, 10, 5, 5);

                int i = Integer.valueOf((String) object);
                TextView number_textView = new TextView(context);
                number_textView.setText(items[i][1]);
                number_textView.setLayoutParams(params1);
                number_textView.setTextSize(size);

                TextView date = new TextView(context);
                date.setText(items[i][2]);
                date.setLayoutParams(params2);
                date.setGravity(Gravity.RIGHT);
                date.setTextSize(size);
                date.setTextColor(Color.GRAY);

                header.addView(number_textView);
                header.addView(date);

                item.addView(header);

                TextView content = new TextView(context);
                if (childposition == 1) {
                    if (items[i].length > 3) {
                        content.setText(items[i][3]);
                        content.setPadding(5, 10, 5, 10);
                        content.setTextSize(size + 1);
                    } else {
                        content.setText("");
                    }
                } else if (childposition == 0) {
                    int duration = Integer.valueOf(items[i][3]);
                    String duration_text = getDuration(duration);
                    content.setText(duration_text);
                    content.setTextSize(size + 5);
                    content.setPadding(5, 10, 5, 10);
                    content.setLayoutParams(params3);
                    content.setGravity(Gravity.CENTER);
                }
                item.addView(content);

                LinearLayout footer = new LinearLayout(context);
                footer.setGravity(Gravity.RIGHT);

                String name = items[i][0];
                if (isNumber(name)) name = "陌生号";
                TextView name_textView = new TextView(context, null);
                name_textView.setText(name + "(" + index + "/" + count + ")");
                name_textView.setTextSize(size - 2);
                name_textView.setPadding(5, 5, 5, 2);
                name_textView.setTextColor(Color.GRAY);


                footer.addView(name_textView);
                item.addView(footer);
            }
        }

        //删除当前内容
        int childIndex = flipper.getDisplayedChild();
        if (rightflip) {
            ++childIndex;
            if (childIndex == count + 1) childIndex = 0;
        } else {
            --childIndex;
            if (childIndex == -1) childIndex = count;
        }

        LinearLayout old_item = (LinearLayout) flipper.getChildAt(childIndex);
        if (old_item != null && old_item.getTag() != null) {
            old_item.removeAllViews();
        }
    }

    public String getDuration(int duration) {
        int hour = 0;
        int minute = 0;
        int second = 0;

        if (duration > 3600) {
            hour = duration / 3600;
            int mod = duration % 3600;
            if (mod > 60) {
                minute = mod / 60;
                second = mod % 60;
            }
        } else if (duration > 60) {
            minute = duration / 60;
            second = duration % 60;
        } else second = duration;

        String duration_text = "";
        if (hour > 0) duration_text = hour + "时";
        if (minute > 0) duration_text = duration_text + minute + "分";
        if (second > 0) duration_text = duration_text + second + "秒";
        if ("".equals(duration_text)) duration_text = "0秒";
        return duration_text;
    }

    private LinearLayout createEmptySmsItem(int i) {
        LinearLayout item_layout = new LinearLayout(context);
        item_layout.setOrientation(LinearLayout.VERTICAL);
        item_layout.setTag(String.valueOf(i));
        return item_layout;
    }

    /**
     * 在Map中根据电话号码，取姓名
     *
     * @param nameMap
     * @param number
     * @return
     */
    private String getNameByNum(HashMap<String, String> nameMap, String number) {
        String name = getName_86(nameMap, number);
        if (name == null || "".equals(name)) {
            name = getName_blank(nameMap, number);
        }
        return name;
    }

    /**
     * 选直接查询，找不到，通过增加或减去+86前缀查询
     *
     * @param nameMap
     * @param number
     * @return
     */
    private String getName_86(HashMap<String, String> nameMap, String number) {
        String name = nameMap.get(number);
        //首先通过变换前置的+86前缀来查询
        if (name == null || "".equals(name)) {
            if (number.startsWith("+86")) {
                name = nameMap.get(number.substring(3));
            } else if (number.length() == 11) {
                name = nameMap.get("+86" + number);
            }
        }
        return name;
    }

    /**
     * 去年空格后再查询
     *
     * @param nameMap
     * @param number
     * @return
     */
    private String getName_blank(HashMap<String, String> nameMap, String number) {
        String name = null;
        int index = number.indexOf(" ");
        if (index != -1) {
            String num = number.replaceAll(" ", "");
            name = getName_86(nameMap, num);
        }
        return name;
    }

}
