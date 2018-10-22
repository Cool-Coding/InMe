package cn.yang.inme.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 14-7-15.
 */
public class UserLocation {
    public Integer userid;//用户ID
    public String latitude;//纬度
    public String longitude;//经度
    public String creattime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//创建日期

    public UserLocation() {

    }

    public UserLocation(Integer userid, String latitude, String longitude) {
        this.userid = userid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
