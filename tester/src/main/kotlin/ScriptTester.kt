package io.githup.limvot.mangagaga

import java.io.File

object ScriptTester {
  var done = false
  var mangaListType = "unset"
  @JvmStatic
  fun main(vararg args : String) {
    println("Hello! This is a world of Kotlin!!!")
    SettingsManager.mangagagaPath = "./Mangagaga"
    initFolders()
    // Overwrite all scripts
    val scriptDir = File(SettingsManager.mangagagaPath, "Scripts/")
    val rawFolder = File("../app/src/main/res/raw/")
    File(scriptDir, "script_prequal").writeText(File(rawFolder, "script_prequal.lua").readText())
    for (name in listOf("kiss_manga", "unixmanga", "read_panda", "manga_stream", "jaiminis_box")) {
        val newScript = File(scriptDir, name)
        // For testing we want to always copy over scripts
        // on every update
        val from = File(rawFolder, when(name) {
          "kiss_manga"   -> "kiss_manga.lua"
          "unixmanga"    -> "unixmanga.lua"
          "read_panda"   -> "read_panda.lua"
          "manga_stream" -> "manga_stream.lua"
          "jaiminis_box" -> "jaiminis_box.lua"
          else           -> ""
        })
        newScript.writeBytes(from.readBytes())
    }
    ScriptManager.init()
    sourceLoop()
  }
  
  fun sourceLoop() {
    var sloop = true
    while(sloop) {
      changeSource()
      mangaLoop()
    }
  }

  fun mangaLoop() {
    var mloop = true
    var list = getMangaList()
    while(mloop) {
      printMangaList(list)
      println("Back (b), Quit (q), or Manga Number:")
      val ln = readLine()!!
      if(ln[0] == 'q') {
        println("Exiting!!")
        System.exit(0)
      } else if(ln[0] == 'b') {
        mloop = false
      } else {
        var num = ln.toInt()
        println("you chose the manga "+list[num].getTitle())
        MangaManager.readingOffline(false)
        MangaManager.currentManga = list[num]
        chapterLoop()
      }
    }
  }

  fun chapterLoop() {
    var cloop = true
    val manga = MangaManager.currentManga!!
    var list = MangaManager.getMangaChapterList()
    println("Description: "+manga.getDescription())
    while(cloop){
      printChapterList(list)
      println("Back (b), Quit (q), or Chapter Number:")
      val ln = readLine()!!
      if(ln[0] == 'q') {
        println("Exiting!!")
        System.exit(0)
      } else if(ln[0] == 'b') {
        cloop = false
      } else {
        var num = ln.toInt()
        println("you chose the chapter "+list[num].getTitle())
        MangaManager.currentChapter = list[num]
        MangaManager.setCurrentPageNum(0)
        println("Debug String 1")
        imageLoop()
      }

    }
  }
  
  fun imageLoop() {
    var iloop = true
    var list : MutableList<String> = mutableListOf()
    var current = updateImage()
    var total = MangaManager.getNumPages()
    list.add(current)
    while(iloop) {
      println("Images downloaded: ")
      printImageList(list)
      println("Back (b), Quit (q), Next image (n), Previous Image (p):")
      val ln = readLine()!!
      if(ln[0] == 'q') {
        System.exit(0)
      } else if(ln[0] == 'b') {
        iloop = false
      } else if(ln[0] == 'n') {
        //get next image!
        var i = MangaManager.getCurrentPageNum()
        if(i < total-1) {
          MangaManager.setCurrentPageNum(i+1)
        } else {
          if(MangaManager.nextChapter()) {
            MangaManager.setCurrentPageNum(0)
          }
        }
        current = updateImage()
        list.add(current)
      } else if(ln[0] == 'p') {
        //get previoust image!
        var i = MangaManager.getCurrentPageNum()
        if(i > 0) {
          MangaManager.setCurrentPageNum(i-1)
        } else {
          if(MangaManager.previousChapter()) {
            MangaManager.setCurrentPageNum(MangaManager.getNumPages() - 1)
          }
        }
        current = updateImage()
        list.add(current)
      } else {
        println("Error, unrecognized command!")
      }
    }
  }

  fun initFolders() {
    println("Making folders")  
    var mainfolder = File("./Mangagaga")
    try {
      if(!mainfolder.exists()) {
        mainfolder.mkdir()
        for (foldername in listOf("Downloaded", "Scripts", "Cache")) {
          val folder = File(mainfolder, foldername)
          if(!folder.exists()) {
            folder.mkdir()
          }
        }
      }
    } catch(e: Exception) {
      println("There was an error making folders!")
    }
  }
  
  fun setSourceNumber(num: Int) {
    if(num < 0) {
      println("Exiting")
    } else {
      ScriptManager.currentSource = num
    }
  }
  
  fun printSources() {
    println("\nSelect a source")
    for(i in 0 until ScriptManager.numSources()) {
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
  
  fun printMangaList(list : List<Manga>) {
    var count = 0
    for(i in list) {
      println("$count: ${i.getTitle()}")
      count += 1
    }
  }
  
  fun printChapterList(list : List<Chapter>) {
    var count = 0
    for(i in list) {
      println("$count: ${i.getTitle()}")
      count += 1
    }
  }

  fun printImageList(list : List<String>) {
    var count = 0
    for(i in list) {
      println("$count: $i")
      count += 1
    }
  }
  
  fun updateImage() : String {
    return MangaManager.getCurrentPage()
  }

  fun getMangaList() : List<Manga> {
    return ScriptManager.getCurrentSource().getMangaList(mangaListType)
  }
}
