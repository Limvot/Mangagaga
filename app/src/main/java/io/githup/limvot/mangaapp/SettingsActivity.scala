package io.githup.limvot.mangaapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.scaloid.common._

class SettingsActivity extends SActivity {

    implicit val tag = LoggerTag("Scala Setting Activity")
    SettingsManager.loadSettings();
    var historySizeText: SEditText = null

    onCreate{
        contentView = new SVerticalLayout() {
            val buttonClearHistory = SButton("Clear History",MangaManager.clearHistory())
            val buttonClearCache = SButton("Clear Cache",Utilities.clearCache())
            val buttonClearFavorites = SButton("Clear Favroites",MangaManager.clearFavorites())
            val buttonClearSaved = SButton("Clear Saved Chapters",MangaManager.clearSaved())
            val buttonClearAll = SButton("Clear All", clearAll)
            val buttonCheckUpdate: SButton = SButton("Check for Updates", update)
            val historySize = STextView("Number of entries to save in history")
            historySizeText = SEditText(SettingsManager.getHistorySize.toString)
            historySizeText.afterTextChanged { SettingsManager.setHistorySize(Integer.parseInt(historySizeText.getText().toString())) }

            val cacheSize = STextView("Number of pages to prefetch")
            val cacheSizeText = SEditText(SettingsManager.getCacheSize.toString)
            cacheSizeText.afterTextChanged { SettingsManager.setCacheSize(Integer.parseInt(cacheSizeText.getText.toString)) }
        }
    }

    def clearAll() {
        Utilities.clearCache()
        MangaManager.clearHistory()
        MangaManager.clearFavorites()
        MangaManager.clearSaved()
    }

    def update() = Utilities.checkForUpdates(this)
}
