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

package com.metinkale.prayerapp.vakit.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App.NotIds;
import com.metinkale.prayerapp.vakit.AlarmReceiver;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

public class NotificationPopup extends Activity {
    @Nullable
    public static NotificationPopup instance;


    @Override
    public void onResume() {
        super.onResume();
        instance = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        instance = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.vakit_notpopup);

        TextView name = (TextView) findViewById(R.id.name);
        name.setText(getIntent().getStringExtra("name"));
        TextView vakit = (TextView) findViewById(R.id.vakit);
        vakit.setText(getIntent().getStringExtra("vakit"));
        vakit.setKeepScreenOn(true);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(@NonNull Context context, Intent intent) {
                context.sendBroadcast(new Intent(context, AlarmReceiver.Audio.class));
                finish();
            }
        };
        registerReceiver(mReceiver, filter);


    }

    void onDismiss() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(getIntent().getIntExtra("city", 0) + "", NotIds.ALARM);
        sendBroadcast(new Intent(this, AlarmReceiver.Audio.class));
        finish();
    }


    @SuppressLint("ClickableViewAccessibility")
    public static class MyView extends View implements OnTouchListener {
        private final Paint paint = new Paint();
        private final Drawable icon;
        private final Drawable silent;
        private final Drawable close;
        private MotionEvent touch;
        private boolean acceptTouch;

        public MyView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            icon = context.getResources().getDrawable(R.drawable.ic_abicon);
            silent = MaterialDrawableBuilder.with(context)
                    .setIcon(MaterialDrawableBuilder.IconValue.VOLUME_OFF)
                    .setColor(Color.WHITE)
                    .setSizeDp(24)
                    .build();
            close = MaterialDrawableBuilder.with(context)
                    .setIcon(MaterialDrawableBuilder.IconValue.CLOSE)
                    .setColor(Color.WHITE)
                    .setSizeDp(24)
                    .build();

            setOnTouchListener(this);
        }

        public MyView(@NonNull Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MyView(@NonNull Context context) {
            this(context, null);
        }

        @Override
        public void onMeasure(int widthSpec, int heightSpec) {
            super.onMeasure(widthSpec, heightSpec);
            int size = Math.min(getMeasuredWidth(), getMeasuredHeight());

            setMeasuredDimension(size, size);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int r = w / 10;

            canvas.translate(w / 2, w / 2);
            if (touch == null) {
                icon.setBounds(-r, -r, r, r);
                icon.draw(canvas);
                return;
            }

            int x = (int) touch.getX();
            int y = (int) touch.getY();
            x -= getLeft();
            y -= getTop();
            x -= w / 2;
            y -= w / 2;

            float tr = (float) Math.sqrt((x * x) + (y * y));
            double angle = Math.atan(y / (double) x);
            if (x < 0) {
                angle += Math.PI;
            }
            if (tr >= ((w / 2) - r)) {
                tr = w / 2 - r;
            }

            x = (int) (Math.cos(angle) * tr);
            y = (int) (Math.sin(angle) * tr);

            if ((touch.getAction() == MotionEvent.ACTION_DOWN) && (Math.abs(x) < r) && (Math.abs(y) < r)) {
                acceptTouch = true;
            }

            if (acceptTouch && (touch.getAction() != MotionEvent.ACTION_UP)) {
                silent.setBounds(-5 * r, -r, -3 * r, r);

                silent.draw(canvas);

                close.setBounds(3 * r, -r, 5 * r, r);
                close.draw(canvas);

                icon.setBounds(x - r, y - r, x + r, y + r);

                paint.setStyle(Style.STROKE);
                paint.setColor(0x88FFFFFF);
                canvas.drawCircle(0f, 0f, tr, paint);

            } else {
                icon.setBounds(-r, -r, r, r);

                if (tr > (3 * r)) {
                    if ((Math.abs(angle) < (Math.PI / 10)) && (instance != null)) {
                        instance.finish();
                    }

                    if ((Math.abs(angle - Math.PI) < (Math.PI / 10)) && (instance != null)) {
                        instance.onDismiss();
                    }
                }
            }

            icon.draw(canvas);

        }

        @Override
        public boolean onTouch(View arg0, @NonNull MotionEvent me) {
            touch = me;
            if (me.getAction() == MotionEvent.ACTION_UP) {
                acceptTouch = false;
            }

            invalidate();

            return true;

        }

    }

}
