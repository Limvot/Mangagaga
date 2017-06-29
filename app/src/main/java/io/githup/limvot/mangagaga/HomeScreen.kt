package io.githup.limvot.mangagaga

import android.os.Bundle
import android.app.Activity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class HomeScreen : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast("Hello Kotlin Again Simpler")

        verticalLayout {
            button("Sources") { onClick { startActivity<SourceActivity>() } }
            button("Settings") { onClick { startActivity<SettingsActivity>() } }
        }
    }
}

