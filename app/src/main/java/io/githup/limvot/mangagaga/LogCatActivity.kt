package io.githup.limvot.mangagaga

import android.app.Activity
import android.widget.TextView
import android.os.Bundle

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class LogCatActivity : Activity(), AnkoLogger {
    var lgText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        verticalLayout{
            button("refresh") { onClick { refreshText() } }
            button("clear")   { onClick { clearLogCat() } }
            scrollView {
                lgText = textView("placeholder") { textSize = 10f }
            }
        }
        refreshText()
    }
    fun clearLogCat() {
        Runtime.getRuntime().exec("logcat -c")
        refreshText()
    }
    fun refreshText() {
        lgText!!.text = Runtime.getRuntime().exec("logcat -d").getInputStream().reader().readText()
        info("Called LogCat referesh ;D")
    }
}
