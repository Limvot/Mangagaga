package io.githup.limvot.mangaapp
import org.scaloid.common._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.collection.JavaConversions._

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
import java.nio.file.StandardCopyOption._;
import java.nio.file._;
import java.util.ArrayList
import java.util.Arrays
import java.util.List

/**
 * Created by nathan on 8/25/14.
 */
object MangaManager {
  implicit val exec = ExecutionContext.fromExecutor(Utilities.executor)
  def instance() = this

  val chapterDownloadMutex = new Semaphore(1)
  val nextPageCachingMutex = new Semaphore(1)
  val gson = Utilities.getGson()
  val chapterHistory = loadHistory()
  val favoriteManga = loadFavorites()

  var mainContext: Context = null
  var isOffline: Boolean = false
  var currentManga: Manga = null
  var currentChapter: Chapter = null
  var currentPage:Int = 0
  // Used for cacheing pages and the downloaded chapters
  val chapterPageMap = collection.mutable.Map[Int, String]()

  def setContext(ctx: Context) { mainContext = ctx }
  def readingOffline(isOffline:Boolean) { this.isOffline = isOffline }

  def  loadHistory(): ArrayList[Chapter] = {
    try {
      gson.fromJson(Utilities.readFile(Environment.getExternalStorageDirectory() + "/Mangagaga/History.json"),
                    new TypeToken[ArrayList[Chapter]]() {}.getType())
    } catch  {
      case e: Exception => new ArrayList[Chapter]
    }
  }

  def saveHistory() { Future { saveHistoryAsync(chapterHistory) } }

