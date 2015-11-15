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
 * Created by marcus on 11/14/15.
 */
class SimpleListAdapter[T](context : Context, values : List[T]) extends ArrayAdapter[T](context, R.layout.simple_entry, values) {


    override def getView(position : Int, convertView : View, parent : ViewGroup) : View = {
        var inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
        var rowView : View = inflater.inflate(R.layout.simple_entry, parent, false)
        var mangaText : TextView = rowView.findViewById(R.id.simpleEntry_line).asInstanceOf[TextView]
        val manga : T = values.get(position).asInstanceOf[T]
        mangaText.setText(manga.toString())
        
        return rowView;
    }
}
