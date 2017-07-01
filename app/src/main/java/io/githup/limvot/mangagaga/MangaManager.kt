package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import java.util.concurrent.Semaphore

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.AsyncTask
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.ArrayAdapter

import java.io.BufferedWriter
import java.io.File

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.reflect.TypeToken

import org.luaj.vm2.LuaTable

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
/*import java.util.ArrayList*/
/*import java.util.Arrays*/
/*import java.util.List*/

/**
 * Created by nathan on 8/25/14.
 */
object MangaManager : AnkoLogger {
  fun instance() = this

  val chapterDownloadMutex = Semaphore(1)
  val nextPageCachingMutex = Semaphore(1)
  val gson = Utilities.getGson()
  val chapterHistory = loadHistory()
  val favoriteManga = loadFavorites()

  var mainContext: Context? = null
  var isOffline: Boolean = false
  var currentManga: Manga? = null
      set(manga) {
        if (!isOffline)
          ScriptManager.getCurrentSource().initManga(manga!!)
        currentManga = manga
      }
  var currentChapter: Chapter? = null
      set(current) { setCurrentChapterImpl(current) }

  var currentPage:Int = 0
  // Used for cacheing pages and the downloaded chapters
  val chapterPageMap = mutableMapOf<Int, String>()

  fun setCurrentChapterImpl(current: Chapter?) {
    // delete all of our cached pages before clearing the cache if we're reading online
    if (!isOffline)
      for (pair in chapterPageMap)
        File(pair.value).delete()
    chapterPageMap.clear()
    currentChapter = current
    chapterHistory.add(0, current!!)
    for (i in SettingsManager.getHistorySize() until chapterHistory.size)
      chapterHistory.removeAt(i)
    saveHistory()
  }

  fun setContext(ctx: Context) { mainContext = ctx }
  fun readingOffline(isOffline:Boolean) { this.isOffline = isOffline }

  fun  loadHistory(): ArrayList<Chapter> {
    return try {
      gson.fromJson<ArrayList<Chapter>>(File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/History.json").readText(),
                    object : TypeToken<ArrayList<Chapter>>() {}.type)
    } catch (e: Exception) {
      ArrayList<Chapter>()
    }
  }

  fun saveHistory() { doAsync { saveHistoryAsync(chapterHistory) } }

  fun saveHistoryAsync(toDownload: ArrayList<Chapter>) {
    info("SAVE_HISTORY - BEGINNING")
    try {
      val history = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga", "History.json")
      history.createNewFile()
      val fw = FileWriter(history.getAbsoluteFile());
      val bw = BufferedWriter(fw)
      bw.write(gson.toJson(toDownload))
      bw.close()
      info("SAVE_HISTORY - SAVED")
    } catch (e: Exception) {
      info("SAVE_HISTORY - Problem")
    }
  }

  fun clearHistory() {
    chapterHistory.clear()
    saveHistory()
  }

  fun saveFavorites() {
    info("SAVE_FAVORITES - BEGINNING")
    try {
      val favorites = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga", "Favorites.json")
      favorites.createNewFile()
      val fw = FileWriter(favorites.getAbsoluteFile())
      val bw = BufferedWriter(fw)
      bw.write(gson.toJson(favoriteManga))
      bw.close()
      info("SAVE_FAVORITES - SAVED")
    } catch (e: Exception) {
      info("SAVE_FAVORITES - Problem")
    }
  }

