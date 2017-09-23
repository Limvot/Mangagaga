package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.app.Activity

class HistoryActivity : Activity() {
    var historyAdapter: SimpleListAdaptor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            listView {
                val listItems = MangaManager.getChapterHistoryList().map { chapter ->
                                                TextListItem(chapter.toString(), {
                                                    MangaManager.readingOffline(false)
                                                    MangaManager.currentManga = chapter.parentManga
                                                    ScriptManager.currentSource =
                                                        chapter.parentManga.sourceNumber
                                                    MangaManager.currentChapter = chapter
                                                    MangaManager.currentPage = 0
                                                    startActivity<ImageViewerActivity>()
                                                }) }
                historyAdapter = SimpleListAdaptor(ctx, listItems)
                adapter = historyAdapter
            }
        }
    }
    override fun onResume() {
        super.onResume()
        historyAdapter!!.notifyDataSetChanged()
    }
}

