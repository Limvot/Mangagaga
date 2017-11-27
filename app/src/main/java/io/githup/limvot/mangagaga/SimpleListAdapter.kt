package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.content.Context
import android.view.View
import android.view.ViewGroup

class SimpleListAdaptor(ctx: Context, items: List<TextListItem>) : ArrayAdapter<TextListItem>(ctx, 0, items) {
    private val ankoContext = AnkoContext.createReusable(ctx, this)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val item = getItem(position)
        if (item != null)
            return item.createView(ankoContext)
        else
            return convertView
    }
}

class TextListItem(val internal_text: String = "empty", val func: () -> Unit, val checkbox_text: String = "none", val checkbox_start: Boolean = false, val checkbox_func: (Boolean) -> Unit = {}) : AnkoComponent<SimpleListAdaptor> {
    override fun createView(ui: AnkoContext<SimpleListAdaptor>) = with(ui) {
        if (checkbox_text == "none") {
            relativeLayout {
                textView { text = internal_text }
                onClick { func() }
            }
        } else {
            relativeLayout {
                textView { text = internal_text }.lparams { alignParentRight() }
                var box: CheckBox? = null
                box = checkBox(checkbox_text) { onClick { checkbox_func(box!!.isChecked()) } }
                box.setChecked(checkbox_start)
                onClick { func() }
            }
        }
    }
}
