package io.githup.limvot.mangaapp
import org.scaloid.common._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.collection.JavaConversions._

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
import java.util.ArrayList
import java.util.Arrays
import java.util.List

/**
 * Created by nathan on 8/25/14.
 */
object MangaManager {
  implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
  def instance() = this

  val gson = Utilities.getGson()
  val chapterHistory = loadHistory()
  val favoriteManga = loadFavorites()

  var mainContext: Context = null
  var isOffline: Boolean = false
  var currentManga: Manga = null
  var currentChapter: Chapter = null
  var currentPage:Int = 0

  def setContext(ctx: Context) { mainContext = ctx }
  def readingOffline(isOffline:Boolean) { this.isOffline = isOffline }

  def  loadHistory(): ArrayList[Chapter] = {
    try {
      gson.fromJson(Utilities.readFile(Environment.getExternalStorageDirectory() + "/Mangagaga/History.json"), new TypeToken[ArrayList[Chapter]]() {}.getType())
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
    false
  }
  def addSaved(chapter: Chapter) {
    if (!isSaved(chapter))
      Future { downloadChaptersAsync(Vector(chapter)) }
  }
  def downloadChaptersAsync(toDownload: List[Chapter]) {
    // Notification
    val context = mainContext
    val notificationID = 0
    val builder =
      new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Downloading Chapter")
        .setContentText("Downloading " + toDownload.size() + " chapters...")
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
            val chapterDir = new File(mangaDir, Integer.toString(chapter.getNum()))
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
              val fromFile = getCurrentPage(parentManga, chapter, i)
              try {
                val is = new FileInputStream(fromFile)
                val filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."))
                val os = new FileOutputStream(chapterDir.getAbsolutePath() + "/" + filename)
                Utilities.copyStreams(is, os)
              } catch {
                case e: Exception => Log.e("Save Chapter ERROR", fromFile)
              }
            }
          }

          builder.setContentTitle("Done!")
          builder.setContentText("Downloaded " + toDownload.size() + " chapters.")
          notificationManager.notify(notificationID, builder.build())
  }

  def removeSaved(chapter: Chapter) {
    //
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
  def clearSaved() {
    Log.i("MANGA_MANAGER", "Clearing Saved!")
    val downloaded = new File(Environment.getExternalStorageDirectory()+"/Mangagaga/Downloaded/")
    Utilities.clearFolder(downloaded)
  }


  def setCurrentManga(manga: Manga) {
    ScriptManager.getCurrentSource().initManga(manga)
    currentManga = manga
  }

  def getCurrentManga() = currentManga

  def setCurrentChapter(current: Chapter) {
    currentChapter = current
    chapterHistory.add(0, current)
    for (i <- SettingsManager.getHistorySize() until chapterHistory.size)
      chapterHistory.remove(i)
      saveHistory()
  }

  def getMangaChapterList(): List[Chapter] = ScriptManager.getCurrentSource().getMangaChapterList(currentManga)
  def getChapterHistoryList(): List[Chapter] = chapterHistory

  // THESE LOOK BACKWARDS
  // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
  // Make this script decidable later
  def previousChapter(): Boolean =  {
    val mangaChapterList = getMangaChapterList()
    Log.i("PREVOUS CHAPTER", Integer.toString(currentChapter.getNum()))
    Log.i("PREVIOUS CHAPTER: size", Integer.toString(mangaChapterList.size()-1))

    if (currentChapter.getNum() < mangaChapterList.size()-1)
      setCurrentChapter(mangaChapterList.get(currentChapter.getNum()+1))
    else
      return false
    true
  }

  def nextChapter(): Boolean = {
    Log.i("NEXT CHAPTER", Integer.toString(currentChapter.getNum()))
    if (currentChapter.getNum() > 0)
      setCurrentChapter(getMangaChapterList().get(currentChapter.getNum()-1))
    else
      return false
    true
  }

  def getNumPages(manga: Manga, chapter: Chapter): Integer = ScriptManager.getCurrentSource().getNumPages(manga, chapter)
  def getNumPages(): Integer = getNumPages(currentManga, currentChapter)
  def setCurrentPageNum(page: Int) { currentPage = page }
  def getCurrentPageNum(): Int = currentPage
  def getCurrentPage(): String = getCurrentPage(currentManga, currentChapter, currentPage)

  def getCurrentPage(manga: Manga, chapter: Chapter, page: Int): String = 
    ScriptManager.getCurrentSource().downloadPage(manga, chapter, page)
}

