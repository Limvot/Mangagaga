package io.githup.limvot.mangagaga;

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
    val luaMakeRequest              = globals.get("handleRequest")

    fun getMangaListTypes() : List<String> {
        val resTable = luaGetMangaListTypes.call().checktable()
        return (0 .. resTable["numTypes"].toint()).map { resTable[it].toString() }
    }

    fun makeRequest(request : Request) : List<String> {
        println("Requesting... " + request.toString())
        val req = CoerceJavaToLua.coerce(request);
        val ret = luaMakeRequest.call(req).checktable()
        val mylist = (0 until ret.length()).map { ret[it].tojstring() }
        println("foo make request")
        println(ret.length())
        return mylist
    }
}
