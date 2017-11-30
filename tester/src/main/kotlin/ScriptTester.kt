package io.githup.limvot.mangagaga

import java.awt.FlowLayout
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

object ScriptTester {
  var mangaListType = "unset"
  var state = "source"
  var ml_list : List<String> = listOf()
  var cl_list : List<String> = listOf()
  var il_frame : JFrame = JFrame()
  var il_lbl : JLabel = JLabel()
  var current : String = ""

  @JvmStatic
  fun main(vararg args : String) {
    println("Hello! This is a world of Kotlin!!!")
    SettingsManager.mangagagaPath = "Mangagaga"
    initFolders()
    SettingsManager.loadSettings()
    // Overwrite all scripts
    //Utilities.gitToScripts()

    Boss.init()
    while (true) {
          if (state == "source") nextState(sourceLoop())
          if (state == "manga") nextState(mangaLoop())
          if (state == "chapter") nextState(chapterLoop())
          if (state == "image") nextState(imageLoop())
      }

  }

  fun nextState(next : String) {
      if (next == state) return
      state = next

      val script = Boss.getCurrentSource()
      if (state == "manga") {
          ml_list = script.makeRequest(Request(source = script.name, filter = mangaListType))
      } else if (state == "chapter") {
          cl_list = script.makeRequest(Request(manga = Boss.currentManga))
          println("Description: "+cl_list[0])
      } else if (state == "image") {
          //request number of pages
          var num_page_list = script.makeRequest(Request(manga = Boss.currentManga, chapter = Boss.currentChapter))
          println("Num Pages "+num_page_list[0])

          //request first page
          script.makeRequest(Request(manga = Boss.currentManga, chapter = Boss.currentChapter, page = "0"))
          current = Boss.getCurrentPagePath();

          il_frame = JFrame()
          il_frame.layout = FlowLayout()
          il_frame.setSize(800,1200)
          il_lbl = JLabel()
          il_frame.add(il_lbl)
          il_frame.setVisible(true)
          il_frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
      }
  }

  fun sourceLoop() : String {
    printSources()
    Boss.currentSource = Boss.scripts.keys.sorted()[readLine()!!.toInt()]
    println("You chose source number ${Boss.currentSource} out of ${Boss.scripts.keys.sorted()}")

    val types = Boss.getCurrentSource().getMangaListTypes()
    for ((i, type) in types.withIndex()) println("$i - $type")
    println("\nSelect a type")
    mangaListType = types[readLine()!!.toInt()]
    println("You chose $mangaListType")
    return "manga"
  }

  fun mangaLoop() : String {
    printList(ml_list, false)
    val ln = printPrompt("Back (b), Quit (q), or Manga Number:")
    if (ln[0] == 'b') return "source"
    var index = ln.toInt()
    println("you chose the manga "+ml_list[index])
    Boss.currentManga = ml_list[index]
    return "chapter"
  }

  fun chapterLoop() : String {
    printList(cl_list,true)
    val ln = printPrompt("Back (b), Quit (q), or Chapter Number:")
    if (ln[0] == 'b') return "manga"
    var num = ln.toInt()
    println("you chose the chapter "+cl_list[num+1])
    Boss.currentChapter = cl_list[num+1]
    Boss.currentPage = 0
    return "image"
  }

  fun imageLoop() : String {
    il_lbl.icon = ImageIcon(ImageIO.read(File(current)))

    val ln = printPrompt("Back (b), Quit (q), Next image (n), Previous Image (p):")
    if (ln[0] == 'b') return "chapter"

    if(ln[0] != 'n' && ln[0] != 'p') {
        println("Error, unrecognized command!")
    } else {
        //n -> true == get next image!
        //p -> false == get previous image!
        Boss.move((ln[0] == 'n'))
        current = Boss.getCurrentPagePath();
    }
    return "image"
  }

  fun initFolders() {
    println("Making folders")  
    var mainfolder = File("./Mangagaga")
    try {
      if (!mainfolder.exists()) {
        mainfolder.mkdir()
        for (foldername in listOf("Downloaded", "Scripts", "Cache")) {
          val folder = File(mainfolder, foldername)
          if (!folder.exists()) folder.mkdir()
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

  fun printList(list : List<String>, skip_description : Boolean) {
      var l = list
      if(skip_description) l = list.subList(1,list.size)
      for ((count, entry) in l.withIndex()) { println("$count: ${entry}") }
  }

  fun printPrompt(msg : String) : String {
      println(msg)
      val ln = readLine()!!
      if (ln[0] == 'q') exitTester()
      return ln
  }
  fun exitTester() {
      println("Exiting!!")
      System.exit(0)
  }
}
