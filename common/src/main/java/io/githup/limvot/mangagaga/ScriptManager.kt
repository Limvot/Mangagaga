package io.githup.limvot.mangagaga;

import java.io.File

object ScriptManager : GenericLogger {
  var luaPrequal = ""
  var currentSource = 0
  val scriptList = mutableListOf<Script>()

  fun init() {
    scriptList.clear()

    val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
    luaPrequal = File(scriptDir, "script_prequal.lua").readText()

    for ((index, script) in scriptDir.listFiles().withIndex()) {
      scriptList.add(Script(script.getName(), File(script.getAbsolutePath()).readText(), index))
    }
  }

  fun numSources() = scriptList.size
  fun getScript(position:Int): Script? = if (position >= 0 && position < numSources())
                                            scriptList[position]
                                         else null
  fun getCurrentSource(): Script = getScript(currentSource)!!

  fun setCurrentSource(src : String) : Boolean {
      //TODO(marcus): should we error if we can't find the script?
      var i = currentSource
      var ret = false
      for ((index, s) in scriptList.withIndex()) {
          if (s.name == src) {
              ret = true
              i = index
              break
          }
      }
      currentSource = i
      return ret
  }
}
