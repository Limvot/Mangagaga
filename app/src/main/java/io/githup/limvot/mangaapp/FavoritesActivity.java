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


public class FavoritesActivity extends Activity {

    ArrayAdapter<Manga> favoritesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        final ListView favorites = (ListView) findViewById(R.id.favoritesListView);
        favoritesAdapter = new ArrayAdapter<Manga>(this, android.R.layout.simple_list_item_1, MangaManager.getMangaManager().getFavoriteList());
        favorites.setAdapter(favoritesAdapter);
        favorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("onItemClick", favorites.getItemAtPosition(i).toString());
                Manga manga = (Manga) favorites.getItemAtPosition(i);
                ScriptManager.getScriptManager().setCurrentSource(manga.getSourceNumber());
                MangaManager.getMangaManager().setCurrentManga(manga);
                startActivity(new Intent(FavoritesActivity.this, ChapterActivity.class));
            }
        });
    }

    protected void onResume() {
        super.onResume();
        // Always refresh list, just in case
        favoritesAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorites, menu);
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
    }
}
