package io.githup.limvot.mangaapp;

import org.scaloid.common._

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

class DownloadedActivity extends SActivity {
  onCreate {
    contentView = new SRelativeLayout {
      STextView("Downloaded").<<.wrap.centerHorizontal.alignParentTop.>>
      SListView().<<.wrap.alignParentRight.>>
      //SListView().<<.wrap.alignParentRight(true).below(R.+id.downloadedTextView).>>
    }
  }
}
