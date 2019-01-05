package com.sparrow.bundle.framework.base.ui.activity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.alibaba.android.arouter.launcher.ARouter;
import com.sparrow.bundle.framework.base.BaseView;
import com.sparrow.bundle.framework.base.BaseViewModel;
import com.sparrow.bundle.framework.base.IBaseActivity;
import com.sparrow.bundle.framework.base.ToolbarUtil;
import com.sparrow.bundle.framework.base.entity.ServiceEntity;
import com.sparrow.bundle.framework.base.ui.view.state.StatusManager;
import com.sparrow.bundle.framework.bus.RxBus;
import com.sparrow.bundle.framework.bus.RxEventObject;
import com.sparrow.bundle.framework.bus.RxSubscriptions;
import com.sparrow.bundle.framework.utils.Utils;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.umeng.analytics.MobclickAgent;

import io.reactivex.disposables.Disposable;


/**
 * 一个拥有DataBinding框架的基Activity
 * 这里根据项目业务可以换成你自己熟悉的BaseActivity, 但是需要继承RxAppCompatActivity,方便LifecycleProvider管理生命周期
 */

public abstract class BaseActivity<V extends ViewDataBinding, VM extends BaseViewModel> extends RxAppCompatActivity implements IBaseActivity, BaseView {
    protected V binding;
    protected VM viewModel;

    private ToolbarUtil toolbarUtil;
    private Disposable mSubscription;
    public StatusManager statusManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();//初始化参数
        initViewDataBinding(savedInstanceState);
        initViewObservable();
        initView();//初始化视图
        initListener();
        initStateLayout();
        initData();//初始化视图数据
        subscribeEvent();
        if (null != viewModel) {
            viewModel.onCreate(this);
            viewModel.registerRxBus();
        }
    }

    private void subscribeEvent() {
        mSubscription = RxBus.getDefault().toObservable(RxEventObject.class)
                .compose(bindToLifecycle())
                .subscribe(eventObject -> onSubscribeEvent(eventObject));
        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription);
    }

    /**
     * 取消订阅，防止内存泄漏
     */
    private void unsubscribeEvent() {
        RxSubscriptions.remove(mSubscription);
    }

    public void onSubscribeEvent(RxEventObject eventObject) {

    }

    @Override
    public void publishEvent(String event, Object data) {
        RxEventObject object = new RxEventObject();
        object.setData(data);
        object.setEvent(event);
        RxBus.getDefault().post(object);
    }

    public ToolbarUtil getToolbar() {
        return toolbarUtil;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribeEvent();
        if (null != viewModel) {
            viewModel.removeRxBus();
            viewModel.onDestroy();
            viewModel = null;
            binding.unbind();
        }
    }

    /**
     * 注入绑定
     */
    private void initViewDataBinding(Bundle savedInstanceState) {
        //DataBindingUtil类需要在project的build中配置 dataBinding {enabled true }, 同步后会自动关联android.databinding包
        binding = DataBindingUtil.setContentView(this, initContentView(savedInstanceState));
        binding.setVariable(initVariableId(), viewModel = initViewModel());
    }

    //刷新布局
    public void refreshLayout() {
        if (viewModel != null) {
            binding.setVariable(initVariableId(), viewModel);
        }
    }

    @Override
    public void initParam() {
        ARouter.getInstance().inject(this);
        toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setInnerBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCommandBack();
            }
        });
    }

    @Override
    public void onBackPressed() {
        onCommandBack();
    }

    public void onCommandBack() {
        finish();
    }

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    public abstract int initContentView(Bundle savedInstanceState);

    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    public abstract int initVariableId();

    /**
     * 初始化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    public abstract VM initViewModel();

    public abstract void initView();
    public abstract void initListener();

    /**
     * 初始化stateLayout,网络请求状态切换用
     *
     * @return
     */
    public void initStateLayout(){

    }

    @Override
    public void initData() {

    }

    @Override
    public void initViewObservable() {

    }

    @Override
    public void handleServiceInfo(ServiceEntity info) {

    }

    /**
     * 点击事件x坐标
     */
    private float downEventX;
    /**
     * 点击事件y坐标
     */
    private float downEventY;

    /**
     * 获取点击事件
     */
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 记录按下坐标
            downEventX = ev.getRawX();
            downEventY = ev.getRawY();
        } else if (ev.getAction() == MotionEvent.ACTION_UP
                || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            // 处理滑动时不关闭键盘
            if (ev.getRawX() == downEventX && ev.getRawY() == downEventY) {
                View view = getCurrentFocus();
                if (isShouldHideKeyBord(view, ev)) {
                    hideSoftInput(view.getWindowToken());
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判定当前是否需要隐藏
     */
    protected boolean isShouldHideKeyBord(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            return !(ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom);
        }
        return false;
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }


    public void showContentView() {
        Utils.objectNonNull(statusManager, "no init StateView");
        statusManager.showContent();
    }

    public void showEmptyView() {
        Utils.objectNonNull(statusManager, "no init StateView");
        statusManager.showEmpty();
    }

    public void showProgressView() {
        Utils.objectNonNull(statusManager, "no init StateView");
        statusManager.showProgress();
    }

    public void showErrorView() {
        Utils.objectNonNull(statusManager, "no init StateView");
        statusManager.showError();
    }

    public void showErrorView(String errorMsg) {
        Utils.objectNonNull(statusManager, "no init StateView");
        statusManager.showError(errorMsg);
    }

}
