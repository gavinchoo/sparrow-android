package com.sparrow.bundle.framework.base.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.sparrow.bundle.framework.base.ui.adapter.BaseBannerAdapter;

/**
 * @author zhangshaopeng
 * @date 2017/12/12
 * @description
 */
public class VerticalBannerView extends LinearLayout implements BaseBannerAdapter.OnDataChangedListener {

    private float mBannerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
    private int mLoadingDuration = 5000;
    private View mFirstView;
    private View mSecondView;
    private boolean isStarted;
    private int mPosition;
    private BaseBannerAdapter mAdapter;

    public VerticalBannerView(Context context) {
        this(context, null);
    }

    public VerticalBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalBannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);
    }

    public void setAdapter(BaseBannerAdapter adapter) {
        if (adapter == null) {
            throw new RuntimeException("adapter must be null");
        }
        mAdapter = adapter;
        mAdapter.setOnDataChangedListener(this);
        if (adapter.getCount() > 0) {
            mAdapter.notifyDataSetChanged();
        }
    }


    private void setupAdapter() {
        removeAllViews();
        if (mAdapter.getCount() == 1) {
            mFirstView = mAdapter.getView(this, 0);
            addView(mFirstView);
        } else {
            mFirstView = mAdapter.getView(this, 0);
            mSecondView = mAdapter.getView(this, 1);
            addView(mFirstView);
            addView(mSecondView);
            mPosition = 1;
            isStarted = false;
        }
        setBackgroundDrawable(mFirstView.getBackground());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View child = getChildAt(0);
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams lp = (MarginLayoutParams) child
                .getLayoutParams();
        int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
        int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        mBannerHeight = childHeight;
        setMeasuredDimension(childWidth + getPaddingLeft() + getPaddingRight(), childHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    public void onChanged() {
        setupAdapter();
        start();
    }


    public void start() {
        if (mAdapter == null) {
            throw new RuntimeException("you must call setAdapter() before start");
        }

        if (!isStarted && mAdapter.getCount() > 0) {
            isStarted = true;
            postDelayed(mRunnable, mLoadingDuration);
        }
    }

    public void stop() {
        removeCallbacks(mRunnable);
        isStarted = false;
    }

    private AnimRunnable mRunnable = new AnimRunnable();

    private class AnimRunnable implements Runnable {

        @Override
        public void run() {
            postDelayed(this, mLoadingDuration);
            performSwitch();
        }
    }

    private void performSwitch() {
        if (mAdapter.getCount() <= 0) {
            return;
        }
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mFirstView, "translationY", mFirstView.getTranslationY() - mBannerHeight);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mSecondView, "translationY", mSecondView.getTranslationY() - mBannerHeight);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator1, animator2);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFirstView.setTranslationY(0);
                mSecondView.setTranslationY(0);
                View removedView = getChildAt(0);
                mPosition++;
                View view = mAdapter.getView(VerticalBannerView.this, mPosition % mAdapter.getCount());
                removeView(removedView);
                addView(view, 1);
            }
        });
        int mAnimDuration = 1000;
        set.setDuration(mAnimDuration);
        set.start();
    }
}
