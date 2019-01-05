package com.sparrow.bundle.photo.camera;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sparrow.bundle.photo.R;
import com.sparrow.bundle.photo.matisse.internal.entity.Item;

import java.util.List;

/**
 * Created by WEI on 2017/7/26.
 */

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private Context mContext;
    private List<Item> mPaths;
    private int thumbnailHeight;
    private RequestOptions options;

    public void setData(List<Item> paths) {
        mPaths = paths;
        notifyDataSetChanged();
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public ThumbnailAdapter(Context context) {
        mContext = context;
        options = new RequestOptions().centerCrop().override(200, 200);
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThumbnailViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_item, parent,
                        false));
    }

    @Override
    public void onBindViewHolder(final ThumbnailViewHolder holder, final int position) {
        Glide.with(mContext)
                .load(mPaths.get(position).getPath())
                .apply(options)
                .into(holder.mThumbnail);
        holder.mThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mItemClickListener) {
                    mItemClickListener.onItemClick(null, holder.mThumbnail, position, 0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == mPaths ? 0 : mPaths.size();
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        public ImageView mThumbnail;

        ThumbnailViewHolder(View contentView) {
            super(contentView);
            mThumbnail = contentView.findViewById(R.id.thumbnail);
            ViewGroup.LayoutParams params = mThumbnail.getLayoutParams();
            params.width = thumbnailHeight;
            params.height = thumbnailHeight;
            mThumbnail.setLayoutParams(params);
            mThumbnail.setAlpha(0.6f);
        }
    }

    private AdapterView.OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }
}
