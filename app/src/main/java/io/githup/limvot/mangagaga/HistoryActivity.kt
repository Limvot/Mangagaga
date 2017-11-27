package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.app.Activity

class HistoryActivity : Activity() {
    private var historyAdapter: SimpleListAdaptor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            listView {
                val listItems = Boss.getChapterHistoryList().map { chapter ->
                                                TextListItem(chapter.chapter, {
                                                    Boss.currentSource = chapter.source
                                                    Boss.currentManga = chapter.manga
                                                    Boss.currentChapter = chapter.chapter
                                                    Boss.currentPage = 0
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

