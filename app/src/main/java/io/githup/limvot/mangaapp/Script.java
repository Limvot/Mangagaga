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
    private LuaValue luaInitManga;
    private LuaValue luaGetMangaChapterList;
    private LuaValue luaGetMangaChapterPage;
    private LuaValue luaGetMangaChapterNumPages;

    private Manga currentManga;
    private Chapter currentChapter;
    private int currentPage;
    ArrayList<Chapter> mangaChapterList;

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
        luaInitManga = globals.get("initManga");
        luaGetMangaChapterList = globals.get("getMangaChapterList");
        luaGetMangaChapterPage = globals.get("getMangaChapterPage");
        luaGetMangaChapterNumPages = globals.get("getMangaChapterNumPages");
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

    public void initManga() {
        luaInitManga.call(currentManga.getTable());
    }

    public List<Chapter> getMangaChapterList() {
        LuaValue result = luaGetMangaChapterList.call(currentManga.getTable());
        LuaTable resTable = result.checktable();

        mangaChapterList = new ArrayList<Chapter>();
        Log.i("getMangaChapterList", "Woooooo: " + resTable.length());
        for (int i = 0; i < resTable.get("numChapters").toint(); i++)
            mangaChapterList.add(new Chapter(resTable.get(i).checktable(), i));

        return mangaChapterList;
    }

    public int getNumPages() {
        return luaGetMangaChapterNumPages.call(currentManga.getTable(), currentChapter.getTable()).toint();
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


    // THESE LOOK BACKWARDS
    // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
    // Make this script decidable later
    public void previousChapter() {
        Log.i("PREVOUS CHAPTER", Integer.toString(currentChapter.getNum()));
        if (currentChapter.getNum() < mangaChapterList.size()-1)
            currentChapter = mangaChapterList.get(currentChapter.getNum()+1);
    }

    public void nextChapter() {
        Log.i("NEXT CHAPTER", Integer.toString(currentChapter.getNum()));
        if (currentChapter.getNum() > 0)
            currentChapter = mangaChapterList.get(currentChapter.getNum()-1);
    }
}
