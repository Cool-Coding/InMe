package cn.yang.inme.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import cn.yang.inme.R;
import cn.yang.inme.activity.homepage.MemoActivity;
import cn.yang.inme.activity.tabs.HomePageActivity;
import cn.yang.inme.asyntask.AsyncShowTodayMessage;
import cn.yang.inme.bean.Memo;
import cn.yang.inme.bean.UserLocation;
import cn.yang.inme.sqlite.LocationDBManager;
import cn.yang.inme.sqlite.MemoDBManager;
import cn.yang.inme.utils.*;
import cn.yang.inme.utils.border.BorderLinearLayout;
import cn.yang.inme.utils.border.TopBorderLinearLayout;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 14-6-27.
 */
public class HomePageTodayMassExpandableListAdapter extends BaseExpandableListAdapter implements SpinnerUtil.OnSpinnerChanged {

    private final int message_count = 2;
    private Context mContext = null;
    // 测试数据，开发时可能来自数据库，网络....
//    private String[] groups = { "今日待办", "今日消息", "今日路径" };
    private String[] groups = {"待办", "消息"};
    private String[] todo = new String[]{""};
    private String[] messagear = new String[]{"", ""};
    //    private String[] route = {"[{latitude=31.238068,longitude=121.501654},{latitude=39.989614,longitude=116.481763},{latitude=30.679879,longitude=104.064855}]"};
    private String[] route = new String[]{""};

    private List<String> groupList = null;
    private List<ArrayList<CharSequence>> itemList = null;
    //今日消息详细信息
    private List<Map<String, List<String>>> msgMapList;
    //备忘录列数
    private int memo_cols;
    //手机工具类对象
    private PhoneUtils phoneUtils;
    //ActionMode对象
    private ActionMode mActionMode;
    //存放将要删除的memoID
    private ArrayList<String> preDele_memoID = new ArrayList<String>();
    //今日路径中的mapView
    private MapView mapView;
    private LinearLayout mapView_layout;
    private MapUtils mapUtils;

    /**
     * 线程池
     */
    private ExecutorService transThread = Executors.newSingleThreadExecutor();
    private Future transPending;

    private Logger log = Logger.getLogger(HomePageTodayMassExpandableListAdapter.class);


    public HomePageTodayMassExpandableListAdapter(Context context) {
        this.mContext = context;
        groupList = new ArrayList<String>();
        itemList = new ArrayList<ArrayList<CharSequence>>();
        msgMapList = new ArrayList<Map<String, List<String>>>();

        //得到屏幕的宽度与高度
        Display display = ((HomePageActivity) mContext).getWindowManager().getDefaultDisplay();
        //得到列数
        memo_cols = Math.round((float) (display.getWidth()) / Constants.HOMEPAGE_MEMO_WIDTH);
        Constants.HOMEPAGE_MEMO_WIDTH = display.getWidth() / memo_cols;
        //初始化数据
        initData();
    }

    /**
     * 初始化数据，将相关数据放到List中，方便处理
     */
    private void initData() {

        int len = groups.length;
        for (int i = 0; i < len; i++) {
            groupList.add(groups[i]);
        }

        ArrayList<CharSequence> item1 = new ArrayList<CharSequence>();
        len = todo.length;
        for (int i = 0; i < len; i++) {
            item1.add(todo[i]);
        }
        itemList.add(item1);

        len = messagear.length;
        ArrayList<CharSequence> item2 = new ArrayList<CharSequence>();
        for (int i = 0; i < len; i++) {
            item2.add(messagear[i]);
        }
        itemList.add(item2);

        len = route.length;
        ArrayList<CharSequence> item3 = new ArrayList<CharSequence>();
        for (int i = 0; i < len; i++) {
            item3.add(route[i]);
        }
        itemList.add(item3);

        //获取待办事项
        new Thread() {
            @Override
            public void run() {
                phoneUtils = new PhoneUtils(mContext);
                JSONArray jsonArray = phoneUtils.getTodayMemo();
                todo[0] = jsonArray.toString();
                ArrayList<CharSequence> item1 = new ArrayList<CharSequence>();
                for (int i = 0; i < todo.length; i++) {
                    item1.add(todo[i]);
                }
                itemList.set(0, item1);
            }
        }.start();

/*        //获得通话记录和短信
        getMessaage();
        //获取路径
        getPath();*/
    }

