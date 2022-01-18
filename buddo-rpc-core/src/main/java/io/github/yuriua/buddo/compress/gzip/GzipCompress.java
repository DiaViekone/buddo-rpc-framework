package io.github.yuriua.buddo.compress.gzip;

import io.github.yuriua.buddo.compress.Compress;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author lyx
 * @date 2022/1/2 10:50
 * Description:
 */
@Slf4j
public class GzipCompress implements Compress {


    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip压缩异常", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            log.error("bytes is null");
            throw new NullPointerException("bytes is null");
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             GZIPInputStream gzip = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 4];//4mb
            int len;
            while ((len=gzip.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("解压缩异常", e);
        }

    }

    public static void main(String[] args) {
        GzipCompress gzipCompress = new GzipCompress();
        byte[] compress = gzipCompress.compress(new byte[]{'a', 'c', 'c', 'c', 'f'});
        byte[] decompress = gzipCompress.decompress(compress);
        for (int i = 0; i < compress.length; i++) {
            System.out.print(compress[i]+" ");
        }
        System.out.println();
        for (int i = 0; i < decompress.length; i++) {
            System.err.print(decompress[i]+" ");
        }
    }
}
