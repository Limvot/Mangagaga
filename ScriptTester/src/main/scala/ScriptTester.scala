import java.io.File
import collection.mutable.Buffer
import scala.collection.JavaConversions._

object ScriptTester {
  var srcnum = 0
  var done = false
  def main(args : Array[String]) {
    println("Hello! This is a world of scala!!!")
    var mangaManager = MangaManager.instance()
    initFolders()
    ScriptManager.init()
    sourceLoop()
    //printSources() 
    //setSourceNumber(readInt)
    //println("You chose source number "+srcnum)
    //changeSource()
    /*var list = getChapterList()*/
    //while(!done) {
    //printMangaList(list)
    //println("Next page (n), Previous page (p), Change Source (c), Quit (q), or Manga Number:")
    //val ln = readLine()
    //if(ln(0) == 'q') {
      //println("Exiting!!")
      //done = true
    //} else if(ln(0) == 'n') {
      //list = getSourceNextPage()
    //} else if(ln(0) == 'p') {
      //list = getSourcePreviousPage()
    //} else if(ln(0) == 'c') {
      //changeSource()
      //list = getChapterList()
    //} else {
      //println("you chose the manga "+list(ln.toInt).getTitle)
      //ScriptManager.setCurrentSource(srcnum)
      //MangaManager.readingOffline(false)
      //MangaManager.setCurrentManga(list(ln.toInt))
      //done = true
    //}
  /*}*/
  }
  
  def sourceLoop() = {
    var sloop = true
    while(sloop) {
      changeSource()
      mangaLoop()
    }
  }

  def mangaLoop() = {
    var mloop = true
    var list = getMangaList()
    while(mloop) {
      printMangaList(list)
      println("Next page (n), Previous page (p), Back (b), Quit (q), or Manga Number:")
      val ln = readLine()
      if(ln(0) == 'q') {
        println("Exiting!!")
        System.exit(0)
      } else if(ln(0) == 'n') {
        list = getSourceNextPage()
      } else if(ln(0) == 'p') {
        list = getSourcePreviousPage()
      } else if(ln(0) == 'b') {
        mloop = false
        //changeSource()
        //list = getChapterList()
      } else {
        var num = ln.toInt
        println("you chose the manga "+list(num).getTitle)
        ScriptManager.setCurrentSource(srcnum)
        MangaManager.readingOffline(false)
        MangaManager.setCurrentManga(list(num))
        chapterLoop()
      }
    }
  }

  def chapterLoop() = {
    var cloop = true
    val manga = MangaManager.getCurrentManga()
    var list = MangaManager.getMangaChapterList()
    println("Description: "+manga.getDescription())
    while(cloop){
      printChapterList(list)
      println("Back (b), Quit (q), or Chapter Number:")
      val ln = readLine()
      if(ln(0) == 'q') {
        println("Exiting!!")
        System.exit(0)
      } else if(ln(0) == 'b') {
        cloop = false
      } else {
        var num = ln.toInt
        println("you chose the chapter "+list(num).getTitle)
        MangaManager.setCurrentChapter(list(num).asInstanceOf[Chapter])
        MangaManager.setCurrentPageNum(0)
        println("Debug String 1")
        imageLoop()
      }

    }
  }
  
  def imageLoop() = {
    var iloop = true
    println("Debug String 2")
    var list : Buffer[String] = Buffer[String]()
    var current : String = null
    var total = MangaManager.getNumPages()
    println("Debug String 3")
    current = updateImage()
    list.append(current)
    println("Debug String 4")
    while(iloop) {
      println("Images downloaded: ")
      printImageList(list)
      println("Back (b), Quit (q), Next image (n), Previous Image (p):")
      val ln = readLine()
      if(ln(0) == 'q') {
        System.exit(0)
      } else if(ln(0) == 'b') {
        iloop = false
      } else if(ln(0) == 'n') {
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
        list.append(current)
      } else if(ln(0) == 'p') {
        //get previoust image!
        var i = MangaManager.getCurrentPageNum()
        if(i > 0) {
          MangaManager.setCurrentPageNum(i-1)
        } else {
          if(MangaManager.previousChapter()) {
            MangaManager.setCurrentPageNum(MangaManager.getNumPages - 1)
          }
        }
        current = updateImage()
        list.append(current)
      } else {
        println("Error, unrecognized command!")
      }
    }
  }

  def initFolders() = {
    println("Making folders")  
    var mainfolder = new File("./Mangagaga")
    try {
      if(!mainfolder.exists) {
        mainfolder.mkdir
        for (foldername <- List("Downloaded", "Scripts", "Cache")) {
          val folder = new File(mainfolder, foldername)
          if(!folder.exists) {
            folder.mkdir
          }
        }
      }
    } catch {
      case e:Exception => System.out.println("There was an error making folders!")
    }
  }
  
  def setSourceNumber(num: Int) = {
    if(num < 0) {
      println("Exiting")
    } else {
      srcnum = num
    }
  }
  
  def printSources() = {
    println("\nSelect a source")
    for(i <- 0 until ScriptManager.numSources) {
      println(i+": "+ScriptManager.getScript(i).getName)
    }
  }
  
  def changeSource() = {
    printSources() 
    setSourceNumber(readInt)
    println("You chose source number "+srcnum)
  }
  
  def printMangaList(list : Buffer[Manga]) = {
    var count = 0
    for(i <- list) {
      println(count + ": "+i.getTitle)
      count += 1
    }
  }
  
  def printChapterList(list : Buffer[Chapter]) = {
    var count = 0
    for(i <- list) {
      println(count + ": "+i.getTitle)
      count += 1
    }
  }

  def printImageList(list : Buffer[String]) = {
    var count = 0
    for(i <- list) {
      println(count + ": "+i)
      count += 1
    }
  }
  
  def updateImage() : String = {
    MangaManager.getCurrentPage()
  }

  def getMangaList() : Buffer[Manga] = {
    ScriptManager.getScript(srcnum).getMangaListPage1()
  }

  def getSourceNextPage() : Buffer[Manga] = {
    ScriptManager.getScript(srcnum).getMangaListNextPage()
  }

  def getSourcePreviousPage() : Buffer[Manga] = {
    ScriptManager.getScript(srcnum).getMangaListPreviousPage()
  }
}
