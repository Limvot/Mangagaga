package io.githup.limvot.mangaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nathan on 9/13/14.
 */
public class ChapterListAdapter extends ArrayAdapter<Chapter> {
    private final Context context;
    private final List<Chapter> values;
    private final MangaManager mangaManager;

    public ChapterListAdapter(Context context, List<Chapter> values) {
        super(context, R.layout.chapter_entry, values);
        this.context = context;
        this.values = values;
        mangaManager = MangaManager.getMangaManager();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.chapter_entry, parent, false);
        TextView chapterText = (TextView) rowView.findViewById(R.id.chapterLine);
        final Chapter chapter = (Chapter) values.get(position);
        chapterText.setText(chapter.toString());
        final CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.downloadChapterCheckBox);
        checkBox.setChecked(mangaManager.isSaved(chapter));
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked())
                    mangaManager.addSaved(chapter);
                else
                    mangaManager.removeSaved(chapter);
            }
        });
        return rowView;
    }
}
