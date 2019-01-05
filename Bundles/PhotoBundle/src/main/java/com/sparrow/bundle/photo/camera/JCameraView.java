package com.sparrow.bundle.photo.camera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.sparrow.bundle.photo.R;
import com.sparrow.bundle.photo.camera.build.SelectionSpec;
import com.sparrow.bundle.photo.camera.lisenter.CaptureLisenter;
import com.sparrow.bundle.photo.camera.lisenter.ErrorLisenter;
import com.sparrow.bundle.photo.camera.lisenter.FirstFoucsLisenter;
import com.sparrow.bundle.photo.camera.lisenter.JCameraLisenter;
import com.sparrow.bundle.photo.camera.lisenter.ReturnLisenter;
import com.sparrow.bundle.photo.camera.lisenter.TypeLisenter;
import com.sparrow.bundle.photo.camera.util.BitmapUtil;
import com.sparrow.bundle.photo.camera.util.CameraUtils;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.camera.build.SelectionSpec;
import com.sparrow.bundle.photo.camera.lisenter.CaptureLisenter;
import com.sparrow.bundle.photo.camera.lisenter.ErrorLisenter;
import com.sparrow.bundle.photo.camera.lisenter.FirstFoucsLisenter;
import com.sparrow.bundle.photo.camera.lisenter.JCameraLisenter;
import com.sparrow.bundle.photo.camera.lisenter.ReturnLisenter;
import com.sparrow.bundle.photo.camera.lisenter.TypeLisenter;
import com.sparrow.bundle.photo.camera.util.BitmapUtil;
import com.sparrow.bundle.photo.camera.util.CameraUtils;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;

