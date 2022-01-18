package io.github.yuriua.buddo.serialize.jdk;

import io.github.yuriua.buddo.exception.SerializeException;
import io.github.yuriua.buddo.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author lyx
 * @date 2022/1/10 16:25
 * Description:
 */
@Slf4j
public class JDKSerializer implements Serializer {
    @Override
    public <T> T deserialize(byte[] bytes,Class<T> clazz) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception e) {
            log.error("JDK反序列化异常: {}",e.getCause());
            throw new SerializeException("JDK反序列化异常: "+e.getCause());
        }
    }

    @Override
    public byte[] serialize(Object object) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(object);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("JDK序列化异常: {}",e.getCause());
            throw new SerializeException("JDK序列化异常: "+e.getCause());
        }
    }
}
