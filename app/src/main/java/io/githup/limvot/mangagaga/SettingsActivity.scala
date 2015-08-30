package io.githup.limvot.mangagaga;

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

        contentView = new SScrollView() {

            this += new SVerticalLayout {
        
            val buttonClearHistory = SButton("Clear History",MangaManager.clearHistory())
            val buttonClearCache = SButton("Clear Cache",Utilities.clearCache())
            val buttonClearFavorites = SButton("Clear Favroites",MangaManager.clearFavorites())
            val buttonClearSaved = SButton("Clear Saved Chapters",MangaManager.clearSaved())
            val buttonClearAll = SButton("Clear All", clearAll)
            val buttonCheckUpdate: SButton = SButton("Check for Updates", update)
            val historySize = STextView("Number of entries to save in history")
            historySizeText = SEditText(SettingsManager.getHistorySize.toString)
            historySizeText.setRawInputType(2)

            historySizeText.afterTextChanged {changeHistorySize(historySizeText)}

            val cacheSize = STextView("Number of pages to prefetch")
            val cacheSizeText = SEditText(SettingsManager.getCacheSize.toString)


            cacheSizeText.afterTextChanged { changeCacheSize(cacheSizeText) }
            cacheSizeText.setRawInputType(2)

            }

        }
    }

    def clearAll() {
        Utilities.clearCache()
        MangaManager.clearHistory()
        MangaManager.clearFavorites()
        MangaManager.clearSaved()
    }

    def update() = Utilities.checkForUpdates(this)

    def changeHistorySize(textView : SEditText) {
      try {
        var newSize = Integer.parseInt(textView.toString)
        SettingsManager.setHistorySize(newSize)
      }
      catch{
        case e:NumberFormatException => { Log.d("Settings Changed","Unable to parse history size!")}
      }
    }
    
    def changeCacheSize(textView : SEditText) {
      try {
        var newSize = Integer.parseInt(textView.toString)
        SettingsManager.setCacheSize(newSize)
      }
      catch{
        case e:NumberFormatException => { Log.d("Settings Changed","Unable to parse cache size!")}
      }
    }
}
