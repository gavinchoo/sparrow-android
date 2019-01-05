package com.sparrow.bundle.framework.base.ui.view.state;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sparrow.bundle.framework.R;

/**
 * @author zhangshaopeng
 * @date 2018/8/25
 * @description
 */
public class SearchEmptyType extends LayoutType {
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private View emptyContentView;
    private TextView emptyPromptTextView;
    private int layoutId;
    private int imageResource;
    private String emptyContent;


    public SearchEmptyType() {
        this.layoutId = com.sparrow.bundle.framework.R.layout.view_search_empty;
    }

    @Override
    public int layoutId() {
        return layoutId;
    }

    @Override
    public void show(View emptyView) {
        emptyContentView = emptyView.findViewById(com.sparrow.bundle.framework.R.id.empty_content);
        emptyTextView = emptyView.findViewById(com.sparrow.bundle.framework.R.id.emptyTextView);
        emptyPromptTextView = emptyView.findViewById(com.sparrow.bundle.framework.R.id.emptyPromptTextView);
        emptyImageView = emptyView.findViewById(com.sparrow.bundle.framework.R.id.searchEmptyImageView);
        if (!TextUtils.isEmpty(emptyContent)) {
            emptyTextView.setText(emptyContent);
        }
        if (imageResource != 0)
            emptyImageView.setImageResource(imageResource);
    }

    @Override
    public void hide() {

    }

    public SearchEmptyType setImageResource(int imageResource) {
        this.imageResource = imageResource;
        return this;
    }

    public SearchEmptyType setEmptyContent(String emptyContent) {
        this.emptyContent = emptyContent;
        return this;
    }
}
