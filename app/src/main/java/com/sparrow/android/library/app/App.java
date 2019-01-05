package com.sparrow.android.library.app;

import com.alibaba.android.arouter.launcher.ARouter;
import com.sparrow.android.demo.BuildConfig;
import com.sparrow.bundle.framework.base.BaseApplication;
import com.sparrow.bundle.framework.utils.KLog;

public class App extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.LOGGABLE) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
        //开启打印日志
        KLog.init(BuildConfig.LOGGABLE);

    }
}
