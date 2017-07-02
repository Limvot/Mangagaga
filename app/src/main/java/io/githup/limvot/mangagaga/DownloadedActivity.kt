package io.githup.limvot.mangagaga

import android.os.Bundle
import android.app.Activity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class DownloadedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toast("there are ${MangaManager.getSavedManga().size} downloaded manga")

        verticalLayout {
            listView {
                val listItems = MangaManager.getSavedManga().map { manga -> TextListItem(manga.toString(), {
                                                    MangaManager.readingOffline(true)
                                                    MangaManager.currentManga = manga
                                                    startActivity<ChapterActivity>()
                                                }) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}

