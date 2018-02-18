/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ptc.services.common.config;

import java.text.DateFormat;
import java.util.Locale;

/**
 *
 * @author veckardt
 */
public class Config {
    public static Locale locale = Locale.getDefault();
    public static DateFormat dfDay = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
    public static DateFormat dfDayTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
    public static DateFormat dfDayTimeUS = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    public static DateFormat dfDayTimeShort = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
    public static DateFormat dfDayTimeShortUS = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);

    public static DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault());
}
