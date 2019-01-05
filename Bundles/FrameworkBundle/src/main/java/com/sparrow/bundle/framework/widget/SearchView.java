package com.sparrow.bundle.framework.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sparrow.bundle.framework.R;
import com.sparrow.bundle.framework.utils.WindowUtils;

public class SearchView extends LinearLayout {

    /**
     * 初始化成员变量
     */
    private Context context;
    // 搜索框组件
    private EditText et_search; // 搜索按键
    private ImageView ivScan;//扫一扫按钮
    // 回调接口
    private ICallBack mCallBack;// 搜索按键回调接口
    private ClickScanListener mClickScanListener;// 点击扫一扫
    private OnTextChangeListener mOnTextChangeListener; //输入内容变化调用接口

    // 自定义属性设置
    // 1. 搜索字体属性设置：大小、颜色 & 默认提示
    private Float textSizeSearch;
    private int textColorSearch;
    private String textHintSearch;

    // 2. 搜索框设置：高度 & 颜色
    private int searchBlockHeight;
    private int searchBlockColor;

    /**
     * 构造函数
     * 作用：对搜索框进行初始化
     */
    public SearchView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAttrs(context, attrs); // ->>关注a
        init();// ->>关注b
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initAttrs(context, attrs);
        init();
    }

    /**
     * 关注a
     * 作用：初始化自定义属性
     */
    private void initAttrs(Context context, AttributeSet attrs) {

        // 控件资源名称
        TypedArray typedArray = context.obtainStyledAttributes(attrs, com.sparrow.bundle.framework.R.styleable.Search_View);

        // 搜索框字体大小（dp）
        textSizeSearch = typedArray.getDimension(com.sparrow.bundle.framework.R.styleable.Search_View_textSizeSearch, 14);

        // 搜索框字体颜色（使用十六进制代码，如#333、#8e8e8e）
        int defaultColor = context.getResources().getColor(com.sparrow.bundle.framework.R.color.white); // 默认颜色 = 灰色
        textColorSearch = typedArray.getColor(com.sparrow.bundle.framework.R.styleable.Search_View_textColorSearch, defaultColor);

        // 搜索框提示内容（String）
        textHintSearch = typedArray.getString(com.sparrow.bundle.framework.R.styleable.Search_View_textHintSearch);

        // 搜索框高度
        searchBlockHeight = typedArray.getInteger(com.sparrow.bundle.framework.R.styleable.Search_View_searchBlockHeight, 150);

        // 搜索框颜色
        int defaultColor2 = context.getResources().getColor(com.sparrow.bundle.framework.R.color.searchColorDefault); // 默认颜色 = 白色
        searchBlockColor = typedArray.getColor(com.sparrow.bundle.framework.R.styleable.Search_View_searchBlockColor, defaultColor2);

        // 释放资源
        typedArray.recycle();
    }


    /**
     * 关注b
     * 作用：初始化搜索框
     */
    private void init() {
        initView();
        /**
         * 监听输入键盘更换后的搜索按键
         * 调用时刻：点击键盘上的搜索键时
         */
        et_search.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 1. 点击搜索按键后，根据输入的搜索字段进行查询
                    // 注：由于此处需求会根据自身情况不同而不同，所以具体逻辑由开发者自己实现，此处仅留出接口
                    WindowUtils.closeKeyBoard((Activity) getContext());
                    if (!(mCallBack == null)) {
                        mCallBack.search(et_search.getText().toString());
                    }
                }
                return false;
            }
        });


        /**
         * 搜索框的文本变化实时监听
         */
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null != mOnTextChangeListener) {
                    mOnTextChangeListener.onTextChange(s.toString());
                }
            }

            // 输入文本后调用该方法
            @Override
            public void afterTextChanged(Editable s) {
                String tempName = et_search.getText().toString();
                if (null != mCallBack && TextUtils.isEmpty(tempName)) {
                    WindowUtils.closeKeyBoard((Activity) getContext());
                    mCallBack.search(tempName);
                }
            }
        });
    }

    /**
     * 关注c：绑定搜索框xml视图
     */
    private void initView() {

        // 1. 绑定R.layout.search_layout作为搜索框的xml文件
        LayoutInflater.from(context).inflate(com.sparrow.bundle.framework.R.layout.layout_searchview, this);

        // 2. 绑定搜索框EditText
        et_search = findViewById(com.sparrow.bundle.framework.R.id.et_search);
        et_search.setTextSize(textSizeSearch);
        et_search.setTextColor(textColorSearch);
        et_search.setHint(textHintSearch);
        et_search.setHintTextColor(context.getResources().getColor(com.sparrow.bundle.framework.R.color.white));

        // 3. 绑定扫一扫按钮
        ivScan = findViewById(com.sparrow.bundle.framework.R.id.iv_scan);
        ivScan.setOnClickListener(v -> {
            if (mClickScanListener != null) {
                mClickScanListener.clickScan();
            }
        });
    }

    public void setTextHintSearch(String textHintSearch) {
        this.textHintSearch = textHintSearch;
        et_search.setHint(textHintSearch);
    }

    public void setTextHintSearch(int textHintSearch) {
        this.textHintSearch = getResources().getString(textHintSearch);
        et_search.setHint(textHintSearch);
    }

    public void setTextSearch(String textSearch) {
        et_search.setText(textSearch);
    }

    /**
     * 点击键盘中搜索键后的操作，用于接口回调
     */
    public void setOnClickSearch(ICallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    public interface ICallBack {
        void search(String string);
    }

    public interface ClickScanListener {
        void clickScan();
    }

    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

    public void setScanVisiable(boolean visiable) {
        if (visiable) {
            ivScan.setVisibility(View.VISIBLE);
        } else {
            ivScan.setVisibility(View.GONE);
        }
    }

    public void setClickScanListener(ClickScanListener clickScanListener) {
        this.mClickScanListener = clickScanListener;
    }

    public void setOnTextChangeListener(OnTextChangeListener onTextChangeListener) {
        this.mOnTextChangeListener = onTextChangeListener;
    }

}
