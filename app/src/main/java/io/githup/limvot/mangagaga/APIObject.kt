package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import java.io.File;
import java.lang.Thread;

import org.mozilla.javascript.*

object APIObject : AnkoLogger {
  fun instance() = this
  fun note(theNote: String) = info(theNote)

  fun doDaJS(to_eval: String): String {
    info("js string to eval: $to_eval")
    var result = ""
    try {
      // yep, new one every time! (because this function can be called from not the main thread,
      // and contexts are associated with threads)
      var rhino = Context.enter()
      rhino.setOptimizationLevel(-1)
      var scope = rhino.initStandardObjects();
      result = rhino.evaluateString(scope, to_eval, "<cmd>", 1, null).toString()
      info("js result: $result")
      Context.exit()
    } catch (e: Exception) {
      error("evaluateString: "+e.toString())
    }
    return result
  }
  
  fun download(filePath: String): String {
    info("Downloading $filePath")
    return Utilities.download(filePath)
  }

  fun downloadWithRequestHeadersAndReferrer(filePath: String, referer: String): Pair<String, MutableMap<String,List<String>>> {
    info("Downloading $filePath")
    return Utilities.downloadWithRequestHeadersAndReferrer(filePath, referer)
  }

  fun readFile(absolutePath: String): String {
    try {
      return File(absolutePath).readText()
    } catch (e: Exception) {
      error("Could not open in APIObject:readFile $e")
    }
    return "FAILURE"
  }
  fun slice(toSlice: String, a: Int, b: Int): String = toSlice.substring(a,b)
  fun sleep(time: Int) = Thread.sleep(time.toLong())
}
