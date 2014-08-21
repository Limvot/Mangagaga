package io.githup.limvot.mangaapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import java.io.StringReader;
import android.content.Intent;


public class HomeScreen extends Activity {

    TextView scriptView;
    SourceDownloader downloader;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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

//        downloader = new SourceDownloader();
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
