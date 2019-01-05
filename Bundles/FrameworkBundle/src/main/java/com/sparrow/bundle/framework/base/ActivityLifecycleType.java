package com.sparrow.bundle.framework.base;

import android.support.annotation.NonNull;

import com.trello.rxlifecycle2.android.ActivityEvent;

import io.reactivex.Observable;

/**
 * @author zhangshaopeng
 * @date 2017/8/21
 * @description
 */

public interface ActivityLifecycleType {

    @NonNull
    Observable<ActivityEvent> lifecycle();

    void handleServiceInfo(String msg, int code);
}