  def saveHistoryAsync(toDownload: List[Chapter]) {
    Log.i("SAVE_HISTORY", "BEGINNING")
    try {
      val history = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "History.json")
      history.createNewFile()
      val fw = new FileWriter(history.getAbsoluteFile());
      val bw = new BufferedWriter(fw)
      bw.write(gson.toJson(toDownload))
      bw.close()
      Log.i("SAVE_HISTORY", "SAVED")
    } catch {
      case e: Exception => Log.i("SAVE_HISTORY", "Problem")
    }
  }

  def clearHistory() {
    chapterHistory.clear()
    saveHistory()
  }

  def saveFavorites() {
    Log.i("SAVE_FAVORITES", "BEGINNING")
    try {
      val favorites = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "Favorites.json")
      favorites.createNewFile()
      val fw = new FileWriter(favorites.getAbsoluteFile())
      val bw = new BufferedWriter(fw)
      bw.write(gson.toJson(favoriteManga))
      bw.close()
      Log.i("SAVE_FAVORITES", "SAVED")
    } catch {
      case e: Exception => Log.i("SAVE_FAVORITES", "Problem")
    }
  }

  def  loadFavorites(): ArrayList[Manga] = {
    try {
      gson.fromJson(Utilities.readFile(Environment.getExternalStorageDirectory() + "/Mangagaga/Favorites.json"), new TypeToken[ArrayList[Manga]]() {}.getType())
    } catch {
      case e: Exception => new ArrayList[Manga]
    }
  }

  def getFavoriteList(): ArrayList[Manga] = favoriteManga
  def isFavorite(manga: Manga): Boolean = favoriteManga.contains(manga)

  def addFavorite(manga: Manga) {
    favoriteManga.add(manga)
    saveFavorites()
  }

  def removeFavorite(manga: Manga) {
    favoriteManga.remove(manga)
    saveFavorites()
  }

  def clearFavorites() {
    favoriteManga.clear()
    saveFavorites()
  }

  def isSaved(chapter: Chapter): Boolean = {
    val savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/" + chapter.getParentManga().getTitle + "/" + chapter.getTitle)
    savedDir.exists()
  }

  def addSaved(chapter: Chapter) {
    if (!isSaved(chapter)) {
      Future { 
        try {
          chapterDownloadMutex.acquire()
          downloadChaptersAsync(Vector(chapter))
        } finally {
          chapterDownloadMutex.release()
        }
      }
    }
  }

  def downloadChaptersAsync(toDownload: List[Chapter]) {
    // Notification
    val context = mainContext
    val notificationID = 0
    val builder =
      new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Downloading Chapter")
        .setContentText("Downloading " + toDownload(0).getTitle)
        val resultIntent = new Intent(context, classOf[DownloadedActivity])
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(classOf[DownloadedActivity])
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent =
          stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
          builder.setContentIntent(resultPendingIntent)
          val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
          notificationManager.notify(notificationID, builder.build())

          for (chapter <- toDownload) {
            val parentManga = chapter.getParentManga
            val savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/")
            val mangaDir = new File(savedDir, parentManga.getTitle())
            val chapterDir = new File(mangaDir, chapter.getTitle)
            mangaDir.mkdir()
            chapterDir.mkdir()

            // Save the Manga object and the Chapter object
            Log.i("SAVE_CHAPTER", "SAVING MANGA")
            try {
              val mangaJson = new File(mangaDir, "manga.json")
              if (!mangaJson.exists()) {
                mangaJson.createNewFile();
                val fw = new FileWriter(mangaJson.getAbsoluteFile())
                val bw = new BufferedWriter(fw)
                bw.write(gson.toJson(parentManga))
                bw.close()
                Log.i("SAVE_CHAPTER_MANgA", "new SAVED")
              } else {
                Log.i("SAVE_CHAPTER_MANgA", "already existed")
              }

              val chapterJson = new File(chapterDir, "chapter.json")
              if (!chapterJson.exists()) {
                chapterJson.createNewFile()
                val fw = new FileWriter(chapterJson.getAbsoluteFile())
                val bw = new BufferedWriter(fw)
                bw.write(gson.toJson(chapter))
                bw.close()
                Log.i("SAVE_CHAPTER_Chapter", "new SAVED")
              } else {
                Log.i("SAVE_CHAPTER_Chapter", "already existed")
              }
              Log.i("SAVE_CHAPTER_MANgA_Chapter", "SAVED success")
            } catch {
              case e: Exception => Log.i("SAVE_CHAPTER_MANgA_chapter", "problem/exception")
            }

            for (i <- 0 until getNumPages(parentManga, chapter)) {
              builder.setContentText("Downloading page " + (i+1) + ".")
              notificationManager.notify(notificationID, builder.build())
              val fromFile = ScriptManager.getCurrentSource().downloadPage(parentManga, chapter, i)
              try {
                val filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."))
                Files.move(Paths.get(fromFile), Paths.get(chapterDir.getAbsolutePath() + "/" + filename), REPLACE_EXISTING)
                //val is = new FileInputStream(fromFile)
                //val os = new FileOutputStream(chapterDir.getAbsolutePath() + "/" + filename)
                //Utilities.copyStreams(is, os)
                //fromFile.delete()
              } catch {
                case e: Exception => Log.e("Save Chapter ERROR", fromFile + " e: " + e.toString)
              }
            }
          }

          builder.setContentTitle("Done!")
          builder.setContentText("Downloaded " + toDownload.size() + " chapters.")
          notificationManager.notify(notificationID, builder.build())
  }

  def removeSaved(chapter: Chapter) {
    val savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/" + chapter.getParentManga().getTitle + "/" + chapter.getTitle)
    if (savedDir.exists())
      Utilities.deleteFolder(savedDir)
  }

  def getSavedManga(): List[Manga] = {
    val list = new ArrayList[Manga]
    val savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/")
    for (dir <- savedDir.list()) {
      Log.i("Get Saved Manga", dir)
      val mangaDir = new File(savedDir, dir)
      val mangaFile = new File(mangaDir, "manga.json")
      try {
        val manga = gson.fromJson(Utilities.readFile(mangaFile.getAbsolutePath()), classOf[Manga])
        list.add(manga)
      } catch {
        case e: Exception => Log.i("GetSaved", "Exception")
      }
    }
    list
  }

  def getSavedChapters(manga: Manga): List[Chapter] = {
    val list = new ArrayList[Chapter]
    val savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/" + manga.getTitle)
    for (dir <- savedDir.list()) {
      Log.i("Get Saved Chapters", dir)
      val chapterDir = new File(savedDir, dir)
      val chapterFile = new File(chapterDir, "chapter.json")
      try {
        val chapter = gson.fromJson(Utilities.readFile(chapterFile.getAbsolutePath()), classOf[Chapter])
        list.add(chapter)
      } catch {
        case e: Exception => Log.i("GetSaved Chapters Chapters", "Exception")
      }
    }
    list.reverse
  }
  def clearSaved() {
    Log.i("MANGA_MANAGER", "Clearing Saved!")
    val downloaded = new File(Environment.getExternalStorageDirectory()+"/Mangagaga/Downloaded/")
    Utilities.clearFolder(downloaded)
  }


  def setCurrentManga(manga: Manga) {
    if (!isOffline)
      ScriptManager.getCurrentSource().initManga(manga)
    currentManga = manga
  }

  def getCurrentManga() = currentManga

  def setCurrentChapter(current: Chapter) {
    // delete all of our cached pages before clearing the cache
    for (pair <- chapterPageMap)
      new File(pair._2).delete()
    chapterPageMap.clear()
    currentChapter = current
    chapterHistory.add(0, current)
    for (i <- SettingsManager.getHistorySize() until chapterHistory.size)
      chapterHistory.remove(i)
      saveHistory()
  }
  // THESE LOOK BACKWARDS
  // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
  // Make this script decidable later
  def previousChapter(): Boolean =  {
    val mangaChapterList = getMangaChapterList()
    Log.i("PREVOUS CHAPTER", Integer.toString(currentChapter.getNum()))
    Log.i("PREVIOUS CHAPTER: size", Integer.toString(mangaChapterList.size()-1))
    val nextIndex = mangaChapterList.indexOf(currentChapter)+1
    if (nextIndex < mangaChapterList.size())
      setCurrentChapter(mangaChapterList.get(nextIndex))
    else
      return false
    true
  }

  def nextChapter(): Boolean = {
    val mangaChapterList = getMangaChapterList()
    Log.i("NEXT CHAPTER", Integer.toString(currentChapter.getNum()))
    val nextIndex = mangaChapterList.indexOf(currentChapter)-1
    if (nextIndex > -1)
      setCurrentChapter(mangaChapterList.get(nextIndex))
    else
      return false
    true
  }

  def getMangaChapterList(): List[Chapter] = {
    if (isOffline)
      getSavedChapters(currentManga)
    else
      ScriptManager.getCurrentSource().getMangaChapterList(currentManga)
  }
  def getChapterHistoryList(): List[Chapter] = chapterHistory

  def getNumPages(): Integer = getNumPages(currentManga, currentChapter)
  def getNumPages(manga: Manga, chapter: Chapter): Integer = { 
    if (!isOffline) {
      ScriptManager.getCurrentSource().getNumPages(manga, chapter)
    } else {
      // This is a bit hacky..... If the size is zero we assume that we haven't laoded the chapter
      // and we load it
      if (chapterPageMap.size == 0)
        addToChapterPageMap(manga, chapter, 0)
      chapterPageMap.size
    }
  }
  def setCurrentPageNum(page: Int) { currentPage = page }
  def getCurrentPageNum(): Int = currentPage
  def getCurrentPage(): String = getCurrentPage(currentManga, currentChapter, currentPage)
  def getCurrentPage(manga: Manga, chapter: Chapter, page: Int): String = { 
    nextPageCachingMutex.acquire() 
    if (chapterPageMap.get(page) == None)
      addToChapterPageMap(manga, chapter, page)
    Future {
      try {
      for (i <- 0 until SettingsManager.getCacheSize)
        if (chapterPageMap.get(page+i) == None && page+i < getNumPages())
          addToChapterPageMap(manga, chapter, page+i)
      } finally {
        nextPageCachingMutex.release()
      }
    }
    chapterPageMap(page)
  }

  def addToChapterPageMap(manga: Manga, chapter: Chapter, page: Int) { 
    if (!isOffline) {
      chapterPageMap(page) = ScriptManager.getCurrentSource().downloadPage(manga, chapter, page)
    } else {
      // As we load all downloaded pages, we must not have loaded this chapter yet
      // Do this now
      chapterPageMap.clear()
      val chapterDirString = Environment.getExternalStorageDirectory() +
                             "/Mangagaga/Downloaded/" + manga.getTitle + "/" + chapter.getTitle
      val savedDir = new File(chapterDirString)
      for ((dir, i) <- savedDir.list.filter(!_.endsWith(".json")).view.zipWithIndex) {
        chapterPageMap(i) = chapterDirString + "/" + dir.toString
      }
    }
  }
}

