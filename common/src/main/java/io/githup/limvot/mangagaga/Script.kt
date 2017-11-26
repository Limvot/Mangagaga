package io.githup.limvot.mangagaga;

import org.mozilla.javascript.*

class Script(val name : String, val code : String, val scriptNumber : Int) : GenericLogger {

    private var scriptScope: Scriptable? = null
    init {
        val cx = Context.enter()
        cx.setOptimizationLevel(-1)
        try {
            scriptScope = cx.initStandardObjects()
            ScriptableObject.putProperty(scriptScope, "api", APIObject.instance())
            cx.evaluateString(scriptScope, Boss.codePrequel, "codePrequel", 1, null)
            cx.evaluateString(scriptScope, code, name, 1, null)
        } finally {
            Context.exit()
        }
    }
    private fun callHelper(function: String, params: Array<Any>): List<String> {
        val cx = Context.enter()
        cx.setOptimizationLevel(-1)
        try {
            val function = (scriptScope!!.get(function, scriptScope) as org.mozilla.javascript.Function)
            // Weirdly, it seems that NativeArray, while it implements List, throws OperationNotSupported
            // on subList. Seems safer to copy it into a new list
            return ArrayList(function.call(cx, scriptScope, scriptScope, params) as List<String>)
        } finally {
            Context.exit();
        }
    }
    fun getMangaListTypes()       = callHelper("getMangaListTypes", arrayOf<Any>())
    fun makeRequest(req: Request) = callHelper("handleRequest",     arrayOf<Any>(req))
}
