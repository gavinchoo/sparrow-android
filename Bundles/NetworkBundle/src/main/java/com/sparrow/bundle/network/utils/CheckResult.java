package com.sparrow.bundle.network.utils;

import com.sparrow.bundle.network.BaseResponse;
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
public class CheckResult implements ObservableTransformer<BaseResponse, BaseResponse> {

    private BaseView view;

    public CheckResult(BaseView view) {
        this.view = view;
    }

    public CheckResult() {
    }

    @Override
    public ObservableSource<BaseResponse> apply(@NonNull Observable<BaseResponse> upstream) {
        return upstream.doOnNext(result -> {
            if (result == null) {
                throw new ServerException(AppException.RESULT_NULL_ERROR_TEXT, AppException.RESULT_NULL_ERROR);
            }
            if ("200".equals(result.getCode())) {
                if (view != null) view.handleServiceInfo(result.getMessage(), 0);
            } else {
                throw new ServerException(result.getMessage(), result.getCode());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
