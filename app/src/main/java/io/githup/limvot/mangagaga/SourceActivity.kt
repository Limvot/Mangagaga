package io.githup.limvot.mangagaga

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.os.Bundle

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class SourceActivity : Activity(), AnkoLogger {
    var mangaList: MutableList<TextListItem>? = null
    var mangaListAdapter: SimpleListAdaptor? = null
    var sourceText: TextView? = null
    var sourceNumber = 0
    var typeText: TextView? = null
    var mangaListType = "default type"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mangaList = mutableListOf(TextListItem("placeholder", { toast("why?") }))
        mangaListAdapter = SimpleListAdaptor(ctx, mangaList!!)

        verticalLayout{
            sourceText = textView("Source: ...") { textSize = 32f }
            button("Change Source") { onClick { doSourcePopup() } }
            typeText = textView("Type: ...") { textSize = 32f }
            button("Change List Type") { onClick { doTypePopup() } }

            /*listView {*/
                /*val items = ScriptManager.getScript(0)!!.getMangaListTypes()*/
                /*val listItems = items.map { TextListItem(it.toString()) }*/
                /*adapter = SimpleListAdaptor(ctx, listItems)*/
            /*}*/
            listView {
                adapter = mangaListAdapter
            }.lparams(weight=0.1f)
            button("Next") { onClick {
                val dialog = indeterminateProgressDialog(title = "Fetching next page", message = "really should be fast...")
                doAsync {
                    val items = ScriptManager.getScript(sourceNumber)!!.getMangaListNextPage()
                    uiThread {
                        dialog.dismiss()
                        showMangaList(items)
                    }
                }
            } }
            button("Previous") { onClick {
                val dialog = indeterminateProgressDialog(title = "Fetching previous page", message = "really should be fast...")
                doAsync {
                    val items = ScriptManager.getScript(sourceNumber)!!.getMangaListPreviousPage()
                    uiThread {
                        dialog.dismiss()
                        showMangaList(items)
                    }
                }
            } }
        }
        doSourcePopup()
    }
    fun doSourcePopup() {
        selector("Which Source", ScriptManager.scriptList.map {it.name}) { dialog_interface, i-> 
            sourceNumber = i
            sourceText!!.text = "Source: ${ScriptManager.scriptList[i].name}"
            val dialog = indeterminateProgressDialog(title = "Loading manga from source", message = "(may take 5 seconds to get through CloudFlare or something)")
            doAsync {
                val items = ScriptManager.getScript(sourceNumber)!!.getMangaListPage1()
                uiThread {
                    dialog.dismiss()
                    showMangaList(items)
                }
            }
        }
    }
    fun doTypePopup() {
        selector("Sorted How", ScriptManager.getScript(sourceNumber)!!.getMangaListTypes()) { dialog_interface, i-> 
            mangaListType = ScriptManager.getScript(sourceNumber)!!.getMangaListTypes()[i]
            typeText!!.text = "List Type: $mangaListType"
            val dialog = indeterminateProgressDialog(title = "Loading manga list from source", message = "(may take 5 seconds to get through CloudFlare or something)")
            doAsync {
                ScriptManager.getCurrentSource().setMangaListType(mangaListType)
                val items = ScriptManager.getScript(sourceNumber)!!.getMangaListPage1()
                uiThread {
                    dialog.dismiss()
                    showMangaList(items)
                }
            }
        }
    }
    fun showMangaList(items: List<Manga>) {
        mangaList!!.clear()

        mangaList!!.addAll(items.map { manga -> TextListItem(manga.toString(), {
                                            ScriptManager.currentSource = sourceNumber
                                            MangaManager.readingOffline(false)
                                            MangaManager.currentManga = manga
                                            startActivity<ChapterActivity>()
                                        }) })
        mangaListAdapter!!.notifyDataSetChanged()
        toast("there are ${items.size} manga")
    }
}
