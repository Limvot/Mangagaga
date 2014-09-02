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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;


public class ChapterActivity extends Activity {

    private TextView title;
    private TextView description;
    private CheckBox favoriteBox;
    private ListView chapterListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        title = (TextView) findViewById(R.id.mangaTitleTextView);
        description = (TextView) findViewById(R.id.mangaDescriptionTextView);
        favoriteBox = (CheckBox) findViewById(R.id.favoriteCheckBox);

        final MangaManager mangaManager = MangaManager.getMangaManager();
        final Manga currentManga = mangaManager.getCurrentManga();

        title.setText(currentManga.toString());
        description.setText(currentManga.getDescription());
        favoriteBox.setChecked(mangaManager.isFavorite(currentManga));
        favoriteBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favoriteBox.isChecked())
                    mangaManager.addFavorite(currentManga);
                else
                    mangaManager.removeFavorite(currentManga);
            }
        });

        chapterListView = (ListView) findViewById(R.id.mangaChapterListView);
        ArrayAdapter<Chapter> arrayAdapter = new ArrayAdapter<Chapter>(this, android.R.layout.simple_list_item_1,
                mangaManager.getMangaChapterList());
        chapterListView.setAdapter(arrayAdapter);

        chapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("onItemClick", chapterListView.getItemAtPosition(i).toString());
                MangaManager.getMangaManager().setCurrentChapter((Chapter) chapterListView.getItemAtPosition(i));
                MangaManager.getMangaManager().setCurrentPageNum(0);
                Intent chapterView = new Intent(ChapterActivity.this, ImageViewerActivity.class);
                startActivity(chapterView);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chapter, menu);
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
