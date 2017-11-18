package io.githup.limvot.mangagaga

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Button
import android.os.Bundle

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class SourceActivity : Activity(), GenericLogger {
    var mangaList: MutableList<TextListItem>? = null
    var mangaListAdapter: SimpleListAdaptor? = null
    var sourceText: TextView? = null
    var typeText: TextView? = null
    var srcButton: Button? = null
    var listTypeButton: Button? = null
    var mangaListType = "All"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mangaList = mutableListOf(TextListItem("placeholder", { toast("why?") }))
        mangaListAdapter = SimpleListAdaptor(ctx, mangaList!!)

        verticalLayout{
            relativeLayout {
                sourceText = textView("Source: ...") { textSize = 22f }
                srcButton = button("Change Source") {
                    onClick { doSourcePopup() }
                }.lparams {
                    alignParentRight()
                }
            }
            relativeLayout {
                typeText = textView("Type: ...") { textSize = 22f }
                listTypeButton = button("Change List Type") { onClick { doTypePopup() } }.lparams {
                    alignParentRight()
                }
            }

            listView { adapter = mangaListAdapter }.lparams(weight=0.1f)
        }
        doSourcePopup()
    }
    fun doSourcePopup() {
        selector("Source", ScriptManager.scriptList.map {it.name}) { _, i ->
            ScriptManager.currentSource = i
            sourceText!!.text = "Source: ${ScriptManager.scriptList[i].name}"
            srcButton!!.text = ScriptManager.scriptList[i].name
            updateMangaList()
        }
    }
    fun doTypePopup() {
        selector("Sorted", ScriptManager.getCurrentSource().getMangaListTypes()) { _, i ->
            mangaListType = ScriptManager.getCurrentSource().getMangaListTypes()[i]
            typeText!!.text = "List Type: $mangaListType"
            listTypeButton!!.text = mangaListType
            updateMangaList()
        }
    }
    fun updateMangaList() {
        val dialog = indeterminateProgressDialog(title = "Loading manga list from source", message = "(may take 5 seconds to get through CloudFlare or something)")
        doAsync {
            var req = Request();
            req.source = ScriptManager.getCurrentSource().name
            req.filter = mangaListType
            val items = ScriptManager.getCurrentSource().makeRequest(req)
            uiThread {
                dialog.dismiss()
                mangaList!!.clear()
                mangaList!!.addAll(items.map { manga -> TextListItem(manga, {
                                                    Boss.readingOffline(false)
                                                    Boss.currentManga = manga
                                                    startActivity<ChapterActivity>()
                                                }) })
                mangaListAdapter!!.notifyDataSetChanged()
            }
        }
    }
}
