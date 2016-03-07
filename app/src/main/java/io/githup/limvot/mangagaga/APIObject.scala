package io.githup.limvot.mangagaga

import org.scaloid.common._

import org.mozilla.javascript._

object APIObject {
  implicit val tag = LoggerTag("Scala APIObject")

  def instance() = this
  def note(theNote: String) =  info(theNote)


  def doDaJS(to_eval: String): String = {
    note("string to eval")
    note(to_eval)
    var result = ""
    try {
      // yep, new one every time! (because this function can be called from not the main thread, and contexts are associated with threads)
      var rhino = Context.enter()
      rhino.setOptimizationLevel(-1)
      var scope = rhino.initStandardObjects();
      var temp = rhino.evaluateString(scope, to_eval, "<cmd>", 1, null)
      note("before toString")
      note(temp.toString())
      result = temp.toString()
      Context.exit()
    } catch {
      case e : Exception => {
        error("evaluateString: "+e.toString())
      }
    }
    result
  }
  
  def download(filePath: String): String =  {
    info("Downloading" + filePath)
    Utilities.download(filePath)
  }
  def downloadWithRequestHeadersAndReferrer(filePath: String, referer: String): (String, java.util.Map[String,java.util.List[String]]) =  {
    info("Downloading" + filePath)
    Utilities.downloadWithRequestHeadersAndReferrer(filePath, referer)
  }

  def readFile(absolutePath: String): String = {
    info("Reader Path is: " + absolutePath)
    try {
      return Utilities.readFile(absolutePath)
    } catch {
      case e:Exception => error("Could not open in APIObject:readFile" + e.toString)
    }
    "FAILURE"
  }
  def slice(toSlice: String, a: Int, b: Int): String = toSlice.substring(a,b)
  def sleep(time: Int) = Thread sleep time
}
