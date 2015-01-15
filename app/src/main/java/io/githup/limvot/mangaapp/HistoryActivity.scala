package io.githup.limvot.mangaapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.scaloid.common._
import scala.collection.JavaConversions._
import collection.mutable.Buffer


class HistoryActivity extends SActivity {
    implicit val tag = LoggerTag("Scala History Activity")
    var historyListView : SListView = null
    var adapter : ArrayAdapter[Chapter] = null

    override def onCreate(savedInstanceState:Bundle) {
        super.onCreate(savedInstanceState)
        
        contentView = new SVerticalLayout() {
            historyListView = SListView()
        }


        var buff : Buffer[Chapter] = MangaManager.getChapterHistoryList()
        adapter = new SArrayAdapter(buff.toArray)
        historyListView.setAdapter(adapter)

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            override def onItemClick(adapterView : AdapterView[_], view : View, i : Int, l : Long) {
                info(historyListView.getItemAtPosition(i).toString())
                MangaManager.readingOffline(false)
                var chapter : Chapter = historyListView.getItemAtPosition(i).asInstanceOf[Chapter]
                MangaManager.setCurrentManga(chapter.getParentManga())
                MangaManager.setCurrentChapter(chapter)
                MangaManager.setCurrentPageNum(0)
                startActivity[ImageViewerActivity]
            }
        })
    }

    def clearHistCallback() {
        MangaManager.clearHistory()
        adapter.notifyDataSetChanged()
    }

    override def onResume() {
        super.onResume();
        // Always refresh history list, just in case
        adapter.notifyDataSetChanged();
    }
    
}
