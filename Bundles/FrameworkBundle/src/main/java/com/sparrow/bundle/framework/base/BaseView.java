package com.sparrow.bundle.framework.base;

import android.content.Context;

import com.sparrow.bundle.framework.base.entity.ServiceEntity;

/**
 * @author zhangshaopeng
 * @date 2016/6/13 0013
 * @description View的基类
 */
public interface BaseView extends AppCompatView {
    /**
     * 每个View必须实现的方法，用来处理消息的显示和错误加载
     */
    void handleServiceInfo(ServiceEntity entity);

    Context getContext();
}
