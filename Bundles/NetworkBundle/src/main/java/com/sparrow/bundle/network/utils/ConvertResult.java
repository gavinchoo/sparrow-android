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
public class ConvertResult<T> implements ObservableTransformer<BaseResponse<T>, BaseResponse<T>> {
    private BaseView view;

    public ConvertResult(BaseView view) {
        this.view = view;
    }

    public ConvertResult() {
    }


    @Override
    public ObservableSource<BaseResponse<T>> apply(@NonNull Observable<BaseResponse<T>> upstream) {
        return handleResult(view, upstream)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static <T> Observable<BaseResponse<T>> handleResult(BaseView view, Observable<BaseResponse<T>> upstream) {
        return upstream.flatMap(result -> getObservableSource(result, view));
    }

    private static <T> ObservableSource<BaseResponse<T>> getObservableSource(BaseResponse<T> result, BaseView view) {
        if (result == null) {
            return Observable.error(new ServerException(AppException.RESULT_NULL_ERROR_TEXT, AppException.RESULT_NULL_ERROR));
        }
        if (result.isOk()) {
//            if (view != null)
//                view.handleServiceInfo(result.getMessage(), 0);
//            if (result.getResult() == null) {
//                return Observable.error(new ServerException("", 0));
//            }
            return Observable.just(result);
        } else {
            return Observable.error(new ServerException(result.getMessage(), result.getCode()));
        }
    }

}
