package com.sparrow.bundle.framework.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import com.medical.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.framework.bundle.PhotoBundle;
import com.sparrow.bundle.framework.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GridImageLayout extends RecyclerView {

    public enum ViewType {
        VIEW, EDIT
    }

    private ArrayList<Item> mPaths = new ArrayList<>();
    private ImageAdapter myAdapter;
    private ViewType viewType = ViewType.EDIT;

    public GridImageLayout(Context context) {
        super(context);
        init(null);
    }

    public GridImageLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GridImageLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        Item item = new Item("");
        mPaths.add(item);
        //Adapter初始化
        myAdapter = new ImageAdapter((Activity) getContext());
        myAdapter.setData(mPaths);

        if (null != attrs) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    com.sparrow.bundle.framework.R.styleable.GridImageLayout,
                    0, 0);

            //Configurable parameters
            try {
                spanCount = a.getInt(com.sparrow.bundle.framework.R.styleable.GridImageLayout_spanCount, 3);
            } finally {
                a.recycle();
            }
        }

        setLayoutManager(new GridLayoutManager(getContext(), spanCount) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        addItemDecoration(new GridSpacingItemDecoration(spanCount, 5, true));
        setAdapter(myAdapter);
    }

    private int spanCount = 3;

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
        if (viewType == ViewType.VIEW) {
            if (mPaths.size() == 0)
                return;
            this.mPaths.remove(this.mPaths.size() - 1);
            myAdapter.notifyDataSetChanged();
        }
    }

    public void setPath(Item item) {
        this.mPaths.clear();
        this.mPaths.add(item);
        myAdapter.notifyDataSetChanged();
    }

    public void setPaths(List<Item> path) {
        if (null == path) {
            path = new ArrayList<>();
        }

        if (viewType == ViewType.VIEW) {
            this.mPaths.clear();
            this.mPaths.addAll(0, path);
        } else {
            List<Item> delPath = new ArrayList<>();
            for (int j = 0; j < mPaths.size(); j++) {
                boolean isNotExist = false;
                String tempPath = mPaths.get(j).getPath();
                for (int i = 0; i < path.size(); i++) {
                    if (!tempPath.equals(path.get(i).path)
                            && path.get(i).id != Item.ITEM_ID_CAPTURE
                            && !TextUtils.isEmpty(tempPath)
                            && !tempPath.startsWith("http")
                            && mPaths.get(j).id != Item.ITEM_ID_CAPTURE) {
                        isNotExist = true;
                        break;
                    }
                }
                if (isNotExist) {
                    delPath.add(mPaths.get(j));
                }
            }
            this.mPaths.removeAll(delPath);

            // 去除重复图片
            ArrayList<Item> temp = new ArrayList<>();
            for (int i = 0; i < path.size(); i++) {
                if (!this.mPaths.contains(path.get(i))) {
                    temp.add(path.get(i));
                }
            }
            this.mPaths.addAll(0, temp);
        }
        myAdapter.notifyDataSetChanged();
    }

    public ArrayList<Item> getAllPath() {
        ArrayList<Item> temp = new ArrayList<>();
        for (int i = 0; i < mPaths.size(); i++) {
            String tempPath = mPaths.get(i).getPath();
            if (!TextUtils.isEmpty(tempPath)) {
                temp.add(mPaths.get(i));
            }
        }
        return temp;
    }

    public void removeItem(int position) {
        mPaths.remove(position);
        myAdapter.notifyDataSetChanged();
    }

    public ArrayList<Item> getPath() {
        ArrayList<Item> temp = new ArrayList<>();
        for (int i = 0; i < mPaths.size(); i++) {
            String tempPath = mPaths.get(i).getPath();
            if (!TextUtils.isEmpty(tempPath)
                    && !tempPath.startsWith("http")) {
                temp.add(mPaths.get(i));
            }
        }
        return temp;
    }

    /**
     * 图库选择的图片数据
     *
     * @return
     */
    public ArrayList<Item> getSelectPath() {
        ArrayList<Item> temp = new ArrayList<>();
        for (int i = 0; i < mPaths.size(); i++) {
            String tempPath = mPaths.get(i).getPath();
            if (!TextUtils.isEmpty(tempPath)
                    && !tempPath.startsWith("http")
                    && mPaths.get(i).id != Item.ITEM_ID_CAPTURE) {
                temp.add(mPaths.get(i));
            }
        }
        return temp;
    }

    /**
     * 拍照、网络图片数据
     *
     * @return
     */
    public ArrayList<Item> getOtherPath() {
        ArrayList<Item> temp = new ArrayList<>();
        for (int i = 0; i < mPaths.size(); i++) {
            String tempPath = mPaths.get(i).getPath();
            if (!TextUtils.isEmpty(tempPath)
                    && (tempPath.startsWith("http")
                    || mPaths.get(i).id == Item.ITEM_ID_CAPTURE)) {
                temp.add(mPaths.get(i));
            }
        }
        return temp;
    }

    public ArrayList<Item> getPreviewPath() {
        ArrayList<Item> temp = new ArrayList<>();
        for (int i = 0; i < mPaths.size(); i++) {
            String tempPath = mPaths.get(i).getPath();
            if (!TextUtils.isEmpty(tempPath)) {
                temp.add(mPaths.get(i));
            }
        }
        return temp;
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        private ArrayList<Item> mPaths;
        private Activity mContext;
        private RequestOptions options;

        public ImageAdapter(Activity context) {
            mContext = context;

            //设置图片圆角角度
            RoundedCorners roundedCorners = new RoundedCorners(20);
            //通过RequestOptions扩展功能
            options = RequestOptions
                    .bitmapTransform(roundedCorners)
                    .override(300, 200)
                    .centerCrop()
                    .placeholder(com.sparrow.bundle.framework.R.drawable.error_image);
        }

        void setData(ArrayList<Item> paths) {
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(com.sparrow.bundle.framework.R.layout.item_grid_image, parent,
                            false));
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            String path = mPaths.get(position).getPath();
            if (TextUtils.isEmpty(path)) {
                Glide.with(mContext)
                        .load(com.sparrow.bundle.framework.R.drawable.icon_camera)
                        .into(holder.imageView);
                holder.imageViewDel.setVisibility(View.GONE);
            } else {
                Glide.with(mContext)
                        .load(path)
                        .apply(options)
                        .into(holder.imageView);

                if (viewType == ViewType.VIEW) {
                    holder.imageViewDel.setVisibility(View.GONE);
                } else {
                    holder.imageViewDel.setVisibility(View.VISIBLE);
                }

                if (spanCount == 1) {
                    RelativeLayout.LayoutParams params
                            = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    holder.imageView.setLayoutParams(params);
                    holder.imageViewDel.setVisibility(View.GONE);
                }
            }

            if (viewType == ViewType.VIEW) {
                RelativeLayout.LayoutParams params
                        = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
                params.setMargins(0, 0, 0, dip2px(getContext(), 10));
                holder.imageView.setLayoutParams(params);
            } else {
                RelativeLayout.LayoutParams params
                        = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();

                params.setMargins(0, dip2px(getContext(), 20), dip2px(getContext(), 10), 0);
                holder.imageView.setLayoutParams(params);
            }

            RxView.clicks(holder.imageViewDel).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if (null != deleteImageListener) {
                    if (!deleteImageListener.onDeleteItem(mPaths.get(position), position)) {
                        removeItem(position);
                    }
                } else {
                    removeItem(position);
                }
            });

            RxView.clicks(holder.imageView).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
                if ((TextUtils.isEmpty(path) || spanCount == 1) && ViewType.VIEW != viewType) {
                    if (null != listener)
                        listener.onStart(GridImageLayout.this);

                    int mostSelectable = 9 - (mPaths.size() - 1);
                    if (mostSelectable > 0 || spanCount == 1) {
                        int maxSelectable = 9 - getOtherPath().size();
                        new PhotoBundle(mContext)
                                .mulitPhoto(spanCount != 1)
                                .maxSelectable(spanCount == 1 ? spanCount : maxSelectable)
                                .maxPhotoable(mostSelectable)
                                .selectItems(spanCount == 1 ? new ArrayList<>() : getSelectPath()).show();
                    } else {
                        ToastUtils.showShort(mContext.getString(com.sparrow.bundle.framework.R.string.error_over_count, 9));
                    }

                } else {
                    List<Item> items = getPreviewPath();
                    if (items.size() > 0) {
                        PhotoBundle.previewTakePhotos(mContext, items, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPaths == null ? 0 : mPaths.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private ImageView imageViewDel;

            ImageViewHolder(View contentView) {
                super(contentView);
                imageView = contentView.findViewById(com.sparrow.bundle.framework.R.id.item_grid_image);
                imageViewDel = contentView.findViewById(com.sparrow.bundle.framework.R.id.item_grid_image_del);
            }
        }
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing / 2;
                }
                outRect.bottom = spacing / 2; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private OnSelectImageListener listener;

    private OnDeleteImageListener deleteImageListener;


    public void setDeleteImageListener(OnDeleteImageListener deleteImageListener) {
        this.deleteImageListener = deleteImageListener;
    }

    public void setOnSelectImageListener(OnSelectImageListener listener) {
        this.listener = listener;
    }

    public interface OnSelectImageListener {
        void onStart(View view);
    }

    public interface OnDeleteImageListener {

        boolean onDeleteItem(Item item, int position);
    }
}
