package com.sparrow.bundle.network.utils;

import com.sparrow.bundle.network.BaseView;
import com.sparrow.bundle.network.exception.AppException;
import com.sparrow.bundle.network.exception.ServerException;
import com.sparrow.bundle.network.exception.AppException;
import com.sparrow.bundle.network.exception.ServerException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * @author zhangshaopeng
 * @date 2017/10/16
 * @description
 */
public class ConvertResultNormal<T> implements ObservableTransformer<T, T> {
    private BaseView view;

    public ConvertResultNormal(BaseView view) {
        this.view = view;
    }

    public ConvertResultNormal() {
    }


    @Override
    public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        return handleResult(view, upstream)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static <T> Observable<T> handleResult(BaseView view, Observable<T> upstream) {
        return upstream.flatMap(result -> getObservableSource(result, view));
    }

    private static <T> ObservableSource<T> getObservableSource(T result, BaseView view) {
        if (result == null) {
            return Observable.error(new ServerException(AppException.RESULT_NULL_ERROR_TEXT, AppException.RESULT_NULL_ERROR));
        }
        return Observable.just(result);
    }

}
