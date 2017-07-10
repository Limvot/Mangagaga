package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import android.os.Bundle
import android.app.Activity

class FavoritesActivity : Activity(), GenericLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout{
            listView {
                val listItems = MangaManager.getFavoriteList().map { manga ->
                                            TextListItem(manga.toString(), {
                                                MangaManager.readingOffline(false)
                                                MangaManager.currentManga = manga
                                                ScriptManager.currentSource = manga.sourceNumber
                                                startActivity<ChapterActivity>()
                                            }) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}

