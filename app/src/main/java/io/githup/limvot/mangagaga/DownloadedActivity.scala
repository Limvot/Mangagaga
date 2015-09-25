package io.githup.limvot.mangagaga;

import org.scaloid.common._

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

class DownloadedActivity extends SActivity {
  onCreate {
    contentView = new SVerticalLayout {
        val mangaListView = SListView().<<.wrap.>>
        

        mangaListView.setAdapter(new SArrayAdapter(MangaManager.getSavedManga().toArray))
        mangaListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
          override def onItemClick(adapterView: AdapterView[_], view: View, i: Int, l: Long) {
            Log.i("onItemClick", mangaListView.getItemAtPosition(i).toString())
            MangaManager.readingOffline(true)
            MangaManager.setCurrentManga(mangaListView.getItemAtPosition(i).asInstanceOf[Manga])
            startActivity[ChapterActivity]
          }
        })
    }
  }
}