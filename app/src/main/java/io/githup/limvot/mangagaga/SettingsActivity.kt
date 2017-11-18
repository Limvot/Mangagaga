package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.app.Activity

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {

            button("Clear History")            { onClick { Boss.clearHistory() } }
            button("Clear Cache")              { onClick { Utilities.clearCache() } }
            button("Clear Favorites")          { onClick { Boss.clearFavorites() } }
            button("Clear Saved Chapters")     { onClick { Boss.clearSaved() } }

            button("Refresh Scripts from Git") { onClick { doAsync { Utilities.gitToScripts()
                                                                     ScriptManager.init() } } }

            button("Clear All")                { onClick { Boss.clearHistory()
                                                           Utilities.clearCache()
                                                           Boss.clearFavorites()
                                                           Boss.clearSaved() } }

            textView("Number of entries to save in history")
            val history_size_entry = editText(SettingsManager.getHistorySize().toString())

            textView("Number of pages to prefetch")
            val cache_size_entry = editText(SettingsManager.getCacheSize().toString())

            textView("http(s) address of git repo to push to")
            val git_url_entry = editText(SettingsManager.getGitURL())

            button("Save") { onClick {
                SettingsManager.setHistorySize(history_size_entry.text.toString().toInt())
                SettingsManager.setCacheSize(cache_size_entry.text.toString().toInt())
                SettingsManager.setGitURL(git_url_entry.text.toString())
            } }
        }
    }
}
