package io.githup.limvot.mangaapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import java.io.StringReader;
import android.content.Intent;

import com.google.gson.Gson;

import java.io.File;

// Created by Nathan Braswell on 8/19/14
// Modified by Pratik Gangwani on 8/24/14

public class HomeScreen extends Activity {

    TextView scriptView;
    Utilities downloader;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File newFolder = new File(Environment.getExternalStorageDirectory(), "Mangagaga");
        File newFolder1 = new File(newFolder, "Downloaded");
        File newFolder2 = new File(newFolder, "Scripts");
        File newFolder3 = new File(newFolder, "Cache");

        if (!newFolder.exists()) {
            try {

                newFolder.mkdir();
                newFolder1.mkdir();
                newFolder2.mkdir();
                newFolder3.mkdir();

            } catch (Exception e) {
                Log.d("OnCreate:", e.toString());
            }
        }

        // Set up the script manager
        ScriptManager.createScriptManager(this);
        // Init the MangaManager with the current context
        // This has to be done after script manager has been set up
        MangaManager.initMangaManager(this);


        setContentView(R.layout.activity_home_screen);

        Button browseSources = (Button) findViewById(R.id.browseSourcesButton);
        browseSources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreen.this, SourceActivity.class));
            }
        });

        Button favorites = (Button) findViewById(R.id.favoritesButton);
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreen.this, FavoritesActivity.class));
            }
        });

        Button history = (Button) findViewById(R.id.historyButton);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreen.this, HistoryActivity.class));
            }
        });

        Button settings = (Button) findViewById(R.id.settingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View view){
                Intent settingsView = new Intent(HomeScreen.this, SettingsActivity.class);
                startActivity(settingsView);
            }
        });

        Button downloaded = (Button) findViewById(R.id.downloadedButton);
        downloaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreen.this, DownloadedActivity.class));

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
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
}
