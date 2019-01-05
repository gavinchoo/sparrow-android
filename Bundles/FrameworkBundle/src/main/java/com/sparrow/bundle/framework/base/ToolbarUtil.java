package com.sparrow.bundle.framework.base;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.sparrow.bundle.framework.R;

import java.util.concurrent.TimeUnit;

public class ToolbarUtil {

    private AppCompatActivity activity;
    private Toolbar toolbar;

    public ToolbarUtil(AppCompatActivity activity) {
        this.activity = activity;
        initToolBar();
    }

    public ToolbarUtil setBackground(int color) {
        initToolBar();
        toolbar.setBackgroundColor(color);
        return this;
    }

    public ToolbarUtil setTitle(@StringRes int resId) {
        initToolBar();
        setTitle(activity.getString(resId));
        return this;
    }

    public ToolbarUtil setTitle(CharSequence sequence) {
        setTitle(sequence, 0);
        return this;
    }

    public ToolbarUtil setTitle(CharSequence sequence, int titleWidth) {
        initToolBar();
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_title);
        if (null != tvewTitle) {
            tvewTitle.setText(sequence);
            if (titleWidth > 0) {
                tvewTitle.setWidth(titleWidth);
            }
        }

        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        activity.getSupportActionBar().setHomeAsUpIndicator(com.sparrow.bundle.framework.R.drawable.icon_back);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (null != toolbar) {
            toolbar.setNavigationOnClickListener(view -> {
                if (null == this.innerListener) {
                    activity.finish();
                } else {
                    this.innerListener.onClick(view);
                }
            });
        }
        return this;
    }

    public View.OnClickListener innerListener;

    public void setInnerBackClickListener(View.OnClickListener innerListener) {
        this.innerListener = innerListener;
    }

    public ToolbarUtil setRightText(@StringRes int resId) {
        initToolBar();
        setRightText(activity.getResources().getString(resId));
        return this;
    }

    public ToolbarUtil setRightText(@StringRes int resId, int titleWidth) {
        initToolBar();
        setRightText(activity.getResources().getString(resId), titleWidth);
        return this;
    }

    public ToolbarUtil setRightText(CharSequence sequence) {
        initToolBar();
        TextView tvewRight = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_txt);
        if (null != tvewRight) {
            tvewRight.setVisibility(View.VISIBLE);
            tvewRight.setText(sequence);
        }
        return this;
    }

    public ToolbarUtil setRightText(CharSequence sequence, int titleWidth) {
        initToolBar();
        TextView tvewRight = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_txt);
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_title);
        if (null != tvewTitle) {
            tvewTitle.setWidth(titleWidth);
            tvewRight.setVisibility(View.VISIBLE);
            tvewRight.setText(sequence);
        }
        return this;
    }

    public TextView getRightTextView() {
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_txt);
        return tvewTitle;
    }

    public ImageView getRightImageView() {
        ImageView imageView = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_img);
        return imageView;
    }

    public void hideTitle() {
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_title);
        if (null != tvewTitle) {
            tvewTitle.setVisibility(View.GONE);
        }
    }

    public void showTitle() {
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_title);
        if (null != tvewTitle) {
            tvewTitle.setVisibility(View.VISIBLE);
        }
    }

    public void hideRightTextView() {
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_txt);
        if (null != tvewTitle) {
            tvewTitle.setVisibility(View.GONE);
        }
    }

    public void hideRightImageView() {
        ImageView imageView = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_img);
        if (null != imageView) {
            imageView.setVisibility(View.GONE);
        }
    }

    public void showRightTextView() {
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_txt);
        if (null != tvewTitle) {
            tvewTitle.setVisibility(View.VISIBLE);
        }
    }

    public void showRightImageView() {
        ImageView imageView = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_img);
        if (null != imageView) {
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private ToolbarUtil initToolBar() {
        if (null == toolbar) {
            toolbar = activity.findViewById(com.sparrow.bundle.framework.R.id.common_toolbar);
            activity.setSupportActionBar(toolbar);
        }
        return this;
    }

    public ToolbarUtil setLeftIndicator(@DrawableRes int resId) {
        initToolBar();
        activity.getSupportActionBar().setHomeAsUpIndicator(resId);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return this;
    }

    public ToolbarUtil setOnLeftClickListener(View.OnClickListener listener) {
        if (null != toolbar) {
            toolbar.setNavigationOnClickListener(listener);
        }
        return this;
    }

    public ToolbarUtil setOnRightClickListener(View.OnClickListener listener) {
        initToolBar();
        TextView tvewTitle = activity.findViewById(com.sparrow.bundle.framework.R.id.common_right_txt);
        if (null != toolbar) {
            RxView.clicks(tvewTitle).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if (null != listener)
                    listener.onClick(tvewTitle);
            });
        }
        return this;
    }

    public ToolbarUtil hideToolBar() {
        initToolBar();
        activity.findViewById(com.sparrow.bundle.framework.R.id.common_toolbar).setVisibility(View.GONE);
        return this;
    }

    public ToolbarUtil hideLeftIndicator() {
        initToolBar();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        return this;
    }

    public ToolbarUtil showToolBar() {
        initToolBar();
        activity.findViewById(com.sparrow.bundle.framework.R.id.common_toolbar).setVisibility(View.VISIBLE);
        return this;
    }

    public ToolbarUtil showLeftIndicator() {
        initToolBar();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return this;
    }

    public ToolbarUtil setBackgroundColor(int color) {
        initToolBar();
        toolbar.setBackgroundColor(color);
        return this;
    }

    public ToolbarUtil setTitleTextColor(int color) {
        initToolBar();
        TextView textView = activity.findViewById(com.sparrow.bundle.framework.R.id.common_title);
        textView.setTextColor(color);
        return this;
    }
}
