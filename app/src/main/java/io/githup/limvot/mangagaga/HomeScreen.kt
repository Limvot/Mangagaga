package io.githup.limvot.mangagaga

import android.os.Bundle
import android.os.Environment;
import android.app.Activity

import java.io.File;

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class HomeScreen : Activity(), AnkoLogger {
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
        }

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
        // Init ScriptManager with context
        ScriptManager.init(this)
        // Init the MangaManager with the current context
        // This has to be done after script manager has been set up
        MangaManager.setContext(this)

        //Have Utilites check for updates
        Utilities.checkForUpdates(this)
    }
}

