package io.githup.limvot.mangagaga;

import java.io.File
import java.io.FileOutputStream

object ScriptManager : GenericLogger {
  var luaPrequal = ""
  var currentSource = 0
  val scriptList = mutableListOf<Script>()

  fun init() {
    scriptList.clear()

    val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
    luaPrequal = File(scriptDir, "script_prequal").readText()

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
