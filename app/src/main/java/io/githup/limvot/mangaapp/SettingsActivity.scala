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
    var historySize : STextView = null
    var editText : SEditText = null

    onCreate{
        contentView = new SVerticalLayout() {
            val buttonClearHistory = SButton("Clear History",MangaManager.clearHistory())
            val buttonClearCache = SButton("Clear Cache",Utilities.clearCache())
            val buttonClearFavorites = SButton("Clear Favorites",MangaManager.clearFavorites())
            val buttonClearSaved = SButton("Clear Saved Chapters",MangaManager.clearSaved())
            val buttonClearAll = SButton("Clear All",clearAll())
            historySize = STextView("Number of entries to save in history")
            editText = SEditText()
            editText.afterTextChanged(historyLimitCallback())
        }
    }

    def clearAll() {
        Utilities.clearCache()
        MangaManager.clearHistory()
        MangaManager.clearFavorites()
        MangaManager.clearSaved()
    }



    /*override def onCreateOptionsMenu(menu : Menu) : Boolean = {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }


    override def onOptionsItemSelected(item : MenuItem) : Boolean = {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    def historyLimitCallback() {
        SettingsManager.setHistorySize(Integer.parseInt(editText.getText().toString()))
        info(Integer.toString(SettingsManager.getHistorySize()))
    }
}
