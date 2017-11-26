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
    private var mangaList: MutableList<TextListItem>? = null
    private var mangaListAdapter: SimpleListAdaptor? = null
    private var srcButton: Button? = null
    private var listTypeButton: Button? = null
    private var mangaListType = "All"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mangaList = mutableListOf(TextListItem("placeholder", { toast("why?") }))
        mangaListAdapter = SimpleListAdaptor(ctx, mangaList!!)

        verticalLayout{
            relativeLayout {
                srcButton = button("Change Source") {
                    onClick { doSourcePopup() }
                }.lparams {
                    alignParentLeft()
                }
                listTypeButton = button("Change List Type") { onClick { doTypePopup() } }.lparams {
                    alignParentRight()
                }
            }

            listView { adapter = mangaListAdapter }.lparams(weight=0.1f)
        }
        doSourcePopup()
    }
    private fun doSourcePopup() {
        selector("Source", Boss.scriptList.map {it.name}) { _, i ->
            Boss.currentSource = i
            srcButton!!.text = Boss.scriptList[i].name
            updateMangaList()
        }
    }
    private fun doTypePopup() {
        selector("Sorted", Boss.getCurrentSource().getMangaListTypes()) { _, i ->
            mangaListType = Boss.getCurrentSource().getMangaListTypes()[i]
            listTypeButton!!.text = mangaListType
            updateMangaList()
        }
    }
    private fun updateMangaList() {
        val dialog = indeterminateProgressDialog(title = "Loading manga list from source", message = "(may take 5 seconds to get through CloudFlare or something)")
        doAsync {
            val req = Request();
            req.source = Boss.getCurrentSource().name
            req.filter = mangaListType
            val items = Boss.getCurrentSource().makeRequest(req)
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
