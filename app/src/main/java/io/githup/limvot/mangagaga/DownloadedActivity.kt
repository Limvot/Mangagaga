package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.app.Activity

class DownloadedActivity : Activity(), GenericLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            listView {
                val listItems = Boss.getSavedManga().map { req ->
                                                TextListItem(req.manga, {
                                                    Boss.currentSource = req.source // this is "downloaded"
                                                    Boss.currentManga = req.manga
                                                    startActivity<ChapterActivity>()
                                                }) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}
