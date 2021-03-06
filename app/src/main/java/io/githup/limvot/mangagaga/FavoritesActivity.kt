package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import android.os.Bundle
import android.app.Activity

class FavoritesActivity : Activity(), GenericLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout{
            listView {
                val listItems = Boss.favoriteManga.map { manga ->
                                            TextListItem(manga.manga, {
                                                Boss.currentSource = manga.source
                                                Boss.currentFilter = manga.filter
                                                Boss.currentManga = manga.manga
                                                startActivity<ChapterActivity>()
                                            }) }
                adapter = SimpleListAdaptor(ctx, listItems)
            }
        }
    }
}

