package com.sparrow.bundle.framework.base.ui.view.state;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sparrow.bundle.framework.utils.ConvertUtils;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * @author zhangshaopeng
 * @date 2016/10/26 0026
 * @description
 */
public class FoodSecurityPullHead extends LinearLayout implements PtrUIHandler {

    private TextView mTitleTv;

    public FoodSecurityPullHead(Context context) {
        this(context, null);
    }

    public FoodSecurityPullHead(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoodSecurityPullHead(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        ProgressBar progressBar = new ProgressBar(context);
        LayoutParams params = new LayoutParams(ConvertUtils.dp2px(40), ConvertUtils.dp2px(40));
        params.setMargins(0, ConvertUtils.dp2px(20), 0, ConvertUtils.dp2px(10));
        progressBar.setLayoutParams(params);
        addView(progressBar);
        mTitleTv = new TextView(context);
        mTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ConvertUtils.sp2px(14));
        mTitleTv.setTextColor(ContextCompat.getColor(context, com.sparrow.bundle.framework.R.color.text_normal_color));
        LayoutParams textParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 0, 0, ConvertUtils.dp2px(20));
        mTitleTv.setLayoutParams(textParams);
        mTitleTv.setText(com.sparrow.bundle.framework.R.string.name_of_app);
        addView(mTitleTv);
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {

    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

    }
}
