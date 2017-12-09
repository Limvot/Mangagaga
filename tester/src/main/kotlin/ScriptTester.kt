package io.githup.limvot.mangagaga

import java.awt.FlowLayout
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

object ScriptTester {
    @JvmStatic
    fun main(vararg args : String) {
        SettingsManager.mangagagaPath = "Mangagaga"

        // make folders
        var mainfolder = File("./Mangagaga")
        try {
            if (!mainfolder.exists()) mainfolder.mkdir()
            for (foldername in listOf("Downloaded", "Scripts", "Cache")) {
                val folder = File(mainfolder, foldername)
                if (!folder.exists()) folder.mkdir()
            }
        } catch (e: Exception) {
            println("There was an error making folders!")
        }
        SettingsManager.loadSettings()
        // Overwrite all scripts
        Utilities.gitToScripts()

        Boss.init()
        var state = "source"
        while (true) { when (state) {
            "source"  -> state = sourceLoop()
            "manga"   -> state = mangaLoop()
            "chapter" -> state = chapterLoop()
            "image"   -> state = imageLoop()
        } }
    }

    fun sourceLoop() : String {
        val script_options = Boss.getScriptList()
        printList(script_options)
        println("\nSelect a source")
        Boss.currentSource = script_options[readLine()!!.toInt()]

        val types = Boss.getFilterTypes()
        printList(types)
        println("\nSelect a type")
        Boss.currentFilter = types[readLine()!!.toInt()]
        return "manga"
    }

    fun mangaLoop() : String {
        val ml_list = Boss.getMangaList()
        printList(ml_list)
        val ln = printPrompt("Back (b), Quit (q), or Manga Number:")
        if (ln[0] == 'b') return "source"
        Boss.currentManga = ml_list[ln.toInt()]
        return "chapter"
    }

    fun chapterLoop() : String {
        println("Description: ${Boss.getMangaDescription()}")
        val cl_list = Boss.getChapterList()
        printList(cl_list)
        val ln = printPrompt("Back (b), Quit (q), or Chapter Number:")
        if (ln[0] == 'b') return "manga"
        Boss.currentChapter = cl_list[ln.toInt()+1]
        Boss.currentPage = 0
        return "image"
    }

    fun printList(list : List<String>) = println(list.withIndex().map { (i,x) -> "$i: $x" }.joinToString("\n"))

    fun imageLoop(): String {
        //request number of pages
        println("Num Pages ${Boss.getNumPages()}")

        //request first page
        Boss.currentPage = 0
        var current = Boss.getPagePath()

        val il_frame = JFrame()
        il_frame.layout = FlowLayout()
        il_frame.setSize(800,1200)
        val il_lbl = JLabel()
        il_frame.add(il_lbl)
        il_frame.setVisible(true)
        il_frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        while (true) {
            il_lbl.icon = ImageIcon(ImageIO.read(File(current)))
            val ln = printPrompt("Back (b), Quit (q), Next image (n), Previous Image (p):")
            when (ln[0]) {
                'b'    -> return "chapter"
                'n','p'-> {
                    Boss.move((ln[0] == 'n'))
                    current = Boss.getPagePath();
                }
                else   -> println("Error, unrecognized command!")
            }
        }
    }

    fun printPrompt(msg : String) : String {
        println(msg)
        val ln = readLine()!!
        if (ln[0] == 'q') {
            println("Exiting!!")
            System.exit(0)
        }
        return ln
    }
}
