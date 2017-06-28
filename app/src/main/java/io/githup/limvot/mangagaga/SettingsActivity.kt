package io.githup.limvot.mangagaga

import android.os.Bundle
import android.app.Activity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast("Hello Kotlin SettingsActivity")
        SettingsActivityUi().setContentView(this)
    }
}

class SettingsActivityUi : AnkoComponent<SettingsActivity> {
    override fun createView(ui: AnkoContext<SettingsActivity>) = with(ui) {
        verticalLayout {
            button("some setting") {
                onClick {
                    toast("set the setting!")
                }
            }
        }
    }
}
