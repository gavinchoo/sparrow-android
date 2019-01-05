package com.sparrow.bundle.framework.jsbridge;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Stack;

public class JSBridge {
    public static final String BRIDGE_NAME = "JSBridge";
    private static JSBridge INSTANCE = new JSBridge();
    private boolean isEnable = true;
    private ArrayMap<String, Class<? extends IInject>> mClassMap = new ArrayMap<>();
    private Stack<JsCallback> mJsCallback = new Stack<>();

    private JSBridge() {
        mClassMap.put(BRIDGE_NAME, JSInterface.class);
    }

    public static JSBridge getInstance() {
        return INSTANCE;
    }

    public void addJsCallback(JsCallback callback) {
        if (!mJsCallback.contains(callback)) {
            mJsCallback.add(callback);
        }
    }

    public void removeJsCallback(JsCallback callback) {
        mJsCallback.remove(callback);
    }

    public JsCallback lastJsCallback() {
        return mJsCallback.lastElement();
    }

    public void clearJsCallback() {
        mJsCallback.clear();
    }

    public boolean addInjectPair(String name, Class<? extends IInject> clazz) {
        if (!mClassMap.containsKey(name)) {
            mClassMap.put(name, clazz);
            return true;
        }
        return false;
    }

    public boolean removeInjectPair(String name, Class<? extends IInject> clazz) {
        if (TextUtils.equals(name, BRIDGE_NAME)) {
            return false;
        }
        Class clazzValue = mClassMap.get(name);
        if (null != clazzValue && (clazzValue == clazz)) {
            mClassMap.remove(name);
            return true;
        }
        return false;
    }

    public ArrayMap<String, Class<? extends IInject>> getInjectPair() {
        return mClassMap;
    }

    public void invokeLaskJSCallback(JSONObject objects) {
        invokeJSCallback(lastJsCallback(), true, null, objects);
    }

    public void invokeJSCallback(JsCallback callback, JSONObject objects) {
        invokeJSCallback(callback, true, null, objects);
    }

    public void invokeJSCallback(JsCallback callback, boolean isSuccess, String message, JSONObject objects) {
        try {
            callback.apply(isSuccess, message, objects);
            removeJsCallback(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}