    /**
     * 清空消息
     */
    public void clearMessage() {
        messagear = new String[]{"", ""};
        int len = messagear.length;
        ArrayList<CharSequence> item2 = new ArrayList<CharSequence>();
        for (int i = 0; i < len; i++) {
            item2.add(messagear[i]);
        }
        itemList.set(1, item2);
        if (msgMapList.size() == 2) {
            msgMapList.remove(0);
            msgMapList.remove(0);
        }
    }

    /**
     * 清空路径
     */
    public void clearPath() {
        route = new String[]{""};
        int len = route.length;
        ArrayList<CharSequence> item3 = new ArrayList<CharSequence>();
        for (int i = 0; i < len; i++) {
            item3.add(route[i]);
        }
        itemList.set(2, item3);
    }

    public void getMessaage() {

        if (transPending != null) transPending.cancel(true);
        //获得通话记录和短信
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                //清除原消息内容
                clearMessage();

                phoneUtils = new PhoneUtils(mContext);
                //phoneUtils.clearMessageMap();

                phoneUtils.getCallogInPhone();
                messagear[0] = phoneUtils.getCalllogjson();
                msgMapList.add(phoneUtils.getCalllogMap());

                //获得短信
                phoneUtils.getSmsInPhone();
                messagear[1] = phoneUtils.getSmsjson();
                msgMapList.add(phoneUtils.getSmsMap());

                int len = messagear.length;
                ArrayList<CharSequence> item2 = new ArrayList<CharSequence>();
                for (int i = 0; i < len; i++) {
                    item2.add(messagear[i]);
                }
                itemList.set(1, item2);
            }
        };

        transPending = transThread.submit(thread);
    }

    public void getPath() {
        clearPath();
        //获取路径
        Thread thread = new Thread() {
            @Override
            public void run() {
                LocationDBManager db = new LocationDBManager(mContext);
                List<UserLocation> userLocations = db.queryByIds(1);
                db.closeDB();

                JSONArray jsonArray = new JSONArray();
                for (UserLocation userLocation : userLocations) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("latitude", userLocation.latitude);
                        jsonObject.put("longitude", userLocation.longitude);
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {

                    }
                }
                if (jsonArray.length() > 0) {
                    route[0] = jsonArray.toString();
                }
                ArrayList<CharSequence> item3 = new ArrayList<CharSequence>();
                for (int i = 0; i < route.length; i++) {
                    item3.add(route[i]);
                }
                itemList.set(2, item3);
            }
        };
        thread.start();
    }

    @Override
    public void change(String frequency) {
        if (Constants.MESSAGE_FREQUENCY.equals(frequency)) {
            getMessaage();
            ExpandableListView listView = ((HomePageActivity) mContext).getListView();
            if (listView.isGroupExpanded(1)) {
                listView.collapseGroup(1);
            }
            listView.expandGroup(1);
        } else if (Constants.PATH_LOCATION_FREQUENCY.equals(frequency)) {
            getPath();
        }
    }

    /**
     * 将新增加的Memo添加到今日待办列表中
     *
     * @param str
     */
    public void addNewMemo(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            String id = jsonObject.getString("_id");
            boolean exist = false;
            JSONArray jsonArray = new JSONArray(todo[0]);
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                if (jsonObject1.getString("_id").equals(id)) {
                    jsonObject1.put("action", jsonObject.getString("action"));
                    jsonObject1.put("time", jsonObject.getString("time"));
                    jsonObject1.put("color", jsonObject.getString("color"));
                    exist = true;
                    break;
                }
            }
            if (!exist) jsonArray.put(jsonObject);
            todo[0] = jsonArray.toString();
            ArrayList<CharSequence> item1 = new ArrayList<CharSequence>();
            for (int i = 0; i < todo.length; i++) {
                item1.add(todo[i]);
            }
            itemList.set(0, item1);
        } catch (JSONException e) {

        }
        notifyDataSetChanged();
    }

    /**
     * 删除今日待办中的备忘录
     *
     * @param str
     */
    public void deleteMemo(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            String id = jsonObject.getString("_id");

            JSONArray jsonArray = new JSONArray(todo[0]);
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                if (jsonObject1.getString("_id").equals(id)) {
                    jsonArray.put(i, null);
                    break;
                }
            }
            todo[0] = jsonArray.toString();
            ArrayList<CharSequence> item1 = new ArrayList<CharSequence>();
            for (int i = 0; i < todo.length; i++) {
                item1.add(todo[i]);
            }
            itemList.set(0, item1);
        } catch (JSONException e) {

        }
        notifyDataSetChanged();
    }

    /**
     * 添加信息
     *
     * @param msg
     */
    public void setMessage(Message msg) {
        PhoneUtils message = (PhoneUtils) msg.obj;
        if (message == null) {
            log.error("响应" + (msg.what == 1 ? "短信" : "通话") + "事件后，取到的是空值");
            return;
        }

        switch (msg.what) {
            case 1: {
                List<CharSequence> message_list = itemList.get(1);//得到今日消息List
                if (message_list != null) {
                    messagear[1] = message.getSmsjson();
                    message_list.set(1, messagear[1]);
                    msgMapList.set(1, message.getSmsMap());
                }
            }
            break;
            case 2: {
                List<CharSequence> message_list = itemList.get(1);//得到今日消息List
                if (message_list != null) {
                    messagear[0] = message.getCalllogjson();
                    message_list.set(0, messagear[0]);
                    msgMapList.set(0, message.getCalllogMap());
                }
            }
            break;
        }

    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        BorderLinearLayout group;
        if (convertView == null) {
            group = new BorderLinearLayout(mContext);
            group.setBottomBorder(Color.LTGRAY, 3);
            group.setBackgroundColor(Color.WHITE);
            group.postInvalidate();

            if (groupPosition == 2) {
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT, 1);
                group.setLayoutParams(lp);
                return group;
            }

            String name = groupList.get(groupPosition);
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            group.setLayoutParams(lp);
            group.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT
                    , LinearLayout.LayoutParams.MATCH_PARENT, 1);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT
                    , LinearLayout.LayoutParams.MATCH_PARENT);

            final TextView text = new TextView(mContext);
            text.setTextSize(mContext.getResources().getDimension(R.dimen.group_size));
            text.setPadding(10, 20, 0, 20);
            text.setLayoutParams(params1);
            text.setText(name);

            int color = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

            text.setTextColor(color);
            text.setAlpha(0.8f);
            group.addView(text);

            switch (groupPosition) {
                case 0:
                    ImageView create = new ImageView(mContext);
                    params2.rightMargin = 10;
                    create.setLayoutParams(params2);
                    create.setImageResource(R.drawable.ic_action_newmemo);
                    group.addView(create);
                    create.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, MemoActivity.class);
                            ((HomePageActivity) mContext).startActivityForResult(intent, 10);
                        }
                    });
                    break;
                case 1:
                    SpinnerUtil spinnerUtil = new SpinnerUtil(Constants.MESSAGE_FREQUENCY, mContext);
                    Spinner spinner = spinnerUtil.getSpinner();
                    spinnerUtil.setOnSpinnerChanged(HomePageTodayMassExpandableListAdapter.this);

                    params2.rightMargin = 10;
                    spinner.setLayoutParams(params2);

