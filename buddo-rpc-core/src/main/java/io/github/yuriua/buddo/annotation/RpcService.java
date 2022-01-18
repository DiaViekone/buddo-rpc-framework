package io.github.yuriua.buddo.annotation;

import java.lang.annotation.*;

/**
 * @author lyx
 * @date 2022/1/5 11:46
 * Description:
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    String version() default "";

    String group() default "";
}
