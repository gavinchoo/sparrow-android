package com.sparrow.bundle.network.utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * @author zhangshaopeng
 * @date 2016/9/9 0009
 * @description
 */
public class MessageUtils extends Handler {
    /**
     * 占位符、默认值
     */
    public static final int B = 0;
    public static final String KEY_MSG = "MSG";
    private WeakReference<Activity> mContext;
    private Toast mToast;

    public MessageUtils(Activity activity) {
        mContext = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Activity activity = mContext.get();
        if (activity == null) return;
        switch (msg.what) {
            /**
             * 发送消息
             */
            case B:
                String message = msg.getData().getString(KEY_MSG);
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                if (mToast == null) {
                    mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(message);
                    mToast.setDuration(Toast.LENGTH_SHORT);
                }
                mToast.show();
                break;
            default:
                break;
        }
    }

    public void showMsg(String msg) {
        Message message = new Message();
        message.what = B;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MSG, msg);
        message.setData(bundle);
        this.sendMessage(message);
    }

    public WeakReference<Activity> getContext() {
        return mContext;
    }
    /*
    *//**
     * 显示提示信息
     *//*
    public void showInfo(final String info) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();//先移除
                Toast.makeText(mContext.get(), info, Toast.LENGTH_LONG).show();
                Looper.loop();// 进入loop中的循环，查看消息队列
            }
        }.start();
    }*/
}
