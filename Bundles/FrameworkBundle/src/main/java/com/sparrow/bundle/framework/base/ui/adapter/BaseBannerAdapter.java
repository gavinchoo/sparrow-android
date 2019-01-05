package com.sparrow.bundle.framework.base.ui.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public abstract class BaseBannerAdapter<T> {
    private List<T> mDatas;
    private final int layoutId;
    private final Context mContext;
    private SparseArray<View> mViews;
    private OnDataChangedListener mOnDataChangedListener;
    private LayoutInflater mInflater;

    public BaseBannerAdapter(Context mContext, int layoutId, List<T> datas) {
        this.layoutId = layoutId;
        this.mContext = mContext;
        if (datas == null || datas.isEmpty()) {
            throw new RuntimeException("nothing to show");
        }
        mDatas = datas;
        init(mContext);
    }

    public BaseBannerAdapter(Context mContext, int layoutId) {
        this.layoutId = layoutId;
        this.mContext = mContext;
        init(mContext);
    }

    private void init(Context mContext) {
        mViews = new SparseArray<>();
        mInflater = LayoutInflater.from(mContext);
    }

    public void setData(List<T> data) {
        this.mDatas = data;
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return this.mDatas;
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        mOnDataChangedListener = listener;
    }

    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public void notifyDataSetChanged() {
        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onChanged();
        }
    }

    public View getView(ViewGroup parent, int position) {
        View view = mViews.get(position);
        if (view == null) {
            mViews.put(position, mInflater.inflate(layoutId, parent, false));
            view = mViews.get(position);
        }
        bindView(parent, view, getData().get(position), position);
        return view;
    }


    public abstract void bindView(ViewGroup parent, View itemView, T data, int position);

    public interface OnDataChangedListener {
        void onChanged();
    }
}
