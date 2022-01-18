package io.github.yuriua.buddo.annotation;

import java.lang.annotation.*;

/**
 * @author lyx
 * @date 2022/1/5 11:45
 * Description:
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    String version() default "";

    String group() default "";
}
