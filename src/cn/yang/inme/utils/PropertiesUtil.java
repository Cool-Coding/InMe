package cn.yang.inme.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Administrator on 14-7-17.
 */
public class PropertiesUtil {
    private Properties properties;
    private static PropertiesUtil propertiesUtil;
    private static Logger log = Logger.getLogger(PropertiesUtil.class);

    private PropertiesUtil() {
        properties = new Properties();
        try {

            FileInputStream s = new FileInputStream(Constants.CONFIG_FILE_PATH);
            properties.load(s);
            s.close();
        } catch (IOException e) {
            log.error("读取properties错误", e.getCause());
        }
    }

    //得到属性文件操作类对象实例
    public static PropertiesUtil instance() {
        if (propertiesUtil == null) propertiesUtil = new PropertiesUtil();
        return propertiesUtil;

    }

    //读取
    public String read(String key) {
        return properties.getProperty(key);
    }

    //增加属性
    public void add(String key, String value) {
        properties.setProperty(key, value);
        saveConfig(properties);
    }

    //删除属性
    public void delete(String key) {
        properties.remove(key);
    }

    public static void saveConfig(Properties properties) {
        try {
            File file = new File(Constants.CONFIG_FILE_PATH);
            FileOutputStream s = new FileOutputStream(file, false);
            properties.store(s, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
