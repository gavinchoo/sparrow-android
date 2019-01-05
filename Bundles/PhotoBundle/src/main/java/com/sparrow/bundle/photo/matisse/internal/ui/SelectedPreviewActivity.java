/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sparrow.bundle.photo.matisse.internal.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.sparrow.bundle.photo.matisse.internal.entity.Item;
import com.sparrow.bundle.photo.matisse.internal.model.SelectedItemCollection;

import java.util.List;

public class SelectedPreviewActivity extends BasePreviewActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE);
        List<Item> selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
        mPreviousPos = bundle.getInt(SelectedItemCollection.STATE_SELECT_POSITION, -1);
        mAdapter.addAll(selected);
        mAdapter.notifyDataSetChanged();
        if (mSpec.countable) {
            mCheckView.setCheckedNum(1);
        } else {
            mCheckView.setChecked(true);
        }

        if (mPreviousPos != -1) {
            updateSize(selected.get(mPreviousPos));
            mPager.setCurrentItem(mPreviousPos);

            findViewById(com.sparrow.bundle.photo.R.id.bottom_toolbar).setVisibility(View.GONE);
            findViewById(com.sparrow.bundle.photo.R.id.check_view).setVisibility(View.GONE);
        }
    }

}
