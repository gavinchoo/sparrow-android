package com.sparrow.bundle.framework.base;

import android.annotation.SuppressLint;
import android.content.Context;

import com.sparrow.bundle.framework.base.contract.PageListContract;
import com.sparrow.bundle.framework.base.entity.ListEntity;
import com.sparrow.bundle.framework.base.entity.PageEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class BaseListViewModel<T> extends BaseViewModel implements PageListContract.Input, PageListContract.Output {

    public List<T> listEntity = new ArrayList<>();

    public PageListContract.Output<List<T>> output = this;
    public PageListContract.Input input = this;

    public PublishSubject<List<T>> refreshData = PublishSubject.create();
    public PublishSubject<Boolean> isMoreLoading = PublishSubject.create();

    public PageEntity pageInfo;

    public boolean isLoadMore = false;

    public BaseListViewModel(Context context) {
        super(context);
    }

    public int getPageNumber() {
        if (isLoadMore && null != pageInfo) {
            int pageNumber = pageInfo.pageNumber;
            if (pageNumber == 0) {
                pageNumber = pageInfo.pageNum;
            }
            return pageNumber + 1;
        }
        return 1;
    }

    @Override
    public Observable<List<T>> refreshData() {
        return refreshData;
    }

    @Override
    public Observable<Boolean> isMoreLoading() {
        return isMoreLoading;
    }

    @Override
    public void errorClick() {
        refresh();
    }

    @Override
    public void toGetMoreData() {
        getMore();
    }

    @Override
    public void toRefreshData() {
        refresh();
    }

    public void refresh() {
        isLoadMore = false;
        getData();

    }

    @SuppressLint("CheckResult")
    private void getMore() {
        isLoadMore = true;
        getData();
    }

    public void getData() {

    }

    public void setResult(ListEntity<T> result){
        if (isLoadMore) {
            if (result.items != null) {
                this.listEntity.addAll(result.items);
            }
        } else {
            this.listEntity.clear();
            if (result.items != null) {
                this.listEntity.addAll(result.items);
            }
        }
        this.pageInfo = result.pageInfo;
        refreshData.onNext(listEntity);
    }
}
