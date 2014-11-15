package io.githup.limvot.mangaapp

import org.scaloid.common._

object APIObject {
  implicit val tag = LoggerTag("Scala APIObject")

  def instance() = this
  def note(theNote: String) =  info(theNote)
  
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
