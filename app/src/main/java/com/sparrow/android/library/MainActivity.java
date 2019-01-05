package com.sparrow.android.library;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sparrow.android.demo.R;
import com.sparrow.bundle.framework.bundle.PhotoBundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tvew_content).setOnClickListener(new View.OnClickListener() {
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
    }
}
