/*
 * Copyright (c) 2016 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.metinkale.prayerapp.compass._3D;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.crashlytics.android.Crashlytics;

@SuppressWarnings("deprecation")
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    @Nullable
    private Camera mCamera;
    private DisplayMetrics mMetrics;

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMetrics = getResources().getDisplayMetrics();
        mHolder = getHolder();
        mHolder.addCallback(this);

    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CameraSurfaceView(Context context) {
        this(context, null);

    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        setMeasuredDimension(mMetrics.widthPixels, mMetrics.heightPixels);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera == null) {
                // Open the Camera in preview mode
                mCamera = Camera.open();
            }
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mCamera == null) {
            return;
        }
        // Now that the size is known, set up the camera parameters and begin
        // the preview.

        Camera.Parameters parameters = mCamera.getParameters();
        Size size = getBestPreviewSize(width, height, parameters);
        if (size != null) {
            parameters.setPreviewSize(size.width, size.height);
        }
        mCamera.setParameters(parameters);

        try {
            mCamera.startPreview();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Nullable
    private Camera.Size getBestPreviewSize(int width, int height, @NonNull Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if ((size.width <= width) && (size.height <= height)) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera == null) {
            return;
        }

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Nullable
    public Camera getCamera() {
        return mCamera;
    }
}