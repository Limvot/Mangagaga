package io.githup.limvot.mangagaga;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

/**
 * Created by marcus on 9/2/14.
 * Converted to Scala by Pratik on 
 */
object SettingsManager {
    class SettingsManager() {
        def instance() = this;
        var historySize = 10;
        var cacheAmmount = 5
        var apkDate = new Date
    }
    
    var settingsMan : SettingsManager = new SettingsManager();

    //comments form java code:
    // Mix of static with nonstatic data is so that it gets saved with Gson but can be referenced
    // from static contexts

    def setHistorySize(size: Int) = {
        settingsMan.historySize = size;
        saveSettings();
    }
    def getHistorySize(): Int = settingsMan.historySize;

    def getCacheSize() = settingsMan.cacheAmmount
    def setCacheSize(size: Int) { settingsMan.cacheAmmount = size; saveSettings() }

    def getApkDate() : Date = settingsMan.apkDate;
    
    def setApkDate(date: Date) = {
        settingsMan.apkDate = date;
        saveSettings();
    }


    def loadSettings() = {
        var savedFile = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "Settings.json");
        try {
            settingsMan = Utilities.getGson().fromJson(Utilities.readFile(savedFile.getAbsolutePath()), classOf[SettingsManager]);
            Log.i("SAVED_SETTINGS", "Loaded!");
        } catch {
            case e:Exception => {
            Log.i("SAVED_SETTINGS", "Exception!");
            }
        }
    }

    def saveSettings() {
        try {
            var settingsFile = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "Settings.json");
            settingsFile.createNewFile();
            var fw = new FileWriter(settingsFile.getAbsoluteFile());
            var bw = new BufferedWriter(fw: FileWriter);
            bw.write(Utilities.getGson().toJson(settingsMan));
            bw.close();
            Log.i("SAVE_SETTINGS", "SAVED");
        } catch {
            case e: Exception => {
            Log.i("SAVE_SETTINGS", "Problem");
            }
        }
    }
}
