package io.githup.limvot.mangaapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by nathan on 8/20/14.
 */
public class ScriptManager {
    static ScriptManager scriptManager;

    ArrayList<Script> scriptList;

    public ScriptManager(Context ctx) {
        scriptList = new ArrayList<Script>();

        File scriptDir = ctx.getDir("MangagagaScripts", Context.MODE_PRIVATE);

        String[] arr = new String[] { "KissManga", "MangaHere", "MangaPanda", "Mangable", "Manga King"};
        for (String name : arr) {
            try {
                File newScript = new File(scriptDir.getAbsolutePath(), name);
                newScript.createNewFile();

                FileOutputStream fos = new FileOutputStream(newScript);
                String program = "print 'Hello from Lua!!!!'\n" +
                        "apiObj = 0\n" +
                        "function init(apiObjIn)\n" +
                        "   apiObj = apiObjIn\n" +
                        "end\n" +
                        "\n" +
                        "function getMangaList()\n" +
                        "   path = apiObj:download('http://kissmanga.com/MangaList')\n" +
                        "   pageSource = apiObj:readFile(path)\n" +
                        "   apiObj:note()\n" +
                        "   daList = {}\n" +
                        "   beginning, ending = string.find(pageSource, '<a href=\"/Manga/.-\">')\n" +
                        "   index = 0\n" +
                        "   while ending do\n" +
                        "       aManga = string.sub(pageSource, beginning, ending)\n" +
                        "       daList[index] = string.sub(aManga, 17, -3)\n" +
                        "       index = index + 1\n" +
                        "       beginning, ending = string.find(pageSource, '<a href=\"/Manga/%w+\">', ending+1)\n" +
                        "   end" +
                        "   return daList\n" +
                        "end";
                fos.write(program.getBytes());
                fos.close();
            } catch (Exception e) {
                Log.e("Script", e.toString());
            }
        }


        for (File script : scriptDir.listFiles()) {
            scriptList.add(new Script(script.getName(), Utilities.readFile(script.getAbsolutePath())));
        }
    }

    public static void createScriptManager(Context ctx) {
        scriptManager = new ScriptManager(ctx);
    }
    public static ScriptManager getScriptManager() {
        return scriptManager;
    }

    public int numSources() {
        return scriptList.size();
    }

    public Script getScript(int position) {
        if (position >= 0 && position < numSources())
            return scriptList.get(position);
        return null;
    }
}
