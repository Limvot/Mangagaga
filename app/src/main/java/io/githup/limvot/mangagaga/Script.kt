package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

import org.luaj.vm2.*
import org.luaj.vm2.lib.jse.*

import java.io.StringReader;

class Script(val name : String, val luaCode : String, val scriptNumber : Int) : AnkoLogger {

    val globals = JsePlatform.standardGlobals()
    init {
        globals.load(StringReader(ScriptManager.luaPrequal), "luaPrequal").call()
        // Call init function which normally saves this APIObject
        globals.get("init").call(CoerceJavaToLua.coerce(APIObject.instance()))
        globals.load(StringReader(luaCode), name).call()
    }

    val luaGetMangaListTypes        = globals.get("getMangaListTypes")
    val luaSetMangaListType         = globals.get("setMangaListType")
    val luaGetMangaListPage1        = globals.get("getMangaListPage1")
    val luaGetMangaListPreviousPage = globals.get("getMangaListPreviousPage")
    val luaGetMangaListNextPage     = globals.get("getMangaListNextPage")
    val luaInitManga                = globals.get("initManga")
    val luaGetMangaChapterList      = globals.get("getMangaChapterList")
    val luaGetMangaChapterPage      = globals.get("getMangaChapterPage")
    val luaGetMangaChapterNumPages  = globals.get("getMangaChapterNumPages")

    fun getMangaListTypes() : List<String> {
        val resTable = luaGetMangaListTypes.call().checktable()
        return (0 .. resTable["numTypes"].toint()).map { resTable[it].toString() }
    }
    fun getMangaList(luaGetMangaListFunc : LuaValue): List<Manga> {
        val table = luaGetMangaListFunc.call().checktable()
        return (0 until table["numManga"].toint()).map {
            Manga(scriptNumber, table[it].checktable()) }
    }
    fun getMangaChapterList(manga : Manga): List<Chapter> {
        val table = luaGetMangaChapterList.call(manga.table).checktable()
        return (0 until table["numChapters"].toint()).map {
            Chapter(manga, table[it].checktable(), it) }
    }

    fun getMangaListPage1()        = getMangaList(luaGetMangaListPage1)
    fun getMangaListPreviousPage() = getMangaList(luaGetMangaListPreviousPage)
    fun getMangaListNextPage()     = getMangaList(luaGetMangaListNextPage)

    fun getNumPages(manga : Manga, chapter : Chapter) =
        luaGetMangaChapterNumPages.call(manga.table, chapter.table).toint()

    fun downloadPage(manga : Manga, chapter : Chapter, page : Int) =
        luaGetMangaChapterPage.call(manga.table, chapter.table, LuaValue.valueOf(page)).toString()

    fun setMangaListType(mangatype : String) { luaSetMangaListType.call(mangatype) }
    fun initManga(manga : Manga) { luaInitManga.call(manga.table) }
}
