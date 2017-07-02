package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.os.Environment;
import android.app.Activity
import android.widget.TextView
import android.widget.CheckBox

class ChapterActivity : Activity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentManga = MangaManager.currentManga!!
        var description: TextView? = null
        var favoriteBox: CheckBox? = null
        var chapterList = mutableListOf(TextListItem("placeholder", { toast("why?") }))
        var chapterListAdapter = SimpleListAdaptor(ctx, chapterList)

        verticalLayout {
            textView("${MangaManager.currentManga}:") { textSize = 32f }
            favoriteBox = checkBox("Favorite?") { onClick {
                if (favoriteBox!!.isChecked())
                    MangaManager.addFavorite(currentManga)
                else
                    MangaManager.removeFavorite(currentManga)
            } }
            description = textView("description...")
            listView {
                adapter = chapterListAdapter
            }.lparams(weight=0.1f)
        }
        favoriteBox!!.setChecked(MangaManager.isFavorite(currentManga))
        val dialog = indeterminateProgressDialog(title = "Initing Manga", message = "(may take a little bit if script sets up pages)")
        doAsync {
            MangaManager.initCurrentManga()
            uiThread {
                dialog.dismiss()
                description!!.text = currentManga.getDescription()

                val items = MangaManager.getMangaChapterList()
                chapterList.clear()
                chapterList.addAll(items.map { chapter -> TextListItem(chapter.toString(), {
                                                    MangaManager.currentChapter = chapter
                                                    MangaManager.setCurrentPageNum(0)
                                                    startActivity<ImageViewerActivity>()
                                                }) })
                chapterList.add(TextListItem("additional", { toast("why additional?") }))
                chapterListAdapter.notifyDataSetChanged()
                toast("there are ${items.size} chapters")
            }
        }
    }
}

