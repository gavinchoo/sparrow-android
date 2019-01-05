package com.sparrow.bundle.framework.binding.viewadapter.image;


import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.utils.ImageUtils;

/**
 * Created by goldze on 2017/6/18.
 */
public final class ViewAdapter {
    @BindingAdapter(value = {"url", "placeholderRes", "round"}, requireAll = false)
    public static void setImageUri(ImageView imageView, String url, int placeholderRes, boolean round) {
        if (!TextUtils.isEmpty(url)) {
            if (round) {
                RoundedCorners roundedCorners = new RoundedCorners(20);
                ImageUtils.loadImage(imageView.getContext(), imageView, url, RequestOptions.bitmapTransform(roundedCorners).placeholder(placeholderRes));
            } else {
                ImageUtils.loadImage(imageView.getContext(), imageView, url, new RequestOptions().placeholder(placeholderRes));
            }
        }
    }

    @BindingAdapter(value = {"roundurl", "placeholderRes"}, requireAll = false)
    public static void setRoundImageUri(ImageView imageView, String roundurl, int placeholderRes) {
        if (!TextUtils.isEmpty(roundurl)) {
            //使用Glide框架加载图片
            RoundedCorners roundedCorners = new RoundedCorners(20);
            Glide.with(imageView.getContext())
                    .load(roundurl)
                    .apply(RequestOptions.bitmapTransform(roundedCorners)
                            .centerCrop().placeholder(placeholderRes).error(com.sparrow.bundle.framework.R.drawable.image_load_error_bg))
                    .into(imageView);
        }else {
            imageView.setImageResource(placeholderRes);
        }
    }

    @BindingAdapter("android:src")
    public static void setSrc(ImageView view, int resId) {
        view.setImageResource(resId);
    }

}

