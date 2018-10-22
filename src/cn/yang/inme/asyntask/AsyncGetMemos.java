package cn.yang.inme.asyntask;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import cn.yang.inme.MainActivity;
import cn.yang.inme.R;
import cn.yang.inme.activity.homepage.MemoActivity;
import cn.yang.inme.activity.tabs.HomePageActivity2;
import cn.yang.inme.bean.Memo;
import cn.yang.inme.sqlite.MemoDBManager;
import cn.yang.inme.utils.*;
import cn.yang.inme.utils.border.BorderLinearLayout;
import cn.yang.inme.utils.border.TopBorderLinearLayout;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 14-10-24.
 */
public class AsyncGetMemos extends AsyncTask<Integer,Void,ArrayList<View>> {

    private Context mContext;
    //备忘录列数
    private int memo_cols;
    private String[] todo = new String[]{""};
    //ActionMode对象
//   private ActionMode mActionMode;
    //手机工具类对象
    private PhoneUtils phoneUtils;

    //存放将要删除的memoID
    private ArrayList<String> preDele_memoID = new ArrayList<String>();

    private Logger log = Logger.getLogger(AsyncGetMemos.class);

    private InMeProgressBar progressBar;
    /**
     * 最终生成的View
     */
    private LinearLayout item;

    public AsyncGetMemos(Context context,LinearLayout view){
        this.mContext=context;
        this.item=view;

        //得到屏幕的宽度与高度
        Display display = ((HomePageActivity2) mContext).getWindowManager().getDefaultDisplay();
        //得到列数
        memo_cols = Math.round((float) (display.getWidth()) / Constants.HOMEPAGE_MEMO_WIDTH);
        Constants.HOMEPAGE_MEMO_WIDTH = display.getWidth() / memo_cols;

        initdata();

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                400,
                180);
        progressBar = new InMeProgressBar(context);
        view.setGravity(Gravity.CENTER);
        view.addView(progressBar,params1);
    }

    @Override
    protected ArrayList<View> doInBackground(Integer... params) {
        int groupPosition=params[0];

        float size = mContext.getResources().getDimension(R.dimen.item_size);
        int purple = Integer.valueOf(PropertiesUtil.instance().read(Constants.SET_THEME_COLOR));
        ArrayList<View> memos_list = new ArrayList<View>();

        switch (groupPosition) {
            case 0: {//今日待办
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT
                        , LinearLayout.LayoutParams.WRAP_CONTENT);//每个待办事项及提醒栏
                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT
                        , LinearLayout.LayoutParams.MATCH_PARENT,1);//待办内容
                params3.topMargin=10;
                params3.leftMargin=10;

                String strs = todo[0];
                if (strs != null && !"".equals(strs)) {
                    try {
                        JSONArray todos = new JSONArray(strs);
                        int len = todos.length();
                        if (len > 0) {
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
                                memo_layout.setBackgroundResource(R.drawable.memo_item_border);
                                memo_layout.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                                params2.setMargins(8, 15, 8, 15);
                                memo_layout.setLayoutParams(params2);
                                memo_layout.setMinimumHeight(150);
                                memo_layout.setTag(R.string.memoId, id);
                                memo_layout.setTag(id);
                                //注册长按事件
                                memo_layout.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        if (Constants.mActionMode != null) {
                                            return false;
                                        }
                                        // Start the Contextual Action Bar using the ActionMode.Callback defined above
                                        Constants.mActionMode = ((HomePageActivity2) mContext).getParent()
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
                                        if (Constants.mActionMode != null) {
                                            //第一次
                                            if (v.isSelected()) {
                                                v.setSelected(false);
                                                deleteOverLay(v);
                                                preDele_memoID.remove(v.getTag(R.string.memoId));
                                                if (preDele_memoID.size() == 0) {
                                                    Constants.mActionMode.finish();
                                                    Constants.mActionMode = null;
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
                                            ((HomePageActivity2) mContext).startActivityForResult(intent, 10);
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
                                    remind.setBackgroundColor(Color.TRANSPARENT);
                                    remind.setPadding(2,0,0,5);
                                    remind.setWidth(3);
                                    remind.setColor(purple);
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

                                memos_list.add(memo_layout);
                            }
                        } else {
                            TextView textView = new TextView(mContext);
                            textView.setText(mContext.getResources().getString(R.string.no_memo_msg));
                            textView.setTextSize(size);
                            textView.setBackgroundResource(R.drawable.toast_item_border);
                            textView.setGravity(Gravity.CENTER);
                            textView.setPadding(20, 15, 20, 15);
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, MemoActivity.class);
                                    ((HomePageActivity2) mContext).startActivityForResult(intent, 10);
                                }
                            });

                            memos_list.add(textView);
                        }
                    } catch (JSONException e) {
                        log.error(e.getMessage());
                    }
                }
            }
            break;
            case 1://今日消息
            {
              //读取短信通话记录
                PhoneUtils phoneUtils = new PhoneUtils(mContext);
                phoneUtils.getCallogInPhone();
                phoneUtils.getSmsInPhone();

                HashMap<String,List<String>> smsMap =phoneUtils.getSmsMap();
                HashMap<String,List<String>> callMap=phoneUtils.getCalllogMap();

                HashSet<String> phoneNumbers=phoneUtils.getHistoryNumbers();

/*                int count=0;
                int index=0;
                count=smsMap.values().size()+callMap.values().size();
                String[][] items=new String[count][4];

                //将通话记录拆分放到数组里
                count=0;
                index=0;
                for(Map.Entry<String,List<String>> entry:smsMap.entrySet()){
                    List<String> values=entry.getValue();
                    if (values!=null){
                        count=values.size();
                        for (int i = 0; i < count; i++) {
                            items[index++] = values.get(i).split("-;");
                        }
                    }
                }
                for(Map.Entry<String,List<String>> entry:callMap.entrySet()){
                    List<String> values=entry.getValue();
                    if (values!=null){
                        count=values.size();
                        for (int i = 0; i < count; i++) {
                            items[index++] = values.get(i).split("-;");
                        }
                    }
                }*/






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
        return memos_list;
    }

    @Override
    protected void onPostExecute(ArrayList<View> result) {
        //关闭进度条
        if(progressBar!=null&&progressBar.isShown()){
            progressBar.dismiss();
            item.removeView(progressBar);
            item.setGravity(Gravity.TOP);
        }

        //如果没有备忘录
        if (result.size()==1){
            View v= result.get(0);
            if( v instanceof TextView ){
                item.setGravity(Gravity.CENTER);
                item.addView(v);
                return;
            }
        }

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                Constants.HOMEPAGE_MEMO_WIDTH
                , ViewGroup.LayoutParams.MATCH_PARENT);//每一列

        //显示结果
        //创建每一列的布局(为垂直布局)
        for (int i = 0; i < memo_cols; i++) {
            LinearLayout col_layout = new LinearLayout(mContext);
            col_layout.setOrientation(LinearLayout.VERTICAL);
            col_layout.setLayoutParams(params1);
            col_layout.setGravity(Gravity.LEFT);
            item.addView(col_layout);
        }

        Iterator<View> iterator=result.iterator();
        while(iterator.hasNext()){
            for(int i=0;i<memo_cols;i++){
                ((LinearLayout)item.getChildAt(i)).addView(iterator.next());
                if (!iterator.hasNext())break;
            }
        }
    }

    /**
     * 初始化数据
     */
    public void initdata(){
        //获取待办事项
       // new Thread() {
       //     @Override
       //     public void run() {
                phoneUtils = new PhoneUtils(mContext);
                JSONArray jsonArray = phoneUtils.getTodayMemo();
                todo[0] = jsonArray.toString();
        //    }
        //}.start();
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


    /**
     * 工具栏
     */
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

                            //刷新列表
                            ((HomePageActivity2)mContext).getAdapter().refresh(0);
                            //更新待办列表
                            //todo[0] = newdos.toString();
                            /*itemList.get(0).set(0, todo[0]);*/
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
            int count=preDele_memoID.size();
            for(int i = 0;i<count;i++){
                View v=item.findViewWithTag(preDele_memoID.get(i));
                if (v!=null)deleteOverLay(v);
            }
            //初始化
            preDele_memoID.clear();
            Constants.mActionMode = null;
        }
    };
}
