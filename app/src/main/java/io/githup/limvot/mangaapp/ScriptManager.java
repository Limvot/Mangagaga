package io.githup.limvot.mangaapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.InputStream;

/**
 * Created by nathan on 8/20/14.
 * Modified by Pratik on 8/24/14.
 */
public class ScriptManager {
    static ScriptManager scriptManager;

    private static String luaPrequal;
    private ArrayList<Script> scriptList;
    private int currentSource;

    public ScriptManager(Context ctx) {
        try {
            luaPrequal = Utilities.readFile(ctx.getResources().openRawResource(R.raw.script_prequal));
        } catch (Exception e) {
            Log.e("Could not open lua prequal", e.toString());
        }

        scriptList = new ArrayList<Script>();

        File scriptDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Scripts/");

        String[] arr = new String[] { "kiss_manga", "MangaHere", "MangaPanda", "Mangable", "Manga King"};
        for (String name : arr) {
            try {
                File newScript = new File(scriptDir, name);
                newScript.createNewFile();

                FileOutputStream fos = new FileOutputStream(newScript);
                InputStream rawResource = ctx.getResources().openRawResource(R.raw.kiss_manga);
                Utilities.copyStreams(rawResource, fos);
                fos.close();
            } catch (Exception e) {
                Log.e("Script", e.toString());
            }
        }


        for (File script : scriptDir.listFiles()) {
            try {
                scriptList.add(new Script(script.getName(), Utilities.readFile(script.getAbsolutePath())));
            } catch (Exception e) {
                Log.e("Could not open lua script", e.toString());
            }
        }
    }

    public static void createScriptManager(Context ctx) {
        scriptManager = new ScriptManager(ctx);
    }
    public static ScriptManager getScriptManager() {
        return scriptManager;
    }
    public static String getLuaPrequal() {
        return luaPrequal;
    }

    public int numSources() {
        return scriptList.size();
    }

    public Script getScript(int position) {
        if (position >= 0 && position < numSources())
            return scriptList.get(position);
        return null;
    }
    public void setCurrentSource(int num) { currentSource = num; }
    public Script getCurrentSource() { return getScript(currentSource); }
}
