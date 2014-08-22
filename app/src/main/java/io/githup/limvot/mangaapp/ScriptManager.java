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

    private ArrayList<Script> scriptList;
    private int currentSource;

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
                        "   apiObj:note('LuaScript downloaded (for manga): ' .. path)\n" +
                        "   daList = {}\n" +
                        "   regex = '<a href=\"/Manga/(.-)\">(.-)</a>'\n" +
                        "   apiObj:note('Manga Regex: ' .. regex)\n" +
                        "   beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex)\n" +
                        "   index = 0\n" +
                        "   while ending do\n" +
                        "       print('URL: ' .. mangaURL .. ', Title: ' .. mangaTitle)\n" +
                        "       daList[index] = {title = mangaTitle, url = mangaURL}\n" +
                        "       beginning, ending, mangaURL, mangaTitle = string.find(pageSource, regex, ending+1)\n" +
                        "       index = index + 1\n" +
                        "   end\n" +
                        "   daList['numManga'] = index\n" +
                        "   return daList\n" +
                        "end\n" +
                        "\n" +
                        "\n" +
                        "function getMangaChapterList(manga)\n" +
                        "   chapterURL = 'http://kissmanga.com/Manga' .. '/' .. manga['url']\n" +
                        "   apiObj:note('Manga Path: ' .. chapterURL)\n" +
                        "   path = apiObj:download(chapterURL)\n" +
                        "   pageSource = apiObj:readFile(path)\n" +
                        "   apiObj:note('LuaScript downloaded (for chapter): ' .. path)\n" +
                        "   daList = {}\n" +
                        "   regex = '<a href=\"/Manga/' .. manga['url'] .. '/(.-)\">(.-)</a>'" +
                        "   apiObj:note('Chapter Regex: ' .. regex)\n" +
                        "   beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex)\n" +
                        "   index = 0\n" +
                        "   while ending do\n" +
                        "       print('Chapter URL: ' .. chapterURL .. ', Chapter Title: ' .. chapterTitle)\n" +
                        "       daList[index] = {title = chapterTitle, url = chapterURL}\n" +
                        "       beginning, ending, chapterURL, chapterTitle = string.find(pageSource, regex, ending+1)\n" +
                        "       index = index + 1\n" +
                        "   end\n" +
                        "   daList['numChapters'] = index\n" +
                        "   return daList\n" +
                        "end\n";
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
    public void setCurrentSource(int num) { currentSource = num; }
    public Script getCurrentSource() { return getScript(currentSource); }
}
