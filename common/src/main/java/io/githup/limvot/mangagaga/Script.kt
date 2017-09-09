package io.githup.limvot.mangagaga;

import org.luaj.vm2.*
import org.luaj.vm2.lib.jse.*

import java.io.StringReader;

class Script(val name : String, val luaCode : String, val scriptNumber : Int) : GenericLogger {

    val globals = JsePlatform.standardGlobals()
    init {
        globals.load(StringReader(ScriptManager.luaPrequal), "luaPrequal").call()
        // Call init function which saves this APIObject
        globals.get("init").call(CoerceJavaToLua.coerce(APIObject.instance()))
        globals.load(StringReader(luaCode), name).call()
    }

    val luaGetMangaListTypes        = globals.get("getMangaListTypes")
    val luaGetMangaListPage         = globals.get("getMangaListPage")
    val luaInitManga                = globals.get("initManga")
    val luaGetMangaChapterList      = globals.get("getMangaChapterList")
    val luaGetMangaChapterNumPages  = globals.get("getMangaChapterNumPages")
    val luaGetMangaChapterPage      = globals.get("getMangaChapterPage")

    fun getMangaListTypes() : List<String> {
        val resTable = luaGetMangaListTypes.call().checktable()
        return (0 .. resTable["numTypes"].toint()).map { resTable[it].toString() }
    }

    fun getMangaList(type: String): List<Manga> {
        val table = luaGetMangaListPage.call(type).checktable()
        return (0 until table["numManga"].toint()).map {
            Manga(scriptNumber, table[it].checktable()) }
    }

    fun initManga(manga : Manga) { luaInitManga.call(manga.table) }

    fun getMangaChapterList(manga : Manga): List<Chapter> {
        val table = luaGetMangaChapterList.call(manga.table).checktable()
        return (0 until table["numChapters"].toint()).map {
            Chapter(manga, table[it].checktable(), it) }
    }

    fun getNumPages(manga : Manga, chapter : Chapter) =
        luaGetMangaChapterNumPages.call(manga.table, chapter.table).toint()

    fun downloadPage(manga : Manga, chapter : Chapter, page : Int) =
        luaGetMangaChapterPage.call(manga.table, chapter.table, LuaValue.valueOf(page)).toString()
}
