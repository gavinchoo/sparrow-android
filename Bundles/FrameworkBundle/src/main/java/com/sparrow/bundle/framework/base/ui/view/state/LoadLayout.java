package com.sparrow.bundle.framework.base.ui.view.state;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;


/**
 * 布局切换视图
 * 修改来至 https://github.com/lufficc/StateLayout
 */
public class LoadLayout extends FrameLayout {

    private boolean shouldPlayAnim = true;
    private LayoutType currentType;
    private Animation hideAnimation;
    private Animation showAnimation;
    private ArrayMap<Class<? extends LayoutType>, LayoutType> types;

    public LoadLayout(Context context) {
        this(context, null);
    }


    public LoadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void loadTypes(ArrayMap<Class<? extends LayoutType>, LayoutType> types, boolean isTitle) {
        this.types = types;
        for (Class<?> key : types.keySet()) {
            LayoutType type = types.get(key);
            type.init(this, getContext(), isTitle);
        }
    }

    public void setShouldPlayAnim(boolean shouldPlayAnim) {
        this.shouldPlayAnim = shouldPlayAnim;
    }

    public void setShowAnimation(Animation showAnimation) {
        this.showAnimation = showAnimation;
    }

    public void setHideAnimation(Animation hideAnimation) {
        this.hideAnimation = hideAnimation;
    }

    public void setViewSwitchAnimProvider(ViewAnimProvider viewSwitchAnimProvider) {
        if (viewSwitchAnimProvider != null) {
            this.showAnimation = viewSwitchAnimProvider.showAnimation();
            this.hideAnimation = viewSwitchAnimProvider.hideAnimation();
        }
    }

    public void showType(Class<? extends LayoutType> type) {
        if (types == null || !types.containsKey(type)) {
            throw new NullPointerException("No loading this type !!!" + type.getClass().getName());
        }
        switchWithAnimation(types.get(type));
    }

    public void showType(Class<? extends LayoutType> type, String errorMsg) {
        if (types == null || !types.containsKey(type)) {
            throw new NullPointerException("No loading this type !!!" + type.getClass().getName());
        }

        if (types.get(type) instanceof ErrorType && !TextUtils.isEmpty(errorMsg)) {
            ((ErrorType) types.get(type)).setErrorMsg(errorMsg);
        }
        switchWithAnimation(types.get(type));
    }

    public void addType(LayoutType layoutType, boolean isTitle) {
        if (types.containsKey(layoutType.getClass())) {
            LayoutType type = types.get(layoutType.getClass());
            type.delete(this);
        }
        layoutType.init(this, getContext(), isTitle);
        types.put(layoutType.getClass(), layoutType);
    }

    private void switchWithAnimation(final LayoutType layoutType) {
        if (currentType == layoutType) {
            return;
        }
        if (currentType != null) {
            View beforeView = currentType.start();
            if (hideAnimation != null && shouldPlayAnim) {
                hideAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        beforeView.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                hideAnimation.setFillAfter(false);
                beforeView.startAnimation(hideAnimation);
            } else
                beforeView.setVisibility(GONE);
        }
        if (layoutType != null) {
            final View afterView = layoutType.start();
            if (shouldPlayAnim) {
                if (afterView.getVisibility() != VISIBLE)
                    afterView.setVisibility(VISIBLE);
                if (showAnimation != null) {
                    showAnimation.setFillAfter(false);
                    afterView.startAnimation(showAnimation);
                }
            } else {
                afterView.setVisibility(VISIBLE);
            }
            currentType = layoutType;
        }

    }
}
