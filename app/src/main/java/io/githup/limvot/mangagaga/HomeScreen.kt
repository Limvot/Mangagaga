package io.githup.limvot.mangagaga

import android.os.Bundle
import android.os.Environment;
import android.app.Activity
import android.content.Intent
import android.net.Uri

import java.io.File;
import java.util.Date;

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class HomeScreen : Activity(), GenericLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Homescreen GUI
        verticalLayout {
            button("Browse Sources") { onClick { startActivity<SourceActivity>() } }
            button("Favroitess")     { onClick { startActivity<FavoritesActivity>() } }
            button("History")        { onClick { startActivity<HistoryActivity>() } }
            button("Downloaded")     { onClick { startActivity<DownloadedActivity>() } }
            button("Settings")       { onClick { startActivity<SettingsActivity>() } }
            button("LogCat")         { onClick { startActivity<LogCatActivity>() } }
            button("Edit Scripts")   { onClick { startActivity<ScriptEditActivity>() } }
        }

        SettingsManager.mangagagaPath = Environment.getExternalStorageDirectory()
                                            .getAbsolutePath() + "/Mangagaga"
        // Setup our main folder
        val mainFolder = File(SettingsManager.mangagagaPath)
        try {
          if (!mainFolder.exists())
            mainFolder.mkdir()
          for (folderName in  listOf("Downloaded", "Scripts", "Cache")) {
            val folder = File(mainFolder, folderName)
            if (!folder.exists())
              folder.mkdir()
          }
        } catch(e: Exception) {
          info("OnCreate exception: $e")
        }

        // Clean out the cache folder
        Utilities.clearCache()

        // Overwrite all scripts
        val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
        File(scriptDir, "script_prequal").writeText(getResources()
                                         .openRawResource(R.raw.script_prequal)
                                         .bufferedReader().use { it.readText() })
        for (name in listOf("kiss_manga", "unixmanga", "read_panda", "manga_stream")) {
            val newScript = File(scriptDir, name)
            // For testing we want to always copy over scripts
            // on every update
            val rawResource = getResources().openRawResource( when(name) {
              "kiss_manga"   -> R.raw.kiss_manga
              "unixmanga"    -> R.raw.unixmanga
              "read_panda"   -> R.raw.read_panda
              "manga_stream" -> R.raw.manga_stream
              else           -> 0
            })
            newScript.writeBytes(rawResource.readBytes())
        }

        // Init ScriptManager
        ScriptManager.init()

        doAsync {
            // Check for updates
            var updateURL : String = "http://mangagaga.room409.xyz/app-debug.apk"
            var siteApkDate : Date = Utilities.getModifiedTime(updateURL)
            
            //This is needed to load the settings file from memory
            //and to make sure SettingsManager isn't null
            SettingsManager.loadSettings();
            
            info("Does this need updates? $siteApkDate")
            if (siteApkDate.after(SettingsManager.getApkDate())) {
                info("Ask user to update after downloading new apk!")
                var downloadedApk : File = File(Utilities.download(updateURL))
                var promptInstall : Intent = Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.fromFile(downloadedApk),
                                "application/vnd.android.package-archive")
                startActivity(promptInstall)
                SettingsManager.setApkDate(siteApkDate)
            }
        }
    }
}

