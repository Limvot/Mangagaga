package io.githup.limvot.mangagaga

import org.scaloid.common._

import org.mozilla.javascript._

object APIObject {
  implicit val tag = LoggerTag("Scala APIObject")

  def instance() = this
  def note(theNote: String) =  info(theNote)

  var rhino = Context.enter()
  rhino.setOptimizationLevel(-1)
  var scope = rhino.initStandardObjects();
  //Context.exit()

  def doDaJS(to_eval: String): String = {
    var result = rhino.evaluateString(scope, to_eval, "<cmd>", 1, null).toString()
    note("before toString")
    note(result)
    result
  }
  
  def download(filePath: String): String =  {
    info("Downloading" + filePath)
    Utilities.download(filePath)
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
}
