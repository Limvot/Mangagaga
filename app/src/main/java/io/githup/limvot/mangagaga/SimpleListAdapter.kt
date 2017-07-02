package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.widget.ArrayAdapter
import android.content.Context
import android.view.View
import android.view.ViewGroup

class SimpleListAdaptor(ctx: Context, items: List<TextListItem>) : ArrayAdapter<TextListItem>(ctx, 0, items) {
    private val ankoContext = AnkoContext.createReusable(ctx, this)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val item = getItem(position)
        if (item != null) {
            // FORGET ABOUT EFFICENCY!
            /*val view = convertView ?: item.createView(ankoContext)*/
            /*item.applyView(view)*/
            /*return view*/
            return item.createView(ankoContext)
        } else return convertView
    }
}

class TextListItem(val internal_text: String = "empty", val func: () -> Unit) : AnkoComponent<SimpleListAdaptor> {
    override fun createView(ui: AnkoContext<SimpleListAdaptor>) = with(ui) {
        textView {
            text = internal_text
            onClick { func() }
        }
    }
    fun applyView(convertView: View) {
        println("apply convertView")
    }
}
