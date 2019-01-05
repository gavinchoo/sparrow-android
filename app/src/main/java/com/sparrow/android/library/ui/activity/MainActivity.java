package com.sparrow.android.library.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.sparrow.android.demo.R;
import com.sparrow.android.demo.BR;
import com.sparrow.android.demo.databinding.ActivityMainBinding;
import com.sparrow.android.library.constant.Router;
import com.sparrow.bundle.framework.base.BaseViewModel;
import com.sparrow.bundle.framework.base.ui.activity.BaseActivity;
import com.sparrow.bundle.framework.bundle.PhotoBundle;

public class MainActivity extends BaseActivity<ActivityMainBinding, BaseViewModel> {

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_main;
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
        getToolbar().setTitle(getString(R.string.app_name)).hideLeftIndicator();
    }

    @Override
    public void initListener() {
        binding.tvewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PhotoBundle(MainActivity.this)
                        .mulitPhoto(true)
                        .showSelectOriginal(false)
                        .selectType(PhotoBundle.SelectType.All)
                        .photoType(PhotoBundle.PhotoType.Certificate)
                        .show();
            }
        });
        binding.btnEtvewDemo.setOnClickListener(view ->
                ARouter.getInstance().build(Router.EdittextDemoActivity).navigation()
        );
    }
}
