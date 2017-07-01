package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

/**
 * Created by marcus on 9/2/14.
 * Converted to Scala by Pratik on 
 * Converted to Kotlin by Nathan on 6/30/17 - 7/1/17
 */
object SettingsManager : AnkoLogger {
    class SettingsManager() {
        fun instance() = this;
        var historySize = 10;
        var cacheAmmount = 5
        var apkDate = Date()
    }
    
    var settingsMan : SettingsManager = SettingsManager();

    //comments form java code:
    // Mix of static with nonstatic data is so that it gets saved with Gson but can be referenced
    // from static contexts

    fun setHistorySize(size: Int) = {
        settingsMan.historySize = size;
        saveSettings();
    }
    fun getHistorySize(): Int = settingsMan.historySize;

    fun getCacheSize() = settingsMan.cacheAmmount
    fun setCacheSize(size: Int) { settingsMan.cacheAmmount = size; saveSettings() }

    fun getApkDate() : Date = settingsMan.apkDate;
    
    fun setApkDate(date: Date) = {
        settingsMan.apkDate = date;
        saveSettings();
    }


    fun loadSettings() = {
        var savedFile = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga", "Settings.json");
        if (!savedFile.exists())
          saveSettings()
        try {
            settingsMan = Utilities.getGson().fromJson(File(savedFile.getAbsolutePath()).readText(), SettingsManager::class.java);
            info("SAVED_SETTINGS - Loaded!");
        } catch (e: Exception) {
            info("SAVED_SETTINGS - Exception! $e");
        }
    }

    fun saveSettings() {
        try {
            var settingsFile = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga", "Settings.json");
            settingsFile.createNewFile();
            var fw = FileWriter(settingsFile.getAbsoluteFile());
            var bw = BufferedWriter(fw);
            bw.write(Utilities.getGson().toJson(settingsMan));
            bw.close();
            info("SAVE_SETTINGS - SAVED");
        } catch (e: Exception) {
            info("SAVE_SETTINGS - Problem");
        }
    }
}
