package com.sparrow.android.library.ui.activity;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.sparrow.android.demo.BR;
import com.sparrow.android.demo.R;
import com.sparrow.android.demo.databinding.ActivityEdittextDemoBinding;
import com.sparrow.android.library.constant.Router;
import com.sparrow.bundle.framework.base.BaseViewModel;
import com.sparrow.bundle.framework.base.ui.activity.BaseActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhujianwei
 * <p>
 * EditText 控件常用方式示例
 * 1. 自定义控件{@link com.sparrow.bundle.framework.widget.EditTextClear} ，支持输入后显示清除按钮
 * 2. BaseActivity 添加事件处理点击空白区域关闭软件排名
 */
@Route(path = Router.EdittextDemoActivity)
public class EdittextDemoActivity extends BaseActivity<ActivityEdittextDemoBinding, BaseViewModel> {

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_edittext_demo;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public BaseViewModel initViewModel() {
        return new BaseViewModel(this);
    }

    @Override
    public void initView() {
        getToolbar().setTitle("EdittextDemo");
    }

    @Override
    public void initListener() {
        // 通过EditText的inputfilter来做限制
        // binding.etPassword.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 16 )});

        // 设置限制中文、英文、数字、特殊字符
        binding.etClear.setFilters(new InputFilter[]{new PatternInputFilter(REGEX_2)});
    }

    /**
     * 中文
     */
    private final static String REGEX_1 = "[\u4e00-\u9fa5]+";
    /**
     * 中文、英文字母
     */
    private final static String REGEX_2 = "[a-zA-Z|\u4e00-\u9fa5]+";
    /**
     * 英文字母、数字
     */
    private final static String REGEX_3 = "[a-zA-Z0-9]+";

    class PatternInputFilter implements InputFilter {
        private String regex;

        public PatternInputFilter(String regex) {
            this.regex = regex;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(source.toString());
            if (!m.matches()) {
                return "";
            }
            return null;
        }
    }
}
