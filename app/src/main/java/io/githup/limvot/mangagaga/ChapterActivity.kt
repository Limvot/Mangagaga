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
        val chapterList = mutableListOf<TextListItem>()
        val chapterListAdapter = SimpleListAdaptor(ctx, chapterList)
        getActionBar().title = "$currentManga:"

        verticalLayout {
            favoriteBox = checkBox("Favorite") { onClick {
                    val fave_set_req = Request()
                    fave_set_req.source = Boss.getCurrentSource().name
                    fave_set_req.manga = currentManga
                    Boss.setFavorite(fave_set_req, favoriteBox!!.isChecked())
            } }
            description = textView("description...")
            listView { adapter = chapterListAdapter }.lparams(weight=0.1f)
        }
        val fave_req = Request()
        fave_req.source = Boss.getCurrentSource().name
        fave_req.manga = currentManga
        favoriteBox!!.setChecked(Boss.isFavorite(fave_req))
        val dialog = indeterminateProgressDialog(title = "Initing Manga", message = "(may take a little bit if script sets up pages)")
        val currentSource = Boss.getCurrentSource().name
        doAsync {
            val req = Request()
            req.source = currentSource
            req.manga = Boss.currentManga
            val description_chapter_list = Boss.getCurrentSource().makeRequest(req)
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
                                                }, "Saved: ",
                                                Boss.isSaved(chapter,currentManga,currentSource),
                                                    {checked ->
                                                    if (checked)
                                                    Boss.addSaved(chapter,currentManga,currentSource)
                                                    else
                                                    Boss.removeSaved(chapter,currentManga,currentSource)
                                                }) })
                chapterListAdapter.notifyDataSetChanged()
                dialog.dismiss()
                toast("there are ${items.size} chapters")
            }
        }
    }
}
