package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

import android.os.Environment;

import java.io.File;
import java.util.Date;

/**
 * Created by marcus on 9/2/14.
 * Converted to Scala by Pratik on 
 * Converted to Kotlin by Nathan on 6/30/17 - 7/1/17
 */
object SettingsManager : AnkoLogger {
    class SettingsManager() {
        var historySize = 10;
        var cacheAmmount = 5
        var apkDate = Date()
    }
    
    val mangagagaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga"
    var settingsMan   = SettingsManager()
    val settingsFile  = File(mangagagaPath, "Settings.json");

    fun setHistorySize(size: Int)   { settingsMan.historySize  = size;  saveSettings() }
    fun setCacheSize(size: Int)     { settingsMan.cacheAmmount = size;  saveSettings() }
    fun setApkDate(date: Date)      { settingsMan.apkDate      = date;  saveSettings() }

    fun getHistorySize() = settingsMan.historySize
    fun getCacheSize()   = settingsMan.cacheAmmount
    fun getApkDate()     = settingsMan.apkDate

    fun loadSettings() {
        if (!settingsFile.exists())
          saveSettings()
        settingsMan = Utilities.getGson().fromJson(File(settingsFile.getAbsolutePath()).readText(),
                                                   SettingsManager::class.java);
    }

    fun saveSettings() { settingsFile.writeText(Utilities.getGson().toJson(settingsMan)) }
}