/*                LinearLayout spinnerParent=(LinearLayout)spinner.getParent();
                if(spinnerParent!=null)spinnerParent.removeView(spinner);*/
                    group.addView(spinner);
                    group.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ExpandableListView listView = ((HomePageActivity) mContext).getListView();
                            if (listView.isGroupExpanded(1)) listView.collapseGroup(groupPosition);
                            else listView.expandGroup(groupPosition);
                        }
                    });
                    break;
                default:
            }
        } else group = (BorderLinearLayout) convertView;
        return group;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        LinearLayout item;
        AbsListView.LayoutParams params0 = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT);
        item = new LinearLayout(mContext);
        item.setLayoutParams(params0);
        item.setBackgroundResource(R.drawable.tuangou_background_border);
        item.setAlpha(0.88f);

        float size = mContext.getResources().getDimension(R.dimen.item_size);
        int purple = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));

        switch (groupPosition) {
            case 0: {//今日待办
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                        Constants.HOMEPAGE_MEMO_WIDTH
                        , ViewGroup.LayoutParams.MATCH_PARENT);//每一列
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT
                        , LinearLayout.LayoutParams.WRAP_CONTENT);//每个待办事项及提醒栏
                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT
                        , LinearLayout.LayoutParams.MATCH_PARENT, 1);//待办内容

                String strs = (String) itemList.get(groupPosition).get(childPosition);
                if (strs != null && !"".equals(strs)) {
                    try {

                        JSONArray todos = new JSONArray(strs);
                        int len = todos.length();
                        if (len > 0) {
                            //创建每一列的布局(为垂直布局)
                            ArrayList<LinearLayout> memos_list = new ArrayList<LinearLayout>();
                            for (int i = 0; i < memo_cols; i++) {
                                LinearLayout col_layout = new LinearLayout(mContext);
                                col_layout.setOrientation(LinearLayout.VERTICAL);
                                col_layout.setLayoutParams(params1);
                                col_layout.setGravity(Gravity.LEFT);
                                memos_list.add(col_layout);

                                item.addView(col_layout);
                            }

                            int col_index = 0;

                            for (int i = 0; i < len; i++) {
                                JSONObject todo = todos.optJSONObject(i);
                                if (todo == null) continue;
                                String id = todo.getString("_id");
                                String thing = todo.getString("action");
                                String time = todo.getString("time");
                                int color = todo.getInt("color");

                                //每一个备忘录的布局
                                LinearLayout memo_layout = new LinearLayout(mContext);
                                memo_layout.setOrientation(LinearLayout.VERTICAL);
                                memo_layout.setBackgroundColor(color);
                                params2.setMargins(8, 15, 8, 15);
                                memo_layout.setLayoutParams(params2);
                                memo_layout.setMinimumHeight(120);
                                memo_layout.setTag(R.string.memoId, id);
                                //注册长按事件
                                memo_layout.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        if (mActionMode != null) {
                                            return false;
                                        }
                                        // Start the Contextual Action Bar using the ActionMode.Callback defined above
                                        mActionMode = ((HomePageActivity) mContext).getParent()
                                                .startActionMode(mActionModeCallback);
                                        v.setSelected(true);
                                        //添加选择标识
                                        drawOverLay(v);
                                        preDele_memoID.add((String) v.getTag(R.string.memoId));
                                        return true;
                                    }
                                });
                                //注册点击事件
                                memo_layout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mActionMode != null) {
                                            //第一次
                                            if (v.isSelected()) {
                                                v.setSelected(false);
                                                deleteOverLay(v);
                                                preDele_memoID.remove(v.getTag(R.string.memoId));
                                                if (preDele_memoID.size() == 0) {
                                                    mActionMode.finish();
                                                    mActionMode = null;
                                                }
                                            } else {
                                                v.setSelected(true);
                                                drawOverLay(v);
                                                preDele_memoID.add((String) v.getTag(R.string.memoId));
                                            }
                                        } else {
                                            //进入备忘录修改界面
                                            Intent intent = new Intent(mContext, MemoActivity.class);
                                            intent.putExtra(mContext.getResources().getString(R.string.memoId), String.valueOf(v.getTag(R.string.memoId)));
                                            ((HomePageActivity) mContext).startActivityForResult(intent, 10);
                                        }
                                    }
                                });

                                //备忘录内容
                                TextView todo_text = new TextView(mContext);
                                todo_text.setText(thing);
                                todo_text.setTextSize(size + 2);
                                todo_text.setLayoutParams(params3);
                                todo_text.setPadding(10, 10, 5, 20);

                                memo_layout.addView(todo_text);

                                //备忘录提醒时间
                                if (time != null && !"".equals(time.trim())) {
                                    TopBorderLinearLayout remind = new TopBorderLinearLayout(mContext, null);
                                    remind.setBackgroundColor(color);
                                    remind.setWidth(3);
                                    //remind.setColor(purple);
                                    remind.drawDashLine();

                                    ImageView clock = new ImageView(mContext);
                                    clock.setImageResource(R.drawable.ic_action_time);

                                    TextView time_text = new TextView(mContext);
                                    time_text.setText(time);
                                    time_text.setTextSize(size - 1);

                                    remind.addView(clock);
                                    remind.addView(time_text);

                                    memo_layout.addView(remind);
                                }

                                memos_list.get(col_index).addView(memo_layout);
                                if (col_index == memo_cols - 1) col_index = -1;
                                col_index++;
                            }
                        } else {
                            TextView textView = new TextView(mContext);
                            textView.setText(mContext.getResources().getString(R.string.no_memo_msg));
                            textView.setTextSize(size);
                            textView.setLayoutParams(params3);
                            textView.setGravity(Gravity.CENTER);
                            textView.setPadding(0, 30, 0, 30);
                            item.removeAllViews();
                            item.addView(textView);
                        }
                    } catch (JSONException e) {
                        log.error(e.getMessage());
                    }
                }
            }
            break;
            case 1://今日消息
            {
                item.setPadding(30, 10, 20, 10);
                item.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

                ImageView waiting = new ImageView(mContext);
                waiting.setImageResource(R.drawable.webloading);
                // 加载旋转动画
                Animation loading_animation = AnimationUtils.loadAnimation(mContext,
                        R.anim.webloading);
                waiting.startAnimation(loading_animation);
                item.setGravity(Gravity.CENTER);

                TextView textView = new TextView(mContext);
                textView.setText("正在加载" + (childPosition == 0 ? "通话" : "短信") + "状态");

                item.addView(waiting);
                item.addView(textView);
                showMessages(item, groupPosition, childPosition, size, purple);
            }
            break;
            case 2://今日路径
