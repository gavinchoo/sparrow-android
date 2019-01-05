package com.sparrow.bundle.network.interceptor;

import android.support.annotation.Nullable;
import android.util.Pair;


import com.sparrow.bundle.network.exception.AppException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;

/**
 * @author zhangshaopeng
 * @date 2017/10/20
 * @description
 */
public class ApiErrorIntercept<T> implements ObservableTransformer<T, T> {
    private final @Nullable
    Consumer<Pair<String, String>> errorAction;

    public ApiErrorIntercept() {
        this.errorAction = null;
    }

    public ApiErrorIntercept(final @Nullable Consumer<Pair<String, String>> errorAction) {
        this.errorAction = errorAction;
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return upstream
                .doOnError(e -> {
                    Pair<String, String> errorContent = AppException.getErrorContent(e);
                    if (this.errorAction != null) {
                        this.errorAction.accept(errorContent);
                    }
                    e.printStackTrace();
                })
                .onErrorResumeNext(e -> {
                    e.printStackTrace();
                    return Observable.empty();
                });
    }
}
