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

class SourceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast("Hello Kotlin listView")

        /*val dialog = progressDialog(message = "please wait...", title = "making you wait")*/
        val dialog = indeterminateProgressDialog(message = "please wait...", title = "making you wait")
        /*dialog.dismiss()*/
        /*val options = listOf("first", "second", "third option")*/
        /*selector("Choose an option!", options) {*/
            /*dialog_interface, i-> toast("you chose ${options[i]}?")*/
        /*}*/

        verticalLayout{
            toolbar {
                title = "Something"
                button("in toolbar")
                button("in toolbar2")
            }

            listView {
                val items = listOf("a", "b", "alpha", "one")
                val listItems = items.map { TextListItem(it) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}
