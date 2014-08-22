package io.githup.limvot.mangaapp;

import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nathan on 8/20/14.
 */
class Script {
    private String name;
    private String luaCode;

    private Globals globals;
    private LuaValue luaGetMangaList;
    private LuaValue luaGetMangaChapterList;

    private Manga currentManga;

    public Script(String name, String luaCode) {
        this.name = name;
        this.luaCode = luaCode;

        globals = JsePlatform.standardGlobals();
        globals.load(new StringReader(luaCode), name).call();
        // Call init function which normally saves this APIObject
        globals.get("init").call(CoerceJavaToLua.coerce(APIObject.getAPIObject()));
        luaGetMangaList = globals.get("getMangaList");
        luaGetMangaChapterList = globals.get("getMangaChapterList");
    }

    public String getName() {
        return name;
    }

    public List<Manga> getMangaList() {
        LuaValue result = luaGetMangaList.call();
        LuaTable resTable = result.checktable();

        ArrayList<Manga> mangaList = new ArrayList<Manga>();
        Log.i("getMangaList", "Woooooo: " + resTable.length());
        for (int i = 0; i < resTable.get("numManga").toint(); i++)
            mangaList.add(new Manga(resTable.get(i).checktable()));

        return mangaList;
    }

    public List<Chapter> getMangaChapterList(Manga manga) {
        LuaValue result = luaGetMangaChapterList.call(manga.getTable());
        LuaTable resTable = result.checktable();

        ArrayList<Chapter> mangaChapterList = new ArrayList<Chapter>();
        Log.i("getMangaChapterList", "Woooooo: " + resTable.length());
        for (int i = 0; i < resTable.get("numChapters").toint(); i++)
            mangaChapterList.add(new Chapter(resTable.get(i).checktable()));

        return mangaChapterList;
    }

    public void setCurrentManga(Manga curr) { currentManga = curr; }
    public Manga getCurrentManga() { return currentManga; }
}