  fun loadFavorites(): ArrayList<Manga> {
    return try {
      gson.fromJson(File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Favorites.json").readText(), object : TypeToken<ArrayList<Manga>>() {}.type)
    } catch (e: Exception) {
      info("Caught exception while trying to load favorites - $e")
      ArrayList<Manga>()
    }
  }

  fun getFavoriteList(): ArrayList<Manga> = favoriteManga
  fun isFavorite(manga: Manga): Boolean = favoriteManga.contains(manga)

  fun addFavorite(manga: Manga) {
    favoriteManga.add(manga)
    saveFavorites()
  }

  fun removeFavorite(manga: Manga) {
    favoriteManga.remove(manga)
    saveFavorites()
  }

  fun clearFavorites() {
    favoriteManga.clear()
    saveFavorites()
  }

  fun isSaved(chapter: Chapter): Boolean {
    val savedDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Downloaded/" + chapter.parentManga.getTitle() + "/" + chapter.getTitle())
    return savedDir.exists()
  }

  fun addSaved(chapter: Chapter) {
    if (!isSaved(chapter)) {
      doAsync { 
        try {
          chapterDownloadMutex.acquire()
          downloadChaptersAsync(listOf(chapter))
        } finally {
          chapterDownloadMutex.release()
        }
      }
    }
  }

  fun downloadChaptersAsync(toDownload: List<Chapter>) {
    // Notification
    val context = mainContext!!
    val notificationID = 0
    val builder =
      NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Downloading Chapter")
        .setContentText("Downloading " + toDownload[0].getTitle())
        val resultIntent = Intent(context, DownloadedActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(DownloadedActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent =
          stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
          builder.setContentIntent(resultPendingIntent)
          val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as  NotificationManager
          notificationManager.notify(notificationID, builder.build())

          for (chapter in toDownload) {
            val parentManga = chapter.parentManga
            val savedDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Downloaded/")
            val mangaDir = File(savedDir, parentManga.getTitle())
            val chapterDir = File(mangaDir, chapter.getTitle())
            mangaDir.mkdir()
            chapterDir.mkdir()

            // Save the Manga object and the Chapter object
            info("SAVE_CHAPTER - SAVING MANGA")
            try {
              val mangaJson = File(mangaDir, "manga.json")
              if (!mangaJson.exists()) {
                mangaJson.createNewFile();
                val fw = FileWriter(mangaJson.getAbsoluteFile())
                val bw = BufferedWriter(fw)
                bw.write(gson.toJson(parentManga))
                bw.close()
                info("SAVE_CHAPTER_MANgA - new SAVED")
              } else {
                info("SAVE_CHAPTER_MANgA - already existed")
              }

              val chapterJson = File(chapterDir, "chapter.json")
              if (!chapterJson.exists()) {
                chapterJson.createNewFile()
                val fw = FileWriter(chapterJson.getAbsoluteFile())
                val bw = BufferedWriter(fw)
                bw.write(gson.toJson(chapter))
                bw.close()
                info("SAVE_CHAPTER_Chapter - new SAVED")
              } else {
                info("SAVE_CHAPTER_Chapter - already existed")
              }
              info("SAVE_CHAPTER_MANgA_Chapter - SAVED success")
            } catch (e: Exception) {
              info("SAVE_CHAPTER_MANgA_chapter - problem/exception")
            }

            val numPages = getNumPages(parentManga, chapter)
            for (i in 0 until numPages) {
              builder.setContentText("Downloading page " + (i+1) + "/" + (numPages) + ".")
              notificationManager.notify(notificationID, builder.build())
              val fromFile = ScriptManager.getCurrentSource().downloadPage(parentManga, chapter, i)
              try {
                val filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."))
                //Files.move(Paths.get(fromFile), Paths.get(chapterDir.getAbsolutePath() + "/" + filename), REPLACE_EXISTING)
                val in_stream = FileInputStream(fromFile)
                val os = FileOutputStream(chapterDir.getAbsolutePath() + "/" + filename)
                Utilities.copyStreams(in_stream, os)
                File(fromFile).delete()
              } catch (e: Exception) {
                error("Save Chapter ERROR $fromFile e: $e")
              }
            }
          }

          builder.setContentTitle("Done!")
          //builder.setContentText("Downloaded " + toDownload.size() + " chapters.")
          notificationManager.notify(notificationID, builder.build())
  }

  fun removeSaved(chapter: Chapter) {
    val savedDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Downloaded/" + chapter.parentManga.getTitle() + "/" + chapter.getTitle())
    if (savedDir.exists())
      Utilities.deleteFolder(savedDir)
  }

  fun getSavedManga(): List<Manga> {
    val list = mutableListOf<Manga>()
    val savedDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Downloaded/")
    for (dir in savedDir.list()) {
      info("Get Saved Manga $dir")
      val mangaDir = File(savedDir, dir)
      val mangaFile = File(mangaDir, "manga.json")
      try {
        val manga = gson.fromJson(mangaFile.readText(), Manga::class.java)
        list.add(manga)
      } catch (e: Exception) {
        info("GetSaved - Exception: $e")
      }
    }
    return list
  }

  fun getSavedChapters(manga: Manga): List<Chapter> {
    val list = mutableListOf<Chapter>()
    val savedDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Downloaded/" + manga.getTitle())
    for (dir in savedDir.list()) {
      info("Get Saved Chapters $dir")
      val chapterDir = File(savedDir, dir)
      val chapterFile = File(chapterDir, "chapter.json")
      try {
        val chapter = gson.fromJson(chapterFile.readText(), Chapter::class.java)
        list.add(chapter)
      } catch (e: Exception) {
        info("GetSaved Chapters Chapters - Exception")
      }
    }
    return list.reversed()
  }
  fun clearSaved() {
    info("MANGA_MANAGER - Clearing Saved!")
    val downloaded = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mangagaga/Downloaded/")
    Utilities.clearFolder(downloaded)
  }


  // THESE LOOK BACKWARDS
  // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
  // Make this script decidable later
  fun previousChapter(): Boolean {
    val mangaChapterList = getMangaChapterList()
    info("PREVOUS CHAPTER ${currentChapter?.num}")
    info("PREVIOUS CHAPTER: size ${mangaChapterList.size-1}")
    val nextIndex = mangaChapterList.indexOf(currentChapter)+1
    if (nextIndex < mangaChapterList.size)
      setCurrentChapterImpl(mangaChapterList.get(nextIndex))
    else
      return false
    return true
  }

  fun nextChapter(): Boolean {
    val mangaChapterList = getMangaChapterList()
    info("NEXT CHAPTER ${currentChapter?.num}")
    val nextIndex = mangaChapterList.indexOf(currentChapter)-1
    if (nextIndex > -1)
      setCurrentChapterImpl(mangaChapterList.get(nextIndex))
    else
      return false
    return true
  }

  fun getMangaChapterList(): List<Chapter> {
    if (isOffline)
      return getSavedChapters(currentManga!!)
    else
      return ScriptManager.getCurrentSource().getMangaChapterList(currentManga!!)
  }
  fun getChapterHistoryList(): List<Chapter> = chapterHistory

  fun getNumPages(): Int = getNumPages(currentManga!!, currentChapter!!)
  fun getNumPages(manga: Manga, chapter: Chapter): Int { 
    return if (!isOffline) {
      ScriptManager.getCurrentSource().getNumPages(manga, chapter)
    } else {
      // This is a bit hacky..... If the size is zero we assume that we haven't laoded the chapter
      // and we load it
      if (chapterPageMap.size == 0)
        addToChapterPageMap(manga, chapter, 0)
      chapterPageMap.size
    }
  }
  fun setCurrentPageNum(page: Int) { currentPage = page }
  fun getCurrentPageNum(): Int = currentPage
  fun getCurrentPage(): String = getCurrentPage(currentManga!!, currentChapter!!, currentPage)
  fun getCurrentPage(manga: Manga, chapter: Chapter, page: Int): String { 
    /*val single = doAsync {*/
      nextPageCachingMutex.acquire() 
      /*if (chapterPageMap.get(page) == null) {*/
      val single = if (chapterPageMap.get(page) == null) {
        nextPageCachingMutex.release() 
        addToChapterPageMap(manga, chapter, page)
      } else {
        nextPageCachingMutex.release() 
      }
    /*}*/

    /*try { */
      /*//Await.result(single, 20 seconds)*/
      /*[>Await.result(single, 5 seconds)<]*/
      /*single.await()*/
    /*} catch (e: TimeoutException) {*/
      /*info("MangaManager - Timeout!!")*/
      /*info("MangaManager", e.getMessage())*/
    /*} catch (e: Exception) {*/
      /*info("MangaManager - Some other exception!!")*/
      /*info("MangaManager", e.getMessage())*/
    /*} */
    info("MangaManager - Finished awaiting!")
    doAsync {
      try {
        for (i in 0 until SettingsManager.getCacheSize()) {
          nextPageCachingMutex.acquire() 
          if (chapterPageMap.get(page+i) == null && page+i < getNumPages()) {
            nextPageCachingMutex.release()
            addToChapterPageMap(manga, chapter, page+i)
          } else {
            nextPageCachingMutex.release()
          }
        }
      } finally {
        nextPageCachingMutex.release()
      }
    }
    return chapterPageMap[page]!!
  }

  fun addToChapterPageMap(manga: Manga, chapter: Chapter, page: Int) { 
    if (!isOffline) {
      val returned_page = ScriptManager.getCurrentSource().downloadPage(manga, chapter, page)
      nextPageCachingMutex.acquire() 
      chapterPageMap[page] = returned_page
      nextPageCachingMutex.release()
    } else {
      // As we load all downloaded pages, we must not have loaded this chapter yet
      // Do this now
      nextPageCachingMutex.acquire() 
      chapterPageMap.clear()
      nextPageCachingMutex.release()
      val chapterDirString =Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/Mangagaga/Downloaded/" + manga.getTitle() + "/" + chapter.getTitle()
      val savedDir = File(chapterDirString)
      for ((i, dir) in savedDir.list().filter{!it.endsWith(".json")}.withIndex()) {
        nextPageCachingMutex.acquire() 
        chapterPageMap[i] = chapterDirString + "/" + dir.toString()
        nextPageCachingMutex.release()
      }
    }
  }
}

