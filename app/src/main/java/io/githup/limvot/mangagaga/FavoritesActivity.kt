package io.githup.limvot.mangagaga

import android.os.Bundle
import android.app.Activity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class FavoritesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast("Hello Kotlin FavoritesActivity")

        verticalLayout {
            button("some favorite") {
                onClick {
                    toast("look at the favorite!")
                }
            }
        }
    }
}

