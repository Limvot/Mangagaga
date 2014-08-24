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
import android.widget.TextView;


public class ChapterActivity extends Activity {

    private TextView title;
    private TextView description;
    private ListView chapterListView;

    private Script currentSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        title = (TextView) findViewById(R.id.mangaTitleTextView);
        description = (TextView) findViewById(R.id.mangaDescriptionTextView);

        currentSource = ScriptManager.getScriptManager().getCurrentSource();
        currentSource.initManga();
        title.setText(currentSource.getCurrentManga().toString());
        description.setText(currentSource.getCurrentManga().getDescription());

        chapterListView = (ListView) findViewById(R.id.mangaChapterListView);
        ArrayAdapter<Chapter> arrayAdapter = new ArrayAdapter<Chapter>(this, android.R.layout.simple_list_item_1,
                currentSource.getMangaChapterList());
        chapterListView.setAdapter(arrayAdapter);

        chapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("onItemClick", chapterListView.getItemAtPosition(i).toString());
                currentSource.setCurrentChapter((Chapter) chapterListView.getItemAtPosition(i));
                currentSource.setCurrentPage(0);
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
