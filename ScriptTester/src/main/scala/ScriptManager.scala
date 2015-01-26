import collection.mutable.ArrayBuffer
import collection.mutable.Buffer

import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.ArrayList
import java.io.InputStream

object ScriptManager {
  def instance() = this
  var luaPrequal = ""
  var currentSource = 0
  val scriptList = Buffer[Script]()
  // Init because of context. Ugh
  def init() {
    try {
      luaPrequal = Utilities.readFile("./scripts/script_prequal.lua")
    } catch {
      case e:Exception => System.out.println("Could not open lua prequal: "+e.toString())
    }

    val scriptDir = new File("./Mangagaga/Scripts/")
    for (name <- Array("kiss_manga", "unixmanga")) {
      try {
        val newScript = new File(scriptDir, name)
        // Right now this is commented out because for testing we want to always copy over scripts
        // on every update
        //if (!newScript.exists()) {
          newScript.createNewFile()
          val fos = new FileOutputStream(newScript)
          val rawResource = new FileInputStream( name match {
            case "kiss_manga" => "./scripts/kiss_manga.lua"
            case "unixmanga" => "./scripts/unixmanga.lua"
          })
          Utilities.copyStreams(rawResource, fos)
          fos.close()
        //}
      } catch {
        case e:Exception => System.out.println("Script: "+e.toString())
      }
    }

    var index = 0;
    for (script <- scriptDir.listFiles()) {
      try {
        scriptList.append(new Script(script.getName(), Utilities.readFile(script.getAbsolutePath()), index))
        index += 1
      } catch {
        case e:Exception => System.out.println("Could not open lua script: "+e.toString)
      }
    }
  }

  def getLuaPrequal = luaPrequal
  def numSources = scriptList.size
  def getScript(position:Int):Script = if (position >= 0 && position < numSources) 
                                        scriptList(position) 
                                       else null
  def setCurrentSource(num:Int):Unit = currentSource = num
  def getCurrentSource():Script = getScript(currentSource)
}