import java.io.File;
import java.io.IOException;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class JCameraView extends FrameLayout implements CameraInterface.CamOpenOverCallback,
        SurfaceHolder.Callback {
    private static final String TAG = "CJT";

    //拍照浏览时候的类型
    private static final int TYPE_PICTURE = 0x001;
    private static final int TYPE_VIDEO = 0x002;


    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 20 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
    public static final int MEDIA_QUALITY_LOW = 12 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 4 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 2 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 1 * 80000;

    //只能拍照
    public static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;
    //只能录像
    public static final int BUTTON_STATE_ONLY_RECORDER = 0x102;
    //两者都可以
    public static final int BUTTON_STATE_BOTH = 0x103;

    //回调监听
    private JCameraLisenter jCameraLisenter;


    private Context mContext;
    private VideoView mVideoView;
    private ImageViewTouch mPhoto;
    private ImageView mSwitchCamera;
    private ImageView mSwitchFlash;
    private CaptureLayout mCaptureLayout;
    private RecyclerView mRecyclerView;

    private FoucsView mFoucsView;
    private MediaPlayer mMediaPlayer;
    private CameraRectView mRectView;

    private ThumbnailAdapter mThumbnailAdapter;

    private int layout_width;
    private int fouce_size;
    private float screenProp;

    //拍照的图片
    private Bitmap captureBitmap;
    //第一帧图片
    private Bitmap firstFrame;
    //视频URL
    private String videoUrl;
    private int type = -1;
    private boolean onlyPause = false;

    private int CAMERA_STATE = -1;
    private static final int STATE_IDLE = 0x010;
    private static final int STATE_RUNNING = 0x020;
    private static final int STATE_WAIT = 0x030;

    private boolean stopping = false;
    private boolean isBorrow = false;
    private boolean takePictureing = false;
    private boolean forbiddenSwitch = false;

    /**
     * switch buttom param
     */
    private int iconSize = 0;
    private int iconMargin = 0;
    private int iconSrc = 0;
    private int duration = 0;

    /**
     * constructor
     */
    public JCameraView(Context context) {
        this(context, null);
    }

    /**
     * constructor
     */
    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * constructor
     */
    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //get AttributeSet
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, com.sparrow.bundle.photo.R.styleable.JCameraView,
                defStyleAttr, 0);
        iconSize = a.getDimensionPixelSize(com.sparrow.bundle.photo.R.styleable.JCameraView_iconSize, (int) TypedValue
                .applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics()));
        iconMargin = a.getDimensionPixelSize(com.sparrow.bundle.photo.R.styleable.JCameraView_iconMargin, (int) TypedValue
                .applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        iconSrc = a.getResourceId(com.sparrow.bundle.photo.R.styleable.JCameraView_iconSrc, com.sparrow.bundle.photo.R.drawable.ic_sync_black_24dp);
        duration = a.getInteger(com.sparrow.bundle.photo.R.styleable.JCameraView_duration_max, 10 * 1000);
        a.recycle();
        initData();
        initView();
    }

    private void initData() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        layout_width = outMetrics.widthPixels;
        fouce_size = layout_width / 4;
        CAMERA_STATE = STATE_IDLE;
    }

    public void setRectView(CameraRectView rectView) {
        this.mRectView = rectView;
    }

    private void initView() {
        setWillNotDraw(false);
        this.setBackgroundColor(0xff000000);
        //VideoView
        mVideoView = new VideoView(mContext);
        LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams
                .MATCH_PARENT);
        mVideoView.setLayoutParams(videoViewParam);

        //mPhoto
        mPhoto = new ImageViewTouch(mContext);
        mPhoto.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        LayoutParams photoParam = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams
                .MATCH_PARENT);
        mPhoto.setLayoutParams(photoParam);
        mPhoto.setBackgroundColor(0xff000000);
        mPhoto.setVisibility(INVISIBLE);

        //switchCamera
        mSwitchCamera = new ImageView(mContext);
        LayoutParams imageViewParam = new LayoutParams(iconSize + 2 * iconMargin, iconSize + 2 *
                iconMargin);
        imageViewParam.gravity = Gravity.RIGHT;
        mSwitchCamera.setPadding(iconMargin, iconMargin, iconMargin, iconMargin);
        mSwitchCamera.setLayoutParams(imageViewParam);
        mSwitchCamera.setImageResource(iconSrc);
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBorrow || switching || forbiddenSwitch) {
                    return;
                }
                switching = true;
                new Thread() {
                    /**
                     * switch camera
                     */
                    @Override
                    public void run() {
                        CameraInterface.getInstance().switchCamera(JCameraView.this);
                    }
                }.start();
            }
        });
        // switchflash
        mSwitchFlash = new ImageView(mContext);
        imageViewParam = new LayoutParams(iconSize + 2 * iconMargin, iconSize + 2 * iconMargin);
        imageViewParam.gravity = Gravity.LEFT;
        mSwitchFlash.setPadding(iconMargin, iconMargin, iconMargin, iconMargin);
        mSwitchFlash.setLayoutParams(imageViewParam);
        mSwitchFlash.setImageResource(com.sparrow.bundle.photo.R.drawable.camera_flash_close);
        mSwitchFlash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int state = CameraUtils.getCameraFlash(mContext);
                if (state == 2) {
                    mSwitchFlash.setImageResource(com.sparrow.bundle.photo.R.drawable.camera_flash_open);
                } else {
                    mSwitchFlash.setImageResource(com.sparrow.bundle.photo.R.drawable.camera_flash_close);
                }
                CameraInterface.getInstance().switchFlash(mContext);
            }
        });

        //CaptureLayout
        mCaptureLayout = new CaptureLayout(mContext);
        LayoutParams layout_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
                .WRAP_CONTENT);
        layout_param.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        mCaptureLayout.setLayoutParams(layout_param);
        mCaptureLayout.setDuration(duration);

        mRecyclerView = new RecyclerView(mContext);
        mRecyclerView.setBackgroundColor(Color.parseColor("#30000000"));
        int captureHeight = mCaptureLayout.getHeight(mContext);
        int thumbnailHeight = (int) TypedValue
                .applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
        final int margin = (int) TypedValue
                .applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
        LayoutParams recycler_param = new LayoutParams(LayoutParams.MATCH_PARENT, thumbnailHeight);
        recycler_param.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        recycler_param.setMargins(0, 0, 0, captureHeight);

        mRecyclerView.setLayoutParams(recycler_param);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(-margin));
        mThumbnailAdapter = new ThumbnailAdapter(mContext);
        mThumbnailAdapter.setThumbnailHeight(thumbnailHeight);
        mRecyclerView.setAdapter(mThumbnailAdapter);
        mRecyclerView.setVisibility(GONE);

        //mFoucsView
        mFoucsView = new FoucsView(mContext, fouce_size);
        LayoutParams foucs_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
                .WRAP_CONTENT);
        foucs_param.gravity = Gravity.CENTER;
        mFoucsView.setLayoutParams(foucs_param);
        mFoucsView.setVisibility(INVISIBLE);

        //add view to ParentLayout
        this.addView(mVideoView);
        this.addView(mRecyclerView);
        this.addView(mPhoto);
        this.addView(mSwitchCamera);
        this.addView(mSwitchFlash);
        this.addView(mCaptureLayout);
        this.addView(mFoucsView);

        //START >>>>>>> captureLayout lisenter callback
        mCaptureLayout.setCaptureLisenter(new CaptureLisenter() {
            @Override
            public void takePictures() {
                if (CAMERA_STATE != STATE_IDLE || takePictureing) {
                    return;
                }

                int maxSelectable = SelectionSpec.getInstance().maxPhotoable;
                if (maxSelectable - mThumbnailAdapter.getItemCount() <= 0) {
                    Toast.makeText(mContext, mContext.getString(com.sparrow.bundle.photo.R.string.error_over_count, maxSelectable
                    ), Toast.LENGTH_SHORT).show();
                    return;
                }

                CAMERA_STATE = STATE_RUNNING;
                takePictureing = true;
                mFoucsView.setVisibility(INVISIBLE);
                CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback
                        () {
                    @Override
                    public void captureResult(Bitmap bitmap, boolean isVertical) {

                        if (SelectionSpec.getInstance().photoType == SelectionSpec.PhotoType.Certificate) {
                            try {
                                bitmap = BitmapUtil.cutBitMap(bitmap, mRectView.getRectLeft(),
                                        mRectView.getRectTop(), mRectView.getRectWidth(), mRectView.getRectHeight());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        captureBitmap = bitmap;
                        CameraInterface.getInstance().doStopCamera();
                        type = TYPE_PICTURE;
                        isBorrow = true;
                        CAMERA_STATE = STATE_WAIT;
                        if (isVertical && SelectionSpec.getInstance().photoType != SelectionSpec.PhotoType.Certificate) {
                            mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
                        } else {
                            mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }
                        mPhoto.setImageBitmap(bitmap);
                        mPhoto.setVisibility(VISIBLE);
                        mRectView.setVisibility(GONE);
                        mCaptureLayout.startAlphaAnimation();
                        mCaptureLayout.startTypeBtnAnimator();
                        takePictureing = false;
                        mSwitchCamera.setVisibility(INVISIBLE);
                        mSwitchFlash.setVisibility(INVISIBLE);
                        CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                    }
                });
            }

            @Override
            public void recordShort(long time) {
                if (CAMERA_STATE != STATE_RUNNING && stopping) {
                    return;
                }
                stopping = true;
                mCaptureLayout.setTextWithAnimation("录制时间过短");
                mSwitchCamera.setRotation(0);
                updateSwitchButton();
                CameraInterface.getInstance().setSwitchView(mSwitchCamera);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CameraInterface.getInstance().stopRecord(true, new
                                CameraInterface.StopRecordCallback() {
                                    @Override
                                    public void recordResult(String url, Bitmap firstFrame) {
                                        Log.i(TAG, "Record Stopping ...");
                                        mCaptureLayout.isRecord(false);
                                        CAMERA_STATE = STATE_IDLE;
                                        stopping = false;
                                        isBorrow = false;
                                    }
                                });
                    }
                }, 1500 - time);
            }

            @Override
            public void recordStart() {
                if (CAMERA_STATE != STATE_IDLE && stopping) {
                    return;
                }

                mSwitchCamera.setVisibility(GONE);
                mSwitchFlash.setVisibility(GONE);
                mCaptureLayout.isRecord(true);
                isBorrow = true;
                CAMERA_STATE = STATE_RUNNING;
                mFoucsView.setVisibility(INVISIBLE);
                CameraInterface.getInstance().startRecord(mVideoView.getHolder().getSurface(),
                        new CameraInterface
                                .ErrorCallback() {
                            @Override
                            public void onError() {
                                Log.i("CJT", "startRecorder error");
                                mCaptureLayout.isRecord(false);
                                CAMERA_STATE = STATE_WAIT;
                                stopping = false;
                                isBorrow = false;
                            }
                        });
            }

            @Override
            public void recordEnd(long time) {
                CameraInterface.getInstance().stopRecord(false, new CameraInterface
                        .StopRecordCallback() {
                    @Override
                    public void recordResult(final String url, Bitmap firstFrame) {
                        CAMERA_STATE = STATE_WAIT;
                        videoUrl = url;
                        type = TYPE_VIDEO;
                        JCameraView.this.firstFrame = firstFrame;
                        new Thread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void run() {
                                try {
                                    if (mMediaPlayer == null) {
                                        mMediaPlayer = new MediaPlayer();
                                    } else {
                                        mMediaPlayer.reset();
                                    }
                                    Log.i("CJT", "URL = " + url);
                                    mMediaPlayer.setDataSource(url);
                                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                                    mMediaPlayer.setVideoScalingMode(MediaPlayer
                                            .VIDEO_SCALING_MODE_SCALE_TO_FIT);
                                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                                            .OnVideoSizeChangedListener() {
                                        @Override
                                        public void
                                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                            updateVideoViewSize(mMediaPlayer.getVideoWidth(),
                                                    mMediaPlayer
                                                            .getVideoHeight());
                                        }
                                    });
                                    mMediaPlayer.setOnPreparedListener(new MediaPlayer
                                            .OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            mMediaPlayer.start();
                                        }
                                    });
                                    mMediaPlayer.setLooping(true);
                                    mMediaPlayer.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
            }

            @Override
            public void recordZoom(float zoom) {
                CameraInterface.getInstance().setZoom(zoom, CameraInterface.TYPE_RECORDER);
            }

            @Override
            public void recordError() {
                //错误回调
                if (errorLisenter != null) {
                    errorLisenter.AudioPermissionError();
                }
            }
        });
        mCaptureLayout.setTypeLisenter(new TypeLisenter() {
            @Override
            public void cancel() {
                if (CAMERA_STATE == STATE_WAIT) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    handlerPictureOrVideo(type, false);
                }
            }

            @Override
            public void confirm() {
                if (CAMERA_STATE == STATE_WAIT) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    handlerPictureOrVideo(type, true);
                }
            }

            @Override
            public void mulitConfirm() {
                CameraInterface.getInstance().doStopCamera();
                CAMERA_STATE = STATE_WAIT;
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }

                if (null != jCameraLisenter) {
                    jCameraLisenter.mulitConfirm();
                }
            }
        });
        mCaptureLayout.setReturnLisenter(new ReturnLisenter() {
            @Override
            public void onReturn() {
                if (jCameraLisenter != null && !takePictureing) {
                    jCameraLisenter.quit();
                }
            }
        });
        //END >>>>>>> captureLayout lisenter callback
        mVideoView.getHolder().addCallback(this);
    }

    public void setOnThumbnailItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mThumbnailAdapter.setOnItemClickListener(onItemClickListener);
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildLayoutPosition(view) != 0) {
                outRect.left = space;
            }
        }
    }

    private boolean switchFlash = false;
    private boolean switchCamera = false;
    private boolean mulitPhoto = false;
    private boolean defaultFront = false;

    public void switchFlash(boolean switchFlash) {
        this.switchFlash = switchFlash;
        mSwitchFlash.setVisibility(switchFlash ? View.VISIBLE : GONE);
    }

    public void switchCamera(boolean switchCamera) {
        this.switchCamera = switchCamera;
        mSwitchCamera.setVisibility(switchCamera ? View.VISIBLE : GONE);
    }

    public void mulitPhoto(boolean mulitPhoto) {
        this.mulitPhoto = mulitPhoto;
    }

    public void defaultFront(boolean defaultFront) {
        this.defaultFront = defaultFront;
    }

    public void updateThumbnailAdapter(List<Item> paths) {
        mThumbnailAdapter.setData(paths);
        mRecyclerView.smoothScrollToPosition(paths.size() - 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = MeasureSpec.getSize(widthMeasureSpec);
        float heightSize = MeasureSpec.getSize(heightMeasureSpec);
        screenProp = heightSize / widthSize;
    }

    @Override
    public void cameraHasOpened() {
        CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), screenProp, new
                FirstFoucsLisenter() {
                    @Override
                    public void onFouce() {
                        JCameraView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                setFocusViewWidthAnimation(getWidth() / 2, getHeight() / 2);
                            }
                        });
                    }
                });
    }

    private boolean switching = false;

    @Override
    public void cameraSwitchSuccess() {
        switching = false;
    }

    /**
     * start preview
     */
    public void onResume() {
        CameraInterface.getInstance().registerSensorManager(mContext);
        CameraInterface.getInstance().setSwitchView(mSwitchCamera);
        if (onlyPause) {
//            if (isBorrow && type == TYPE_VIDEO) {
//                new Thread(new Runnable() {
//                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//                    @Override
//                    public void run() {
//                        try {
//                            if (mMediaPlayer == null) {
//                                mMediaPlayer = new MediaPlayer();
//                            } else {
//                                mMediaPlayer.reset();
//                            }
//                            Log.i("CJT", "URL = " + videoUrl);
//                            mMediaPlayer.setDataSource(videoUrl);
//                            mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
//                            mMediaPlayer.setVideoScalingMode(MediaPlayer
// .VIDEO_SCALING_MODE_SCALE_TO_FIT);
//                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                            mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
//                                    .OnVideoSizeChangedListener() {
//                                @Override
//                                public void
//                                onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                                    updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
//                                            .getVideoHeight());
//                                }
//                            });
//                            mMediaPlayer.setOnPreparedListener(new MediaPlayer
// .OnPreparedListener() {
//                                @Override
//                                public void onPrepared(MediaPlayer mp) {
//                                    mMediaPlayer.start();
//                                }
//                            });
//                            mMediaPlayer.setLooping(true);
//                            mMediaPlayer.prepare();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//            } else {
            new Thread() {
                @Override
                public void run() {
                    CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                }
            }.start();
            mFoucsView.setVisibility(INVISIBLE);
//            }
        }
    }

    /**
     * destory preview
     */
    public void onPause() {
        onlyPause = true;
        CameraInterface.getInstance().unregisterSensorManager(mContext);
        CameraInterface.getInstance().doStopCamera();
    }

    private boolean firstTouch = true;
    private float firstTouchLength = 0;
    private int zoomScale = 0;

    /**
     * handler touch focus
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                if (event.getPointerCount() == 2) {
                    Log.i("CJT", "ACTION_DOWN = " + 2);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    firstTouch = true;
                }
                if (event.getPointerCount() == 2) {
                    //第一个点
                    float point_1_X = event.getX(0);
                    float point_1_Y = event.getY(0);
                    //第二个点
                    float point_2_X = event.getX(1);
                    float point_2_Y = event.getY(1);

                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math
                            .pow(point_1_Y -
                                    point_2_Y, 2));

                    if (firstTouch) {
                        firstTouchLength = result;
                        firstTouch = false;
                    }
                    if ((int) (result - firstTouchLength) / 40 != 0) {
                        firstTouch = true;
                        CameraInterface.getInstance().setZoom(result - firstTouchLength,
                                CameraInterface.TYPE_CAPTURE);
                    }
                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
                break;
            case MotionEvent.ACTION_UP:
                firstTouch = true;
                break;
        }
        return true;
    }

    /**
     * focusview animation
     */
    private void setFocusViewWidthAnimation(float x, float y) {
        if (isBorrow) {
            return;
        }
        if (y > mCaptureLayout.getTop()) {
            return;
        }
        mFoucsView.setVisibility(VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > layout_width - mFoucsView.getWidth() / 2) {
            x = layout_width - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
        if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
            y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
        }
        CameraInterface.getInstance().handleFocus(mContext, x, y, new CameraInterface
                .FocusCallback() {
            @Override
            public void focusSuccess() {
                mFoucsView.setVisibility(INVISIBLE);
            }
        });

        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.3f, 1f, 0.3f,
                1f, 0.3f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
    }

    public void setJCameraLisenter(JCameraLisenter jCameraLisenter) {
        this.jCameraLisenter = jCameraLisenter;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void handlerPictureOrVideo(int type, boolean confirm) {
        if (jCameraLisenter == null || type == -1) {
            return;
        }
        switch (type) {
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                if (SelectionSpec.getInstance().photoType == SelectionSpec.PhotoType.Certificate) {
                    mRectView.setVisibility(View.VISIBLE);
                }
                if (confirm && captureBitmap != null) {
                    jCameraLisenter.captureSuccess(captureBitmap);
                    if (mulitPhoto) {
                        mRecyclerView.setVisibility(VISIBLE);
                    }
                } else {
                    if (captureBitmap != null) {
                        captureBitmap.recycle();
                    }
                    captureBitmap = null;
                }
                break;
            case TYPE_VIDEO:
                if (confirm) {
                    //回调录像成功后的URL
                    jCameraLisenter.recordSuccess(videoUrl, firstFrame);
                    if (mulitPhoto) {
                        mRecyclerView.setVisibility(VISIBLE);
                    }
                } else {
                    //删除视频
                    File file = new File(videoUrl);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                mCaptureLayout.isRecord(false);
                LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                mVideoView.setLayoutParams(videoViewParam);
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                mSwitchCamera.setRotation(0);
                CameraInterface.getInstance().setSwitchView(mSwitchCamera);
                break;
        }
        isBorrow = false;
        updateSwitchButton();
        CAMERA_STATE = STATE_IDLE;
        mFoucsView.setVisibility(VISIBLE);
        setFocusViewWidthAnimation(getWidth() / 2, getHeight() / 2);

    }

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }

    private void updateSwitchButton() {
        if (switchCamera) {
            mSwitchCamera.setVisibility(VISIBLE);
        }
        if (switchFlash) {
            mSwitchFlash.setVisibility(VISIBLE);
        }
    }

    /**
     * TextureView resize
     */
    public void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT,
                    height);
            videoViewParam.gravity = Gravity.CENTER;
//            videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            mVideoView.setLayoutParams(videoViewParam);
        }
    }

    /**
     * forbidden audio
     */
    public void enableshutterSound(boolean enable) {
    }

    public void forbiddenSwitchCamera(boolean forbiddenSwitch) {
        this.forbiddenSwitch = forbiddenSwitch;
    }

    private ErrorLisenter errorLisenter;

    //启动Camera错误回调
    public void setErrorLisenter(ErrorLisenter errorLisenter) {
        this.errorLisenter = errorLisenter;
        CameraInterface.getInstance().setErrorLinsenter(errorLisenter);
    }

    //设置CaptureButton功能（拍照和录像）
    public void setFeatures(int state) {
        this.mCaptureLayout.setButtonFeatures(state);
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        CameraInterface.getInstance().setMediaQuality(quality);
    }

    public void setTip(String tip) {
        mCaptureLayout.setTip(tip);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("CJT", "surfaceCreated");
        new Thread() {
            @Override
            public void run() {
                CameraInterface.getInstance().selectCamera(defaultFront);
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        onlyPause = false;
        Log.i("CJT", "surfaceDestroyed");
        CameraInterface.getInstance().doDestroyCamera();
    }
}
