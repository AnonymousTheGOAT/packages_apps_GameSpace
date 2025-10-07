/*
 * SPDX-FileCopyrightText: 2022 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.TextView;

import io.chaldeaprjkt.gamespace.R;

public class MemoryView extends TextView {

    private ActivityManager mActivityManager;

    private Handler mHandler;
    private MemInfoWorker mWorker;

    public MemoryView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        mWorker = new MemInfoWorker();
    }

    /* Hijack this method to detect visibility rather than
     * onVisibilityChanged() because the the latter one can be
     * influenced by more factors, leading to unstable behavior. */
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility == VISIBLE) mHandler.post(mWorker);
        else mHandler.removeCallbacks(mWorker);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setVisibility(VISIBLE);
    }

    @Override
    public void onDetachedFromWindow() {
        setVisibility(GONE);
        super.onDetachedFromWindow();
    }

    private class MemInfoWorker implements Runnable {
        @Override
        public void run() {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            mActivityManager.getMemoryInfo(memInfo);
            int availMemMiB = (int) (memInfo.availMem / 1048576L);
            int totalMemMiB = (int) (memInfo.totalMem / 1048576L);
            setText(getContext().getString(R.string.memory_format, availMemMiB, totalMemMiB));

            mHandler.postDelayed(this, 1000);
        }
    }
}
