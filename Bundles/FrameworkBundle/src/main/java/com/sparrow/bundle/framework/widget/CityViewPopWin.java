package com.sparrow.bundle.framework.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.PopupWindow;

import com.bruce.pickerview.LoopScrollListener;
import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.base.entity.ProvinceEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CityViewPopWin extends PopupWindow implements View.OnClickListener {

    public Button cancelBtn;
    public Button confirmBtn;
    public CenterLoopView yearLoopView;
    public CenterLoopView monthLoopView;
    public CenterLoopView dayLoopView;
    public View pickerContainerV;
    public View contentView;//root view

    private int yearPos = 0;
    private int monthPos = 0;
    private int dayPos = 0;
    private Context mContext;
    private String textCancel;
    private String textConfirm;
    private int colorCancel;
    private int colorConfirm;
    private int btnTextsize;//text btnTextsize of cancel and confirm button
    private int viewTextSize;

    private List<String> firstList;
    private List<String> secondList;
    private List<String> thirdList;
    private List<ProvinceEntity> mProvinceList = new ArrayList<>();
    private List<ProvinceEntity.city> mCityList = new ArrayList<>();
    private List<ProvinceEntity.child> mQuList = new ArrayList<>();


    public static class Builder {

        //Required
        private Context context;
        private LoopViewPopWin.OnDatePickedListener listener;

        public Builder(Context context) {
            this.context = context;
            textCancel = context.getResources().getString(com.sparrow.bundle.framework.R.string.common_txt_cancel);
            textConfirm = context.getResources().getString(com.sparrow.bundle.framework.R.string.common_txt_confirm);
        }

        private String textCancel;
        private String textConfirm;
        private int colorCancel = Color.parseColor("#999999");
        private int colorConfirm = Color.parseColor("#108EE9");
        private int btnTextSize = 16;//text btnTextsize of cancel and confirm button
        private int viewTextSize = 18;

        private List<String> firstList;
        private List<String> secondList;
        private List<String> thirdList;
        private List<ProvinceEntity> mProvinceList = new ArrayList<>();
        private List<ProvinceEntity.city> mCityList = new ArrayList<>();
        private List<ProvinceEntity.child> mQuList = new ArrayList<>();

        //省数据处理
        private   List<String> getProviceistForWindow()
        {
            List<String> list = new ArrayList<>();

            for (int i = 0; i < mProvinceList.size(); i++) {
                list.add(mProvinceList.get(i).name);
            }
            return list;
        }

        //市数据处理
        private   List<String> getCityListForWindow(int num)
        {
            List<String> list = new ArrayList<>();
            mCityList = mProvinceList.get(num).childs;

            for (int i = 0; i < mCityList.size(); i++) {
                list.add(mCityList.get(i).name);
            }
            return list;
        }

        //区数据处理
        private   List<String> getQuListForWindow(int num)
        {
            List<String> list = new ArrayList<>();
            mQuList = mCityList.get(num).childs;

            for (int i = 0; i < mQuList.size(); i++) {
                list.add(mQuList.get(i).name);
            }
            return list;
        }

        public CityViewPopWin.Builder textCancel(String textCancel) {
            this.textCancel = textCancel;
            return this;
        }

        public CityViewPopWin.Builder textConfirm(String textConfirm) {
            this.textConfirm = textConfirm;
            return this;
        }

        public CityViewPopWin.Builder colorCancel(int colorCancel) {
            this.colorCancel = colorCancel;
            return this;
        }

        public CityViewPopWin.Builder colorConfirm(int colorConfirm) {
            this.colorConfirm = colorConfirm;
            return this;
        }

        /**
         * set btn text btnTextSize
         *
         * @param textSize dp
         */
        public CityViewPopWin.Builder btnTextSize(int textSize) {
            this.btnTextSize = textSize;
            return this;
        }

        public CityViewPopWin.Builder viewTextSize(int textSize) {
            this.viewTextSize = textSize;
            return this;
        }

        public CityViewPopWin.Builder setData(List<ProvinceEntity> mProvinceList,int citynum,int qunum) {
            this.mProvinceList = mProvinceList;
            this.firstList = getProviceistForWindow();
            this.secondList = getCityListForWindow(citynum);
            this.thirdList = getQuListForWindow(qunum);
            return this;
        }
//
//        public CityViewPopWin.Builder setSecondData(List<String> textSize) {
//            this.secondList = textSize;
//            return this;
//        }
//
//        public CityViewPopWin.Builder setThirdData(List<String> textSize) {
//            this.thirdList = textSize;
//            return this;
//        }

        public CityViewPopWin.Builder setListener(LoopViewPopWin.OnDatePickedListener listener) {
            this.listener = listener;
            return this;
        }


        public CityViewPopWin build() {
            return new CityViewPopWin(this);
        }
    }

    public CityViewPopWin(CityViewPopWin.Builder builder) {
        this.firstList = builder.firstList;
        this.secondList = builder.secondList;
        this.thirdList = builder.thirdList;
        this.textCancel = builder.textCancel;
        this.textConfirm = builder.textConfirm;
        this.mContext = builder.context;
        this.mListener = builder.listener;
        this.colorCancel = builder.colorCancel;
        this.colorConfirm = builder.colorConfirm;
        this.btnTextsize = builder.btnTextSize;
        this.viewTextSize = builder.viewTextSize;
        this.mProvinceList = builder.mProvinceList;
        this.mCityList = builder.mCityList;
        this.mQuList = builder.mQuList;
        initView();
    }

    private LoopViewPopWin.OnDatePickedListener mListener;

    private void initView() {

        contentView = LayoutInflater.from(mContext).inflate(com.sparrow.bundle.framework.R.layout.layout_center_loopview, null);
        cancelBtn = contentView.findViewById(com.sparrow.bundle.framework.R.id.btn_cancel);
        cancelBtn.setTextColor(colorCancel);
        cancelBtn.setTextSize(btnTextsize);
        confirmBtn = contentView.findViewById(com.sparrow.bundle.framework.R.id.btn_confirm);
        confirmBtn.setTextColor(colorConfirm);
        confirmBtn.setTextSize(btnTextsize);
        yearLoopView = contentView.findViewById(com.sparrow.bundle.framework.R.id.picker_year);
        monthLoopView = contentView.findViewById(com.sparrow.bundle.framework.R.id.picker_month);
        dayLoopView = contentView.findViewById(com.sparrow.bundle.framework.R.id.picker_day);
        pickerContainerV = contentView.findViewById(com.sparrow.bundle.framework.R.id.container_picker);

//        //do not loop,default can loop
//        yearLoopView.setNotLoop();
//        monthLoopView.setNotLoop();
//        dayLoopView.setNotLoop();
//
        //set loopview text size
        yearLoopView.setTextSize(this.viewTextSize);
        monthLoopView.setTextSize(this.viewTextSize);
        dayLoopView.setTextSize(this.viewTextSize);

        //set checked listen
        yearLoopView.setLoopListener(new LoopScrollListener() {
            @Override
            public void onItemSelect(int item) {
                yearPos = item;
                initCityPickerView(yearPos);
            }
        });
        monthLoopView.setLoopListener(new LoopScrollListener() {
            @Override
            public void onItemSelect(int item) {
                monthPos = item;
                initQuPickerView(monthPos);
            }
        });
        dayLoopView.setLoopListener(new LoopScrollListener() {
            @Override
            public void onItemSelect(int item) {
                dayPos = item;
            }
        });

        initPickerViews(); // init year and month loop view

        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        contentView.setOnClickListener(this);

        if (!TextUtils.isEmpty(textConfirm)) {
            confirmBtn.setText(textConfirm);
        }

        if (!TextUtils.isEmpty(textCancel)) {
            cancelBtn.setText(textCancel);
        }

        setTouchable(true);
        setFocusable(true);
        // setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(com.sparrow.bundle.framework.R.style.FadeInPopWin);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * Init year and month loop view,
     * Let the day loop view be handled separately
     */
    private void initPickerViews() {

        if (null != firstList) {
            yearLoopView.setDataList(firstList);
            yearLoopView.setInitPosition(yearPos);
        } else {
            yearLoopView.setVisibility(View.GONE);
        }

        if (null != secondList) {
            monthLoopView.setDataList(secondList);
            monthLoopView.setInitPosition(yearPos);
        } else {
            monthLoopView.setVisibility(View.GONE);
        }

        if (null != thirdList) {
            dayLoopView.setDataList(thirdList);
            dayLoopView.setInitPosition(yearPos);
        } else {
            dayLoopView.setVisibility(View.GONE);
        }
    }

    /**
     * Init city item
     */
    private void initCityPickerView(int num) {

        List<String> list = new ArrayList<>();
        mCityList = mProvinceList.get(num).childs;

        for (int i = 0; i < mCityList.size(); i++) {
            list.add(mCityList.get(i).name);
        }

        monthLoopView.setDataList(list);
        monthLoopView.setInitPosition(0);
        initQuPickerView(0);
    }

    /**
     * Init qu item
     */
    private void initQuPickerView(int num) {

        List<String> list = new ArrayList<>();
        mQuList = mCityList.get(num).childs;

        for (int i = 0; i < mQuList.size(); i++) {
            list.add(mQuList.get(i).name);
        }

        if(list.size() > 0)
        {
            dayLoopView.setDataList(list);
            dayLoopView.setInitPosition(0);
        }

    }


    /**
     * Show date picker popWindow
     *
     * @param activity
     */
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

    @Override
    public void onClick(View v) {
        if (v == contentView || v == cancelBtn) {
            dismissPopWin();
        } else if (v == confirmBtn) {
            if (null != mListener) {
                mListener.onDatePickCompleted(yearPos, monthPos, dayPos);
            }
            dismissPopWin();
        }
    }

    /**
     * Transform int to String with prefix "0" if less than 10
     *
     * @param num
     * @return
     */
    public static String format2LenStr(int num) {

        return (num < 10) ? "0" + num : String.valueOf(num);
    }

    public static int spToPx(Context context, int spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    public interface OnDatePickedListener {

        /**
         * Listener when date has been checked
         *
         * @param first
         * @param second
         * @param third
         */
        void onDatePickCompleted(int first, int second, int third);
    }
}
