package com.sparrow.bundle.framework.base.ui.adapter;


import com.sparrow.bundle.framework.R;

/**
 * Created by BlingBling on 2016/10/11.
 */

public final class SimpleLoadMoreView extends LoadMoreView {

    @Override public int getLayoutId() {
        return com.sparrow.bundle.framework.R.layout.quick_view_load_more;
    }

    @Override protected int getLoadingViewId() {
        return com.sparrow.bundle.framework.R.id.load_more_loading_view;
    }

    @Override protected int getLoadFailViewId() {
        return com.sparrow.bundle.framework.R.id.load_more_load_fail_view;
    }

    @Override protected int getLoadEndViewId() {
        return com.sparrow.bundle.framework.R.id.load_more_load_end_view;
    }
}