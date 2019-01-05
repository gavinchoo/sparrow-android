package com.sparrow.bundle.framework.base.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sparrow.bundle.framework.base.BaseView;
import com.sparrow.bundle.framework.base.BaseViewModel;
import com.sparrow.bundle.framework.base.IBaseActivity;
import com.sparrow.bundle.framework.base.entity.ServiceEntity;
import com.sparrow.bundle.framework.base.ui.view.state.StatusManager;
import com.sparrow.bundle.framework.bus.RxBus;
import com.sparrow.bundle.framework.bus.RxEventObject;
import com.sparrow.bundle.framework.bus.RxSubscriptions;
import com.sparrow.bundle.framework.utils.Utils;
import com.trello.rxlifecycle2.components.support.RxFragment;

import io.reactivex.disposables.Disposable;


public abstract class BaseFragment<V extends ViewDataBinding, VM extends BaseViewModel> extends RxFragment implements IBaseActivity, BaseView {
    protected V binding;
    protected VM viewModel;
    private Disposable mSubscription;
    /**
     * rootView是否初始化标志，防止回调函数在rootView为空的时候触发
     */
    private boolean hasCreateView;
    /**
     * 当前Fragment是否处于可见状态标志，防止因ViewPager的缓存机制而导致回调函数的触发
     */
    private boolean isFragmentVisible;
    /**
     * onCreateView()里返回的view，修饰为protected,所以子类继承该类时，在onCreateView里必须对该变量进行初始化
     */
    protected View viewRoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribeEvent();
        viewModel.removeRxBus();
        viewModel.onDestroy();
        viewModel = null;
        binding.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, initContentView(inflater, container, savedInstanceState), container, false);
        binding.setVariable(initVariableId(), viewModel = initViewModel());
        viewRoot = binding.getRoot();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initViewObservable();
        subscribeEvent();
        viewModel.onCreate(this);
        viewModel.registerRxBus();
        if (!hasCreateView && getUserVisibleHint()) {
            onFragmentVisibleChange(true);
            isFragmentVisible = true;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (viewRoot == null) {
            return;
        }
        hasCreateView = true;
        if (isVisibleToUser) {
            onFragmentVisibleChange(true);
            isFragmentVisible = true;
            return;
        }
        if (isFragmentVisible) {
            onFragmentVisibleChange(false);
            isFragmentVisible = false;
        }
    }

    private void subscribeEvent() {
        mSubscription = RxBus.getDefault().toObservable(RxEventObject.class)
                .compose(bindToLifecycle())
                .subscribe(messageObject -> onSubscribeEvent(messageObject));
        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription);
    }

    @Override
    public void publishEvent(String event, Object data) {
        RxEventObject object = new RxEventObject();
        object.setData(data);
        object.setEvent(event);
        RxBus.getDefault().post(object);
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
    public void handleServiceInfo(ServiceEntity entity) {

    }

    @Override
    public void initParam() {

    }

    //刷新布局
    public void refreshLayout() {
        if (viewModel != null) {
            binding.setVariable(initVariableId(), viewModel);
        }
    }

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    public abstract int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

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

    @Override
    public void initData() {

    }

    @Override
    public void initViewObservable() {

    }

    public boolean onBackPressed() {
        return false;
    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    public StatusManager statusManager;

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

    /**
     * 当前fragment可见状态发生变化时会回调该方法
     * 如果当前fragment是第一次加载，等待onCreateView后才会回调该方法，其它情况回调时机跟{@link #setUserVisibleHint(boolean)}一致，
     * 在该回调方法中你可以做一些加载数据操作，甚至是控件的操作，因为配合fragment的view复用机制，不用担心在对控件操作中会报 null 异常
     * */
    protected void onFragmentVisibleChange(boolean isVisible) {}
}
