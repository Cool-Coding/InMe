package cn.yang.inme;

import android.app.Application;
import android.os.Environment;
import android.view.WindowManager;
import cn.yang.inme.bean.MsgAndPathFrequency;
import cn.yang.inme.utils.Constants;
import cn.yang.inme.utils.PropertiesUtil;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import org.apache.log4j.Level;

import java.io.File;
import java.util.Properties;

/**
 * Created by Administrator on 14-6-5.
 */
public class Inme extends Application {
    //定义全局变量
    private int screen_width;
    private int screen_height;

    private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

    public WindowManager.LayoutParams getMywmParams() {
        return wmParams;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(Environment.getExternalStorageDirectory()
                + File.separator + "Inme" + File.separator + "logs"
                + File.separator + "log4j.txt");
        logConfigurator.setRootLevel(Level.ERROR);
        logConfigurator.setLevel("org.apache", Level.INFO);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setMaxFileSize(1024 * 50);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();

        //写入配置
        File file = new File(Constants.CONFIG_FILE_PATH);
        if (!file.exists()) {
            Properties properties = new Properties();
            /**
             * 消息显示的频率
             */
            properties.put(Constants.MESSAGE_FREQUENCY, MsgAndPathFrequency.MONTH.toString());
            /**
             * 路径显示的频率
             */
            properties.put(Constants.PATH_LOCATION_FREQUENCY, MsgAndPathFrequency.TODAY.toString());

            /**
             * 配置是否启动路径定位功能
             */
            properties.put(Constants.PATH_LOCATION_ENABLE, "false");
            /**
             * 路径定位时间默认间隔(单位为秒)
             */
            properties.put(Constants.PATH_LOCATION_INTERVAL, "300");

            /**
             * 登陆用户名
             */
            //properties.put(Constants.LOGIN_NAME, "光用");

            /**
             * 默认主题颜色
             */
            properties.put(Constants.SET_THEME_COLOR, "-6736902");

            /**
             * 默认主题背景
             */
            properties.put(Constants.SET_THEME_BACKGROUND, "");

            PropertiesUtil.saveConfig(properties);
        }
    }
}
