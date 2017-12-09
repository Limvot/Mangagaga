package io.githup.limvot.mangagaga

import kotlin.concurrent.*

import java.util.concurrent.Semaphore

import java.io.BufferedWriter
import java.io.File

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken

import java.io.FileWriter

/*
 * Created by marcus on 11/04/17.
 */
 object Boss : GenericLogger {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val chapterHistory = loadHistory()
    val favoriteManga = loadFavorites()

    var currentSource  = ""
    var currentFilter  = ""
    var currentManga   = ""
    var currentChapter = ""
    var currentPage = 0

    var codePrequel = ""
    private var scripts = mapOf<String, Script>()

    private val cacheMutex = Semaphore(1)
    private val requestMutex = Semaphore(1)
    private val reqCache = mutableMapOf<Request, List<String>>()

    fun init() {
        val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
        codePrequel = File(scriptDir, "script_prequel.js").readText()

        scripts = scriptDir.listFiles().filter { it.name.endsWith(".js") }.map {
          it.name to Script(it.name, File(it.absolutePath).readText())
        }.toMap()
    }

    fun makeCachedRequest(req: Request): List<String> {
        // need to check to see if reading requires lock
        cacheMutex.acquire()
        if (req !in reqCache) {
            cacheMutex.release()
            try {
                requestMutex.acquire()
                val to_cache = scripts[req.source]!!.makeRequest(req)
                cacheMutex.acquire()
                reqCache[req] = to_cache
                cacheMutex.release()
            } finally {
                requestMutex.release()
            }
        } else {
            cacheMutex.release()
        }
        cacheMutex.acquire()
        val to_ret = reqCache[req]!!
        cacheMutex.release()
        return to_ret
    }

    fun getScriptList(): List<String> = scripts.keys.sorted()
    fun getFilterTypes(source: String = currentSource): List<String> {
        return makeCachedRequest(Request(source = source))
    }
    fun getMangaList(source: String = currentSource, filter: String = currentFilter): List<String> {
        return makeCachedRequest(Request(source = source, filter = filter))
    }
    fun getMangaDescription(source: String = currentSource, filter: String = currentFilter, manga: String = currentManga): String {
        return makeCachedRequest(Request(source = source, filter = filter, manga = manga))[0]
    }
    fun getChapterList(source: String = currentSource, filter: String = currentFilter, manga: String = currentManga): List<String> {
        return makeCachedRequest(Request(source = source, filter = filter, manga = manga)).drop(1)
    }
    fun getNumPages(source: String = currentSource, filter: String = currentFilter, manga: String = currentManga, chapter: String = currentChapter): Int {
        return makeCachedRequest(Request(source = source, filter = filter, manga = manga, chapter = chapter))[0].toInt()
    }
    fun getPagePath(source: String = currentSource, filter: String = currentFilter, manga: String = currentManga, chapter: String = currentChapter, page: Int = currentPage): String {
        val to_ret = makeCachedRequest(Request(source = source, filter = filter, manga = manga, chapter = chapter, page = page.toString()))[0]
        thread {
            var cacheChapter = chapter
            var cachePage = page
            for (i in 0..SettingsManager.getCacheSize()) {
                makeCachedRequest(Request(source = source, filter = filter, manga = manga,
                                          chapter = cacheChapter, page = cachePage.toString()))

                // increment, into the next chapter if necessary
                if (cachePage + 1 < getNumPages(chapter = cacheChapter)) {
                    cachePage++
                } else {
                    cacheChapter = chapterDelta(source, filter, manga, cacheChapter, -1)
                    cachePage = 0
                }
            }
        }

        return to_ret
    }

    fun move(forwards: Boolean) {
        // THESE LOOK BACKWARDS
        // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
        if(forwards) {
            if(currentPage < getNumPages()-1) {
                currentPage++
            } else {
                moveChapter(-1)
                currentPage = 0
            }
        } else {
            if(currentPage > 0) {
                currentPage--
            } else {
                moveChapter(1)
                currentPage = getNumPages() - 1
            }
        }
    }

    private fun moveChapter(delta: Int): Boolean {
        val newChapter = chapterDelta(currentSource, currentFilter, currentManga, currentChapter, delta)
        if (newChapter == currentChapter)
        return false
        currentChapter = newChapter

        val req = Request(source = currentSource, manga = currentManga, chapter = currentChapter)
        chapterHistory.add(0, req)
        for (i in SettingsManager.getHistorySize() until chapterHistory.size)
        chapterHistory.removeAt(i)
        saveHistory()
        return true
    }

    private fun chapterDelta(source: String, filter: String, manga: String, chapter: String, delta: Int): String {
        val chapterList = getChapterList(source = source, filter = filter, manga = manga)
        val oldIdx = chapterList.indexOf(chapter)
        // 1 min because of description
        val newIdx = maxOf(0, minOf(chapterList.size -1, oldIdx + delta))
        if (oldIdx == newIdx)
            return chapter
        return chapterList[newIdx]
    }

    fun getSavedManga(): List<Request> {
        return File(SettingsManager.mangagagaPath, "Downloaded/").list().map { Request(source = "downloaded", manga = it)}
    }

    fun clearSaved() {
        Utilities.clearFolder(File(SettingsManager.mangagagaPath, "Downloaded/"))
    }

    fun isSaved(req: Request): Boolean {
        return File(SettingsManager.mangagagaPath + "/Downloaded/" + req.manga + "/" + req.chapter).exists()
    }

    fun removeSaved(req: Request) {
        val savedDir = File(SettingsManager.mangagagaPath + "/Downloaded/" + req.manga + "/" + req.chapter)
        if (savedDir.exists())
            Utilities.deleteFolder(savedDir)
    }

    fun addSaved(req: Request) {
        if (!isSaved(req)) {
            thread {
                downloadChapters(listOf(req))
            }
        }
    }

    fun downloadChapters(toDownload: List<Request>) {
        val notification = notify("Downloading chapter...")
        for (req in toDownload) {
            val savedDir = File(SettingsManager.mangagagaPath, "Downloaded/")
            val mangaDir = File(savedDir, req.manga)
            val chapterDir = File(mangaDir, req.chapter)
            mangaDir.mkdir()
            chapterDir.mkdir()
            val numPages = getNumPages(req.source, req.filter, req.manga, req.chapter)
            for (i in 0 until numPages) {
                notification.text = "Downloading page " + (i+1) + "/" + (numPages) + "."
                val fromFile = makeCachedRequest(req.copy(page = i.toString()))[0]
                try {
                    val filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."))
                    File(chapterDir, filename).writeBytes(File(fromFile).readBytes())
                } catch (e: Exception) {
                    error("Save Chapter ERROR $fromFile e: $e")
                }
            }
        }
        notification.title = "Done!"
    }

    fun clearHistory() {
        chapterHistory.clear()
        saveHistory()
    }

    private fun loadHistory(): ArrayList<Request> {
        return try {
          gson.fromJson(File(SettingsManager.mangagagaPath, "History.json").readText(),
                        object : TypeToken<ArrayList<Request>>() {}.type)
        } catch (e: Exception) {
          ArrayList<Request>()
        }
    }

    private fun saveHistory() {
        thread {
            try {
                val history = File(SettingsManager.mangagagaPath, "History.json")
                history.createNewFile()
                val fw = FileWriter(history.getAbsoluteFile());
                val bw = BufferedWriter(fw)
                bw.write(gson.toJson(chapterHistory))
                bw.close()
            } catch (e: Exception) {
                info("SAVE_HISTORY - Problem $e")
            }
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
}
