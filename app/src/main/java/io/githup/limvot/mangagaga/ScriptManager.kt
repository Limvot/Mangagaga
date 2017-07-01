package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

import android.content.Context
import android.os.Environment
import android.util.Log

import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.io.InputStream

object ScriptManager : AnkoLogger {
  fun instance() = this
  var luaPrequal = ""
  var currentSource = 0
  val scriptList = mutableListOf<Script>()
  // Init because of context. Ugh
  fun init(context:Context) {
    try {
      luaPrequal = context.getResources().openRawResource(R.raw.script_prequal).bufferedReader().use { it.readText() }
    } catch (e : Exception) {
      error("Could not open lua prequal: $e")
    }

    val scriptDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Scripts/")
    for (name in listOf("kiss_manga", "unixmanga", "read_panda", "manga_stream")) {
      try {
        val newScript = File(scriptDir, name)
        // Right now this is commented out because for testing we want to always copy over scripts
        // on every update
        //if (!newScript.exists()) {
          newScript.createNewFile()
          val fos = FileOutputStream(newScript)
          val rawResource = context.getResources().openRawResource( when(name) {
            "kiss_manga" -> R.raw.kiss_manga
            "unixmanga" -> R.raw.unixmanga
            "read_panda" -> R.raw.read_panda
            "manga_stream" -> R.raw.manga_stream
            else -> 0
          })
          Utilities.copyStreams(rawResource, fos)
          fos.close()
        //}
      } catch (e:Exception) { error("Script: $e") }
    }

    var index = 0;
    for (script in scriptDir.listFiles()) {
      try {
        scriptList.add(Script(script.getName(), File(script.getAbsolutePath()).readText(), index))
        index += 1
      } catch (e: Exception) {
        error("Could not open lua script $e")
      }
    }
  }

  fun numSources() = scriptList.size
  fun getScript(position:Int):Script? = if (position >= 0 && position < numSources()) 
                                        scriptList[position] 
                                       else null
  fun getCurrentSource():Script = getScript(currentSource)!!
}
