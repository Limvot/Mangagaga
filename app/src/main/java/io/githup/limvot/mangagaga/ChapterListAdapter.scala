package io.githup.limvot.mangagaga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import org.scaloid.common._
import scala.collection.JavaConversions._

/**
 * Created by marcus on 12/21/14.
 */
class ChapterListAdapter(context : Context, values : List[Chapter]) extends ArrayAdapter[Chapter](context, R.layout.chapter_entry, values) {


    override def getView(position : Int, convertView : View, parent : ViewGroup) : View = {
        var inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
        var rowView : View = inflater.inflate(R.layout.chapter_entry, parent, false)
        var chapterText : TextView = rowView.findViewById(R.id.chapterLine).asInstanceOf[TextView]
        val chapter : Chapter = values.get(position).asInstanceOf[Chapter]
        chapterText.setText(chapter.toString())
        val checkBox : CheckBox = rowView.findViewById(R.id.downloadChapterCheckBox).asInstanceOf[CheckBox]
        checkBox.setChecked(MangaManager.isSaved(chapter));
        checkBox.setOnClickListener(new View.OnClickListener() {
            override def onClick(view : View) {
                if (checkBox.isChecked())
                    MangaManager.addSaved(chapter);
                else
                    MangaManager.removeSaved(chapter);
            }
        });
        return rowView;
    }
}
