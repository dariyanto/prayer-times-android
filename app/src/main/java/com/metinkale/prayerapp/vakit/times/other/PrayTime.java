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






package com.metinkale.prayerapp.vakit.times.other;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PrayTime {

    // ---------------------- Global Variables --------------------
    private double[] methodParams;// caculation method methodParams
    private Juristic asrJuristic; // Juristic method for Asr
    private AdjMethod adjustHighLats; // adjusting method for higher latitudes
    private double lat; // latitude
    private double lng; // longitude
    private double timeZone; // time-zone
    private double JDate; // Julian date

    // ------------------------------------------------------------
    // Calculation Methods
    private String InvalidTime; // The string used for invalid times
    // --------------------- Technical Settings --------------------
    private int numIterations; // number of iterations needed to compute times

    public PrayTime() {

        InvalidTime = "00:00"; // The string used for invalid times

        // --------------------- Technical Settings --------------------

        setNumIterations(1); // number of iterations needed to compute
        // times

    }

    // ---------------------- Trigonometric Functions -----------------------
    // range reduce angle in degrees.
    private double fixangle(double a) {

        a -= 360 * Math.floor(a / 360.0);

        a = (a < 0) ? (a + 360) : a;

        return a;
    }

    // range reduce hours to 0..23
    private double fixhour(double a) {
        a -= 24.0 * Math.floor(a / 24.0);
        a = (a < 0) ? (a + 24) : a;
        return a;
    }

    // radian to degree
    private double radiansToDegrees(double alpha) {
        return (alpha * 180.0) / Math.PI;
    }

    // deree to radian
    private double DegreesToRadians(double alpha) {
        return (alpha * Math.PI) / 180.0;
    }

    // degree sin
    private double dsin(double d) {
        return Math.sin(DegreesToRadians(d));
    }

    // degree cos
    private double dcos(double d) {
        return Math.cos(DegreesToRadians(d));
    }

    // degree tan
    private double dtan(double d) {
        return Math.tan(DegreesToRadians(d));
    }

    // degree arcsin
    private double darcsin(double x) {
        double val = Math.asin(x);
        return radiansToDegrees(val);
    }

    // degree arccos
    private double darccos(double x) {
        double val = Math.acos(x);
        return radiansToDegrees(val);
    }

    // degree arctan2
    private double darctan2(double y, double x) {
        double val = Math.atan2(y, x);
        return radiansToDegrees(val);
    }

    // degree arccot
    private double darccot(double x) {
        double val = Math.atan2(1.0, x);
        return radiansToDegrees(val);
    }

    // ---------------------- Julian Date Functions -----------------------
    // calculate julian date from a calendar date
    private double julianDate(int year, int month, int day) {

        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100.0);

        double B = (2 - A) + Math.floor(A / 4.0);

        return (Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B) - 1524.5;

    }

    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    @NonNull
    private double[] sunPosition(double jd) {

        double D = jd - 2451545;
        double g = fixangle(357.529 + (0.98560028 * D));
        double q = fixangle(280.459 + (0.98564736 * D));
        double L = fixangle(q + (1.915 * dsin(g)) + (0.020 * dsin(2 * g)));

        // double R = 1.00014 - 0.01671 * [self dcos:g] - 0.00014 * [self dcos:
        // (2*g)];
        double e = 23.439 - (0.00000036 * D);
        double d = darcsin(dsin(e) * dsin(L));
        double RA = darctan2(dcos(e) * dsin(L), dcos(L)) / 15.0;
        RA = fixhour(RA);
        double EqT = (q / 15.0) - RA;
        double[] sPosition = new double[2];
        sPosition[0] = d;
        sPosition[1] = EqT;

        return sPosition;
    }

    // compute equation of time
    private double equationOfTime(double jd) {
        return sunPosition(jd)[1];
    }

    // compute declination angle of sun
    private double sunDeclination(double jd) {
        return sunPosition(jd)[0];
    }

    // compute mid-day (Dhuhr, Zawal) time
    private double computeMidDay(double t) {
        double T = equationOfTime(getJDate() + t);
        return fixhour(12 - T);
    }

    // compute time for a given angle G
    private double computeTime(double G, double t) {

        double D = sunDeclination(getJDate() + t);
        double Z = computeMidDay(t);
        double Beg = -dsin(G) - (dsin(D) * dsin(getLat()));
        double Mid = dcos(D) * dcos(getLat());
        double V = darccos(Beg / Mid) / 15.0;

        return Z + ((G > 90) ? -V : V);
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private double computeAsr(double step, double t) {
        double D = sunDeclination(getJDate() + t);
        double G = -darccot(step + dtan(Math.abs(getLat() - D)));
        return computeTime(G, t);
    }

    // ---------------------- Misc Functions -----------------------
    // compute the difference between two times
    private double timeDiff(double time1, double time2) {
        return fixhour(time2 - time1);
    }

    // -------------------- Interface Functions --------------------
    // return prayer times for a given date
    @NonNull
    public List<String> getDatePrayerTimes(int year, int month, int day, double latitude, double longitude) {
        setLat(latitude);
        setLng(longitude);
        setJDate(julianDate(year, month, day));
        double lonDiff = longitude / (15.0 * 24.0);
        setJDate(getJDate() - lonDiff);
        return computeDayTimes();
    }

    // return prayer times for a given date
    @NonNull
    List<String> getPrayerTimes(@NonNull Calendar date, double latitude, double longitude) {

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);

        return getDatePrayerTimes(year, month + 1, day, latitude, longitude);
    }

    // convert double hours to 24h format
    String floatToTime24(double time) {

        String result;

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + (0.5 / 60.0)); // add 0.5 minutes to round
        int hours = (int) Math.floor(time);
        double minutes = Math.floor((time - hours) * 60.0);

        if ((hours >= 0) && (hours <= 9) && (minutes >= 0) && (minutes <= 9)) {
            result = "0" + hours + ":0" + Math.round(minutes);
        } else if ((hours >= 0) && (hours <= 9)) {
            result = "0" + hours + ":" + Math.round(minutes);
        } else if ((minutes >= 0) && (minutes <= 9)) {
            result = hours + ":0" + Math.round(minutes);
        } else {
            result = hours + ":" + Math.round(minutes);
        }
        return result;
    }

    // convert double hours to 12h format
    String floatToTime12(double time, boolean noSuffix) {

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + (0.5 / 60)); // add 0.5 minutes to round
        int hours = (int) Math.floor(time);
        double minutes = Math.floor((time - hours) * 60);
        String suffix;
        String result;
        if (hours >= 12) {
            suffix = "pm";
        } else {
            suffix = "am";
        }
        hours = (((hours + 12) - 1) % 12) + 1;
        /*
         * hours = (hours + 12) - 1; int hrs = (int) hours % 12; hrs += 1;
		 */
        if (noSuffix) {
            if ((hours >= 0) && (hours <= 9) && (minutes >= 0) && (minutes <= 9)) {
                result = "0" + hours + ":0" + Math.round(minutes);
            } else if ((hours >= 0) && (hours <= 9)) {
                result = "0" + hours + ":" + Math.round(minutes);
            } else if ((minutes >= 0) && (minutes <= 9)) {
                result = hours + ":0" + Math.round(minutes);
            } else {
                result = hours + ":" + Math.round(minutes);
            }
        } else {
            if ((hours >= 0) && (hours <= 9) && (minutes >= 0) && (minutes <= 9)) {
                result = "0" + hours + ":0" + Math.round(minutes) + " " + suffix;
            } else if ((hours >= 0) && (hours <= 9)) {
                result = "0" + hours + ":" + Math.round(minutes) + " " + suffix;
            } else if ((minutes >= 0) && (minutes <= 9)) {
                result = hours + ":0" + Math.round(minutes) + " " + suffix;
            } else {
                result = hours + ":" + Math.round(minutes) + " " + suffix;
            }

        }
        return result;

    }

    // convert double hours to 12h format with no suffix
    public String floatToTime12NS(double time) {
        return floatToTime12(time, true);
    }

    // ---------------------- Compute Prayer Times -----------------------
    // compute prayer times at given julian date
    @NonNull
    private double[] computeTimes(double[] times) {

        double[] t = dayPortion(times);

        double Fajr = computeTime(180 - methodParams[0], t[0]);

        double Sunrise = computeTime(180 - 0.833, t[1]);

        double Dhuhr = computeMidDay(t[2]);
        double Asr = computeAsr(1 + getAsrJuristic().ordinal(), t[3]);
        double Sunset = computeTime(0.833, t[4]);

        double Maghrib = computeTime(methodParams[2], t[5]);
        double Isha = computeTime(methodParams[4], t[6]);

        return new double[]{Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha};

    }

    // compute prayer times at given julian date
    @NonNull
    private ArrayList<String> computeDayTimes() {
        double[] times = {5, 6, 12, 13, 18, 18, 18}; // default times

        for (int i = 1; i <= getNumIterations(); i++) {
            times = computeTimes(times);
        }

        times = adjustTimes(times);

        return adjustTimesFormat(times);
    }

    // adjust times in a prayer time array
    @NonNull
    private double[] adjustTimes(@NonNull double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] += getTimeZone() - (getLng() / 15);
        }

        if (methodParams[1] == 1) // Maghrib
        {
            times[5] = times[4] + (methodParams[2] / 60);
        }
        if (methodParams[3] == 1) // Isha
        {
            times[6] = times[5] + (methodParams[4] / 60);
        }

        times = adjustHighLatTimes(times);

        return times;
    }

    // convert times array to given time format
    @NonNull
    private ArrayList<String> adjustTimesFormat(double[] times) {

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            result.add(floatToTime24(times[i]));
        }
        return result;
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private double[] adjustHighLatTimes(double[] times) {
        double nightTime = timeDiff(times[4], times[1]); // sunset to sunrise

        // Adjust Fajr
        double FajrDiff = nightPortion(methodParams[0]) * nightTime;

        if (Double.isNaN(times[0]) || (timeDiff(times[0], times[1]) > FajrDiff)) {
            times[0] = times[1] - FajrDiff;
        }

        // Adjust Isha
        double IshaAngle = (methodParams[3] == 0) ? methodParams[4] : 18;
        double IshaDiff = nightPortion(IshaAngle) * nightTime;
        if (Double.isNaN(times[6]) || (timeDiff(times[4], times[6]) > IshaDiff)) {
            times[6] = times[4] + IshaDiff;
        }

        // Adjust Maghrib
        double MaghribAngle = (methodParams[1] == 0) ? methodParams[2] : 4;
        double MaghribDiff = nightPortion(MaghribAngle) * nightTime;
        if (Double.isNaN(times[5]) || (timeDiff(times[4], times[5]) > MaghribDiff)) {
            times[5] = times[4] + MaghribDiff;
        }

        return times;
    }

    // the night portion used for adjusting times in higher latitudes
    private double nightPortion(double angle) {
        double calc = 0;

        if (adjustHighLats == AdjMethod.AngleBased) {
            calc = angle / 60.0;
        } else if (adjustHighLats == AdjMethod.MidNight) {
            calc = 0.5;
        } else if (adjustHighLats == AdjMethod.OneSeventh) {
            calc = 0.14286;
        }

        return calc;
    }

    // convert hours to day portions
    private double[] dayPortion(double[] times) {
        for (int i = 0; i < 7; i++) {
            times[i] /= 24;
        }
        return times;
    }

    public double[] getMethodParams() {
        return methodParams;
    }


    public void setMethodParams(double[] params) {
        this.methodParams = params;
    }


    public void setMethod(@NonNull Method method) {
        this.methodParams = method.params;
    }

    Juristic getAsrJuristic() {
        return asrJuristic;
    }

    public void setAsrJuristic(Juristic asrJuristic) {
        this.asrJuristic = asrJuristic;
    }

    public AdjMethod getAdjustHighLats() {
        return adjustHighLats;
    }

    public void setAdjustHighLats(AdjMethod adjustHighLats) {
        this.adjustHighLats = adjustHighLats;
    }

    double getLat() {
        return lat;
    }

    void setLat(double lat) {
        this.lat = lat;
    }

    double getLng() {
        return lng;
    }

    void setLng(double lng) {
        this.lng = lng;
    }

    double getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    double getJDate() {
        return JDate;
    }

    void setJDate(double jDate) {
        JDate = jDate;
    }

    private int getNumIterations() {
        return numIterations;
    }

    private void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }


}