import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

import java.util.concurrent.Semaphore

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
  def instance() = this

  val chapterDownloadMutex = new Semaphore(1)
  val nextPageCachingMutex = new Semaphore(1)
  val gson = Utilities.getGson()

  var isOffline: Boolean = false
  var currentManga: Manga = null
  var currentChapter: Chapter = null
  var currentPage:Int = 0
  // Used for cacheing pages and the downloaded chapters
  val chapterPageMap = collection.mutable.Map[Int, String]()

  def readingOffline(isOffline:Boolean) { this.isOffline = isOffline }


  def isSaved(chapter: Chapter): Boolean = {
    val savedDir = new File("./Mangagaga/Downloaded/" + chapter.getParentManga().getTitle + "/" + chapter.getTitle)
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

          for (chapter <- toDownload) {
            val parentManga = chapter.getParentManga
            val savedDir = new File("./Mangagaga/Downloaded/")
            val mangaDir = new File(savedDir, parentManga.getTitle())
            val chapterDir = new File(mangaDir, chapter.getTitle)
            mangaDir.mkdir()
            chapterDir.mkdir()

            // Save the Manga object and the Chapter object
            System.out.println("SAVE_CHAPTER: SAVING MANGA")
            try {
              val mangaJson = new File(mangaDir, "manga.json")
              if (!mangaJson.exists()) {
                mangaJson.createNewFile();
                val fw = new FileWriter(mangaJson.getAbsoluteFile())
                val bw = new BufferedWriter(fw)
                bw.write(gson.toJson(parentManga))
                bw.close()
                System.out.println("SAVE_CHAPTER_MANgA: new SAVED")
              } else {
                System.out.println("SAVE_CHAPTER_MANgA: already existed")
              }

              val chapterJson = new File(chapterDir, "chapter.json")
              if (!chapterJson.exists()) {
                chapterJson.createNewFile()
                val fw = new FileWriter(chapterJson.getAbsoluteFile())
                val bw = new BufferedWriter(fw)
                bw.write(gson.toJson(chapter))
                bw.close()
                System.out.println("SAVE_CHAPTER_Chapter: new SAVED")
              } else {
                System.out.println("SAVE_CHAPTER_Chapter: already existed")
              }
              System.out.println("SAVE_CHAPTER_MANgA_Chapter: SAVED success")
            } catch {
              case e: Exception => System.out.println("SAVE_CHAPTER_MANgA_chapter: problem/exception")
            }

            for (i <- 0 until getNumPages(parentManga, chapter)) {
              val fromFile = ScriptManager.getCurrentSource().downloadPage(parentManga, chapter, i)
              try {
                val is = new FileInputStream(fromFile)
                val filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."))
                val os = new FileOutputStream(chapterDir.getAbsolutePath() + "/" + filename)
                Utilities.copyStreams(is, os)
              } catch {
                case e: Exception => System.out.println("Save Chapter ERROR: "+fromFile + " e: " + e.toString)
              }
            }
          }
  }


  def setCurrentManga(manga: Manga) {
    if (!isOffline)
      ScriptManager.getCurrentSource().initManga(manga)
    currentManga = manga
  }

  def getCurrentManga() = currentManga

  def setCurrentChapter(current: Chapter) {
    chapterPageMap.clear()
    currentChapter = current
  }
  // THESE LOOK BACKWARDS
  // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
  // Make this script decidable later
  def previousChapter(): Boolean =  {
    val mangaChapterList = getMangaChapterList()
    System.out.println("PREVOUS CHAPTER: "+Integer.toString(currentChapter.getNum()))
    System.out.println("PREVIOUS CHAPTER: size "+Integer.toString(mangaChapterList.size()-1))
    val nextIndex = mangaChapterList.indexOf(currentChapter)+1
    if (nextIndex < mangaChapterList.size())
      setCurrentChapter(mangaChapterList.get(nextIndex))
    else
      return false
    true
  }

  def nextChapter(): Boolean = {
    val mangaChapterList = getMangaChapterList()
    System.out.println("NEXT CHAPTER: "+Integer.toString(currentChapter.getNum()))
    val nextIndex = mangaChapterList.indexOf(currentChapter)-1
    if (nextIndex > -1)
      setCurrentChapter(mangaChapterList.get(nextIndex))
    else
      return false
    true
  }

  def getMangaChapterList(): List[Chapter] = {
    if (isOffline) {
      //getSavedChapters(currentManga)
      System.out.println("No saved chapters!")
    }
    ScriptManager.getCurrentSource().getMangaChapterList(currentManga)
  }

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
        //SettingsManager.getCacheSize replaced with 5
      for (i <- 0 until 5)
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
      val chapterDirString = "/Mangagaga/Downloaded/" + manga.getTitle + "/" + chapter.getTitle
      val savedDir = new File(chapterDirString)
      for ((dir, i) <- savedDir.list.filter(!_.endsWith(".json")).view.zipWithIndex) {
        chapterPageMap(i) = chapterDirString + "/" + dir.toString
      }
    }
  }
}

