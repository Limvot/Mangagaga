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


public class HistoryActivity extends Activity {

    ListView historyListView;
    ArrayAdapter<Chapter> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button clearHistoryButton = (Button)findViewById(R.id.clear_history);
        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MangaManager.getMangaManager().clearHistory();
                adapter.notifyDataSetChanged();
            }
        });

        historyListView = (ListView) findViewById(R.id.chapterHistoryListView);
        adapter = new ArrayAdapter<Chapter>(this, android.R.layout.simple_list_item_1,
                MangaManager.getMangaManager().getChapterHistoryList());
        historyListView.setAdapter(adapter);

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("onItemClick", historyListView.getItemAtPosition(i).toString());
                MangaManager mangaManager = MangaManager.getMangaManager();
                mangaManager.readingOffline(false);
                Chapter chapter = (Chapter) historyListView.getItemAtPosition(i);
                mangaManager.setCurrentManga(chapter.getParentManga());
                mangaManager.setCurrentChapter(chapter);
                mangaManager.setCurrentPageNum(0);
                Intent chapterView = new Intent(HistoryActivity.this, ImageViewerActivity.class);
                startActivity(chapterView);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always refresh history list, just in case
        adapter.notifyDataSetChanged();
    }

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
    }
}
