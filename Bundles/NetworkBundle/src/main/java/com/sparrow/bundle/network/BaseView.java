package com.sparrow.bundle.network;

import android.content.Context;

/**
 * @author zhangshaopeng
 * @date 2016/6/13 0013
 * @description View的基类
 */
public interface BaseView extends AppCompatView {
    /**
     * 每个View必须实现的方法，用来处理消息的显示和错误加载
     * <p>
     * 如果只有消息code为C.B==0
     *
     * @param msg  消息
     * @param code 编号
     */
    void handleServiceInfo(String msg, int code);

    void showContentView();

    void showEmptyView();

    void showProgressView();

    void showErrorView();

    Context getContext();
}
