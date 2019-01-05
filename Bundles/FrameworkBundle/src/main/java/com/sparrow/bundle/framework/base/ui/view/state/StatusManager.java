package com.sparrow.bundle.framework.base.ui.view.state;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.ViewGroup;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * @author zhangshaopeng
 * @date 2017/9/26
 * @description
 */

public class StatusManager {
    private final LoadLayout loadLayout;
    private boolean isTitle;

    private StatusManager(LoadLayout loadLayout, ArrayMap<Class<? extends LayoutType>, LayoutType> types, boolean isTitle) {
        this.loadLayout = loadLayout;
        this.isTitle = isTitle;
        loadLayout.loadTypes(types, isTitle);
        showContent();
    }

    public void showContent() {
        if (null != loadLayout) {
            loadLayout.showType(ContentType.class);
        }
    }

    /**
     * @param networkTypeIsWifi 是否为Wi-Fi环境
     */
    public void showUIContent(boolean networkTypeIsWifi) {
        if (networkTypeIsWifi) {
            Observable.empty().delay(300, TimeUnit.MILLISECONDS).subscribe(v -> showContent());
        } else {
            showContent();
        }
    }

    public void showEmpty() {
        loadLayout.showType(EmptyType.class);
    }

    public void showProgress() {
        loadLayout.showType(LoadingType.class);
    }

    public void showError() {
        loadLayout.showType(ErrorType.class);
    }

    public void showError(String errorMsg) {
        loadLayout.showType(ErrorType.class, errorMsg);
    }

    public void showSearchEmpty() {loadLayout.showType(SearchEmptyType.class);}

    public void addType(LayoutType layoutType) {
        loadLayout.addType(layoutType, isTitle);
    }

    public static class Builder {
        private final LoadLayout loadLayout;
        private ArrayMap<Class<? extends LayoutType>, LayoutType> types = new ArrayMap<>();
        private boolean title;

        public Builder(LoadLayout loadLayout) {
            this.loadLayout = loadLayout;
        }

        public Builder(Context context) {
            loadLayout = new LoadLayout(context);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            loadLayout.setLayoutParams(params);
        }

        public Builder addType(LayoutType layoutType) {
            types.put(layoutType.getClass(), layoutType);
            return this;
        }

        /**
         * 是否显示Title  如果显示子View需要给Toolbar腾出位置
         *
         * @param title 是否显示
         * @return this
         */
        public Builder setTitle(boolean title) {
            this.title = title;
            return this;
        }

        public StatusManager build() {
            return new StatusManager(loadLayout, types, title);
        }
    }

    public LoadLayout view() {
        return loadLayout;
    }
}
