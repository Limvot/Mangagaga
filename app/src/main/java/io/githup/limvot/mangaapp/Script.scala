package io.githup.limvot.mangaapp;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.StringReader;

import collection.mutable.ListBuffer
import collection.mutable.Buffer
import org.scaloid.common._
import scala.collection.JavaConversions._
import java.util.List


class Script(name : String, luaCode : String, scriptNumber : Int) {
    implicit val tag = LoggerTag("Scala Script")

    val globals = JsePlatform.standardGlobals()
    globals.load(new StringReader(ScriptManager.getLuaPrequal), "luaPrequal").call()
    // Call init function which normally saves this APIObject
    info(APIObject.instance().toString())
    globals.get("init").call(CoerceJavaToLua.coerce(APIObject.instance()))

    globals.load(new StringReader(luaCode), name).call()
    var luaGetMangaListTypes = globals.get("getMangaListTypes")
    var luaSetMangaListType = globals.get("setMangaListType")
    var luaGetMangaListPage1 = globals.get("getMangaListPage1")
    var luaGetMangaListPreviousPage = globals.get("getMangaListPreviousPage")
    var luaGetMangaListNextPage = globals.get("getMangaListNextPage")
    var luaInitManga = globals.get("initManga")
    var luaGetMangaChapterList = globals.get("getMangaChapterList")
    var luaGetMangaChapterPage = globals.get("getMangaChapterPage")
    var luaGetMangaChapterNumPages = globals.get("getMangaChapterNumPages")


    def getName() : String = name


    def getMangaListTypes() : java.util.List[String] = {
        var result = luaGetMangaListTypes.call()
        var resTable = result.checktable()

        var typeList = Buffer[String]();

        for(i <- 0 until (resTable.get("numTypes").toint() - 1))
            typeList.append(resTable.get(i).toString())

        typeList
    }


    def setMangaListType(mangatype : String) {
        luaSetMangaListType.call(mangatype)
    }


    def getMangaListPage1() : java.util.List[Manga] = getMangaList(luaGetMangaListPage1)
    def getMangaListPreviousPage() : java.util.List[Manga] = getMangaList(luaGetMangaListPreviousPage)
    def getMangaListNextPage() : java.util.List[Manga] = getMangaList(luaGetMangaListNextPage)


    def getMangaList(luaGetMangaListFunc : LuaValue) : java.util.List[Manga] = {
        var result = luaGetMangaListFunc.call()
        var resTable = result.checktable()

        var mangaList = Buffer[Manga]()

        for(i <- 0 until resTable.get("numManga").toint())
            mangaList.append(new Manga(scriptNumber, resTable.get(i).checktable()))

        mangaList
    }

    def initManga(manga : Manga) {
        luaInitManga.call(manga.getTable())
    }

    def getMangaChapterList(manga : Manga) : java.util.List[Chapter] = {
        var result = luaGetMangaChapterList.call(manga.getTable());
        var resTable = result.checktable();

        var mangaChapterList = Buffer[Chapter]()

        for(i <- 0 until resTable.get("numChapters").toint())
            mangaChapterList.append(new Chapter(manga, resTable.get(i).checktable(), i))

        mangaChapterList
    }

    def getNumPages(manga : Manga, chapter : Chapter) : Int = {
        luaGetMangaChapterNumPages.call(manga.getTable(), chapter.getTable()).toint()
    }

    def downloadPage(manga : Manga, chapter : Chapter, page : Int) : String = {
        luaGetMangaChapterPage.call(manga.getTable(), chapter.getTable(), LuaValue.valueOf(page)).toString()
    }
}