/*            {
                ImageView waiting = new ImageView(mContext);
                waiting.setImageResource(R.drawable.webloading);
                // 加载旋转动画
                Animation loading_animation = AnimationUtils.loadAnimation(mContext,
                        R.anim.webloading);
                waiting.startAnimation(loading_animation);

                item.setGravity(Gravity.CENTER);

                TextView textView=new TextView(mContext);
                textView.setText("正加载路径");

                item.addView(waiting);
                item.addView(textView);

                showPath(item,groupPosition,childPosition);
                }*/
                break;
        }
        return item;
    }

    public MapView getMapView() {
        return mapView;
    }

    /**
     * 添加半透明覆盖层
     *
     * @param v
     */
    public void drawOverLay(View v) {

        LinearLayout linearLayout = (LinearLayout) v.getParent();
        int index = linearLayout.indexOfChild(v);
        linearLayout.removeViewAt(index);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        relativeLayout.addView(v, params);

        BorderLinearLayout layer = new BorderLinearLayout(mContext);
        ColorDrawable drawable = new ColorDrawable(Color.YELLOW);
        drawable.setAlpha(0);
        layer.setBackgroundDrawable(drawable);
        layer.drawOverLayRect(mContext.getResources().getColor(R.color.homepage_list_header), 5, 127);
        relativeLayout.addView(layer, params);

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(8, 5, 8, 5);
        linearLayout.addView(relativeLayout, index, params1);

    }

    /**
     * 去掉半透明覆盖层
     *
     * @param v
     */
    public void deleteOverLay(View v) {
        RelativeLayout relativeLayout = (RelativeLayout) v.getParent();
        LinearLayout item = (LinearLayout) relativeLayout.getParent();
        relativeLayout.removeView(v);

        int index = item.indexOfChild(relativeLayout);
        item.removeViewAt(index);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 5, 8, 5);
        item.addView(v, index, params);
    }

    public boolean ifGoBack() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
            return true;
        }
        return false;
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            // R.menu.contextual 是 contextual action bar 的布局文件， 在 /res/menu/ 文件夹下
            inflater.inflate(R.menu.memo_contextbar_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // 当用户点击 contextual action bar 的 menu item 的时候产生点击事件
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_deleteMemo:
                    if (preDele_memoID.size() > 0) {
                        try {
                            JSONArray todos = new JSONArray(todo[0]);
                            JSONArray newdos = new JSONArray();
                            MemoDBManager db = new MemoDBManager(mContext);

                            //从今日待办中找出要删除的待办
                            int len = todos.length();
                            for (int i = 0; i < len; i++) {
                                JSONObject jsonObject = todos.getJSONObject(i);
                                String id = jsonObject.getString("_id");
                                if (!preDele_memoID.contains(id)) {
                                    newdos.put(jsonObject);
                                } else {
                                    Memo memo = new Memo();
                                    memo._id = Integer.valueOf(id);
                                    db.delete(memo);
                                }
                            }
                            ToastUtil.show(mContext, "删除成功!");
                            db.closeDB();

                            //更新待办列表
                            todo[0] = newdos.toString();
                            itemList.get(0).set(0, todo[0]);
                        } catch (JSONException e) {
                        }
                    }
                    mode.finish(); // 关闭 contextual action bar
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            notifyDataSetChanged();
            mActionMode = null;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    LinearLayout item = (LinearLayout) msg.obj;
                    Bundle bundle = msg.getData();
                    showMessages(item, bundle.getInt("group"), bundle.getInt("child"), bundle.getFloat("size"), bundle.getInt("color"));
                    break;
                }
                case 2: {
                    LinearLayout item = (LinearLayout) msg.obj;
                    Bundle bundle = msg.getData();
                    showPath(item, bundle.getInt("group"), bundle.getInt("child"));
                    break;
                }
            }

        }
    };

    //显示今日路径
    private void showPath(LinearLayout item, final int groupPosition, final int childPosition) {
        String strs = (String) itemList.get(groupPosition).get(childPosition);
        if (strs == null || "".equals(strs)) {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = item;
            Bundle bundle = new Bundle();
            bundle.putInt("group", groupPosition);
            bundle.putInt("child", childPosition);
            msg.setData(bundle);
            handler.sendMessageDelayed(msg, 500);
            return;
        }
        //首先删除等待图片
//        item.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        ((ImageView) item.getChildAt(0)).clearAnimation();
        item.removeAllViews();

        if (strs != null && !"".equals(strs)) {
            if (mapView_layout == null) {
                mapView_layout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.mapview, null);
                mapView = (MapView) mapView_layout.getChildAt(0);
                mapView.onCreate(((HomePageActivity) mContext).getSaveInstanceState());// 此方法必须重写
                mapUtils = new MapUtils(mContext, mapView.getMap());
            }
            try {
                JSONArray locations = new JSONArray(strs);
                int len = locations.length();
                LatLng[] latLngs = new LatLng[len];
                for (int i = 0; i < len; i++) {
                    JSONObject jsonObject = locations.getJSONObject(i);
                    String lat = jsonObject.getString("latitude");
                    String longti = jsonObject.getString("longitude");
                    latLngs[i] = new LatLng(Double.valueOf(lat), Double.valueOf(longti));
                }
                mapUtils.drawPolyLine(latLngs);
            } catch (JSONException e) {
                e.printStackTrace();
                log.error("today-path", e.getCause());
            }
            if (mapView_layout.getParent() != null) ((LinearLayout) mapView_layout.getParent()).removeAllViews();
/*            item.setMinimumHeight(300);
            item.measure(400,400);*/
            item.addView(mapView_layout);
        }
    }

    //显示今日消息
    private void showMessages(LinearLayout item, final int groupPosition, final int childPosition, final float size, int purple) {
        String strs = (String) itemList.get(groupPosition).get(childPosition);
        if (strs == null || "".equals(strs)) {
            Message msg = new Message();
            msg.what = 1;
            msg.obj = item;
            Bundle bundle = new Bundle();
            bundle.putInt("group", groupPosition);
            bundle.putInt("child", childPosition);
            bundle.putFloat("size", size);
            bundle.putInt("color", purple);
            msg.setData(bundle);
            handler.sendMessageDelayed(msg, 500);
            return;
        }
        //首先删除等待图片
        item.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        ((ImageView) item.getChildAt(0)).clearAnimation();
        item.removeAllViews();

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.setMargins(8, 10, 8, 10);

        if (strs != null && !"".equals(strs)) {

            try {
                JSONObject object = new JSONObject(strs);
                String category = object.getString("category");

                TextView category_textView = new TextView(mContext);
                category_textView.setText(category);
                category_textView.getPaint().setFakeBoldText(true);
                category_textView.setTextSize(size + 1);

                item.addView(category_textView);

                //对于通话与短信，读取接收，发送，未读以及未接，已接，拨打数量
                JSONArray details = object.getJSONArray("details");
                int len = details.length();
                for (int i = 0; i < len; i++) {
                    JSONObject message = details.getJSONObject(i);
                    final String name = message.getString("name");
                    String count = message.getString("count");

                    LinearLayout cell = new LinearLayout(mContext);
                    cell.setLayoutParams(params1);

                    TextView answer = new TextView(mContext);
                    answer.setText(name);

                    answer.setTextColor(purple);
                    answer.setAlpha(0.6f);
                    answer.setTextSize(size - 1);

                    TextView count_textView = new TextView(mContext);
                    count_textView.setText(count);
                    count_textView.setTextSize(size + 1);
                    count_textView.setMinWidth(50);
                    count_textView.setMinHeight(50);
                    count_textView.setPadding(5, 5, 5, 5);
                    count_textView.setGravity(Gravity.CENTER);
                    if (name.startsWith("未") && Integer.valueOf(count) > 0) {
                        count_textView.setBackgroundResource(R.drawable.homepage_item_message_count_red);
                    } else {
                        count_textView.setBackgroundResource(R.drawable.homepage_item_message_count);
                    }

                    cell.addView(answer);
                    cell.addView(count_textView);
                    item.addView(cell);

                    cell.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<String> list = msgMapList.get(childPosition).get(name);
                            if (list != null) {
                                new AsyncShowTodayMessage(mContext, childPosition, name).execute(list);
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return super.getChildType(groupPosition, childPosition);
    }
}
