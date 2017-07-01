package io.githup.limvot.mangagaga

import android.os.Bundle
import android.app.Activity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class FavoritesActivity : Activity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast("Hello Kotlin FavoritesActivity")

        info("current favorites: ${MangaManager.getFavoriteList().size}")

        verticalLayout{
            toolbar {
                title = "Something"
                button("in toolbar")
                button("in toolbar2")
            }

            listView {
                val listItems = MangaManager.getFavoriteList().map { TextListItem(it.toString()) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}

