package com.sparrow.bundle.framework.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.sparrow.bundle.framework.R;

public class EditTextPassword extends AppCompatEditText {
    /**
     * 切换drawable的引用
     */
    private Drawable visibilityDrawable;

    private boolean visibililty = false;

    public EditTextPassword(Context context) {
        this(context, null);
        init();
    }

    public EditTextPassword(Context context, AttributeSet attrs) {
        //指定了默认的style属性
        this(context, attrs, android.R.attr.editTextStyle);
        init();
    }

    public EditTextPassword(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        visibilityDrawable = getResources().getDrawable(R.drawable.common_eyeopen);
        visibilityDrawable.setBounds(0, 0, visibilityDrawable.getMinimumWidth(),
                visibilityDrawable.getMinimumHeight());
        setCompoundDrawablesWithIntrinsicBounds(null, null,
                visibilityDrawable, null);
        this.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    /**
     * 用按下的位置来模拟点击事件
     * 当按下的点的位置 在  EditText的宽度 - (图标到控件右边的间距 + 图标的宽度)  和
     * EditText的宽度 - 图标到控件右边的间距 之间就模拟点击事件，
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {

            if (getCompoundDrawables()[2] != null) {
                boolean xFlag = false;
                boolean yFlag = false;
                //得到用户的点击位置，模拟点击事件
                xFlag = event.getX() > getWidth() - (visibilityDrawable.getIntrinsicWidth() + getCompoundPaddingRight
                        ()) &&
                        event.getX() < getWidth() - (getTotalPaddingRight() - getCompoundPaddingRight());

                if (xFlag) {
                    visibililty = !visibililty;
                    if (visibililty) {
                        visibilityDrawable = getResources().getDrawable(R.drawable.common_eyeclose);
                        /*this.setInputType(EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);*/
                        this.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        //隐藏密码
                        visibilityDrawable = getResources().getDrawable(R.drawable.common_eyeopen);
                        //this.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                        this.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }

                    //将光标定位到指定的位置
                    CharSequence text = this.getText();
                    if (text instanceof Spannable) {
                        Spannable spanText = (Spannable) text;
                        Selection.setSelection(spanText, text.length());
                    }
                    visibilityDrawable.setBounds(0, 0, visibilityDrawable.getMinimumWidth(),
                            visibilityDrawable.getMinimumHeight());
                    setCompoundDrawablesWithIntrinsicBounds(null, null,
                            visibilityDrawable, null);
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
