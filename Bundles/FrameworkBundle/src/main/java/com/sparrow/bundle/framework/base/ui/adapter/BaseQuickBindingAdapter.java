/**
 * Copyright 2013 Joan Zapata
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sparrow.bundle.framework.base.ui.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.View;
import android.view.ViewGroup;

import com.sparrow.bundle.framework.R;

import java.util.List;


public class BaseQuickBindingAdapter<T> extends BaseQuickAdapter<T, BaseQuickBindingAdapter.BindingViewHolder> {

    protected int variableId;   //布局内VariableId

    public BaseQuickBindingAdapter(List<T> data, int layoutResId, int variableId) {
        super(layoutResId, data);
        this.variableId = variableId;
    }

    @Override
    protected void convert(BindingViewHolder helper, T item , int position) {
        ViewDataBinding binding = helper.getBinding();
        binding.setVariable(this.variableId, item);

        if (listener != null) {
            helper.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(binding, position);
                }
            });
        }

        if (longListener != null) {
            helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longListener.onLongItemClick(binding, position);
                    return false;
                }
            });
        }
        binding.executePendingBindings();

    }

    @Override
    protected BindingViewHolder createBaseViewHolder(View view) {
        return new BindingViewHolder(view);
    }

    @Override
    protected View getItemView(int layoutResId, ViewGroup parent) {
        ViewDataBinding binding = DataBindingUtil.inflate(mLayoutInflater, layoutResId, parent, false);
        if (binding == null) {
            return super.getItemView(layoutResId, parent);
        }
        View view = binding.getRoot();
        view.setTag(com.sparrow.bundle.framework.R.id.BaseQuickAdapter_databinding_support, binding);
        return view;
    }

    //自定义item单击事件
    protected BaseRecycleViewAdapter.OnItemClickListener listener;
    protected BaseRecycleViewAdapter.OnItemLongClickListener longListener;

    public void setOnItemClickListener(BaseRecycleViewAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(BaseRecycleViewAdapter.OnItemLongClickListener listener) {
        this.longListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(ViewDataBinding dataBinding, int position);
    }

    public interface OnItemLongClickListener {
        void onLongItemClick(ViewDataBinding dataBinding, int position);
    }

    public static class BindingViewHolder extends BaseViewHolder {

        public BindingViewHolder(View view) {
            super(view);
        }

        public ViewDataBinding getBinding() {
            return (ViewDataBinding) itemView.getTag(com.sparrow.bundle.framework.R.id.BaseQuickAdapter_databinding_support);
        }
    }
}
