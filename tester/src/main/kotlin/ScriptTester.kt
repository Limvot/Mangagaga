package io.githup.limvot.mangagaga

import java.awt.FlowLayout
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

object ScriptTester {
  var done = false
  var mangaListType = "unset"
  @JvmStatic
  fun main(vararg args : String) {
    println("Hello! This is a world of Kotlin!!!")
    SettingsManager.mangagagaPath = "Mangagaga"
    initFolders()
    SettingsManager.loadSettings()
    // Overwrite all scripts
    Utilities.gitToScripts()

    ScriptManager.init()
    sourceLoop()
  }
  
  fun sourceLoop() {
    var sloop = true
    while (sloop) {
      changeSource()
      mangaLoop()
    }
  }

  fun mangaLoop() {

    val script = ScriptManager.getCurrentSource()
    var test_request = Request();
    test_request.source = script.name;
    test_request.filter = mangaListType
    val manga_list = script.makeRequest(test_request);

    var mloop = true
    var list : List<String> = manga_list
    while (mloop) {
      printMangaList(list)
      println("Back (b), Quit (q), or Manga Number:")
      val ln = readLine()!!
      if (ln[0] == 'q') {
        println("Exiting!!")
        System.exit(0)
      } else if (ln[0] == 'b') {
        mloop = false
      } else {
        var num = ln.toInt()
        var index = num
        println("you chose the manga "+list[index])
        Boss.readingOffline(false)
        Boss.currentManga = list[index]
        chapterLoop()
      }
    }
  }

  fun chapterLoop() {
    var cloop = true
    //val manga = MangaManager.currentManga!!
    //TODO(marcus): this has different behavior based on online/offline reading
    //var list = MangaManager.getMangaChapterList()

    val script = ScriptManager.getCurrentSource()
    var req = Request()
    req.manga = Boss.currentManga
    var list = script.makeRequest(req)
    Boss.currentChapterList = list

    println("Description: "+list[0])
    while (cloop){
      printChapterList(list)
      println("Back (b), Quit (q), or Chapter Number:")
      val ln = readLine()!!
      if (ln[0] == 'q') {
        println("Exiting!!")
        System.exit(0)
      } else if (ln[0] == 'b') {
        cloop = false
      } else {
        var num = ln.toInt()
        println("you chose the chapter "+list[num+1])
        Boss.currentChapter = list[num+1]
        Boss.currentPage = 0
        println("Debug String 1")
        imageLoop()
        return
      }

    }
  }
  
  fun imageLoop() {
    //var list : MutableList<String> = mutableListOf()
    //request number of pages
    var req = Request()
    req.manga = Boss.currentManga
    req.chapter = Boss.currentChapter
    val script = ScriptManager.getCurrentSource()
    var num_page_list = script.makeRequest(req)
    println("Num Pages "+num_page_list[0])

    //request first page
    req.page = "0"
    var page = script.makeRequest(req)
    var current = page[0];
    var total = num_page_list[0].toInt()

    val frame = JFrame()
    frame.layout = FlowLayout()
    frame.setSize(800,1200)
    val lbl = JLabel()
    frame.add(lbl)
    frame.setVisible(true)

    while (true) {
      val img = ImageIO.read(File(current))
      val icon = ImageIcon(img)
      lbl.icon = icon
      frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

      println("Back (b), Quit (q), Next image (n), Previous Image (p):")
      val ln = readLine()!!
      if (ln[0] == 'q') {
        System.exit(0)
      } else if (ln[0] == 'b') {
          break
      } else if (ln[0] == 'n') {
        //get next image!
        var i = Boss.currentPage
        if (i < total-1) {
          Boss.currentPage = i+1
        } else {
            if (Boss.nextChapter()) {
                req.chapter = Boss.currentChapterList[Boss.currentChapterList.indexOf(req.chapter)-1]
                req.page = ""
                var next_page_list = script.makeRequest(req)
                total = next_page_list[0].toInt()
                Boss.currentPage = 0
            }
        }
        req.page = Boss.currentPage.toString()
        page = script.makeRequest(req)
        current = page[0]
      } else if (ln[0] == 'p') {
        //get previoust image!
        var i = Boss.currentPage
        if (i > 0) {
          Boss.currentPage = i-1
        } else {
            if (Boss.previousChapter()) {
                req.chapter = Boss.currentChapterList[Boss.currentChapterList.indexOf(req.chapter)+1]
                req.page = ""
                var prev_page_list = script.makeRequest(req)
                total = prev_page_list[0].toInt()
                Boss.currentPage = total - 1
            }
        }
        req.page = Boss.currentPage.toString()
        page = script.makeRequest(req)
        current = page[0]
      } else {
        println("Error, unrecognized command!")
      }
    }
  }

  fun initFolders() {
    println("Making folders")  
    var mainfolder = File("./Mangagaga")
    try {
      if (!mainfolder.exists()) {
        mainfolder.mkdir()
        for (foldername in listOf("Downloaded", "Scripts", "Cache")) {
          val folder = File(mainfolder, foldername)
          if (!folder.exists()) {
            folder.mkdir()
          }
        }
      }
    } catch (e: Exception) {
      println("There was an error making folders!")
    }
  }
  
  fun setSourceNumber(num: Int) {
    if (num < 0) {
      println("Exiting")
    } else {
      ScriptManager.currentSource = num
    }
  }
  
  fun printSources() {
    println("\nSelect a source")
    for (i in 0 until ScriptManager.numSources()) {
      println("$i: ${ScriptManager.getScript(i)!!.name}")
    }
  }
  
  fun changeSource() {
    printSources() 
    setSourceNumber(readLine()!!.toInt())
    println("You chose source number ${ScriptManager.currentSource}")

    val types = ScriptManager.getCurrentSource().getMangaListTypes()
    for ((i, type) in types.withIndex()) println("$i - $type")
    println("\nSelect a type")
    mangaListType = types[readLine()!!.toInt()]
    println("You chose $mangaListType")
  }
  
  fun printMangaList(list : List<String>) {
      var count = 0
      for (entry in list) {
          println("$count: ${entry}")
          count += 1
      }
  }
  
  fun printChapterList(list : List<String>) {
    var count = 0
    var skip_descr = 0
    for (i in list) {
        if(skip_descr == 0) {
            skip_descr = 1
            continue
        }
        println("$count: ${i}")
        count += 1
    }
  }

  fun printImageList(list : List<String>) {
    var count = 0
    for (i in list) {
      println("$count: $i")
      count += 1
    }
  }
  
}
