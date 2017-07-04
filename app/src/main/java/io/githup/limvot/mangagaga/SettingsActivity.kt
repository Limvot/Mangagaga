package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.app.Activity

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            button("Clear History")         { onClick { MangaManager.clearHistory() } }
            button("Clear Cache")           { onClick { Utilities.clearCache() } }
            button("Clear Favorites")       { onClick { MangaManager.clearFavorites() } }
            button("Clear Saved Chapters")  { onClick { MangaManager.clearSaved() } }

            button("Clear All")             { onClick { MangaManager.clearHistory()
                                                        Utilities.clearCache()
                                                        MangaManager.clearFavorites()
                                                        MangaManager.clearSaved() } }

            button("Check for Updates")     { onClick { Utilities.checkForUpdates(ctx) } }

            textView("Number of entries to save in history")
            val history_size_entry = editText(SettingsManager.getHistorySize().toString()) 

            textView("Number of pages to prefetch")
            val cache_size_entry = editText(SettingsManager.getCacheSize().toString()) 

            button("Save") { onClick {
                SettingsManager.setHistorySize(history_size_entry.text.toString().toInt())
                SettingsManager.setCacheSize(cache_size_entry.text.toString().toInt())
            } }
        }
    }
}
