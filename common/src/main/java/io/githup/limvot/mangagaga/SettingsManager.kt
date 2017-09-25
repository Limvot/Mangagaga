package io.githup.limvot.mangagaga;

import java.io.File;
import java.util.Date;

/**
 * Created by marcus on 9/2/14.
 * Converted to Scala by Pratik on 
 * Converted to Kotlin by Nathan on 6/30/17 - 7/1/17
 */
object SettingsManager : GenericLogger {
    class SettingsManager() {
        var historySize = 10;
        var cacheAmmount = 5
        var apkDate = Date()
        var gitURL = "https://github.com/Limvot/MangagagaScripts.git"
    }
    
    var mangagagaPath = "_uninit_"
    var settingsMan   = SettingsManager()

    fun setHistorySize(size: Int)   { settingsMan.historySize  = size;  saveSettings() }
    fun setCacheSize(size: Int)     { settingsMan.cacheAmmount = size;  saveSettings() }
    fun setApkDate(date: Date)      { settingsMan.apkDate      = date;  saveSettings() }
    fun setGitURL(url: String)      { settingsMan.gitURL       = url;   saveSettings() }

    fun getHistorySize() = settingsMan.historySize
    fun getCacheSize()   = settingsMan.cacheAmmount
    fun getApkDate()     = settingsMan.apkDate
    fun getGitURL()      = settingsMan.gitURL

    fun loadSettings() {
        val settingsFile  = File(mangagagaPath, "Settings.json");
        if (!settingsFile.exists())
          saveSettings()
        settingsMan = Utilities.getGson().fromJson(File(settingsFile.getAbsolutePath()).readText(),
                                                   SettingsManager::class.java);
    }

    fun saveSettings() {
        File(mangagagaPath, "Settings.json").writeText(Utilities.getGson().toJson(settingsMan))
    }
}
