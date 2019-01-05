package com.sparrow.bundle.framework.base;

import android.support.annotation.NonNull;

public interface IBaseViewModel {
//    void initData();

    /**
     * View的界面创建时回调
     */
    void onCreate(final @NonNull BaseView view);

    /**
     * View的界面销毁时回调
     */
    void onDestroy();

    /**
     * 注册RxBus
     */
    void registerRxBus();
    /**
     * 移除RxBus
     */
    void removeRxBus();

    void publishEvent(String event, Object data);
}
