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
        val thisReq = Request(source = Boss.getCurrentSource().name, manga = Boss.currentManga)
        var description: TextView? = null
        var favoriteBox: CheckBox? = null
        val chapterList = mutableListOf<TextListItem>()
        val chapterListAdapter = SimpleListAdaptor(ctx, chapterList)
        getActionBar().title = "${Boss.currentManga}:"

        verticalLayout {
            favoriteBox = checkBox("Favorite") { onClick {
                    Boss.setFavorite(thisReq, favoriteBox!!.isChecked())
            } }
            description = textView("description...")
            listView { adapter = chapterListAdapter }.lparams(weight=0.1f)
        }
        favoriteBox!!.setChecked(Boss.isFavorite(thisReq))
        val dialog = indeterminateProgressDialog(title = "Initing Manga", message = "(may take a little bit if script sets up pages)")
        val currentSource = Boss.getCurrentSource().name
        doAsync {
            val req = Request(source = currentSource, manga = Boss.currentManga)
            val description_chapter_list = Boss.getCurrentSource().makeRequest(req)
            uiThread {
                description!!.text = description_chapter_list[0]
                val items = description_chapter_list.subList(1,description_chapter_list.size)
                Boss.currentChapterList = items

                chapterList.clear()
                chapterList.addAll(items.map { chapter -> TextListItem(chapter, {
                                                    Boss.currentChapter = chapter
                                                    Boss.currentPage = 0
                                                    startActivity<ImageViewerActivity>()
                                                }, "Saved: ",
                                                Boss.isSaved(thisReq.copy(chapter = chapter)),
                                                    {checked ->
                                                    if (checked)
                                                        Boss.addSaved(thisReq.copy(chapter = chapter))
                                                    else
                                                        Boss.removeSaved(thisReq.copy(chapter = chapter))
                                                }) })
                chapterListAdapter.notifyDataSetChanged()
                dialog.dismiss()
                toast("there are ${items.size} chapters")
            }
        }
    }
}
