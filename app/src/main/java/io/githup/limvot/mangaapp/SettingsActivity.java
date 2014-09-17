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


public class SettingsActivity extends Activity {

    private SettingsManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        manager = SettingsManager.getSettingsManager();

        EditText historySize = (EditText) findViewById(R.id.editText);
        historySize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                historyLimitCallback();
            }
        });

        Button CCache = (Button) findViewById(R.id.buttonClearCache);
        CCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.clearCache();
            }
        });

        Button CHistory = (Button) findViewById(R.id.buttonClearHistory);
        CHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MangaManager.getMangaManager().clearHistory();
            }
        });

        Button CFavorites = (Button) findViewById(R.id.buttonClearFavorites);
        CFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MangaManager.getMangaManager().clearFavorites();
            }
        });

        Button CSaved = (Button) findViewById(R.id.buttonClearSaved);
        CSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MangaManager.getMangaManager().clearSaved();
            }
        });

        Button CAll = (Button) findViewById(R.id.buttonClearAll);
        CAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.clearCache();
                MangaManager.getMangaManager().clearHistory();
                MangaManager.getMangaManager().clearFavorites();
                MangaManager.getMangaManager().clearSaved();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void historyLimitCallback()
    {
        EditText historySize = (EditText) findViewById(R.id.editText);
        manager.setHistorySize(Integer.parseInt(historySize.getText().toString()));
        Log.d("historyLimitCallback", Integer.toString(manager.getHistorySize()));
    }
}
