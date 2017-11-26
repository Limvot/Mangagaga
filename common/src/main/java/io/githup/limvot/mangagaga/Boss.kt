package io.githup.limvot.mangagaga

import kotlin.concurrent.*

import java.util.concurrent.Semaphore

import java.io.BufferedWriter
import java.io.File

import com.google.gson.reflect.TypeToken

import java.io.FileWriter

/*
 * Created by marcus on 11/04/17.
 */
 object Boss : GenericLogger {
     val chapterDownloadMutex = Semaphore(1)
    val gson = Utilities.getGson()
    val chapterHistory = loadHistory()
    val favoriteManga = loadFavorites()

    var isOffline: Boolean = false
    var currentManga: String = ""
    var currentChapter: String = ""

    var currentChapterList : List<String> = listOf()

    var currentPage = 0
    var numChapPages = -1
    val chapterPageMap = mutableMapOf<Int, String>()

    var codePrequel = ""
    var currentSource = 0
    val scriptList = mutableListOf<Script>()

    fun init() {
        scriptList.clear()

        val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
        codePrequel = File(scriptDir, "script_prequel.js").readText()

        for ((index, script) in scriptDir.listFiles().filter { it.name.endsWith(".js") }.withIndex()) {
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

    fun readingOffline(isOffline: Boolean) { this.isOffline = isOffline }
    
    fun  loadHistory(): ArrayList<Request> {
        //TODO(marcus): implement
        return try {
          gson.fromJson(File(SettingsManager.mangagagaPath, "History.json").readText(),
                        object : TypeToken<ArrayList<Request>>() {}.type)
        } catch (e: Exception) {
          ArrayList<Request>()
        }
    }
    
    fun getChapterHistoryList(): List<Request> = chapterHistory
    
    fun clearHistory() {
        chapterHistory.clear()
        saveHistory()
    }
    fun saveHistory() { thread { saveHistoryAsync(chapterHistory) } }

    fun saveHistoryAsync(toDownload: ArrayList<Request>) {
        try {
          val history = File(SettingsManager.mangagagaPath, "History.json")
          history.createNewFile()
          val fw = FileWriter(history.getAbsoluteFile());
          val bw = BufferedWriter(fw)
          bw.write(gson.toJson(toDownload))
          bw.close()
        } catch (e: Exception) {
          info("SAVE_HISTORY - Problem $e")
        }
    }

    fun  loadFavorites(): ArrayList<Request> {
        return try {
          gson.fromJson(File(SettingsManager.mangagagaPath, "Favorites.json").readText(),
                        object : TypeToken<ArrayList<Request>>() {}.type)
        } catch (e: Exception) {
          info("Caught exception while trying to load favorites - $e")
          ArrayList<Request>()
        }
    }

    fun getFavoriteList(): ArrayList<Request> = favoriteManga
    //TODO(marcus): get a .equals method for request
    fun isFavorite(manga: Request): Boolean = favoriteManga.contains(manga)

    fun setFavorite(source : String, manga: String, add: Boolean) {
        val req = Request()
        req.source = source
        req.manga = manga
        setFavorite(req, add)
    }
    fun setFavorite(manga: Request, add: Boolean) {
        if (add)
            favoriteManga.add(manga)
        else
            favoriteManga.remove(manga)
        saveFavorites()
    }
    fun clearFavorites() {
        favoriteManga.clear()
        saveFavorites()
    }

    fun saveFavorites() {
        try {
          val favorites = File(SettingsManager.mangagagaPath, "Favorites.json")
          favorites.createNewFile()
          val fw = FileWriter(favorites.getAbsoluteFile())
          val bw = BufferedWriter(fw)
          bw.write(gson.toJson(favoriteManga))
          bw.close()
        } catch (e: Exception) {
          info("SAVE_FAVORITES - Problem $e")
        }
    }
    
    fun clearSaved() {
        val downloaded = File(SettingsManager.mangagagaPath, "Downloaded/")
        Utilities.clearFolder(downloaded)
    }

    fun isSaved(chapter: String, manga: String, source: String): Boolean {
        val req = Request()
        req.chapter = chapter
        req.manga = manga
        req.source = source
        return isSaved(req)
    }
    fun isSaved(req : Request): Boolean {
        val savedDir = File(SettingsManager.mangagagaPath + "/Downloaded/" +
                            req.manga + "/" + req.chapter)
        return savedDir.exists()
    }

    fun addSaved(chapter: String, manga: String, source: String) {
        val req = Request()
        req.source = source
        req.manga = manga
        req.chapter = chapter
        addSaved(req)
    }
    fun addSaved(req : Request) {
        if (!isSaved(req)) {
          thread { 
            try {
              chapterDownloadMutex.acquire()
              downloadChaptersAsync(listOf(req))
            } finally {
              chapterDownloadMutex.release()
            }
          }
        }
    }

    fun removeSaved(chapter: String, manga: String, source: String) {
        //TODO(marcus): will we only ever call this method on chapters of the current manga?
        val savedDir = File(SettingsManager.mangagagaPath + "/Downloaded/" + currentManga + "/" + chapter)
        if (savedDir.exists())
          Utilities.deleteFolder(savedDir)
    }

    fun downloadChaptersAsync(toDownload: List<Request>) {
        //TODO(marcus): implement this
        // Notification
        val notificationID = 0
        val notification = notify("Downloading chapter...")
        /*
          for (chapter in toDownload) {
            val parentManga = chapter.parentManga
            initManga(parentManga)
            val savedDir = File(SettingsManager.mangagagaPath, "Downloaded/")
            val mangaDir = File(savedDir, parentManga.getTitle())
            val chapterDir = File(mangaDir, chapter.getTitle())
            mangaDir.mkdir()
            chapterDir.mkdir()

            // Save the Manga object and the Chapter object
            try {
              val mangaJson = File(mangaDir, "manga.json")
              if (!mangaJson.exists()) {
                mangaJson.createNewFile();
                val fw = FileWriter(mangaJson.getAbsoluteFile())
                val bw = BufferedWriter(fw)
                bw.write(gson.toJson(parentManga))
                bw.close()
              }

              val chapterJson = File(chapterDir, "chapter.json")
              if (!chapterJson.exists()) {
                chapterJson.createNewFile()
                val fw = FileWriter(chapterJson.getAbsoluteFile())
                val bw = BufferedWriter(fw)
                bw.write(gson.toJson(chapter))
                bw.close()
              }
            } catch (e: Exception) {
              info("SAVE_CHAPTER_MANgA_chapter - problem/exception $e")
            }

            val numPages = getNumPages(parentManga, chapter)
            for (i in 0 until numPages) {
              notification.text = "Downloading page " + (i+1) + "/" + (numPages) + "."
              val fromFile = ScriptManager.getCurrentSource().downloadPage(parentManga, chapter, i)
              try {
                val filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."))
                File(chapterDir, filename).writeBytes(File(fromFile).readBytes())
                File(fromFile).delete()
              } catch (e: Exception) {
                error("Save Chapter ERROR $fromFile e: $e")
              }
            }
          }*/

          notification.title = "Done!"
    }
    // THESE LOOK BACKWARDS
    // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
    // Make this script decidable later
    fun previousChapter(): Boolean {
        val nextIndex = currentChapterList.indexOf(currentChapter)+1
        if (nextIndex < currentChapterList.size)
          setCurrentChapterImpl(currentChapterList[nextIndex])
        else
          return false
        return true
    }

    fun nextChapter(): Boolean {
        val nextIndex = currentChapterList.indexOf(currentChapter)-1
        if (nextIndex > 0)
          setCurrentChapterImpl(currentChapterList[nextIndex])
        else
          return false
        return true
    }
    
    fun setCurrentChapterImpl(chapter : String) {
        // delete all of our cached pages before clearing the cache if we're reading online
        if (!isOffline)
          for (pair in chapterPageMap)
            File(pair.value).delete()
        chapterPageMap.clear()
        currentChapter = chapter
        val req = Request()
        req.source = getCurrentSource().name
        req.manga = currentManga
        req.chapter = chapter
        chapterHistory.add(0, req)
        for (i in SettingsManager.getHistorySize() until chapterHistory.size)
          chapterHistory.removeAt(i)
        saveHistory()
    }
    fun getNumPages(): Int {
        if(numChapPages <= 0) {
            val req = Request()
            req.manga = Boss.currentManga
            req.chapter = Boss.currentChapter
            val script = getCurrentSource()
            val num_page_list = script.makeRequest(req)
            numChapPages = num_page_list[0].toInt()
        }
        return numChapPages
    }
    fun move(forwards: Boolean) {
        if(forwards) {
            println("Moving forward")
            println(numChapPages-1)
            println(currentPage)
            if(currentPage < getNumPages()-1) {
                currentPage++
            } else {
                println("Moving to next chapter...")
                println(nextChapter())
                currentPage = 0
                numChapPages = -1
            }
        } else {
            if(currentPage > 0) {
                currentPage--
            } else {
                previousChapter()
                numChapPages = -1
                //TODO(marcus): this call causes an exception in app because it is on main thread
                //currentPage = getNumPages() - 1
                //TODO(marcus):  for now we just jump back to the beginning of the last chapter
                currentPage = 0
                numChapPages = -1
            }
        }
    }
    fun getSavedManga(): List<Request> {
        val list = mutableListOf<Request>()
        val savedDir = File(SettingsManager.mangagagaPath, "Downloaded/")
        /*
        for (dir in savedDir.list()) {
          val mangaDir = File(savedDir, dir)
          val mangaFile = File(mangaDir, "manga.json")
          val manga = gson.fromJson(mangaFile.readText(), Manga::class.java)
          list.add(manga)
        }
        */
        return list
    }
 }
