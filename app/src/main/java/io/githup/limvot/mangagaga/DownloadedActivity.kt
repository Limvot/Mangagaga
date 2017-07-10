package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.app.Activity

class DownloadedActivity : Activity(), GenericLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedManga = MangaManager.getSavedManga()

        verticalLayout {
            listView {
                val listItems = MangaManager.getSavedManga().map { manga ->
                                                TextListItem(manga.toString(), {
                                                    MangaManager.readingOffline(true)
                                                    MangaManager.currentManga = manga
                                                    startActivity<ChapterActivity>()
                                                }) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}
