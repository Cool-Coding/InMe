package cn.yang.inme.bean;

/**
 * Created by Administrator on 14-7-4.
 */
public class Memo {
    public int _id;//ID
    public String content;//内容
    public MemoFrequency frequency;//频率
    public Integer month;//月
    public Integer day;//日
    public Integer week;//周
    public String time;//时间
    public String createTime;//创建时间
    public int backgroundcolor;

    public Memo() {
    }

    public Memo(String content, MemoFrequency frequency, Integer month, Integer day, Integer week, String time, String createTime, int color) {
        this.content = content;
        this.frequency = frequency;
        this.month = month;
        this.day = day;
        this.week = week;
        this.time = time;
        this.createTime = createTime;
        this.backgroundcolor = color;
    }
}
