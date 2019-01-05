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
package com.sparrow.bundle.photo.matisse.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sparrow.bundle.photo.R;
import com.sparrow.bundle.photo.matisse.MimeType;
import com.sparrow.bundle.photo.matisse.internal.entity.Album;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.entity.SelectionSpec;
import com.sparrow.bundle.photo.matisse.internal.model.AlbumCollection;
import com.sparrow.bundle.photo.matisse.internal.model.SelectedItemCollection;
import com.sparrow.bundle.photo.matisse.internal.ui.AlbumPreviewActivity;
import com.sparrow.bundle.photo.matisse.internal.ui.BasePreviewActivity;
import com.sparrow.bundle.photo.matisse.internal.ui.MediaSelectionFragment;
import com.sparrow.bundle.photo.matisse.internal.ui.SelectedPreviewActivity;
import com.sparrow.bundle.photo.matisse.internal.ui.adapter.AlbumMediaAdapter;
import com.sparrow.bundle.photo.matisse.internal.ui.adapter.AlbumsAdapter;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.AlbumsSpinner;
import com.sparrow.bundle.photo.matisse.internal.utils.MediaStoreCompat;
import com.sparrow.bundle.photo.matisse.internal.utils.PathUtils;
import com.sparrow.bundle.photo.matisse.upload.UploadService;
import com.sparrow.bundle.photo.matisse.internal.entity.Album;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.entity.SelectionSpec;
import com.sparrow.bundle.photo.matisse.internal.model.AlbumCollection;
import com.sparrow.bundle.photo.matisse.internal.model.SelectedItemCollection;
import com.sparrow.bundle.photo.matisse.internal.ui.AlbumPreviewActivity;
import com.sparrow.bundle.photo.matisse.internal.ui.BasePreviewActivity;
import com.sparrow.bundle.photo.matisse.internal.ui.MediaSelectionFragment;
import com.sparrow.bundle.photo.matisse.internal.ui.SelectedPreviewActivity;
import com.sparrow.bundle.photo.matisse.internal.ui.adapter.AlbumMediaAdapter;
import com.sparrow.bundle.photo.matisse.internal.ui.adapter.AlbumsAdapter;
import com.sparrow.bundle.photo.matisse.internal.ui.widget.AlbumsSpinner;
import com.sparrow.bundle.photo.matisse.internal.utils.MediaStoreCompat;
import com.sparrow.bundle.photo.matisse.upload.UploadService;

import net.bither.util.NativeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity to display albums and media content (images/videos) in each album
 * and also support media selecting operations.
 */
