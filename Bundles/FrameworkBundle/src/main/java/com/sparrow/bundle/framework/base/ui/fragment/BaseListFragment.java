package com.sparrow.bundle.framework.base.ui.fragment;

import android.databinding.ViewDataBinding;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.base.BaseListViewModel;
import com.sparrow.bundle.framework.base.entity.ServiceEntity;
import com.sparrow.bundle.framework.base.ui.adapter.BaseQuickAdapter;
import com.sparrow.bundle.framework.base.ui.adapter.BaseQuickBindingAdapter;
import com.sparrow.bundle.framework.base.ui.view.state.ContentType;
import com.sparrow.bundle.framework.base.ui.view.state.EmptyType;
import com.sparrow.bundle.framework.base.ui.view.state.ErrorType;
import com.sparrow.bundle.framework.base.ui.view.state.FoodSecurityPullHead;
import com.sparrow.bundle.framework.base.ui.view.state.LoadLayout;
import com.sparrow.bundle.framework.base.ui.view.state.LoadingType;
import com.sparrow.bundle.framework.base.ui.view.state.StatusManager;
import com.sparrow.bundle.framework.utils.NetworkUtil;
import com.sparrow.bundle.framework.utils.ToastUtils;
import com.sparrow.bundle.framework.utils.NetworkUtil;
import com.sparrow.bundle.framework.utils.ToastUtils;

import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public abstract class BaseListFragment<T, V extends ViewDataBinding, VM extends BaseListViewModel<T>> extends BaseFragment<V, VM>{

    public BaseQuickBindingAdapter<T> adapter;

    private PtrFrameLayout ptrLayout;
    private FoodSecurityPullHead headView;
    private RecyclerView recyclerview;
    private LoadLayout loadLayout;

    private boolean markFirstLoadForLazyLoad;//懒加载是否已进行第一次加载

    @Override
    public void initData() {
        super.initData();
        ptrLayout = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.ptrLayout);
        headView = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.headView);
        recyclerview = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.recyclerview);
        loadLayout = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.loadLayout);

        ptrLayout.addPtrUIHandler(headView);
        recyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        ptrLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return !(ptrLayout.isRefreshing() || ViewCompat.canScrollVertically(recyclerview, -1));
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                viewModel.input.toRefreshData();
            }
        });

        adapter = new BaseQuickBindingAdapter<T>(viewModel.listEntity, initAdapterContentView(), initAdapterVariableId()) {
            @Override
            public void convert(BindingViewHolder helper, T item, int position) {
                super.convert(helper, item, position);
                adapterConvert(helper, item, position);
            }
        };
        recyclerview.setAdapter(adapter);

        //设置上拉监听
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                viewModel.input.toGetMoreData();
            }
        });

        statusManager = new StatusManager.Builder(loadLayout)
                .addType(new ContentType(ptrLayout))
                .addType(new EmptyType())
                .addType(new LoadingType())
                .addType(new ErrorType(v ->
                {
                    showProgressView();
                    this.viewModel.input.errorClick();
                }, !NetworkUtil.isNetworkAvailable(getContext())))
                .build();

        this.viewModel.output.refreshData()
                .compose(bindToLifecycle())
                .subscribe(this::refreshData);
        this.viewModel.output.isMoreLoading()
                .compose(bindToLifecycle())
                .subscribe(adapter::isLoadMore);

        if (!needLazyLoad()) {
            getData();
        }
    }

    public void adapterConvert(BaseQuickBindingAdapter.BindingViewHolder helper, T item, int position) {

    }

    private void refreshData(List<T> data) {
        if (null == data || data.size() == 0) {
            showEmptyView();
        } else {
            showContentView();
        }
        adapter.setNewData(data);
        if (null != viewModel.pageInfo) {
            int totalCount = viewModel.pageInfo.totalCount;
            if (totalCount == 0) {
                totalCount = viewModel.pageInfo.total;
            }
            adapter.isLoadMore(adapter.getData().size() < totalCount);
        } else {
            adapter.isLoadMore(false);
        }
        ptrLayout.refreshComplete();
    }

    @Override
    public void handleServiceInfo(ServiceEntity entity) {
        super.handleServiceInfo(entity);
        ptrLayout.refreshComplete();
        if (isDataEmpty()) {
            showErrorView("0".equals(entity.code) ? "" : entity.msg);
        } else if (!TextUtils.isEmpty(entity.msg)) {
            ToastUtils.showShort(entity.msg);
        }
    }

    protected boolean isDataEmpty() {
        return null == adapter
                || null == adapter.getData()
                || adapter.getData().size() == 0;
    }

    public abstract int initAdapterVariableId();

    public abstract int initAdapterContentView();

    public void autoRefresh() {
        ptrLayout.autoRefresh();
        if (isDataEmpty()) {
            showProgressView();
        }
    }

    private void getData() {
        showProgressView();
        viewModel.getData();
    }

    /**
     * 是否需要懒加载，默认不需要
     * */
    public abstract boolean needLazyLoad();

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        super.onFragmentVisibleChange(isVisible);
        if (needLazyLoad() && !markFirstLoadForLazyLoad) {
            getData();
            markFirstLoadForLazyLoad = true;
        }
    }
}

