package com.sparrow.bundle.framework.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.utils.DensityUtils;

import java.util.List;

public class AlertActionPopwin extends PopupWindow implements View.OnClickListener {

    public View pickerContainerV;
    public View contentView;//root view
    public LinearLayout llBtnContent;
    public Button cancelBtn;
    private Context mContext;

    public AlertActionPopwin(Context context) {
        super(context);
        mContext = context;

        initView();
    }

    private void initView() {

        contentView = LayoutInflater.from(mContext).inflate(com.sparrow.bundle.framework.R.layout.layout_map_pop, null);
        pickerContainerV = contentView.findViewById(com.sparrow.bundle.framework.R.id.container_picker);
        llBtnContent = contentView.findViewById(com.sparrow.bundle.framework.R.id.ll_btn_content);
        llBtnContent.removeAllViews();
        cancelBtn = contentView.findViewById(com.sparrow.bundle.framework.R.id.btn_cancel);

        contentView.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(com.sparrow.bundle.framework.R.style.FadeInPopWin);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setTitle(String title) {
        TextView tvTitle = new TextView(mContext);
        int padding = DensityUtils.dip2px(mContext, 10);
        tvTitle.setText(title);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(mContext.getResources().getColor(com.sparrow.bundle.framework.R.color.gray));
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvTitle.setPadding(0, padding, 0, padding);

        llBtnContent.addView(tvTitle, 0);
    }

    public void setItems(List<String> btnString) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        for (int i = 0; i < btnString.size(); i++) {
            Button view = (Button) inflater.inflate(com.sparrow.bundle.framework.R.layout.layout_map_pop_btn, null);
            view.setText(btnString.get(i));
            view.setOnClickListener(new ItemClickListener(i));
            llBtnContent.addView(view);
        }
    }

    private class ItemClickListener implements View.OnClickListener {

        private int position = -1;

        public ItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            dismissPopWin();
            if (null != listener)
                listener.onItemClick(null, view, this.position, this.position);
        }
    }

    public void showPopWin(Activity activity) {
        if (null != activity) {
            TranslateAnimation trans = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                    0, Animation.RELATIVE_TO_SELF, 1,
                    Animation.RELATIVE_TO_SELF, 0);

            showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM,
                    0, 0);
            trans.setDuration(400);
            trans.setInterpolator(new AccelerateDecelerateInterpolator());

            pickerContainerV.startAnimation(trans);
        }
    }

    /**
     * Dismiss date picker popWindow
     */
    public void dismissPopWin() {

        TranslateAnimation trans = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);

        trans.setDuration(400);
        trans.setInterpolator(new AccelerateInterpolator());
        trans.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismiss();
            }
        });
        pickerContainerV.startAnimation(trans);
    }

    private AdapterView.OnItemClickListener listener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v == contentView || v == cancelBtn) {
            dismissPopWin();
        }
    }
}
