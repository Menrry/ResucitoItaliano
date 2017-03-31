/*
 * Copyright 2015-2016 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.cammino.risuscito.layoutmanagers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import eu.davidea.flexibleadapter.common.TopSnappedSmoothScroller;

/**
 * Optimized implementation of LinearLayoutManager to SmoothScroll to a Top position.
 *
 * @since 5.0.0-b1
 */
public class SmoothScrollLinearLayoutManager extends LinearLayoutManager {

    private RecyclerView.SmoothScroller mSmoothScroller;

    public SmoothScrollLinearLayoutManager(Context context) {
        this(context, VERTICAL, false);
    }

    public SmoothScrollLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mSmoothScroller = new TopSnappedSmoothScroller(context, this);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        mSmoothScroller.setTargetPosition(position);
        startSmoothScroll(mSmoothScroller);
    }

}