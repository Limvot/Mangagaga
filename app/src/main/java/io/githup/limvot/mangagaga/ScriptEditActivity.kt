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
            linearLayout {
                button("load")      { onClick { load() } }
                button("save as")   { onClick { save() } }
                button("push")      { onClick { push() } }
                button("delete")    { onClick {
                    File(filePath).delete()
                    load()
                } }
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
    fun push() {
        var popup: DialogInterface? = null
        popup = alert { customView { verticalLayout {
            val username = editText("username")
            val password = editText("password")
            val commit_msg = editText("commit message")
            button("push") { onClick {
                doAsync {
                    Utilities.scriptToGit(File(filePath), username.getText().toString(),
                                                          password.getText().toString(),
                                                          commit_msg.getText().toString())
                }
                popup!!.dismiss()
            } }
            button("cancel") { onClick { popup!!.dismiss() } }
        } } }.show()
    }
}
