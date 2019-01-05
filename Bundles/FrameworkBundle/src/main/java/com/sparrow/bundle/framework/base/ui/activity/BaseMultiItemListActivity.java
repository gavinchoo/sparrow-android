package com.sparrow.bundle.framework.base.ui.activity;

import android.databinding.ViewDataBinding;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.base.entity.ServiceEntity;
import com.sparrow.bundle.framework.base.ui.BaseMultiItemListViewModel;
import com.sparrow.bundle.framework.base.ui.adapter.BaseMultiItemQuickAdapter;
import com.sparrow.bundle.framework.base.ui.view.state.ContentType;
import com.sparrow.bundle.framework.base.ui.view.state.EmptyType;
import com.sparrow.bundle.framework.base.ui.view.state.ErrorType;
import com.sparrow.bundle.framework.base.ui.view.state.FoodSecurityPullHead;
import com.sparrow.bundle.framework.base.ui.view.state.LoadLayout;
import com.sparrow.bundle.framework.base.ui.view.state.LoadingType;
import com.sparrow.bundle.framework.base.ui.view.state.StatusManager;
import com.sparrow.bundle.framework.utils.NetworkUtil;
import com.sparrow.bundle.framework.utils.ToastUtils;

import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * @author zhangshaopeng
 * @date 2018/8/19
 * @description
 */
public abstract class BaseMultiItemListActivity<T, V extends ViewDataBinding, VM extends BaseMultiItemListViewModel<T>> extends BaseActivity<V , VM>{
    public PtrFrameLayout ptrLayout;
    private FoodSecurityPullHead headView;
    private RecyclerView recyclerview;
    private LoadLayout loadLayout;
    public BaseMultiItemQuickAdapter mAdapter;

    @Override
    public void initStateLayout() {
        statusManager = new StatusManager.Builder(loadLayout)
                .addType(new ContentType(ptrLayout))
                .addType(new EmptyType())
                .addType(new LoadingType())
                .addType(new ErrorType(v -> this.viewModel.input.errorClick(), !NetworkUtil.isNetworkAvailable(getContext())))
                .build();
    }

    @Override
    public void handleServiceInfo(ServiceEntity entity) {
        super.handleServiceInfo(entity);
        ptrLayout.refreshComplete();
        if (null == mAdapter
                || null == mAdapter.getData()
                || mAdapter.getData().size() == 0) {
            showErrorView("0".equals(entity.code) ? "" : entity.msg);
        } else if (!TextUtils.isEmpty(entity.msg)) {
            ToastUtils.showShort(entity.msg);
        }
    }

    @Override
    public void initView() {
        ptrLayout = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.ptrLayout);
        headView = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.headView);
        recyclerview = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.recyclerview);
        loadLayout = binding.getRoot().findViewById(com.sparrow.bundle.framework.R.id.loadLayout);

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
                    viewModel.refreshData();
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

        mAdapter = setAdapter();
        recyclerview.setAdapter(mAdapter);

        if (setCanPullUp()) {
            mAdapter.setOnLoadMoreListener(() -> viewModel.getMoreData());
        }

        this.viewModel.output.refreshDataObservable()
                .compose(bindToLifecycle())
                .subscribe(this::refreshData);
    }

    private void refreshData(List<T> data) {
        if (data == null || data.size() == 0) {
            showEmptyView();
        } else {
            showContentView();
        }
        if (viewModel.isRefresh) {
            mAdapter.setNewData(data);
        } else {
            mAdapter.addData(data);
        }
        ptrLayout.refreshComplete();
    }

    public abstract BaseMultiItemQuickAdapter setAdapter();

    public abstract boolean setCanPullDown();

    public abstract boolean setCanPullUp();

    public void refreshComplete(){
        ptrLayout.refreshComplete();
    }

    public void loadMoreComplete() {
        mAdapter.loadMoreComplete();
    }

    public void refreshNoty()
    {
        mAdapter.notifyDataSetChanged();
    }
}
