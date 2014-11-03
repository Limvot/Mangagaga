package io.githup.limvot.mangaapp;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

/**
 * Created by marcus on 9/2/14.
 */
public class SettingsManager {
    private static SettingsManager settings;
    private int historySize;
    private Date apkDate;

    public SettingsManager()
    {
        historySize = 10;
    }

    public static SettingsManager getSettingsManager()
    {
        if(settings == null)
            loadSettings();
        if(settings == null)
            settings = new SettingsManager();
        return settings;
    }

    // Mix of static with nonstatic data is so that it gets saved with Gson but can be referenced
    // from static contexts

    public static void setHistorySize(int size)
    {
        settings.historySize = size;
        saveSettings();
    }

    public static int getHistorySize()
    {
        return settings.historySize;
    }

    public static Date getApkDate() {
        if (settings.apkDate == null)
            settings.apkDate = new Date(0);
        return settings.apkDate;
    }
    public static void setApkDate(Date date) {
        settings.apkDate = date;
        saveSettings();
    }

    public static void loadSettings() {
        File savedFile = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "Settings.json");
        try {
            settings = Utilities.getGson().fromJson(Utilities.readFile(savedFile.getAbsolutePath()), SettingsManager.class);
            Log.i("SAVED_SETTINGS", "Loaded!");
        } catch (Exception e) {
            Log.i("SAVED_SETTINGS", "Exception!");
        }
    }

    public static void saveSettings() {
        try {
            File settingsFile = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "Settings.json");
            settingsFile.createNewFile();
            FileWriter fw = new FileWriter(settingsFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(Utilities.getGson().toJson(settings));
            bw.close();
            Log.i("SAVE_SETTINGS", "SAVED");
        } catch (Exception e) {
            Log.i("SAVE_SETTINGS", "Problem");
        }
    }
}
