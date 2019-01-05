package com.sparrow.bundle.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface MultiIImagePart {
    // 上传的字段名称
    String name() default "";
    // 是否读取缓存图片
    boolean readCache() default false;
}
