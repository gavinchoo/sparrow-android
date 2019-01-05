package com.sparrow.bundle.network;

import android.support.annotation.NonNull;

import com.sparrow.bundle.network.utils.CheckResult;
import com.sparrow.bundle.network.utils.CombineLatestPairTransformer;
import com.sparrow.bundle.network.utils.ConvertResult;
import com.sparrow.bundle.network.utils.ConvertResultNormal;
import com.sparrow.bundle.network.utils.TakePairWhenTransformer;
import com.sparrow.bundle.network.utils.TakeWhenTransformer;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

/**
 * @author zhangshaopeng
 * @date 2017/9/29
 * @description
 */
public class Transformer {

    public static <T> ObservableTransformer<BaseResponse<T>, BaseResponse<T>> convertResult() {
        return new ConvertResult<>();
    }

    public static <T> ObservableTransformer<T, T> convertResultNormal() {
        return new ConvertResultNormal<>();
    }


    public static <T> ObservableTransformer<BaseResponse<T>, BaseResponse<T>> convertResult(BaseView view) {
        return new ConvertResult<>(view);
    }

    public static ObservableTransformer<BaseResponse, BaseResponse> checkResult() {
        return new CheckResult();
    }

    public static ObservableTransformer<BaseResponse, BaseResponse> checkResult(BaseView view) {
        return new CheckResult(view);
    }

    /**
     * Emits the latest value of the source observable whenever the `when`
     * observable emits.
     */
    public static <S, T> TakeWhenTransformer<S, T> takeWhen(final @NonNull Observable<T> when) {
        return new TakeWhenTransformer<>(when);
    }

    /**
     * Emits the latest value of the source `when` observable whenever the
     * `when` observable emits.
     */
    public static <S, T> TakePairWhenTransformer<S, T> takePairWhen(final @NonNull Observable<T> when) {
        return new TakePairWhenTransformer<>(when);
    }

    /**
     * Emits the latest values from two observables whenever either emits.
     */
    public static <S, T> CombineLatestPairTransformer<S, T> combineLatestPair(final @NonNull Observable<T> second) {
        return new CombineLatestPairTransformer<>(second);
    }
}
