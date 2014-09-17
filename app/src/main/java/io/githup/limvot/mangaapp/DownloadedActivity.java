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


public class DownloadedActivity extends Activity {
    private ListView mangaListView;
    private MangaManager mangaManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded);

        mangaManager = MangaManager.getMangaManager();
        mangaListView = (ListView) findViewById(R.id.downloadedMangaListView);

        ArrayAdapter<Manga> mangaArrayAdapter = new ArrayAdapter<Manga>(this, android.R.layout.simple_list_item_1,
                mangaManager.getSavedManga());

        mangaListView.setAdapter(mangaArrayAdapter);

        mangaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("onItemClick", mangaListView.getItemAtPosition(i).toString());
                Intent chapterView = new Intent(DownloadedActivity.this, ChapterActivity.class);
                //ScriptManager.getScriptManager().setCurrentSource(sourceNumber);
                MangaManager.getMangaManager().setCurrentManga((Manga) mangaListView.getItemAtPosition(i));
                startActivity(chapterView);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.downloaded, menu);
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
