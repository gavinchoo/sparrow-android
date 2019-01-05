package com.sparrow.bundle.framework.binding.viewadapter.viewgroup;

/**
 * Created by goldze on 2017/6/15.
 */

import android.databinding.ViewDataBinding;

public interface IBindingItemViewModel<V extends ViewDataBinding> {
    void injecDataBinding(V binding);
}
