package io.githup.limvot.mangaapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import java.io.StringReader;
import android.content.Intent;

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


        setContentView(R.layout.activity_home_screen);
        scriptView = (TextView) findViewById(R.id.script_box);
        scriptView.append("\n");

        String luaFunctionString = "function Echo(it) return 'Lua: ' .. it end";

        Globals globals = JsePlatform.standardGlobals();
        globals.load(new StringReader(luaFunctionString), "main.lua").call();
        LuaValue echoFun = globals.get("Echo");
        scriptView.append(echoFun.call(LuaValue.valueOf("Java String!")).toString());

//        downloader = new Utilities();
//        downloader.SetSource("http://kissmanga.com/Manga/Naruto");
//        String res = downloader.Download();
//        Log.i("onCreate", res);

        scriptView.append("\n");

        Intent sourceView = new Intent(this, SourceActivity.class);
        startActivity(sourceView);

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
