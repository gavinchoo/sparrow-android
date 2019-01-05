package com.sparrow.bundle.framework.base.ui.view.state;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import com.sparrow.bundle.framework.R;

/**
 * @author zhangshaopeng
 * @date 2017/9/26
 * @description
 */

public class LoadingType extends LayoutType {

    @Override
    public int layoutId() {
        return com.sparrow.bundle.framework.R.layout.view_progress;
    }

    @Override
    public void show(View rootView) {
    }

    @Override
    public void hide() {
    }
}
