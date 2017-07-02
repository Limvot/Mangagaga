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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*dialog.dismiss()*/
        mangaList = mutableListOf(TextListItem("placeholder"))
        mangaListAdapter = SimpleListAdaptor(ctx, mangaList!!)

        verticalLayout{
            sourceText = textView("Source: ...") { textSize = 32f }
            button("Change Source") { onClick { doSourcePopup() } }

            /*listView {*/
                /*val items = ScriptManager.getScript(0)!!.getMangaListTypes()*/
                /*val listItems = items.map { TextListItem(it.toString()) }*/
                /*adapter = SimpleListAdaptor(ctx, listItems)*/
            /*}*/
            listView {
                adapter = mangaListAdapter
            }.lparams(height=matchParent)
        }
        doSourcePopup()
    }
    fun doSourcePopup() {
        selector("Which Sourcce", ScriptManager.scriptList.map {it.name}) { dialog_interface, i-> 
            sourceText!!.text = "Source: ${ScriptManager.scriptList[i].name}"
            val dialog = indeterminateProgressDialog(title = "Loading manga from source", message = "(may take 5 seconds to get through CloudFlare or something)")
            doAsync {
                val items = ScriptManager.getScript(i)!!.getMangaListPage1()
                mangaList!!.clear()
                mangaList!!.addAll(items.map { TextListItem(it.toString()) })
                mangaList!!.add(TextListItem("additional"))
                uiThread { dialog.dismiss(); mangaListAdapter!!.notifyDataSetChanged(); toast("there are ${items.size} manga")} 
            }
        }
    }
}
