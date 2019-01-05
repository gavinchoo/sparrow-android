package com.sparrow.bundle.framework.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.sparrow.bundle.framework.base.entity.ServiceEntity;
import com.sparrow.bundle.framework.base.ui.activity.BaseActivity;
import com.sparrow.bundle.framework.base.ui.activity.ContainerActivity;
import com.sparrow.bundle.framework.bus.RxBus;
import com.sparrow.bundle.framework.bus.RxEventObject;
import com.sparrow.bundle.framework.utils.ToastUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class BaseViewModel<ViewType extends ActivityLifecycleType> implements IBaseViewModel {
    private PublishSubject<ViewType> viewSubject = PublishSubject.create();
    private Observable<ViewType> view = viewSubject.filter(v -> v != null);

    protected Context context;
    protected Fragment fragment;

    private BaseView baseView;

    private final Consumer<Pair<String, String>> handleError = throwable -> {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
                if (baseView != null) {
                    ServiceEntity entity = new ServiceEntity();
                    entity.msg = throwable.first;
                    entity.code = throwable.second;
                    baseView.handleServiceInfo(entity);
                }
            }
        });
    };

    private final Consumer<Pair<String, String>> handleToastError = throwable -> {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort(throwable.first);
                dismissDialog();
            }
        });
    };

    public BaseViewModel(Context context) {
        this.context = context;
        view.compose(bindToLifecycle())
                .subscribe(v -> baseView = (BaseView) v);
    }

    public BaseViewModel(Fragment fragment) {
        this(fragment.getContext());
        this.fragment = fragment;
        view.compose(bindToLifecycle())
                .subscribe(v -> baseView = (BaseView) v);
    }

    public BaseView getBaseView() {
        return baseView;
    }

    private KProgressHUD dialog;

    public void showDialog() {
        showDialog("请稍后...");
    }

    public void showDialog(String title) {
        if (dialog == null) {
            dialog = KProgressHUD.create(context)
                    .setCancellable(true)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        }
        dialog.setLabel(title);
        if (!dialog.isShowing())
            dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 跳转页面
     *
     * @param clz 所跳转的目的Activity类
     */
    public void startActivity(Class<?> clz) {
        context.startActivity(new Intent(context, clz));
    }

    public void startActivityForResult(Class<?> clz, int requestCode) {
        ((Activity) context).startActivityForResult(new Intent(context, clz), requestCode);
    }

    public void finish() {
        ((Activity) context).finish();
    }

    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(context, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    public void startActivityForResult(Class<?> clz, Bundle bundle, int requestCode) {
        Intent intent = new Intent(context, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     * @param bundle        跳转所携带的信息
     */
    public void startContainerActivity(String canonicalName, Bundle bundle) {
        Intent intent = new Intent(context, ContainerActivity.class);
        intent.putExtra(ContainerActivity.FRAGMENT, canonicalName);
        if (bundle != null) {
            intent.putExtra(ContainerActivity.BUNDLE, bundle);
        }
        context.startActivity(intent);
    }

    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     */
    public void startContainerActivity(String canonicalName) {
        Intent intent = new Intent(context, ContainerActivity.class);
        intent.putExtra(ContainerActivity.FRAGMENT, canonicalName);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(final @NonNull BaseView view) {
        baseView = view;
    }

    @Override
    public void onDestroy() {
        viewSubject.onComplete();
    }

    @Override
    public void registerRxBus() {
    }

    @Override
    public void removeRxBus() {
    }

    @Override
    public void publishEvent(String event, Object data) {
        RxEventObject object = new RxEventObject();
        object.setData(data);
        object.setEvent(event);
        RxBus.getDefault().post(object);
    }

    public <T> ObservableTransformer<T, T> bindToLifecycle() {
        return upstream -> upstream.takeUntil(this.view.switchMap(v -> v.lifecycle().map(e -> Pair.create(v, e)))
                .filter(ve -> isFinished(ve.first, ve.second)));
    }

    private boolean isFinished(final @NonNull ViewType view, final @NonNull ActivityEvent event) {
        if (view instanceof BaseActivity) {
            return event == ActivityEvent.DESTROY && ((BaseActivity) view).isFinishing();
        }
        return event == ActivityEvent.DESTROY;
    }

    public Consumer<Pair<String, String>> baseHandler() {
        return handleError;
    }

    public Consumer<Pair<String, String>> baseToastHandler() {
        return handleToastError;
    }
}
