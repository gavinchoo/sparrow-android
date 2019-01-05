package com.sparrow.bundle.photo.camera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.sparrow.bundle.photo.R;
import com.sparrow.bundle.photo.camera.build.SelectionSpec;
import com.sparrow.bundle.photo.camera.lisenter.ErrorLisenter;
import com.sparrow.bundle.photo.camera.lisenter.JCameraLisenter;
import com.sparrow.bundle.photo.camera.util.DeviceUtil;
import com.sparrow.bundle.photo.camera.util.FileUtil;
import com.sparrow.bundle.photo.matisse.Matisse;
import com.sparrow.bundle.photo.matisse.MimeType;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.model.SelectedItemCollection;

import net.bither.util.NativeUtil;

import java.io.File;
import java.util.ArrayList;

import static com.sparrow.bundle.photo.matisse.internal.ui.BasePreviewActivity.EXTRA_RESULT_APPLY;
import static com.sparrow.bundle.photo.matisse.internal.ui.BasePreviewActivity.EXTRA_RESULT_BUNDLE;
import static com.sparrow.bundle.photo.matisse.internal.ui.BasePreviewActivity.EXTRA_RESULT_UPLOAD;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PREVIEW = 23;
    private JCameraView jCameraView;
    private SelectionSpec selectionSpec = SelectionSpec.getInstance();
    private ArrayList<Item> paths = new ArrayList<>();

    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_photo_camera);

        mSelectedCollection.onCreate(savedInstanceState);

        jCameraView = findViewById(R.id.jcameraview);
        //设置视频保存路径
        jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");
        jCameraView.setFeatures(selectionSpec.buttonState);
        if (selectionSpec.buttonState == JCameraView.BUTTON_STATE_ONLY_CAPTURE) {
            jCameraView.setTip("轻触拍照");
        } else if (selectionSpec.buttonState == JCameraView.BUTTON_STATE_BOTH) {
            jCameraView.setTip("轻触拍照，按住摄像");
        } else if (selectionSpec.buttonState == JCameraView.BUTTON_STATE_ONLY_RECORDER) {
            jCameraView.setTip("按住摄像");
        }

        CameraRectView view = findViewById(R.id.rectview);
        if (selectionSpec.photoType == SelectionSpec.PhotoType.Certificate) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        jCameraView.setRectView(view);
        jCameraView.switchFlash(selectionSpec.switchFlash);
        jCameraView.switchCamera(selectionSpec.switchCamera);
        jCameraView.mulitPhoto(selectionSpec.mulitPhoto);
        jCameraView.defaultFront(selectionSpec.defaultFront);
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        jCameraView.setErrorLisenter(new ErrorLisenter() {
            @Override
            public void onError() {
                //错误监听
                Log.i("CJT", "camera error");
                Intent intent = new Intent();
                setResult(103, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(CameraActivity.this, "给点录音权限可以?", Toast.LENGTH_SHORT).show();
            }
        });
        //JCameraView监听
        jCameraView.setJCameraLisenter(new JCameraLisenter() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                String path = FileUtil.getSavePath(getSaveDir());
                NativeUtil.compressBitmap(bitmap, path);

                File file = new File(path);
                Item item = new Item(Item.ITEM_ID_CAPTURE, MimeType.JPEG.toString(), file.length(), 0, path);
                mSelectedCollection.add(item);
                paths.add(item);
                if (selectionSpec.mulitPhoto) {
                    jCameraView.updateThumbnailAdapter(paths);
                } else {
                    if (selectionSpec.upload) {
                        previewTakePhotos();
                    } else {
                        resultCallBack();
                    }
                }
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                // 获取视频路径
                String path = FileUtil.saveBitmap(getSaveDir(), firstFrame);

                File file = new File(url);
                Item item = new Item(0, MimeType.MPEG.toString(), file.length(), 0, url);
                mSelectedCollection.add(item);
                paths.add(item);
                if (selectionSpec.mulitPhoto) {
                    jCameraView.updateThumbnailAdapter(paths);
                } else {
                    if (selectionSpec.upload) {
                        previewTakePhotos();
                    } else {
                        resultCallBack();
                    }
                }
            }

            @Override
            public void quit() {
                //退出按钮
                CameraActivity.this.finish();
            }

            @Override
            public void mulitConfirm() {
                if (selectionSpec.mulitPhoto && selectionSpec.upload) {
                    previewTakePhotos();
                } else {
                    resultCallBack();
                }
            }
        });

        jCameraView.setOnThumbnailItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                previewTakePhotos();
            }
        });
        Log.i("CJT", DeviceUtil.getDeviceModel());
    }

    private void previewTakePhotos() {
        Matisse.from(CameraActivity.this)
                .choose(MimeType.ofImage(), false)
                .upload(selectionSpec.upload)
                .uploadParams(selectionSpec.uploadParams)
                .maxSelectable(selectionSpec.maxSelectable)
                .maxPhotoable(selectionSpec.maxPhotoable)
                .showSelectOriginal(selectionSpec.selectOriginal)
                .forResultPreview(REQUEST_CODE_PREVIEW, mSelectedCollection
                        .getDataWithBundle());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
    }

    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";

    private void resultCallBack() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, mSelectedCollection.asList());
        setResult(RESULT_OK, intent);
        finish();
    }

    private String getSaveDir() {
        String saveDir = "JCamera";
        if (!TextUtils.isEmpty(selectionSpec.saveDir)) {
            saveDir = selectionSpec.saveDir;
        }
        return saveDir;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PREVIEW) {
            Bundle resultBundle = data.getBundleExtra(EXTRA_RESULT_BUNDLE);
            ArrayList<Item> selected = resultBundle.getParcelableArrayList(SelectedItemCollection
                    .STATE_SELECTION);
            if (data.getBooleanExtra(EXTRA_RESULT_APPLY, false)) {
                Intent result = new Intent();
                if (data.getBooleanExtra(EXTRA_RESULT_UPLOAD, false)) {
                    result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION,
                            resultBundle.getParcelableArrayList(EXTRA_RESULT_SELECTION));
                    result.putExtra(EXTRA_RESULT_UPLOAD, true);
                } else {
                    ArrayList<String> selectedPaths = new ArrayList<>();
                    result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
                    result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
                }

                setResult(RESULT_OK, result);
                finish();
            }
        }
    }
}
