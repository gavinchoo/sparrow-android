package com.sparrow.bundle.framework.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ReplacementSpan;
import android.widget.TextView;

/**
 * zhujianwei134
 *
 * 圆角文本标签工具类  SpannableString
 */
public class RoundSpanUtil {


    public static void roundSpan(Context context, TextView textView, String txtBgColor, String content, int start, int end) {
        RoundBackgroundColorSpan colorSpan = new RoundBackgroundColorSpan(context, Color.parseColor(txtBgColor));
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(colorSpan, start,
                end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    public static void roundSpan(Context context, TextView textView, @ColorRes int txtBgColor, String content, int start, int end) {
        RoundBackgroundColorSpan colorSpan = new RoundBackgroundColorSpan(context, ContextCompat.getColor(context, txtBgColor));
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(colorSpan, start,
                end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    public static class RoundBackgroundColorSpan extends ReplacementSpan {
        private int bgColor;
        private int textColor;
        private Context mContext;

        public RoundBackgroundColorSpan(Context context, int bgColor) {
            this.bgColor = bgColor;
            this.textColor = Color.parseColor("#FFFFFF");
            this.mContext = context;
        }

        public RoundBackgroundColorSpan(Context context, int bgColor, int textColor) {
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.mContext = context;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return ((int) paint.measureText(text, start, end) + 60);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            int color1 = paint.getColor();
            paint.setColor(this.bgColor);
            paint.setTextSize(DensityUtils.dip2px(mContext, 16));
            canvas.drawRoundRect(new RectF(x + 10, top, x + ((int) paint.measureText(text, start, end) + 40) - 10, bottom), 8, 8, paint);
            paint.setColor(this.textColor);
            canvas.drawText(text, start, end, x + 20, y - 5, paint);
            paint.setColor(color1);
        }
    }
}
