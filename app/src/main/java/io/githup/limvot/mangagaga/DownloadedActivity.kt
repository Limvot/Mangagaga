package io.githup.limvot.mangagaga

import android.os.Bundle
import android.app.Activity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class DownloadedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast("Hello Kotlin DownloadedActivity")

        verticalLayout {
            button("some downloads") {
                onClick {
                    toast("set the download!")
                }
            }
        }
    }
}

