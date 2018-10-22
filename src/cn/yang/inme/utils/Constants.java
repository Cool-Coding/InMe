package cn.yang.inme.utils;

import android.view.ActionMode;
import cn.yang.inme.indicator.AsyncAdapter;

import java.io.Serializable;

/**
 * Created by yang on 2014/5/22.
 */
public class Constants {
    private Constants() {
    }

    /**
     * 应用Header与Footer高度
     */
    public static int headerHeight = 0;
    public static int footerHeight = 0;

    /**
     * 大众点评的Secret
     */
    public final static String DIANPING_KEY = "391283518";
    public final static String DIANPING_SECRET = "02921eba0e1d41f49b5368c6b25bfad8";

    /**
     * 周边团购图片宽度
     */
    public static int AROUNDTUANGOU_IMAGE = 300;
    public final static String AROUNDTUANGOU_URL = "http://api.dianping.com/v1/deal/find_deals";
    /**
     * 周边团购图片圆角
     */
    public final static int AROUNDTUANGOU_IMAGE_RADIUS=20;
    /**
     * 周边店铺图片宽度
     */
    public final static int AROUNDSHOP_IMAGE = 200;
    public final static String AROUNDSHOP_URL = "http://api.dianping.com/v1/business/find_businesses";
    public final static int AROUNDSHOP_DETAIL_IMAGE = 700;
    /**
     * 周边店铺团购券图片圆角
     */
    public final static int SHOP_ICON_RADIUS=10;
    /**
     * 首页
     */
    public static int HOMEPAGE_MEMO_WIDTH = 300;

    public final static String MESSAGE_FREQUENCY = "message_frequency";

    public final static String PATH_LOCATION_ENABLE = "path_location_enable";

    public final static String PATH_LOCATION_FREQUENCY = "path_location_frequency";
    /**
     * 路径时间间隔
     */
    public final static String PATH_LOCATION_INTERVAL = "path_location_interval";
    /**
     * 配置文件路径
     */
    public final static String CONFIG_FILE_PATH = "/sdcard/conf.properties";

    /**
     * 登陆名
     */
    public final static String LOGIN_NAME = "login_name";

    /**
     * 微博
     */
    public final static String WEIBO_UPLOAD_IMAGE = "weibo_upload_image";
    public final static String WEIBO_PICKED_IMAGE_COUNT = "weibo_picked_image_count";
    public final static String WEIBO_LIST_PAGECOUNT = "weibo_list_pagecount";
    public final static String BIG_IMAGE_PREFIX = "BIG-";

    /**
     * 设置
     */
    public final static String SET_THEME_COLOR = "set_theme_color";
    public final static String SET_THEME_BACKGROUND = "set_theme_background";
    /**
     * 网络设置参数
     */
    public final static String DEFAULT_HOST = "yanggy.nat123.net";
    public final static int DEFAULT_PORT = 33398;
    public final static int PORT2 = 33340;

    /**
     * 保存店铺信息
     */
    public static Serializable shop;

    /**
     * 待办的adapter
     */
    public static AsyncAdapter adapter;

    public static ActionMode mActionMode;
}
