package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

import android.content.Context
import android.os.Environment

import java.io.File
import java.io.FileOutputStream

object ScriptManager : AnkoLogger {
  var luaPrequal = ""
  var currentSource = 0
  val scriptList = mutableListOf<Script>()

  // Init because of context. Ugh
  fun init(context: Context) {
    scriptList.clear()
    luaPrequal = context.getResources().openRawResource(R.raw.script_prequal)
                                       .bufferedReader().use { it.readText() }

    val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
    for (name in listOf("kiss_manga", "unixmanga", "read_panda", "manga_stream")) {
        val newScript = File(scriptDir, name)
        // For testing we want to always copy over scripts
        // on every update
        val rawResource = context.getResources().openRawResource( when(name) {
          "kiss_manga"   -> R.raw.kiss_manga
          "unixmanga"    -> R.raw.unixmanga
          "read_panda"   -> R.raw.read_panda
          "manga_stream" -> R.raw.manga_stream
          else           -> 0
        })
        newScript.writeBytes(rawResource.readBytes())
    }

    for ((index, script) in scriptDir.listFiles().withIndex()) {
      scriptList.add(Script(script.getName(), File(script.getAbsolutePath()).readText(), index))
    }
  }

  fun numSources() = scriptList.size
  fun getScript(position:Int): Script? = if (position >= 0 && position < numSources())
                                            scriptList[position]
                                         else null
  fun getCurrentSource(): Script = getScript(currentSource)!!
}
