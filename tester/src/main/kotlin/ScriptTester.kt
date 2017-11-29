package io.githup.limvot.mangagaga

import java.awt.FlowLayout
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

object ScriptTester {
  var mangaListType = "unset"
  @JvmStatic
  fun main(vararg args : String) {
    println("Hello! This is a world of Kotlin!!!")
    SettingsManager.mangagagaPath = "Mangagaga"
    initFolders()
    SettingsManager.loadSettings()
    // Overwrite all scripts
    //Utilities.gitToScripts()

    Boss.init()
    sourceLoop()
  }
  
  fun sourceLoop() {
    while (true) {
      changeSource()
      mangaLoop()
    }
  }

  fun mangaLoop() {
    val script = Boss.getCurrentSource()
    var list = script.makeRequest(Request(source = script.name, filter = mangaListType))

    while (true) {
      printList(list, false)
      val ln = printPrompt("Back (b), Quit (q), or Manga Number:")
      if (ln[0] == 'b') break
      var index = ln.toInt()
      println("you chose the manga "+list[index])
      Boss.currentManga = list[index]
      chapterLoop()
    }
  }

  fun chapterLoop() {
    var list = Boss.getCurrentSource().makeRequest(Request(manga = Boss.currentManga))

    println("Description: "+list[0])
    while (true){
      printList(list,true)
      val ln = printPrompt("Back (b), Quit (q), or Chapter Number:")
      if (ln[0] == 'b') break
      var num = ln.toInt()
      println("you chose the chapter "+list[num+1])
      Boss.currentChapter = list[num+1]
      Boss.currentPage = 0
      imageLoop()
      return
    }
  }
  
  fun imageLoop() {
    //request number of pages
    var req = Request(manga = Boss.currentManga, chapter = Boss.currentChapter)
    val script = Boss.getCurrentSource()
    var num_page_list = script.makeRequest(req)
    println("Num Pages "+num_page_list[0])

    //request first page
    req = req.copy(page = "0")
    var page = script.makeRequest(req)
    var current = Boss.getCurrentPagePath();

    val frame = JFrame()
    frame.layout = FlowLayout()
    frame.setSize(800,1200)
    val lbl = JLabel()
    frame.add(lbl)
    frame.setVisible(true)

    while (true) {
      val img = ImageIO.read(File(current))
      lbl.icon = ImageIcon(img)
      frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

      val ln = printPrompt("Back (b), Quit (q), Next image (n), Previous Image (p):")
      if (ln[0] == 'b') break

      if (ln[0] == 'n') {
        //get next image!
        Boss.move(true)
        current = Boss.getCurrentPagePath();
      } else if (ln[0] == 'p') {
        //get previoust image!
        Boss.move(false)
        current = Boss.getCurrentPagePath();
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
  
  fun printSources() {
    println("\nSelect a source")
    for ((i, name) in Boss.scripts.keys.sorted().withIndex()) {
      println("$i: $name")
    }
  }
  
  fun changeSource() {
    printSources() 
    Boss.currentSource = Boss.scripts.keys.sorted()[readLine()!!.toInt()]
    println("You chose source number ${Boss.currentSource} out of ${Boss.scripts.keys.sorted()}")

    val types = Boss.getCurrentSource().getMangaListTypes()
    for ((i, type) in types.withIndex()) println("$i - $type")
    println("\nSelect a type")
    mangaListType = types[readLine()!!.toInt()]
    println("You chose $mangaListType")
  }

  fun printList(list : List<String>, skip_description : Boolean) {
      var l = list
      if(skip_description) {
          l = list.subList(1,list.size)
      }
      for ((count, entry) in l.withIndex()) {
          println("$count: ${entry}")
      }
  }

  fun printPrompt(msg : String) : String {
      println(msg)
      val ln = readLine()!!
      if (ln[0] == 'q') {
          exitTester()
      }
      return ln
  }
  fun exitTester() {
      println("Exiting!!")
      System.exit(0)
  }
}
