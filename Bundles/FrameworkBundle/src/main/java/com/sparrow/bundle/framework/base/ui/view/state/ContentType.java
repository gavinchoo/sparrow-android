package com.sparrow.bundle.framework.base.ui.view.state;

import android.view.View;

/**
 * @author zhangshaopeng
 * @date 2017/9/26
 * @description
 */

public class ContentType extends LayoutType {

    private int layoutId = 0;

    public ContentType(int layoutId) {
        this.layoutId = layoutId;
    }

    public ContentType(View contentView) {
        super(contentView);
    }

    @Override
    public int layoutId() {
        return layoutId;
    }

    @Override
    public void show(View rootView) {

    }

    @Override
    public void hide() {

    }
}
