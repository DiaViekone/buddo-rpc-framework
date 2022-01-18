package io.github.yuriua.buddo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author lyx
 * @date 2022/1/3 21:56
 * Description:
 */
public class PropertiesUtil {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static final Properties EMPTY_PROPERTIES = new Properties();
    private PropertiesUtil(){
    }

    public static Properties loadProperties(String fileName){
        // 获取用户线程类加载路径的资源
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (url==null){
            //todo
            return EMPTY_PROPERTIES;
        }
        String propertiesFileName = null;
        try {
            propertiesFileName = URLDecoder.decode(url.getPath(),"utf-8");
        } catch (UnsupportedEncodingException e) {
            return EMPTY_PROPERTIES;
        }
        Properties properties = null;
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(propertiesFileName))){
            properties = new Properties();
            properties.load(isr);
        } catch (Exception e){
            logger.error("加载properties文件出错: "+propertiesFileName);
            e.printStackTrace();;
        }
        return properties;
    }
}
