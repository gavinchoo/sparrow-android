package com.sparrow.bundle.framework.base.ui;

import android.content.Context;
import android.support.annotation.NonNull;

import com.sparrow.bundle.framework.base.BaseView;
import com.sparrow.bundle.framework.base.BaseViewModel;
import com.sparrow.bundle.framework.base.contract.MultiItemListPageContract;
import com.sparrow.bundle.framework.base.contract.PageListContract;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * @author zhangshaopeng
 * @date 2018/8/19
 * @description
 */
public abstract class BaseMultiItemListViewModel<T> extends BaseViewModel implements MultiItemListPageContract.Input,
MultiItemListPageContract.Output<T>{
    public boolean isRefresh = true;
    public MultiItemListPageContract.Output<T> output = this;
    public MultiItemListPageContract.Input input = this;
    public PublishSubject<List<T>> refreshDataObservable = PublishSubject.create();
    public PublishSubject<T> isMoreLoadingObservable = PublishSubject.create();

    public BaseMultiItemListViewModel(Context context) {
        super(context);
    }

    public abstract void refreshData();

    public abstract void getMoreData();

    @Override
    public void onCreate(@NonNull BaseView view) {
        super.onCreate(view);
        refreshData();
    }

    @Override
    public void toRefreshData() {
        isRefresh = true;
        refreshData();
    }

    @Override
    public void toGetMoreData() {
        isRefresh = false;
    }

    @Override
    public void errorClick() {
        isRefresh = true;
        refreshData();
    }

    @Override
    public Observable<T> isMoreLoadingObservable() {
        return isMoreLoadingObservable;
    }

    @Override
    public Observable<List<T>> refreshDataObservable() {
        return refreshDataObservable;
    }
}
