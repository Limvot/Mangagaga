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
        val currentManga = Boss.currentManga
        var description: TextView? = null
        var favoriteBox: CheckBox? = null
        var chapterList = mutableListOf<TextListItem>()
        var chapterListAdapter = SimpleListAdaptor(ctx, chapterList)
        getActionBar().title = "$currentManga:"

        verticalLayout {
            favoriteBox = checkBox("Favorite") { onClick {
                    Boss.setFavorite(currentManga, favoriteBox!!.isChecked())
            } }
            description = textView("description...")
            listView { adapter = chapterListAdapter }.lparams(weight=0.1f)
        }
        favoriteBox!!.setChecked(Boss.isFavorite(currentManga))
        val dialog = indeterminateProgressDialog(title = "Initing Manga", message = "(may take a little bit if script sets up pages)")
        doAsync {
            var req = Request()
            req.source = ScriptManager.getCurrentSource().name
            req.manga = Boss.currentManga
            val description_chapter_list = ScriptManager.getCurrentSource().makeRequest(req)
            //MangaManager.initCurrentManga()
            uiThread {
                description!!.text = description_chapter_list[0]
                val items : List<String> =
                description_chapter_list.subList(1,description_chapter_list.size)
                Boss.currentChapterList = items

                chapterList.clear()
                chapterList.addAll(items.map { chapter -> TextListItem(chapter, {
                                                    Boss.currentChapter = chapter
                                                    Boss.currentPage = 0
                                                    startActivity<ImageViewerActivity>()
                                                }, "Saved: ", Boss.isSaved(chapter),
                                                    {checked ->
                                                    if (checked) Boss.addSaved(chapter)
                                                    else Boss.removeSaved(chapter)
                                                }) })
                chapterListAdapter.notifyDataSetChanged()
                dialog.dismiss()
                toast("there are ${items.size} chapters")
            }
        }
    }
}
