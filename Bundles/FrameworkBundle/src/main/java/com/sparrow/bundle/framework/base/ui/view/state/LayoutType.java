package com.sparrow.bundle.framework.base.ui.view.state;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.sparrow.bundle.framework.utils.WindowUtils;

/**
 * @author zhangshaopeng
 * @date 2017/9/26
 * @description
 */
public abstract class LayoutType {

    private ViewStub viewStub;
    private View contentView;
    private boolean isTitle;
    private boolean isFirstLoad = true;

    public LayoutType() {
    }

    public LayoutType(View contentView) {
        this.contentView = contentView;
    }

    public LayoutType(ViewGroup rootView, Context context) {
        init(rootView, context, false);
    }


    public abstract int layoutId();

    public abstract void show(View rootView);

    public abstract void hide();

    public void init(ViewGroup rootView, Context context, boolean isTitle) {
        this.isTitle = isTitle;
        if (contentView == null) {
            viewStub = new ViewStub(context);
            viewStub.setLayoutResource(layoutId());
            rootView.addView(viewStub);
        } else {
            View child = rootView.findViewById(contentView.getId());
            if (child == null) {
                rootView.addView(contentView);
            }
        }
    }

    public View start() {
        if (contentView == null && viewStub != null) {
            contentView = viewStub.inflate();
        }
        if (isTitle && isFirstLoad) {
            isFirstLoad = false;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
            params.setMargins(params.leftMargin, WindowUtils.getActionBarHegith(viewStub.getContext()) + params.topMargin, params.rightMargin, params.bottomMargin);
            contentView.setLayoutParams(params);
        }
        if (contentView == null) {
            throw new NullPointerException("Layout Type Fatal error !");
        }
        show(contentView);
        return contentView;
    }

    public void delete(ViewGroup rootView) {
        if (viewStub != null) {
            rootView.removeView(viewStub);
        } else if (contentView != null) {
            rootView.removeView(contentView);
        }
    }
}
