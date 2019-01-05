
package com.sparrow.bundle.framework.base.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class BaseAdapter extends android.widget.BaseAdapter {

    protected Context mContext;
    protected LayoutInflater layoutInflater = null;
    protected List<?> mList = null;
    protected int miSelectPosition = -1;

    public BaseAdapter(Context context, List<?> list) {
        if (null != context) {
            layoutInflater = LayoutInflater.from(context);
        }
        mList = list;
        mContext = context;
    }

    public void setSelectPosition(int position) {
        miSelectPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectPosition() {
        return miSelectPosition;
    }

    public int getColor(int resId) {
        return mContext.getResources().getColor(resId);
    }

    public ColorStateList getColorStateList(int resId) {
        return mContext.getResources().getColorStateList(resId);
    }

    public void refreshAdapterData(List<?> newListData) {
        mList = newListData;
        this.notifyDataSetChanged();
    }

    public List<?> getData() {
        return mList;
    }

    @Override
    public int getCount() {
        if (null != mList) {
            return mList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public <T extends View> T findViewById(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }

    public void intentToActivity(Bundle bundle, Class<?> cls) {
        Intent intent = new Intent();

        if (null != bundle) {
            intent.putExtras(bundle);
        }
        intent.putExtra("classname", ((Activity) mContext).getComponentName()
                .getClassName());
        intent.setClass(mContext, cls);
        mContext.startActivity(intent);
    }

    public void intentToActivity(Class<?> cls) {
        intentToActivity(null, cls);
    }
}
