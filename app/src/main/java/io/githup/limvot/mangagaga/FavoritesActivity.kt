package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import android.os.Bundle
import android.app.Activity

class FavoritesActivity : Activity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        info("current favorites: ${MangaManager.getFavoriteList().size}")

        verticalLayout{
            listView {
                val listItems = MangaManager.getFavoriteList().map { manga -> TextListItem(manga.toString(), {
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

