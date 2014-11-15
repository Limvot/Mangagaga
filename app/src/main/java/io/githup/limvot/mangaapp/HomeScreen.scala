package io.githup.limvot.mangaapp
import org.scaloid.common._

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import java.io.StringReader;
import com.google.gson.Gson;
import java.io.File;

class HomeScreen extends SActivity {
  implicit val tag = LoggerTag("Scala APIObject")

  override def onCreate(savedInstanceState:Bundle) {
    super.onCreate(savedInstanceState)
    contentView = new SVerticalLayout() {
      SButton("Browse Sources").onClick(startActivity[SourceActivity])
        SButton("Favroites").onClick(startActivity[FavoritesActivity])
        SButton("History").onClick(startActivity[HistoryActivity])
        SButton("Downloaded").onClick(startActivity[DownloadedActivity])
        SButton("Settings").onClick(startActivity[SettingsActivity])
    }

    var mainFolder = new File(Environment.getExternalStorageDirectory, "Mangagaga")
    try {
      if (!mainFolder.exists)
        mainFolder.mkdir()
      for (folderName <-  List("Downloaded", "Scripts", "Cache")) {
        val folder = new File(mainFolder, folderName)
        if (!folder.exists)
          folder.mkdir()
      }
    } catch {
      case e:Exception => info("OnCreate:" + e.toString)
    }
    // Set up the script manager
    ScriptManager.createScriptManager(this)
    // Init the MangaManager with the current context
    // This has to be done after script manager has been set up
    MangaManager.initMangaManager(this)

    //Have Utilites check for updates
    Utilities.checkForUpdates(this)
  }

  //@Override
  //public boolean onCreateOptionsMenu(Menu menu) {
  //// Inflate the menu; this adds items to the action bar if it is present.
  //getMenuInflater().inflate(R.menu.home_screen, menu);
  //return true;
  //}

  //@Override
  //public boolean onOptionsItemSelected(MenuItem item) {
  //// Handle action bar item clicks here. The action bar will
  //// automatically handle clicks on the Home/Up button, so long
  //// as you specify a parent activity in AndroidManifest.xml.
  //int id = item.getItemId();
  //if (id == R.id.action_settings) {
  //return true;
  //}
  //return super.onOptionsItemSelected(item);
  //}
}
