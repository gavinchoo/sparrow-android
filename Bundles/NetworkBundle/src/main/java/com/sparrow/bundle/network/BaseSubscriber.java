package com.sparrow.bundle.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import io.reactivex.observers.DisposableObserver;

/**
 * 该类仅供参考，实际业务Code, 根据需求来定义，
 */
public abstract class BaseSubscriber<T> extends DisposableObserver<T> {
    public abstract void onResult(T t);

    private Context context;
    private boolean isNeedCahe;

    public BaseSubscriber(Context context) {
        this.context = context;
    }

    @Override
    public void onError(Throwable e) {
        Log.e("BaseSubscriber", e.getMessage());
        // todo error somthing

        if (e instanceof ResponseThrowable) {
            onError((ResponseThrowable) e);
        } else {
            onError(new ResponseThrowable(e, ExceptionHandle.ERROR.UNKNOWN));
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        Toast.makeText(context, "http is start", Toast.LENGTH_SHORT).show();
        // todo some common as show loadding  and check netWork is NetworkAvailable
        // if  NetworkAvailable no !   must to call onCompleted
        if (!NetworkUtil.isNetworkAvailable(context)) {
            Toast.makeText(context, "无网络，读取缓存数据", Toast.LENGTH_SHORT).show();
            onComplete();
        }

    }

    @Override
    public void onComplete() {

        Toast.makeText(context, "http is Complete", Toast.LENGTH_SHORT).show();
        // todo some common as  dismiss loadding
    }


    public abstract void onError(ResponseThrowable e);

    @Override
    public void onNext(Object o) {
        BaseResponse baseResponse = (BaseResponse) o;
        if ("200".equals(baseResponse.getCode())) {
            onResult((T) baseResponse.getResult());
        } else if ("330".equals(baseResponse.getCode())) {
          //  ToastUtils.showShort(baseResponse.getMessage());
            Log.e("BaseSubscriber", baseResponse.getMessage());
        } else if ("503".equals(baseResponse.getCode())) {
            Log.e("BaseSubscriber", baseResponse.getMessage());
        } else {
           // ToastUtils.showShort("操作失败！错误代码:" + baseResponse.getCode());
        }
    }
}
