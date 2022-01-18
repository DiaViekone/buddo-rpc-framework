package io.github.yuriua.buddo.config;

import io.github.yuriua.buddo.exception.BuddoException;
import io.github.yuriua.buddo.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author lyx
 * @date 2022/1/2 1:23
 * Description: RPC相关参数的配置类
 */
@Slf4j
public abstract class BuddoConfig {
    private static final String DEFAULT_BUDDO_CONFIG = "buddo.properties";
    protected static final Pattern URL_PATTERN = Pattern.compile(".+\\..+\\..+\\..+:\\d{1,5}");
    protected static final Properties properties;
    static {
        properties = PropertiesUtil.loadProperties(DEFAULT_BUDDO_CONFIG);
    }


    /**
     * 获取序列化算法配置
     */
//    public static Serializer.Algorithm getSerializerAlgorithm(){
//        String getSerializerAlgorithm = properties.getProperty("buddo.serializer.algorithm", String.valueOf(Serializer.Algorithm.JDK));
//        Serializer.Algorithm serializerAlgorithm = Serializer.Algorithm.valueOf(getSerializerAlgorithm);
//        if (serializerAlgorithm != null){
//            return Serializer.Algorithm.valueOf(getSerializerAlgorithm);
//        } else {
//            Serializer.Algorithm[] values = Serializer.Algorithm.values();
//            List<Serializer.Algorithm> algorithms = Arrays.asList(values);
//            logger.error("找不到该序列化算法: "+getSerializerAlgorithm);
//            throw new BuddoException("找不到该序列化算法: "+getSerializerAlgorithm+", 您可以修改为: "+algorithms+" 其中之一");
//        }
//    }

    /**
     * 获取时间单位：默认秒
     */
    public static TimeUnit getDefaultTimeUnit(){
        return TimeUnit.SECONDS;
    }





}
