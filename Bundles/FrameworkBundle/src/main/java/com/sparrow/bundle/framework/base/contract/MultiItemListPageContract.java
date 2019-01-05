package com.sparrow.bundle.framework.base.contract;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author zhangshaopeng
 * @date 2018/9/28
 * @description
 */
public class MultiItemListPageContract {
    public interface Input {
        void errorClick();
        void toGetMoreData();
        void toRefreshData();
    }

    public interface Output<T> {
        Observable<List<T>> refreshDataObservable();
        Observable<T> isMoreLoadingObservable();
    }
}
