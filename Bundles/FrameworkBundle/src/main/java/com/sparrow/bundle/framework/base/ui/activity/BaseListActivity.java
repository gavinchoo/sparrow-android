package com.sparrow.bundle.framework.base.ui.activity;

import android.databinding.ViewDataBinding;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.base.BaseListViewModel;
import com.sparrow.bundle.framework.base.entity.ServiceEntity;
import com.sparrow.bundle.framework.base.ui.adapter.BaseQuickBindingAdapter;
import com.sparrow.bundle.framework.base.ui.view.state.ContentType;
import com.sparrow.bundle.framework.base.ui.view.state.EmptyType;
import com.sparrow.bundle.framework.base.ui.view.state.ErrorType;
import com.sparrow.bundle.framework.base.ui.view.state.FoodSecurityPullHead;
import com.sparrow.bundle.framework.base.ui.view.state.LoadLayout;
import com.sparrow.bundle.framework.base.ui.view.state.LoadingType;
import com.sparrow.bundle.framework.base.ui.view.state.SearchEmptyType;
import com.sparrow.bundle.framework.base.ui.view.state.StatusManager;
import com.sparrow.bundle.framework.databinding.CommonRefreshListLayoutBinding;
import com.sparrow.bundle.framework.utils.NetworkUtil;
import com.sparrow.bundle.framework.utils.ToastUtils;

import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * zhujianwei134
 */
public abstract class BaseListActivity<T, V extends ViewDataBinding, VM extends BaseListViewModel<T>> extends BaseActivity<V, VM> {

    public BaseQuickBindingAdapter<T> adapter;
    protected PtrFrameLayout ptrLayout;
    protected FoodSecurityPullHead headView;
    protected RecyclerView recyclerview;
    protected LoadLayout loadLayout;

    @Override
    public void initStateLayout() {
        statusManager = new StatusManager.Builder(loadLayout)
                .addType(new ContentType(ptrLayout))
                .addType(new EmptyType())
                .addType(new LoadingType())
                .addType(new SearchEmptyType())
                .addType(new ErrorType(v ->
                {
                    showProgressView();
                    this.viewModel.input.errorClick();
                }, !NetworkUtil.isNetworkAvailable(getContext())))
                .build();
    }

    @Override
    public void initView() {
        ptrLayout = binding.getRoot().findViewById(R.id.ptrLayout);
        headView = binding.getRoot().findViewById(R.id.headView);
        recyclerview = binding.getRoot().findViewById(R.id.recyclerview);
        loadLayout = binding.getRoot().findViewById(R.id.loadLayout);

        ptrLayout.addPtrUIHandler(headView);
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        if (setCanPullDown()) {
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
        } else {
            ptrLayout.setPtrHandler(new PtrHandler() {
                @Override
                public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                    return false;
                }

                @Override
                public void onRefreshBegin(PtrFrameLayout frame) {

                }
            });
        }

        adapter = new BaseQuickBindingAdapter<T>(viewModel.listEntity, initAdapterContentView(), initAdapterVariableId()) {
            @Override
            protected void convert(BindingViewHolder helper, T item, int position) {
                super.convert(helper, item, position);
                adapterConvert(helper, item, position);
            }
        };
        recyclerview.setAdapter(adapter);

        //设置上拉监听
        if (setCanPullUp()) {
            adapter.setOnLoadMoreListener(() -> viewModel.input.toGetMoreData());
        }

        this.viewModel.output.refreshData()
                .compose(bindToLifecycle())
                .subscribe(this::refreshData);
        this.viewModel.output.isMoreLoading()
                .compose(bindToLifecycle())
                .subscribe(adapter::isLoadMore);
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

    public void autoRefresh() {
        ptrLayout.autoRefresh();
        if (isDataEmpty()) {
            showProgressView();
        }
    }

    public void adapterConvert(BaseQuickBindingAdapter.BindingViewHolder helper, T item, int position) {

    }

    public abstract int initAdapterVariableId();

    public abstract int initAdapterContentView();

    public abstract boolean setCanPullDown();

    public abstract boolean setCanPullUp();

}
