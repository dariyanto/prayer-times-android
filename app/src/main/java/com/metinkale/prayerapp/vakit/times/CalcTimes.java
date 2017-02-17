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

package com.metinkale.prayerapp.vakit.times;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.times.other.AdjMethod;
import com.metinkale.prayerapp.vakit.times.other.Juristic;
import com.metinkale.prayerapp.vakit.times.other.Method;
import com.metinkale.prayerapp.vakit.times.other.PrayTime;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.TimeZone;

public class CalcTimes extends Times {

    private String method;
    private double[] customMethodParams = new double[6];
    private String adjMethod;
    private String juristic;
    private transient PrayTime mPrayTime;


    @SuppressWarnings("unused")
    CalcTimes() {
        super();
    }

    CalcTimes(long id) {
        super(id);
    }


    private PrayTime getPrayTime() {
        if (mPrayTime == null) {
            mPrayTime = new PrayTime();
            try {
                if (getMethod() == Method.Custom)
                    mPrayTime.setMethodParams(customMethodParams);
                else
                    mPrayTime.setMethodParams(getMethod().params);
                mPrayTime.setAsrJuristic(getJuristic());
                mPrayTime.setAdjustHighLats(getAdjMethod());
            } catch (NullPointerException ignore) {
            }
            mPrayTime.setTimeZone(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000d * 60d * 60d));
        }
        return mPrayTime;
    }

    @SuppressLint("InflateParams")
    public static void add(@NonNull final Activity c, @NonNull final Bundle bdl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        final LayoutInflater inflater = LayoutInflater.from(c);
        View view = inflater.inflate(R.layout.calcmethod_dialog, null);
        ListView lv = (ListView) view.findViewById(R.id.listView);
        final Spinner sp = (Spinner) view.findViewById(R.id.spinner);
        final Spinner sp2 = (Spinner) view.findViewById(R.id.spinner2);

        lv.setAdapter(new ArrayAdapter<Method>(c, R.layout.calcmethod_dlgitem, R.id.legacySwitch, Method.values()) {
            @NonNull
            @Override
            public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
                ViewGroup vg = (ViewGroup) super.getView(pos, convertView, parent);
                ((TextView) vg.getChildAt(0)).setText(getItem(pos).title);
                ((TextView) vg.getChildAt(1)).setText(getItem(pos).desc);
                return vg;
            }

        });


        sp.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.adjMethod)));
        sp2.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.juristicMethod)));


        builder.setTitle(R.string.calcMethod);
        builder.setView(view);
        final AlertDialog dlg = builder.create();
        lv.setOnItemClickListener((arg0, arg1, pos, index) -> {
            Method method1 = Method.values()[pos];

            if (method1 == Method.Custom) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(c);
                final View custom = inflater.inflate(R.layout.calcmethod_custom_dialog, null);

                builder1.setView(custom);
                final Dialog dlg1 = builder1.show();
                custom.findViewById(R.id.ok).setOnClickListener(view1 -> {
                    CalcTimes t = new CalcTimes(System.currentTimeMillis());
                    t.setSource(Source.Calc);
                    t.setName(bdl.getString("city"));
                    t.setLat(bdl.getDouble("lat"));
                    t.setLng(bdl.getDouble("lng"));
                    t.setMethodParams(new double[]{
                            Integer.parseInt(((EditText) custom.findViewById(R.id.sabahPicker)).getText().toString()),
                            ((RadioButton) custom.findViewById(R.id.ogleAngleBased)).isChecked() ? 0 : 1,
                            Integer.parseInt(((EditText) custom.findViewById(R.id.oglePicker)).getText().toString()),
                            ((RadioButton) custom.findViewById(R.id.yatsiAngleBased)).isChecked() ? 0 : 1,
                            Integer.parseInt(((EditText) custom.findViewById(R.id.yatsiPicker)).getText().toString())
                    });
                    t.setJuristic(Juristic.values()[sp2.getSelectedItemPosition()]);
                    t.setAdjMethod(AdjMethod.values()[sp.getSelectedItemPosition()]);
                    t.setSortId(99);
                    c.finish();


                    dlg1.cancel();
                });
            } else {
                CalcTimes t = new CalcTimes(System.currentTimeMillis());
                t.setSource(Source.Calc);
                t.setName(bdl.getString("city"));
                t.setLat(bdl.getDouble("lat"));
                t.setLng(bdl.getDouble("lng"));
                t.setMethod(method1);
                t.setJuristic(Juristic.values()[sp2.getSelectedItemPosition()]);
                t.setAdjMethod(AdjMethod.values()[sp.getSelectedItemPosition()]);
                t.setSortId(99);
                c.finish();
            }
            dlg.cancel();
        });

        try {
            dlg.show();
        } catch (WindowManager.BadTokenException ignore) {
        }
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Calc;
    }

    @Override
    public String _getTime(@NonNull LocalDate date, int time) {
        List<String> times = getPrayTime().getDatePrayerTimes(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), getLat(), getLng());
        times.remove(4);
        return times.get(time);
    }


    public synchronized Method getMethod() {
        return Method.valueOf(method);
    }

    public synchronized void setMethod(@NonNull Method value) {
        method = value.name();
        save();
    }

    public synchronized double[] getMethodParams() {
        return customMethodParams;
    }

    public synchronized void setMethodParams(double[] params) {
        method = Method.Custom.name();
        customMethodParams = params;
        save();
    }

    public synchronized Juristic getJuristic() {
        return Juristic.valueOf(juristic);
    }

    public synchronized void setJuristic(@NonNull Juristic value) {
        juristic = value.name();
        save();
    }

    public synchronized AdjMethod getAdjMethod() {
        return AdjMethod.valueOf(adjMethod);
    }

    public synchronized void setAdjMethod(@NonNull AdjMethod value) {
        adjMethod = value.name();
        save();
    }


}
