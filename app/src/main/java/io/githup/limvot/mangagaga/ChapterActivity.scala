package io.githup.limvot.mangagaga;

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView

import android.app.ActionBar
import android.os.Build;

import org.scaloid.common._

class ChapterActivity extends SActivity {

    private var description : TextView = null
    private var favoriteBox : CheckBox = null
    private var chapterListView : ListView = null


    override def onCreate(savedInstanceState : Bundle) = {
        super.onCreate(savedInstanceState)
        contentView = new SVerticalLayout() {
            favoriteBox = SCheckBox("Favorite")
            description = STextView()
            chapterListView = SListView()
        }

        val currentManga : Manga = MangaManager.getCurrentManga()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
          getActionBar().setTitle(currentManga.toString())

        description.setText(currentManga.getDescription())
        favoriteBox.setChecked(MangaManager.isFavorite(currentManga))
        favoriteBox.setOnClickListener(new View.OnClickListener() {
            override def onClick(view : View) {
                if (favoriteBox.isChecked())
                    MangaManager.addFavorite(currentManga)
                else
                    MangaManager.removeFavorite(currentManga)
            }
        });

        var chapterAdapter : ChapterListAdapter = new ChapterListAdapter(this, MangaManager.getMangaChapterList())

        chapterListView.setAdapter(chapterAdapter)
        chapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            override def onItemClick(adapterView : AdapterView[_], view : View, i : Int, l : Long) {
                Log.i("onItemClick", chapterListView.getItemAtPosition(i).toString())
                MangaManager.setCurrentChapter(chapterListView.getItemAtPosition(i).asInstanceOf[Chapter])
                MangaManager.setCurrentPageNum(0)
                startActivity[ImageViewerActivity]
            }
        });
    }


   /* override def onCreateOptionsMenu(Menu menu) : Boolean = {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chapter, menu);
        return true;
    }

    override def onOptionsItemSelected(MenuItem item) : Boolean = {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        var id : Int = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
