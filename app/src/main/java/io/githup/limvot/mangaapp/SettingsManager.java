package io.githup.limvot.mangaapp;

import java.util.Date;

/**
 * Created by marcus on 9/2/14.
 */
public class SettingsManager {
    private static SettingsManager settings;
    private static int historySize;
    private static Date apkDate;

    public SettingsManager()
    {
        historySize = 10;
    }

    public static SettingsManager getSettingsManager()
    {
        if(settings == null)
            settings = new SettingsManager();
        return settings;
    }

    public static void setHistorySize(int size)
    {
        historySize = size;
    }

    public static int getHistorySize()
    {
        return historySize;
    }

    public static Date getApkDate() {
        if (apkDate == null)
            apkDate = new Date(0);
        return apkDate;
    }
    public static void setApkDate(Date date) {
        apkDate = date;
    }
}
