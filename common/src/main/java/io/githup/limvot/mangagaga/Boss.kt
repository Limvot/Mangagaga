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
    private val chapterDownloadMutex = Semaphore(1)
    private val gson = Utilities.getGson()
    private val chapterHistory = loadHistory()
    private val favoriteManga = loadFavorites()

    var currentSource = ""
    var currentManga: String = ""
    var currentChapter: String = ""
    var currentPage = 0

    var codePrequel = ""
    var scripts = mapOf<String, Script>()

    fun init() {
        val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
        codePrequel = File(scriptDir, "script_prequel.js").readText()

        scripts = scriptDir.listFiles().filter { it.name.endsWith(".js") }.map {
          it.name to Script(it.name, File(it.absolutePath).readText())
        }.toMap()
    }

    fun getCurrentSource(): Script = scripts[currentSource]!!

    private fun  loadHistory(): ArrayList<Request> {
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
    private fun saveHistory() { thread { saveHistoryAsync(chapterHistory) } }

    private fun saveHistoryAsync(toDownload: ArrayList<Request>) {
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

    private fun loadFavorites(): ArrayList<Request> {
        return try {
          gson.fromJson(File(SettingsManager.mangagagaPath, "Favorites.json").readText(),
                        object : TypeToken<ArrayList<Request>>() {}.type)
        } catch (e: Exception) {
          info("Caught exception while trying to load favorites - $e")
          ArrayList<Request>()
        }
    }

    fun getFavoriteList(): ArrayList<Request> = favoriteManga
    fun isFavorite(manga: Request): Boolean = favoriteManga.contains(manga)

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

    private fun saveFavorites() {
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

    fun isSaved(req : Request): Boolean {
        val savedDir = File(SettingsManager.mangagagaPath + "/Downloaded/" +
                            req.manga + "/" + req.chapter)
        return savedDir.exists()
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

    fun removeSaved(req: Request) {
        val savedDir = File(SettingsManager.mangagagaPath + "/Downloaded/" + req.manga + "/" + req.chapter)
        if (savedDir.exists())
          Utilities.deleteFolder(savedDir)
    }

    fun downloadChaptersAsync(toDownload: List<Request>) {
        val notification = notify("Downloading chapter...")
        for (req in toDownload) {
            val savedDir = File(SettingsManager.mangagagaPath, "Downloaded/")
            val mangaDir = File(savedDir, req.manga)
            val chapterDir = File(mangaDir, req.chapter)
            mangaDir.mkdir()
            chapterDir.mkdir()
            val numPages = getNumPages(req.manga, req.chapter)
            for (i in 0 until numPages) {
              notification.text = "Downloading page " + (i+1) + "/" + (numPages) + "."
              val fromFile = getCurrentSource().makeRequest(req.copy(page = i.toString()))[0]
              try {
                val filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."))
                File(chapterDir, filename).writeBytes(File(fromFile).readBytes())
                File(fromFile).delete()
              } catch (e: Exception) {
                error("Save Chapter ERROR $fromFile e: $e")
              }
            }
          }
          notification.title = "Done!"
    }
    fun getSavedManga(): List<Request> {
        return File(SettingsManager.mangagagaPath, "Downloaded/").list().map { Request(source = "downloaded", manga = it)}
    }
    // THESE LOOK BACKWARDS
    // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
    // Make this script decidable later
    fun previousChapter() = setCurrentChapterImpl(1)
    fun nextChapter() = setCurrentChapterImpl(-1)
    
    private fun setCurrentChapterImpl(delta: Int): Boolean {
        val chapterList = getCurrentSource().makeRequest(Request(source = currentSource, manga = currentManga))
        val oldIdx = chapterList.indexOf(currentChapter)
        val newIdx = maxOf(0, minOf(chapterList.size -1, oldIdx + delta))
        if (oldIdx == newIdx)
            return false
        currentChapter = chapterList[newIdx]

        val req = Request(source = getCurrentSource().name, manga = currentManga, chapter = currentChapter)
        chapterHistory.add(0, req)
        for (i in SettingsManager.getHistorySize() until chapterHistory.size)
          chapterHistory.removeAt(i)
        saveHistory()
        return true
    }

    fun getNumPages(manga: String = Boss.currentManga, chapter: String = Boss.currentChapter): Int {
        return getCurrentSource().makeRequest(Request(manga = manga, chapter = chapter))[0].toInt()
    }

    fun move(forwards: Boolean) {
        if(forwards) {
            if(currentPage < getNumPages()-1) {
                currentPage++
            } else {
                nextChapter()
                currentPage = 0
            }
        } else {
            if(currentPage > 0) {
                currentPage--
            } else {
                previousChapter()
                currentPage = getNumPages() - 1
            }
        }
    }
 }
