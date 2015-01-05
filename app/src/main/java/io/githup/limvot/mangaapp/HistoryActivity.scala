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
            SButton("Clear History").onClick(clearHistCallback())
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
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
