package io.githup.limvot.mangaapp;

import java.util.ArrayList;
import java.util.List;

import io.githup.limvot.mangaapp.Script;

/**
 * Created by nathan on 8/20/14.
 */
public class ScriptManager {
    static ScriptManager scriptManager;

    ArrayList<Script> scriptList;

    public ScriptManager() {
        scriptList = new ArrayList<Script>();
        String[] arr = new String[] { "KissManga", "MangaHere", "MangaPanda", "Mangable", "Manga King"};
        for (String name : arr)
            scriptList.add(new Script(name, name));
    }

    public static ScriptManager getScriptManager() {
        if (scriptManager == null)
            scriptManager = new ScriptManager();
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
