/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sparrow.bundle.photo.matisse.internal.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sparrow.bundle.photo.R;
import com.sparrow.bundle.photo.matisse.internal.entity.IncapableCause;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.entity.SelectionSpec;
import com.sparrow.bundle.photo.matisse.internal.model.SelectedItemCollection;
import com.sparrow.bundle.photo.matisse.internal.ui.adapter.PreviewPagerAdapter;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.CheckView;
import com.sparrow.bundle.photo.matisse.internal.utils.PhotoMetadataUtils;
import com.sparrow.bundle.photo.matisse.internal.utils.Platform;
import com.sparrow.bundle.photo.matisse.upload.UploadService;
import com.sparrow.bundle.photo.matisse.internal.entity.IncapableCause;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.entity.SelectionSpec;
import com.sparrow.bundle.photo.matisse.internal.model.SelectedItemCollection;
import com.sparrow.bundle.photo.matisse.internal.ui.adapter.PreviewPagerAdapter;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.CheckView;
import com.sparrow.bundle.photo.matisse.internal.utils.PhotoMetadataUtils;
import com.sparrow.bundle.photo.matisse.internal.utils.Platform;

public abstract class BasePreviewActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, UploadService.UploadCallback {

    public static final String EXTRA_DEFAULT_BUNDLE = "extra_default_bundle";
    public static final String EXTRA_RESULT_BUNDLE = "extra_result_bundle";
    public static final String EXTRA_RESULT_APPLY = "extra_result_apply";
    public static final String EXTRA_RESULT_UPLOAD = "extra_result_upload";

    protected final SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    protected SelectionSpec mSpec;
    protected ViewPager mPager;

    protected PreviewPagerAdapter mAdapter;

    protected CheckView mCheckView;
    protected TextView mButtonBack;
    protected TextView mButtonApply;
    protected TextView mSize;

    private View mOriginalView;
    private CheckBox mOriginalCheckBox;
    private TextView mOriginalTvew;

    protected int mPreviousPos = -1;

