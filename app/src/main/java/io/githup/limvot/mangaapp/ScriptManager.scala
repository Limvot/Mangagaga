package io.githup.limvot.mangaapp;

import org.scaloid.common._
import collection.mutable.ArrayBuffer
import collection.mutable.Buffer

import android.content.Context
import android.os.Environment
import android.util.Log

import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.io.InputStream

object ScriptManager {
  def instance() = this
  var luaPrequal = ""
  var currentSource = 0
  val scriptList = Buffer[Script]()
  // Init because of context. Ugh
  def init(context:Context) {
    try {
      luaPrequal = Utilities.readFile(context.getResources().openRawResource(R.raw.script_prequal))
    } catch {
      case e:Exception => Log.e("Could not open lua prequal", e.toString())
    }

    val scriptDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Scripts/")
    for (name <- Array("kiss_manga", "unixmanga")) {
      try {
        val newScript = new File(scriptDir, name)
        // Right now this is commented out because for testing we want to always copy over scripts
        // on every update
        //if (!newScript.exists()) {
          newScript.createNewFile()
          val fos = new FileOutputStream(newScript)
          val rawResource = context.getResources().openRawResource( name match {
            case "kiss_manga" => R.raw.kiss_manga
            case "unixmanga" => R.raw.unixmanga
          })
          Utilities.copyStreams(rawResource, fos)
          fos.close()
        //}
      } catch {
        case e:Exception => Log.e("Script", e.toString())
      }
    }

    var index = 0;
    for (script <- scriptDir.listFiles()) {
      try {
        scriptList.append(new Script(script.getName(), Utilities.readFile(script.getAbsolutePath()), index))
        index += 1
      } catch {
        case e:Exception => Log.e("Could not open lua script", e.toString)
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
