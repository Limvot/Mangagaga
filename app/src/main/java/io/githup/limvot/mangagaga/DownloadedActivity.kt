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
            button("how many downloads?") {
                onClick {
                    toast("there are ${MangaManager.getSavedManga().size} downloaded manga")
                }
            }
            listView {
                val listItems = MangaManager.getSavedManga().map { TextListItem(it.toString(), { toast("Downloaded?") }) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}

