package io.githup.limvot.mangagaga

import java.io.File;
import java.lang.Thread;

object APIObject : GenericLogger {
  fun instance() = this
  fun note(theNote: String) = info(theNote)
  // This lambda is reassigned by ImageViewerActivity to be a title bar change
  var onStatus = { text:String -> note("status: $text") }
  fun status(text: String) = onStatus(text)
  
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
  fun sleep(time: Int) = Thread.sleep(time.toLong())
}
