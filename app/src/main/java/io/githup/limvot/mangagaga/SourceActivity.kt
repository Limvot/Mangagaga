package io.githup.limvot.mangagaga

import android.app.ListActivity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.os.Bundle

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class SourceActivity : ListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast("Hello Kotlin List Activity")

        val items = listOf("a", "b", "alpha", "one")
        val listItems = items.map { TextListItem(it) }
        listAdapter = SimpleListAdaptor(this, listItems)
    }
}

class SimpleListAdaptor(ctx: Context, items: List<TextListItem>) : ArrayAdapter<TextListItem>(ctx, 0, items) {
    private val ankoContext = AnkoContext.createReusable(ctx, this)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val item = getItem(position)
        if (item != null) {
            val view = convertView ?: item.createView(ankoContext)
            item.applyView(view)
            return view
        } else return convertView
    }
}

class TextListItem(val internal_text: String = "empty") : AnkoComponent<SimpleListAdaptor> {
    override fun createView(ui: AnkoContext<SimpleListAdaptor>) = with(ui) {
        textView {
            text = internal_text
        }
    }
    fun applyView(convertView: View) {
        println("apply convertView")
    }
}
