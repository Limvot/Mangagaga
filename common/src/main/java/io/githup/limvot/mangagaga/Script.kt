package io.githup.limvot.mangagaga;

import org.mozilla.javascript.*

class Script(val name : String, val code : String, val scriptNumber : Int) : GenericLogger {

    private var scriptScope: Scriptable? = null
    init {
        val cx = Context.enter()
        try {
            scriptScope = cx.initStandardObjects()
            ScriptableObject.putProperty(scriptScope, "api", APIObject.instance())
            cx.evaluateString(scriptScope, ScriptManager.codePrequel, "codePrequel", 1, null)
            cx.evaluateString(scriptScope, code, name, 1, null)
        } finally {
            Context.exit()
        }
    }
    fun callHelper(function: String, params: Array<Any>): List<String> {
        val cx = Context.enter()
        try {
            val function = (scriptScope!!.get(function, scriptScope) as org.mozilla.javascript.Function)
            return function.call(cx, scriptScope, scriptScope, params) as List<String>
        } finally {
            Context.exit();
        }
    }
    fun getMangaListTypes()       = callHelper("getMangaListTypes", arrayOf<Any>())
    fun makeRequest(req: Request) = callHelper("handleRequest",     arrayOf<Any>(req))
}
