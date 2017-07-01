package io.githup.limvot.mangagaga

import android.os.Bundle
import android.app.Activity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class HistoryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            button("how much history?") {
                onClick {
                    toast("there are ${MangaManager.getChapterHistoryList().size} things in history")
                }
            }
            listView {
                val listItems = MangaManager.getChapterHistoryList().map { TextListItem(it.toString()) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}

