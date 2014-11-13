package io.githup.limvot.mangaapp;

import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nathan on 8/20/14.
 */
class Script {
    private String name;
    private String luaCode;
    private int scriptNumber;

    private Globals globals;
    private LuaValue luaGetMangaListTypes;
    private LuaValue luaSetMangaListType;
    private LuaValue luaGetMangaListPage1;
    private LuaValue luaGetMangaListPreviousPage;
    private LuaValue luaGetMangaListNextPage;
    private LuaValue luaInitManga;
    private LuaValue luaGetMangaChapterList;
    private LuaValue luaGetMangaChapterPage;
    private LuaValue luaGetMangaChapterNumPages;

    public Script(String name, String luaCode, int scriptNumber) {
        this.name = name;
        this.luaCode = luaCode;
        this.scriptNumber = scriptNumber;

        globals = JsePlatform.standardGlobals();
        globals.load(new StringReader(ScriptManager.getLuaPrequal()), "luaPrequal").call();
        // Call init function which normally saves this APIObject
        APIObject apiObj = APIObject.getAPIObject();
        Log.i("TheAPIOBject", apiObj.toString());
        globals.get("init").call(CoerceJavaToLua.coerce(apiObj));

        globals.load(new StringReader(luaCode), name).call();
        luaGetMangaListTypes = globals.get("getMangaListTypes");
        luaSetMangaListType = globals.get("setMangaListType");
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

    public List<String> getMangaListTypes() {
        LuaValue result = luaGetMangaListTypes.call();
        LuaTable resTable = result.checktable();

        ArrayList<String> typeList = new ArrayList<String>();
        for (int i = 0; i < resTable.get("numTypes").toint(); i++)
            typeList.add(resTable.get(i).toString());

        return typeList;
    }

    public void setMangaListType(String type) {
        luaSetMangaListType.call(type);
    }


    public List<Manga> getMangaListPage1() { return getMangaList(luaGetMangaListPage1); }
    public List<Manga> getMangaListPreviousPage() { return getMangaList(luaGetMangaListPreviousPage); }
    public List<Manga> getMangaListNextPage() { return getMangaList(luaGetMangaListNextPage); }


    private List<Manga> getMangaList(LuaValue luaGetMangaListFunc) {
        LuaValue result = luaGetMangaListFunc.call();
        LuaTable resTable = result.checktable();

        ArrayList<Manga> mangaList = new ArrayList<Manga>();
        for (int i = 0; i < resTable.get("numManga").toint(); i++)
            mangaList.add(new Manga(scriptNumber, resTable.get(i).checktable()));

        return mangaList;
    }

    public void initManga(Manga manga) {
        luaInitManga.call(manga.getTable());
    }

    public List<Chapter> getMangaChapterList(Manga manga) {
        LuaValue result = luaGetMangaChapterList.call(manga.getTable());
        LuaTable resTable = result.checktable();

        ArrayList<Chapter> mangaChapterList = new ArrayList<Chapter>();
        for (int i = 0; i < resTable.get("numChapters").toint(); i++) {
            mangaChapterList.add(new Chapter(manga, resTable.get(i).checktable(), i));
        }

        return mangaChapterList;
    }

    public int getNumPages(Manga manga, Chapter chapter) {
        return luaGetMangaChapterNumPages.call(manga.getTable(), chapter.getTable()).toint();
    }

    public String downloadPage(Manga manga, Chapter chapter, int page) {
        return luaGetMangaChapterPage.call(manga.getTable(), chapter.getTable(), LuaValue.valueOf(page)).toString();
    }
}
