package io.githup.limvot.mangagaga;

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
    //var favoritesAdapter : ArrayAdapter[Manga] = null
    var favoritesAdapter : SimpleListAdapter[Manga] = null
    var listFavorites : SListView = null

    override def onCreate(savedInstanceState:Bundle) {
        super.onCreate(savedInstanceState);
        contentView = new SVerticalLayout() {
            listFavorites = SListView()            
        }
        
        var buff : Buffer[Manga] =  MangaManager.getFavoriteList()
        //favoritesAdapter = new SArrayAdapter(buff)
        favoritesAdapter = new SimpleListAdapter[Manga](this, buff)
        listFavorites.setAdapter(favoritesAdapter)

        listFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            override def onItemClick(adapterView : AdapterView[_], view : View, i : Int, l : Long) {
                info(listFavorites.getItemAtPosition(i).toString())
                val manga = listFavorites.getItemAtPosition(i).asInstanceOf[Manga]
                ScriptManager.setCurrentSource(manga.getSourceNumber())
                MangaManager.readingOffline(false)
                MangaManager.setCurrentManga(manga)
                startActivity[ChapterActivity]
            }
        })
    }

    override def onResume() {
        super.onResume()
        // Always refresh list, just in case
        favoritesAdapter.notifyDataSetChanged()
    }

}
