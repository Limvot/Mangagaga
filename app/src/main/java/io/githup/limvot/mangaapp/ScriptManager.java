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
                        "function getMangaList()\n" +
                        "   daList = {}\n" +
                        "   daList[0] = 'Durarara'\n" +
                        "   daList[1] = 'One Piece'\n" +
                        "   daList[2] = 'Bleach'\n" +
                        "   return daList\n" +
                        "end";
                fos.write(program.getBytes());
                fos.close();
            } catch (Exception e) {
                Log.e("Script", e.toString());
            }
        }


        for (File script : scriptDir.listFiles()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(script)));
                StringBuilder sb = new StringBuilder();
                for (String line = reader.readLine(); line != null; line = reader.readLine())
                    sb.append(line).append("\n");
                String scriptString = sb.toString();

                scriptList.add(new Script(script.getName(), scriptString));
            } catch (Exception e) {
                Log.e("Script", e.toString());
            }
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
