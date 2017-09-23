package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.app.Activity
import android.widget.TextView
import android.widget.CheckBox

class ChapterActivity : Activity(), GenericLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentManga = MangaManager.currentManga!!
        var description: TextView? = null
        var favoriteBox: CheckBox? = null
        var chapterList = mutableListOf<TextListItem>()
        var chapterListAdapter = SimpleListAdaptor(ctx, chapterList)
        getActionBar().title = "$currentManga:"

        verticalLayout {
            favoriteBox = checkBox("Favorite") { onClick {
                    MangaManager.setFavorite(currentManga, favoriteBox!!.isChecked())
            } }
            description = textView("description...")
            listView { adapter = chapterListAdapter }.lparams(weight=0.1f)
        }
        favoriteBox!!.setChecked(MangaManager.isFavorite(currentManga))
        val dialog = indeterminateProgressDialog(title = "Initing Manga", message = "(may take a little bit if script sets up pages)")
        doAsync {
            MangaManager.initCurrentManga()
            uiThread {
                description!!.text = currentManga.getDescription()

                val items = MangaManager.getMangaChapterList()
                chapterList.clear()
                chapterList.addAll(items.map { chapter -> TextListItem(chapter.toString(), {
                                                    MangaManager.currentChapter = chapter
                                                    MangaManager.currentPage = 0
                                                    startActivity<ImageViewerActivity>()
                                                }, "Saved: ", MangaManager.isSaved(chapter),
                                                    {checked ->
                                                    if (checked) MangaManager.addSaved(chapter)
                                                    else MangaManager.removeSaved(chapter)
                                                }) })
                chapterListAdapter.notifyDataSetChanged()
                dialog.dismiss()
                toast("there are ${items.size} chapters")
            }
        }
    }
}
