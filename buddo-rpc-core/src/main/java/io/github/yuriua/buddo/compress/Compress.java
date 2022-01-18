package io.github.yuriua.buddo.compress;

import io.github.yuriua.buddo.extension.SPI;

/**
 * @author lyx
 * @date 2022/1/2 10:43
 * Description: 压缩的接口
 */
@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);

}
