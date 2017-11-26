package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import android.os.Bundle
import android.app.Activity

class FavoritesActivity : Activity(), GenericLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout{
            listView {
                val listItems = Boss.getFavoriteList().map { manga ->
                                            TextListItem(manga.manga, {
                                                Boss.readingOffline(false)
                                                Boss.currentManga = manga.manga
                                                Boss.setCurrentSource(manga.source)
                                                startActivity<ChapterActivity>()
                                            }) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}

