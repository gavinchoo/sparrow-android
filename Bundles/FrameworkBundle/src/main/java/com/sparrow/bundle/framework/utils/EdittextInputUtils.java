package com.sparrow.bundle.framework.utils;

import android.text.InputFilter;
import android.widget.EditText;

/**
 * @author zhangshaopeng
 * @date 2018/11/16
 * @description
 */
public class EdittextInputUtils {
    /**
     *  取消空格
     * */
    public static void setEditTextInhibitInputSpace(EditText editText, String reason) {
        InputFilter spaceFilter = (source, start, end, dest, dstart, dend) -> {
            if (source.equals(" ")) {
                ToastUtils.showShort(reason);
                return "";
            } else {
                return null;
            }
        };
        editText.setFilters(new InputFilter[]{spaceFilter});
    }
}