    private UploadService mUploadService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(SelectionSpec.getInstance().themeId);
        super.onCreate(savedInstanceState);
        setContentView(com.sparrow.bundle.photo.R.layout.activity_media_preview);
        if (Platform.hasKitKat()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        hideBottomUIMenu();

        mSpec = SelectionSpec.getInstance();
        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.orientation);
        }

        if (savedInstanceState == null) {
            mSelectedCollection.onCreate(getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE));
        } else {
            mSelectedCollection.onCreate(savedInstanceState);
        }

        mButtonBack = findViewById(com.sparrow.bundle.photo.R.id.button_back);
        mButtonApply = findViewById(com.sparrow.bundle.photo.R.id.button_apply);
        mSize = findViewById(com.sparrow.bundle.photo.R.id.size);
        mButtonBack.setOnClickListener(this);
        mButtonApply.setOnClickListener(this);

        mPager = findViewById(com.sparrow.bundle.photo.R.id.pager);
        mPager.addOnPageChangeListener(this);
        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
        mPager.setAdapter(mAdapter);
        mCheckView = findViewById(com.sparrow.bundle.photo.R.id.check_view);
        mCheckView.setCountable(mSpec.countable);

        mCheckView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Item item = mAdapter.getMediaItem(mPager.getCurrentItem());
                if (mSelectedCollection.isSelected(item)) {
                    mSelectedCollection.remove(item);
                    if (mSpec.countable) {
                        mCheckView.setCheckedNum(CheckView.UNCHECKED);
                    } else {
                        mCheckView.setChecked(false);
                    }
                } else {
                    if (assertAddSelection(item)) {
                        mSelectedCollection.add(item);
                        if (mSpec.countable) {
                            mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                        } else {
                            mCheckView.setChecked(true);
                        }
                    }
                }
                updateApplyButton();
                updateOriginal();
            }
        });

        mOriginalView = findViewById(com.sparrow.bundle.photo.R.id.layout_original);
        mOriginalView.setVisibility(mSpec.selectOriginal ? View.VISIBLE : View.GONE);
        mOriginalCheckBox = findViewById(com.sparrow.bundle.photo.R.id.checkbox_original);
        mOriginalTvew = findViewById(com.sparrow.bundle.photo.R.id.textview_original);
        mOriginalView.setOnClickListener(this);

        mOriginalCheckBox.setChecked(mSelectedCollection.isOriginalSelected());
        mOriginalCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSelectedCollection.setOriginalSelected(isChecked);
                updateOriginal();
            }
        });

        updateApplyButton();
        updateOriginal();
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mSelectedCollection.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        sendBackResult(false);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mUploadService) {
            mUploadService.destory();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == com.sparrow.bundle.photo.R.id.button_back) {
            onBackPressed();
        } else if (v.getId() == com.sparrow.bundle.photo.R.id.button_apply) {
            if (mSpec.upload) {
                mButtonApply.setEnabled(false);
                mUploadService = new UploadService(this)
                        .uploadParams(mSpec.uploadParams)
                        .uploadData(mSelectedCollection.asList())
                        .uploadCallBack(this)
                        .start();
            } else {
                sendBackResult(true);
                finish();
            }
        } else if (v.getId() == com.sparrow.bundle.photo.R.id.layout_original) {
            boolean toggle = !mOriginalCheckBox.isChecked();
            mOriginalCheckBox.setChecked(toggle);
        }
    }

    @Override
    public void uploadSuccess() {
        sendBackResultUpload(true);
        finish();
    }

    @Override
    public void uploadCancel() {
        mButtonApply.setEnabled(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mPager.getAdapter();
        if (mPreviousPos != -1 && mPreviousPos != position) {
            ((PreviewItemFragment) adapter.instantiateItem(mPager, mPreviousPos)).resetView();

            Item item = adapter.getMediaItem(position);
            if (mSpec.countable) {
                int checkedNum = mSelectedCollection.checkedNumOf(item);
                mCheckView.setCheckedNum(checkedNum);
                if (checkedNum > 0) {
                    mCheckView.setEnabled(true);
                } else {
                    mCheckView.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            } else {
                boolean checked = mSelectedCollection.isSelected(item);
                mCheckView.setChecked(checked);
                if (checked) {
                    mCheckView.setEnabled(true);
                } else {
                    mCheckView.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            }
            updateSize(item);
        }
        mPreviousPos = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateOriginal() {
        boolean isChecked = mSelectedCollection.isOriginalSelected();
        if (isChecked) {
            String showSize = mSelectedCollection.totalStrSize();
            if (!TextUtils.isEmpty(showSize)) {
                mOriginalTvew.setText(getString(com.sparrow.bundle.photo.R.string.button_original, showSize));
            } else {
                mOriginalTvew.setText(getString(com.sparrow.bundle.photo.R.string.button_original_default));
            }
        } else {
            mOriginalTvew.setText(getString(com.sparrow.bundle.photo.R.string.button_original_default));
        }
    }

    private void updateApplyButton() {
        int selectedCount = mSelectedCollection.count();
        if (selectedCount == 0) {
            mButtonApply.setText(getApplyDefaultResId());
            mButtonApply.setEnabled(false);
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            mButtonApply.setText(getApplyDefaultResId());
            mButtonApply.setEnabled(true);
        } else {
            mButtonApply.setEnabled(true);
            mButtonApply.setText(getString(getApplyResId(), selectedCount));
        }
    }

    private int getApplyDefaultResId() {
        return mSpec.upload ? com.sparrow.bundle.photo.R.string.button_upload_default : com.sparrow.bundle.photo.R.string.button_apply_default;
    }

    private int getApplyResId() {
        return mSpec.upload ? com.sparrow.bundle.photo.R.string.button_upload : com.sparrow.bundle.photo.R.string.button_apply;
    }

    protected void updateSize(Item item) {
        if (item.isGif()) {
            mSize.setVisibility(View.VISIBLE);
            mSize.setText(PhotoMetadataUtils.getSizeInMB(item.size) + "M");
        } else {
            mSize.setVisibility(View.GONE);
        }
    }

    protected void sendBackResult(boolean apply) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(EXTRA_RESULT_APPLY, apply);
        setResult(Activity.RESULT_OK, intent);
    }

    protected void sendBackResultUpload(boolean apply) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_BUNDLE, mUploadService.getDataWithBundle());
        intent.putExtra(EXTRA_RESULT_APPLY, apply);
        intent.putExtra(EXTRA_RESULT_UPLOAD, true);
        setResult(Activity.RESULT_OK, intent);
    }

    private boolean assertAddSelection(Item item) {
        IncapableCause cause = mSelectedCollection.isAcceptable(item);
        IncapableCause.handleCause(this, cause);
        return cause == null;
    }
}
