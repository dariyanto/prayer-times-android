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

package com.metinkale.prayerapp.zikr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class ZikrView extends View {
    private final Paint mPaint = new Paint();
    private int mMax = 33;
    private int mCount;
    private int mCount2;
    private int mColor = 0xFF33B5E5;
    @NonNull
    private RectF mRectF = new RectF();

    public ZikrView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public ZikrView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZikrView(Context context) {
        this(context, null);
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
        mRectF.set(0, 0, size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int width = getWidth();
        int center = width / 2;
        canvas.scale(0.95f, 0.95f, center, center);

        mPaint.setAntiAlias(true);

        mPaint.setStrokeWidth(center / 15);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(center / 10, center / 10, center / 10, mPaint);

        canvas.drawCircle(center, center, center, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(getColor());
        if ((getCount() * getMax()) != 0) {
            canvas.drawArc(mRectF, -90, (getCount() * 360) / getMax(), false, mPaint);
        }
        mPaint.setStrokeWidth(1);

        mPaint.setColor(Color.BLACK);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize((center * 2) / 5);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText(Math.round(mCount) + "", center, center, mPaint);

        mPaint.setTextSize((center * 2) / 20);
        canvas.drawText(getCount2() + "", center / 10, center * 0.13f, mPaint);


        mPaint.setTextSize((center * 2) / 10);
        mPaint.setColor(Color.GRAY);
        canvas.drawText("/" + Math.round(getMax()), center, (width * 2) / 3, mPaint);

    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
        if (mColor == 0) {
            this.mColor = 0xFF33B5E5;
        }

        invalidate();
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
        invalidate();
    }

    public int getCount2() {
        return mCount2;
    }

    public void setCount2(int mCount) {
        mCount2 = mCount;
        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int mMax) {
        this.mMax = mMax;
    }

    public void counter() {
        mCount2++;
    }
}