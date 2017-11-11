package io.githup.limvot.mangagaga

import kotlin.concurrent.*

import java.util.concurrent.Semaphore

import java.io.BufferedWriter
import java.io.File

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
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
    
    fun readingOffline(isOffline: Boolean) { this.isOffline = isOffline }
    
    fun  loadHistory(): ArrayList<String> {
        //TODO(marcus): implement
        return ArrayList<String>()
    }
    
    fun getChapterHistoryList(): List<String> = chapterHistory
    
    fun clearHistory() {
        chapterHistory.clear()
        saveHistory()
    }
    fun saveHistory() { thread { saveHistoryAsync(chapterHistory) } }

    fun saveHistoryAsync(toDownload: ArrayList<String>) {
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

    fun  loadFavorites(): ArrayList<String> {
        //TODO(marcus): implement
        return ArrayList<String>()
    }

    fun getFavoriteList(): ArrayList<String> = favoriteManga
    fun isFavorite(manga: String): Boolean = favoriteManga.contains(manga)

    fun setFavorite(manga: String, add: Boolean) {
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

    fun isSaved(chapter: String): Boolean {
        //TODO(marcus): will we only ever call this method on chapters of the current manga?
        val savedDir = File(SettingsManager.mangagagaPath + "/Downloaded/" +
                            currentManga + "/" + chapter)
        return savedDir.exists()
    }

    fun addSaved(chapter: String) {
        if (!isSaved(chapter)) {
          thread { 
            try {
              chapterDownloadMutex.acquire()
              downloadChaptersAsync(listOf(chapter))
            } finally {
              chapterDownloadMutex.release()
            }
          }
        }
    }

    fun removeSaved(chapter: String) {
        //TODO(marcus): will we only ever call this method on chapters of the current manga?
        val savedDir = File(SettingsManager.mangagagaPath + "/Downloaded/" + currentManga + "/" + chapter)
        if (savedDir.exists())
          Utilities.deleteFolder(savedDir)
    }

    fun downloadChaptersAsync(toDownload: List<String>) {
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
          setCurrentChapterImpl(currentChapterList.get(nextIndex))
        else
          return false
        return true
    }

    fun nextChapter(): Boolean {
        val nextIndex = currentChapterList.indexOf(currentChapter)-1
        if (nextIndex > 0)
          setCurrentChapterImpl(currentChapterList.get(nextIndex))
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
        chapterHistory.add(0, chapter)
        for (i in SettingsManager.getHistorySize() until chapterHistory.size)
          chapterHistory.removeAt(i)
        saveHistory()
    }
    fun getNumPages(): Int {
        if(numChapPages <= 0) {
            var req = Request()
            req.manga = Boss.currentManga
            req.chapter = Boss.currentChapter
            val script = ScriptManager.getCurrentSource()
            var num_page_list = script.makeRequest(req)
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
 }