public class MatisseActivity extends AppCompatActivity implements
        AlbumCollection.AlbumCallbacks, AdapterView.OnItemSelectedListener,
        MediaSelectionFragment.SelectionProvider, View.OnClickListener,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener,
        AlbumMediaAdapter.OnPhotoCapture, UploadService.UploadCallback {

    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";
    private static final int REQUEST_CODE_PREVIEW = 23;
    private static final int REQUEST_CODE_CAPTURE = 24;
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private MediaStoreCompat mMediaStoreCompat;
    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    private SelectionSpec mSpec;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsAdapter mAlbumsAdapter;
    private TextView mButtonPreview;
    private TextView mButtonApply;
    private View mContainer;
    private View mEmptyView;

    private View mOriginalView;
    private CheckBox mOriginalCheckBox;
    private TextView mOriginalTvew;
    private UploadService mUploadService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // programmatically set theme before super.onCreate()
        mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);
        super.onCreate(savedInstanceState);

        setContentView(com.sparrow.bundle.photo.R.layout.activity_matisse);

        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.orientation);
        }

        if (mSpec.capture) {
            mMediaStoreCompat = new MediaStoreCompat(this);
            if (mSpec.captureStrategy == null)
                throw new RuntimeException("Don't forget to set CaptureStrategy.");
            mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);
        }

        Toolbar toolbar = findViewById(com.sparrow.bundle.photo.R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable navigationIcon = toolbar.getNavigationIcon();
        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{com.sparrow.bundle.photo.R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        mButtonPreview = findViewById(com.sparrow.bundle.photo.R.id.button_preview);
        mButtonApply = findViewById(com.sparrow.bundle.photo.R.id.button_apply);
        mButtonPreview.setOnClickListener(this);
        mButtonApply.setOnClickListener(this);
        mContainer = findViewById(com.sparrow.bundle.photo.R.id.container);
        mEmptyView = findViewById(com.sparrow.bundle.photo.R.id.empty_view);

        mSelectedCollection.onCreate(savedInstanceState);
        List<Item> selected = getIntent().getParcelableArrayListExtra(SelectedItemCollection.STATE_SELECTION);
        mSelectedCollection.setDefaultSelection(compressToSource(selected));

        updateBottomToolbar();

        mAlbumsAdapter = new AlbumsAdapter(this, null, false);
        mAlbumsSpinner = new AlbumsSpinner(this);
        mAlbumsSpinner.setOnItemSelectedListener(this);
        mAlbumsSpinner.setSelectedTextView(findViewById(com.sparrow.bundle.photo.R.id.selected_album));
        mAlbumsSpinner.setPopupAnchorView(findViewById(com.sparrow.bundle.photo.R.id.toolbar));
        mAlbumsSpinner.setAdapter(mAlbumsAdapter);
        mAlbumCollection.onCreate(this, this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();

        mOriginalView = findViewById(com.sparrow.bundle.photo.R.id.layout_original);
        mOriginalView.setVisibility(mSpec.selectOriginal ? View.VISIBLE : View.GONE);
        mOriginalCheckBox = findViewById(com.sparrow.bundle.photo.R.id.checkbox_original);
        mOriginalTvew = findViewById(com.sparrow.bundle.photo.R.id.textview_original);

        mOriginalView.setOnClickListener(this);
        mOriginalCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSelectedCollection.setOriginalSelected(isChecked);
                updateOriginal();
            }
        });
    }

    private List<Item> compressToSource(List<Item> compressItems) {
        if (null == compressItems)
            return null;

        for (int i = 0; i < compressItems.size(); i++) {
            if (!TextUtils.isEmpty(compressItems.get(i).sourcePath)) {
                compressItems.get(i).path = compressItems.get(i).sourcePath;
            }
        }
        return compressItems;
    }

    private ArrayList<Item> sourceToCompress(ArrayList<Item> sourceItems) {
        if (null == sourceItems)
            return null;

        String rootPath = parentPath.getAbsolutePath() + File.separator + mSpec.saveDir;
        if (!TextUtils.isEmpty(rootPath)) {
            File file = new File(rootPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        for (int i = 0; i < sourceItems.size(); i++) {
            File file = new File(sourceItems.get(i).path);
            String compressPath = rootPath + File.separator + file.getName();
            if (!new File(compressPath).exists() && file.exists()) {
                NativeUtil.compressBitmap(sourceItems.get(i).path, compressPath);
            }
            sourceItems.get(i).sourcePath = new String(sourceItems.get(i).path);
            sourceItems.get(i).path = compressPath;
        }
        return sourceItems;
    }

    private static final File parentPath = Environment.getExternalStorageDirectory();


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumCollection.onDestroy();
        if (null != mUploadService) {
            mUploadService.destory();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CODE_PREVIEW) {
            Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);

            ArrayList<Item> selected = resultBundle.getParcelableArrayList(
                    SelectedItemCollection.STATE_SELECTION);
            int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE,
                    SelectedItemCollection.COLLECTION_UNDEFINED);
            boolean isOriginalSelected = resultBundle.getBoolean(SelectedItemCollection.STATE_ORIGINAL_STATUS);

            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                Intent result = new Intent();
                if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_UPLOAD, false)) {
                    result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION,
                            resultBundle.getParcelableArrayList(EXTRA_RESULT_SELECTION));
                    result.putExtra(BasePreviewActivity.EXTRA_RESULT_UPLOAD, true);
                } else {
                    result.putExtra(SelectedItemCollection.STATE_ORIGINAL_STATUS, mSelectedCollection.isOriginalSelected());
                    // 压缩文件后返回
                    result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, sourceToCompress(selected));
                }

                setResult(RESULT_OK, result);
                finish();
            } else {
                mSelectedCollection.overwrite(selected, collectionType);
                mSelectedCollection.setOriginalSelected(isOriginalSelected);
                Fragment mediaSelectionFragment = getSupportFragmentManager().findFragmentByTag(
                        MediaSelectionFragment.class.getSimpleName());
                if (mediaSelectionFragment instanceof MediaSelectionFragment) {
                    ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
                }
                updateBottomToolbar();
                updateOriginal();
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE) {
            // Just pass the data back to previous calling Activity.
            Uri contentUri = mMediaStoreCompat.getCurrentPhotoUri();
            String path = mMediaStoreCompat.getCurrentPhotoPath();
//            ArrayList<Uri> selected = new ArrayList<>();
//            selected.add(contentUri);
//            ArrayList<String> selectedPath = new ArrayList<>();
//            selectedPath.add(path);

            Item item = new Item(0, MimeType.JPEG.toString(), 0, 0, path);
            ArrayList<Item> selected = new ArrayList<>();
            selected.add(item);
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
            // result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath);
            setResult(RESULT_OK, result);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                MatisseActivity.this.revokeUriPermission(contentUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent
                                .FLAG_GRANT_READ_URI_PERMISSION);
            finish();
        }
    }

    private void updateBottomToolbar() {
        int selectedCount = mSelectedCollection.count();
        if (selectedCount == 0) {
            mButtonPreview.setEnabled(false);
            mButtonApply.setEnabled(false);
            mButtonApply.setText(getString(getApplyDefaultResId()));
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            mButtonPreview.setEnabled(true);
            mButtonApply.setText(getString(getApplyDefaultResId()));
            mButtonApply.setEnabled(true);
        } else {
            mButtonPreview.setEnabled(true);
            mButtonApply.setEnabled(true);
            mButtonApply.setText(getString(getApplyResId(), selectedCount));
        }
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

    private int getApplyDefaultResId() {
        return mSpec.upload ? com.sparrow.bundle.photo.R.string.button_upload_default : com.sparrow.bundle.photo.R.string.button_apply_default;
    }

    private int getApplyResId() {
        return mSpec.upload ? com.sparrow.bundle.photo.R.string.button_upload : com.sparrow.bundle.photo.R.string.button_apply;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == com.sparrow.bundle.photo.R.id.button_preview) {
            Intent intent = new Intent(this, SelectedPreviewActivity.class);
            intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection
                    .getDataWithBundle());
            startActivityForResult(intent, REQUEST_CODE_PREVIEW);
        } else if (v.getId() == com.sparrow.bundle.photo.R.id.button_apply) {
            if (mSpec.upload) {
                mButtonApply.setEnabled(false);
                mUploadService = new UploadService(this)
                        .uploadParams(mSpec.uploadParams)
                        .uploadData(mSelectedCollection.asList())
                        .uploadCallBack(this)
                        .start();
            } else {
                Intent result = new Intent();
                result.putExtra(SelectedItemCollection.STATE_ORIGINAL_STATUS, mSelectedCollection.isOriginalSelected());
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, sourceToCompress(mSelectedCollection.asList()));
                setResult(RESULT_OK, result);
                finish();
            }
        } else if (v.getId() == com.sparrow.bundle.photo.R.id.layout_original) {
            boolean toggle = !mOriginalCheckBox.isChecked();
            mOriginalCheckBox.setChecked(toggle);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAlbumCollection.setStateCurrentSelection(position);
        mAlbumsAdapter.getCursor().moveToPosition(position);
        Album album = Album.valueOf(mAlbumsAdapter.getCursor());
        if (album.isAll() && SelectionSpec.getInstance().capture) {
            album.addCaptureCount();
        }
        onAlbumSelected(album);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onAlbumLoad(final Cursor cursor) {
        mAlbumsAdapter.swapCursor(cursor);
        // select default album.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
                mAlbumsSpinner.setSelection(MatisseActivity.this,
                        mAlbumCollection.getCurrentSelection());
                Album album = Album.valueOf(cursor);
                if (album.isAll() && SelectionSpec.getInstance().capture) {
                    album.addCaptureCount();
                }
                onAlbumSelected(album);
            }
        });
    }

    @Override
    public void onAlbumReset() {
        mAlbumsAdapter.swapCursor(null);
    }

    private void onAlbumSelected(Album album) {
        if (album.isAll() && album.isEmpty()) {
            mContainer.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mContainer.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            Fragment fragment = MediaSelectionFragment.newInstance(album);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(com.sparrow.bundle.photo.R.id.container, fragment, MediaSelectionFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar();
        updateOriginal();
    }

    @Override
    public void onMediaClick(Album album, Item item, int adapterPosition) {
        Intent intent = new Intent(this, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection
                .getDataWithBundle());
        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

    @Override
    public SelectedItemCollection provideSelectedItemCollection() {
        return mSelectedCollection;
    }

    @Override
    public void capture() {
        if (mMediaStoreCompat != null) {
            mMediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE);
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

    protected void sendBackResultUpload(boolean apply) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, mUploadService.getData());
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, apply);
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_UPLOAD, true);
        setResult(Activity.RESULT_OK, intent);
    }
}
