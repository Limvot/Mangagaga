package io.githup.limvot.mangagaga
import org.scaloid.common._

import java.util.List
import java.util.Locale

import android.app.Activity

import android.app.ActionBar
import android.os.Build;

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Intent
//import android.support.v13.app.FragmentPagerAdapter
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import java.util.Arrays
import scala.collection.JavaConversions._

// ActionBar tabs based off of
// https://github.com/pocorall/scaloid-apidemos/blob/master/src/main/java/com/example/android/apis/app/ActionBarTabs.scala
class SourceActivity extends SActivity {
  var frameLayoutId:Int = 0
  var fLayout: SFrameLayout = null
  onCreate {
    contentView = new SVerticalLayout {
      fLayout = new SFrameLayout().<<(MATCH_PARENT, 0 dip).Weight(1).>>
      frameLayoutId = fLayout.uniqueId
      this += fLayout
    }

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) { 
      val actionBar = getActionBar()
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
      for (i <- 0 until ScriptManager.numSources)
        actionBar.addTab(actionBar.newTab.setText(ScriptManager.getScript(i).getName)
          .setTabListener(new TabListener(new TabContentFragment(i))))
    } else {
      fLayout += makeInner(0, this)
    }
  }

      private class TabListener extends ActionBar.TabListener {
        private var mFrag: TabContentFragment = null
        def this(frag: TabContentFragment) {
          this()
          mFrag = frag
        }
        def onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) { ft.add(frameLayoutId, mFrag, mFrag.getSourceNumber.toString) }
        def onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) { ft.remove(mFrag) }
        def onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) { toast("Reselecting does nothing, mmmk?") }
      }

      private class TabContentFragment(sourceNumber: Int) extends Fragment {
        def getSourceNumber = sourceNumber
        override def onCreateView(inf: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
          makeInner(sourceNumber, getActivity())
        }
      }
       def makeInner(sourceNumber: Int, actvity:Activity) = {
          new SRelativeLayout {
            val linearLayout0 = new SLinearLayout {
              val catagoriesText = STextView("List By =>").<<.wrap.>>
              val mangaListTypeSpinner = SSpinner().<<.wrap.>>
              catagoriesText.<<.weight = 1.0f
              mangaListTypeSpinner.<<.weight = 1.0f

              val mangaListTypes = new ArrayAdapter[String](actvity, android.R.layout.simple_list_item_1,
                ScriptManager.getScript(sourceNumber).getMangaListTypes())

              mangaListTypeSpinner.setAdapter(mangaListTypes)
              mangaListTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                override def onItemSelected(adapterView: AdapterView[_], view: View, i: Int, l: Long) {
                  val selected = adapterView.getItemAtPosition(i).toString()
                  Log.i("New selected:", selected)
                  // Set the new type and get its first page
                  ScriptManager.getCurrentSource().setMangaListType(selected);
                  arrayAdapter.clear()
                  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
                    arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListPage1())
                  else
                    for (item <- ScriptManager.getScript(sourceNumber).getMangaListPage1()) arrayAdapter.add(item);
                }
                override def onNothingSelected(adapterView: AdapterView[_]) { Log.i("Nothing selected:", "Nothin!") }
              })

            }

            this += linearLayout0
            linearLayout0.<<.alignParentTop  

            val linearLayout1 = new SLinearLayout {

              val previousButton = SButton("Previous").<<.wrap.>>
              val nextButton = SButton("  Next  ").<<.wrap.>>
              nextButton.<<.weight = 1.0f
              previousButton.<<.weight = 1.0f

              previousButton.setOnClickListener(new View.OnClickListener() {
                override def onClick(view: View) {
                  arrayAdapter.clear()
                  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
                    arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListPreviousPage())
                  else
                    for (item <- ScriptManager.getScript(sourceNumber).getMangaListPreviousPage()) arrayAdapter.add(item);
                }
              })

              nextButton.setOnClickListener(new View.OnClickListener() {
                override def onClick(view: View) {
                  arrayAdapter.clear()
                  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
                    arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListNextPage())
                  else
                    for (item <- ScriptManager.getScript(sourceNumber).getMangaListNextPage()) arrayAdapter.add(item);
                }
              })
            }

            this += linearLayout1
            linearLayout1.<<.alignParentBottom
            val mangaListView = SListView().<<.below(linearLayout0).above(linearLayout1)>>

            val arrayAdapter = new ArrayAdapter[Manga](actvity, android.R.layout.simple_list_item_1,
              ScriptManager.getScript(sourceNumber).getMangaListPage1())
            mangaListView.setAdapter(arrayAdapter)

            mangaListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
              override def onItemClick(adapterView: AdapterView[_], view: View, i: Int, l: Long) {
                Log.i("onItemClick", mangaListView.getItemAtPosition(i).toString())
                ScriptManager.setCurrentSource(sourceNumber)
                MangaManager.readingOffline(false)
                MangaManager.setCurrentManga(mangaListView.getItemAtPosition(i).asInstanceOf[Manga])
                startActivity(new Intent(actvity, classOf[ChapterActivity]))
              }
            })
          }
      }
}