package io.github.yuriua.buddo.spring;

import io.github.yuriua.buddo.annotation.RpcScan;
import io.github.yuriua.buddo.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * <h2>关于该类实现的两个接口解释</h2>
 * <b>ImportBeanDefinitionRegistrar</b><br/>
 * 本质上是一个接口。在ImportBeanDefinitionRegistrar接口中，有一个registerBeanDefinitions()方法，
 * 通过registerBeanDefinitions()方法，我们可以向Spring容器中注册bean实例。
 * Spring官方在动态注册bean时，大部分套路其实是使用ImportBeanDefinitionRegistrar接口。
 * 所有实现了该接口的类都会被ConfigurationClassPostProcessor处理，ConfigurationClassPostProcessor实现了BeanFactoryPostProcessor接口，
 * 所以ImportBeanDefinitionRegistrar中动态注册的bean是优先于依赖其的bean初始化的，也能被aop、validator等机制处理。
 * <br/>
 * 基本步骤：<br/>
 * 1.实现ImportBeanDefinitionRegistrar接口<br/>
 * 2.通过registerBeanDefinitions实现具体的类初始化<br/>
 * 3.在@Configuration注解的配置类上使用@Import导入实现类<br/><br/>
 * <b>ResourceLoaderAware</b><br/>
 * 如果需要获取Spring中的一些数据，可实现一些Aware接口，这里实现了ResourceLoaderAware。
 * @author yuriua
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    /**
     * IOC扫描包路径
     */
    private static final String SPRING_BEAN_BASE_PACKAGE = "io.github.yuriua.buddo";
    /**
     * 注解@RpcScan的basePackage属性名字符串
     */
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    /**
     *
     */
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        //System.out.println(resourceLoader);
        this.resourceLoader = resourceLoader;

    }

    /**
     * AnnotationMetadata：当前类的注解信息
     * BeanDefinitionRegistry：BeanDefinition注册类
     *
     * 我们可以通过调用BeanDefinitionRegistry接口中的registerBeanDefinition方法，手动注册所有需要添加到容器中的bean
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 获取@RpcScan注解的 <k:属性,v:值> map集合
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        // 默认扫描的包路径
        String[] rpcScanBasePackages = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            // 获取该注解basePackage属性的值
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            // 没写就使用当前@Import注解即@RpcScan标注类的包名
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // 扫描@RpcService属性的自定义扫描器
        CustomScanner rpcServiceScanner = new CustomScanner(beanDefinitionRegistry, RpcService.class);
        // 扫描@Component属性的自定义扫描器
        CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        // spring bean
        int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        // buddo service bean
        int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);

    }

}
