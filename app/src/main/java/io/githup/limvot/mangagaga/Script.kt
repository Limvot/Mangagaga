package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.StringReader;

class Script(val name : String, val luaCode : String, val scriptNumber : Int) : AnkoLogger {

    val globals = JsePlatform.standardGlobals()
    init {
        globals.load(StringReader(ScriptManager.luaPrequal), "luaPrequal").call()
        // Call init function which normally saves this APIObject
        info(APIObject.instance().toString())
        globals.get("init").call(CoerceJavaToLua.coerce(APIObject.instance()))

        globals.load(StringReader(luaCode), name).call()
    }
    var luaGetMangaListTypes = globals.get("getMangaListTypes")
    var luaSetMangaListType = globals.get("setMangaListType")
    var luaGetMangaListPage1 = globals.get("getMangaListPage1")
    var luaGetMangaListPreviousPage = globals.get("getMangaListPreviousPage")
    var luaGetMangaListNextPage = globals.get("getMangaListNextPage")
    var luaInitManga = globals.get("initManga")
    var luaGetMangaChapterList = globals.get("getMangaChapterList")
    var luaGetMangaChapterPage = globals.get("getMangaChapterPage")
    var luaGetMangaChapterNumPages = globals.get("getMangaChapterNumPages")


    fun getMangaListTypes() : List<String> {
        var result = luaGetMangaListTypes.call()
        var resTable = result.checktable()

        var typeList = mutableListOf<String>()

        for(i in 0 until (resTable.get("numTypes").toint() - 1))
            typeList.add(resTable.get(i).toString())

        return typeList
    }


    fun setMangaListType(mangatype : String) {
        luaSetMangaListType.call(mangatype)
    }


    fun getMangaListPage1() = getMangaList(luaGetMangaListPage1)
    fun getMangaListPreviousPage() = getMangaList(luaGetMangaListPreviousPage)
    fun getMangaListNextPage() = getMangaList(luaGetMangaListNextPage)


    fun getMangaList(luaGetMangaListFunc : LuaValue): List<Manga> {
        var result = luaGetMangaListFunc.call()
        var resTable = result.checktable()

        var mangaList = mutableListOf<Manga>()

        for(i in 0 until resTable.get("numManga").toint())
            mangaList.add(Manga(scriptNumber, resTable.get(i).checktable()))

        return mangaList
    }

    fun initManga(manga : Manga) {
        luaInitManga.call(manga.table)
    }

    fun getMangaChapterList(manga : Manga): List<Chapter> {
        var result = luaGetMangaChapterList.call(manga.table);
        var resTable = result.checktable();

        var mangaChapterList = mutableListOf<Chapter>()

        for(i in 0 until resTable.get("numChapters").toint())
            mangaChapterList.add(Chapter(manga, resTable.get(i).checktable(), i))

        return mangaChapterList
    }

    fun getNumPages(manga : Manga, chapter : Chapter) : Int {
        return luaGetMangaChapterNumPages.call(manga.table, chapter.table).toint()
    }

    fun downloadPage(manga : Manga, chapter : Chapter, page : Int) : String {
        return luaGetMangaChapterPage.call(manga.table, chapter.table, LuaValue.valueOf(page)).toString()
    }
}
