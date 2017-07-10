package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import java.io.File;

import android.app.Activity
import android.content.DialogInterface
import android.widget.EditText
import android.os.Bundle

class ScriptEditActivity : Activity(), GenericLogger {
    var scriptText: EditText? = null
    var filePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        verticalLayout {
            relativeLayout {
                button("load")      { onClick { load() } }.lparams { alignParentLeft() }
                button("save as")   { onClick { save() } }.lparams { alignParentRight() }
                button("delete")    { onClick {
                    File(filePath).delete()
                    load()
                } }.lparams { centerInParent() }
            }
            scrollView { scriptText = editText("placeholder") { textSize = 10f } }
        }
        load()
    }
    fun load() {
        val scriptFiles = File(SettingsManager.mangagagaPath, "Scripts").listFiles()
        selector("Load", scriptFiles.map { it.getAbsolutePath() }) { _, i ->
            filePath = scriptFiles[i].getAbsolutePath()
            scriptText!!.setText(scriptFiles[i].readText())
        }
    }
    fun save() {
        var popup: DialogInterface? = null
        popup = alert { customView { verticalLayout {
            val savePath = editText(filePath)
            button("save") { onClick {
                File(savePath.getText().toString()).writeText(scriptText!!.getText().toString())
                filePath = savePath.getText().toString()
                ScriptManager.init()
                popup!!.dismiss()
            } }
            button("cancel") { onClick { popup!!.dismiss() } }
        } } }.show()
    }
}
