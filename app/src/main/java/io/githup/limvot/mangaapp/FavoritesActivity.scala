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
import android.widget.ListView;

import org.scaloid.common._
import scala.collection.JavaConversions._
import collection.mutable.Buffer


class FavoritesActivity extends SActivity {
    implicit val tag = LoggerTag("Scala Favorites Activity")
    var favoritesAdapter : ArrayAdapter[Manga] = null
    var listFavorites : SListView = null

    override def onCreate(savedInstanceState:Bundle) {
        super.onCreate(savedInstanceState);
        contentView = new SVerticalLayout() {
            var favorites_title = STextView("Favorites")
            listFavorites = SListView()
            
        }

        var buff : Buffer[Manga] =  MangaManager.getMangaManager().getFavoriteList()
        favoritesAdapter = new SArrayAdapter(buff.toArray)
        listFavorites.setAdapter(favoritesAdapter)

        listFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            override def onItemClick(adapterView : AdapterView[_], view : View, i : Int, l : Long) {
                info(listFavorites.getItemAtPosition(i).toString())
                val manga = listFavorites.getItemAtPosition(i).asInstanceOf[Manga]
                ScriptManager.setCurrentSource(manga.getSourceNumber())
                MangaManager.getMangaManager().readingOffline(false)
                MangaManager.getMangaManager().setCurrentManga(manga)
                startActivity[ChapterActivity]
            }
        })
    }

    override def onResume() {
        super.onResume()
        // Always refresh list, just in case
        favoritesAdapter.notifyDataSetChanged()
    }
    /*
    override def onCreateOptionsMenu(Menu menu) : Boolean = {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorites, menu);
        return true;
    }

    override def onOptionsItemSelected(MenuItem item) : Boolean = {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    } */
}
