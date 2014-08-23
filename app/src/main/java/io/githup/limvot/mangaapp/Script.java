package io.githup.limvot.mangaapp;

import android.provider.MediaStore;
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
    private LuaValue luaGetMangaListPage1;
    private LuaValue luaGetMangaListPreviousPage;
    private LuaValue luaGetMangaListNextPage;
    private LuaValue luaGetMangaChapterList;
    private LuaValue luaGetMangaChapterPage;

    private Manga currentManga;
    private Chapter currentChapter;
    private int currentPage;

    public Script(String name, String luaCode) {
        this.name = name;
        this.luaCode = luaCode;

        globals = JsePlatform.standardGlobals();
        globals.load(new StringReader(ScriptManager.getLuaPrequal()), "luaPrequal").call();
        // Call init function which normally saves this APIObject
        globals.get("init").call(CoerceJavaToLua.coerce(APIObject.getAPIObject()));

        globals.load(new StringReader(luaCode), name).call();
        luaGetMangaListPage1 = globals.get("getMangaListPage1");
        luaGetMangaListPreviousPage = globals.get("getMangaListPreviousPage");
        luaGetMangaListNextPage = globals.get("getMangaListNextPage");
        luaGetMangaChapterList = globals.get("getMangaChapterList");
        luaGetMangaChapterPage = globals.get("getMangaChapterPage");
    }

    public String getName() {
        return name;
    }

    public List<Manga> getMangaListPage1() { return getMangaList(luaGetMangaListPage1); }
    public List<Manga> getMangaListPreviousPage() { return getMangaList(luaGetMangaListPreviousPage); }
    public List<Manga> getMangaListNextPage() { return getMangaList(luaGetMangaListNextPage); }


    private List<Manga> getMangaList(LuaValue luaGetMangaListFunc) {
        LuaValue result = luaGetMangaListFunc.call();
        LuaTable resTable = result.checktable();

        ArrayList<Manga> mangaList = new ArrayList<Manga>();
        Log.i("getMangaList", "Woooooo: " + resTable.length());
        for (int i = 0; i < resTable.get("numManga").toint(); i++)
            mangaList.add(new Manga(resTable.get(i).checktable()));

        return mangaList;
    }

    public List<Chapter> getMangaChapterList() {
        LuaValue result = luaGetMangaChapterList.call(currentManga.getTable());
        LuaTable resTable = result.checktable();

        ArrayList<Chapter> mangaChapterList = new ArrayList<Chapter>();
        Log.i("getMangaChapterList", "Woooooo: " + resTable.length());
        for (int i = 0; i < resTable.get("numChapters").toint(); i++)
            mangaChapterList.add(new Chapter(resTable.get(i).checktable()));

        return mangaChapterList;
    }

    public String downloadPage() {
        Log.i("Downloading Page!", "doing that page");
        return luaGetMangaChapterPage.call(currentManga.getTable(), currentChapter.getTable(), LuaValue.valueOf(currentPage)).toString();
    }

    public void setCurrentManga(Manga curr) { currentManga = curr; }
    public Manga getCurrentManga() { return currentManga; }

    public void setCurrentChapter(Chapter curr) { currentChapter = curr; }
    public Chapter getCurrentChapter() { return currentChapter; }

    public void setCurrentPage(int page) { currentPage = page; }
    public int getCurrentPage() { return currentPage; }
}
