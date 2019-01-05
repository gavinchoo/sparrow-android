package com.sparrow.bundle.framework.base.ui.view.state;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sparrow.bundle.framework.R;

/**
 * @author zhangshaopeng
 * @date 2017/11/16
 * @description
 */
public class ErrorType extends ContentType {
    private View.OnClickListener clickListener;
    private boolean mShowNetworkError = false;//是否无网络
    private String errorMsg;
    public ErrorType() {
        super(com.sparrow.bundle.framework.R.layout.view_error);
    }

    public ErrorType(View.OnClickListener clickListener, boolean showNetworkError) {
        super(com.sparrow.bundle.framework.R.layout.view_error);
        this.clickListener = clickListener;
        this.mShowNetworkError = showNetworkError;
    }

    @Override
    public void show(View rootView) {
        super.show(rootView);
        ImageView imageView = rootView.findViewById(com.sparrow.bundle.framework.R.id.errorImageView);
        TextView tvError = rootView.findViewById(com.sparrow.bundle.framework.R.id.errorTextView);
        if (mShowNetworkError) {
            imageView.setImageResource(com.sparrow.bundle.framework.R.drawable.icon_error);
            tvError.setText(com.sparrow.bundle.framework.R.string.network_error);
        } else if (!TextUtils.isEmpty(errorMsg)){
            imageView.setImageResource(com.sparrow.bundle.framework.R.drawable.icon_error);
            tvError.setText(errorMsg);
        } else {
            imageView.setImageResource(com.sparrow.bundle.framework.R.drawable.icon_error);
            tvError.setText(com.sparrow.bundle.framework.R.string.error_occur);
        }
        if (clickListener != null) {
            rootView.setOnClickListener(clickListener);
        }
    }

    public void setErrorMsg(String errorMsg){
        this.errorMsg = errorMsg;
    }

}
