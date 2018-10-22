package cn.yang.inme.utils;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import cn.yang.inme.bean.MsgAndPathFrequency;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 14-7-18.
 */
public class SpinnerUtil {
    private Spinner spinner;
    private Context context;
    private String frequency;
    private OnSpinnerChanged onSpinnerChanged;
    private static HashMap<String, Spinner> spinner_container = new HashMap<String, Spinner>();

    public SpinnerUtil(String frequency, Context context) {
        this.context = context;
        this.frequency = frequency;
        init();
    }

    public Spinner getSpinner() {
//        return spinner_container.get(frequency);
        return spinner;
    }

    private void init() {
//        if(spinner_container.containsKey(frequency))return;
        spinner = new Spinner(context);
//        spinner_container.put(frequency,spinner);


        final ArrayList<String> list = new ArrayList<String>();
        if (Constants.MESSAGE_FREQUENCY.equals(frequency)) {//今日消息
            list.add(MsgAndPathFrequency.WEEK.label());
            list.add(MsgAndPathFrequency.MONTH.label());
            list.add(MsgAndPathFrequency.YEAR.label());
            list.add(MsgAndPathFrequency.ALL.label());
        } else if (Constants.PATH_LOCATION_FREQUENCY.equals(frequency)) {//今日路径
            list.add(MsgAndPathFrequency.TODAY.label());
            list.add(MsgAndPathFrequency.WEEK.label());
            list.add(MsgAndPathFrequency.MONTH.label());
        }

        spinner.setAdapter(new SpinnerAdapter() {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = new TextView(context);
                textView.setText(list.get(position));
                textView.setMinimumHeight(80);
                textView.setMinimumWidth(120);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(Color.WHITE);
                return textView;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return list.get(position);
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
                TextView textView = new TextView(context);
                textView.setText(list.get(position));
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
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
                if (list.size() == 0) return true;
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MsgAndPathFrequency frequency_obj = MsgAndPathFrequency.getFrequencyFromLabel(list.get(position));
                if (frequency_obj != null) {
                    PropertiesUtil.instance().add(frequency, frequency_obj.toString());
                    if (onSpinnerChanged != null) {
                        onSpinnerChanged.change(frequency);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (Constants.MESSAGE_FREQUENCY.equals(frequency)) {//今日消息
            String frequency_value = PropertiesUtil.instance().read(frequency);
            if (frequency_value != null) {
                MsgAndPathFrequency frequency_obj = MsgAndPathFrequency.valueOf(frequency_value);
                spinner.setSelection(frequency_obj.ordinal() - 1);
            }
        } else if (Constants.PATH_LOCATION_FREQUENCY.equals(frequency)) {//今日路径
            String frequency_value = PropertiesUtil.instance().read(frequency);
            if (frequency_value != null) {
                MsgAndPathFrequency frequency_obj = MsgAndPathFrequency.valueOf(frequency_value);
                spinner.setSelection(frequency_obj.ordinal());
            }
        }
    }


    public void setOnSpinnerChanged(OnSpinnerChanged onSpinnerChanged) {
        this.onSpinnerChanged = onSpinnerChanged;
    }

    public interface OnSpinnerChanged {
        public void change(String frequency);
    }

}
