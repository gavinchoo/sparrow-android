package com.sparrow.bundle.framework.base.contract;

import io.reactivex.Observable;

public class PageListContract {
    public interface Input {
        void errorClick();
        void toGetMoreData();
        void toRefreshData();
    }

    public interface Output<T> {
        Observable<T> refreshData();
        Observable<Boolean> isMoreLoading();
    }
}
