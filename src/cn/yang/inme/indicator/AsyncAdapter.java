package cn.yang.inme.indicator;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.yang.inme.MainActivity;
import cn.yang.inme.R;
import cn.yang.inme.activity.tabs.HomePageActivity2;
import cn.yang.inme.asyntask.AsyncGetMemos;
import cn.yang.inme.utils.ApplicationUtil;
import cn.yang.inme.utils.InMeProgressBar;
import org.apache.log4j.chainsaw.Main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AsyncAdapter extends BaseAdapter implements TitleProvider {

//    private LayoutInflater mInflater;
    private Context context;
    private static final int dataSize = 3;
    private static String[] dates = new String[dataSize];
    private LinearLayout[] contents=new LinearLayout[dataSize];

    public AsyncAdapter(Context context) {
        this.context=context;
/*        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);*/
        prepareDates();
    }

    @Override
    public LinearLayout getItem(int position) {
        return contents[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return drawView(position, convertView);
    }

    private View drawView(int position, View view) {
        if (view==null) {
            view = new LinearLayout(context);
            LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params0);
        }

        final LinearLayout o = getItem(position);
        if (o != null) {
/*            ((LinearLayout) view).removeAllViews();
             view=o;*/
        } else {
            new AsyncGetMemos(context,(LinearLayout)view).execute(position);
            contents[position]=(LinearLayout)view;
        }
        return view;
    }

    @Override
    public String getTitle(int position) {
        return dates[position];
    }

    @Override
    public int getCount() {
        return dates.length;
    }

    public int getTodayId() {
        return 0;
    }

    /**
     * Prepare dates for navigation, to past and to future
     */
    private void prepareDates() {
        dates[1]="消息";
        dates[0]="待办";
        dates[2]="路径";
    }

    /**
     * 刷新
     * @param position
     */
    public void refresh(int position){
        switch(position){
            case 0:
                System.out.println("refresh");
                LinearLayout o = getItem(position);
                o.removeAllViews();
                //if (o==null)o=new LinearLayout(context);
                new AsyncGetMemos(context,o).execute(position);
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    /**
     * 设置状态栏上的菜单
     * @param position
     */
    public void setActionMenu(int position){
        if(position==0){
            MainActivity mainActivity=(MainActivity)((HomePageActivity2)context).getParent();
            mainActivity.setIs_in_memo(true);
            mainActivity.invalidateOptionsMenu();
        }else{
            MainActivity mainActivity=(MainActivity)((HomePageActivity2)context).getParent();
            mainActivity.setIs_in_memo(false);
            mainActivity.invalidateOptionsMenu();
        }
    }
}
