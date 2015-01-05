package io.githup.limvot.mangaapp
import org.scaloid.common._

import java.util.List
import java.util.Locale

import android.app.Activity
import android.app.ActionBar
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Intent
import android.support.v13.app.FragmentPagerAdapter
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

// ActionBar tabs based off of
// https://github.com/pocorall/scaloid-apidemos/blob/master/src/main/java/com/example/android/apis/app/ActionBarTabs.scala
class SourceActivity extends SActivity {
  var frameLayoutId:Int = 0
  onCreate {
    contentView = new SVerticalLayout {
      lazy val fLayout = new SFrameLayout().<<(MATCH_PARENT, 0 dip).Weight(1).>>
      frameLayoutId = fLayout.uniqueId
      this += fLayout
    }
    getActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    for (i <- 0 until ScriptManager.numSources)
      getActionBar.addTab(getActionBar.newTab.setText(ScriptManager.getScript(i).getName)
        .setTabListener(new TabListener(new TabContentFragment(i))))

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
      new SRelativeLayout {
        val catagoriesText = STextView("Manga Catagories:").<<.wrap.>>
        val mangaListTypeSpinner = SSpinner().<<.wrap.below(catagoriesText).>>
        val listText = STextView("Manga List:").<<.wrap.below(mangaListTypeSpinner).>>
        val previousButton = SButton("Previous").<<.wrap.alignParentBottom.alignParentLeft.>>
        val mangaListView = SListView().<<.wrap.below(listText).above(previousButton).>>
        val nextButton = SButton("Next").<<.wrap.below(mangaListView).rightOf(previousButton).alignParentRight.>>

        val arrayAdapter = new ArrayAdapter[Manga](getActivity(), android.R.layout.simple_list_item_1,
                                                   ScriptManager.getScript(sourceNumber).getMangaListPage1())
        mangaListView.setAdapter(arrayAdapter)

        mangaListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
          override def onItemClick(adapterView: AdapterView[_], view: View, i: Int, l: Long) {
            Log.i("onItemClick", mangaListView.getItemAtPosition(i).toString())
            ScriptManager.setCurrentSource(sourceNumber)
            MangaManager.readingOffline(false)
            MangaManager.setCurrentManga(mangaListView.getItemAtPosition(i).asInstanceOf[Manga])
            startActivity(new Intent(getActivity(), classOf[ChapterActivity]))
          }
        })

        val mangaListTypes = new ArrayAdapter[String](getActivity(), android.R.layout.simple_list_item_1,
                                                      ScriptManager.getScript(sourceNumber).getMangaListTypes())
        mangaListTypeSpinner.setAdapter(mangaListTypes)
        mangaListTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          override def onItemSelected(adapterView: AdapterView[_], view: View, i: Int, l: Long) {
            val selected = adapterView.getItemAtPosition(i).toString()
            Log.i("New selected:", selected)
            // Set the new type and get its first page
            ScriptManager.getCurrentSource().setMangaListType(selected);
            arrayAdapter.clear()
            arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListPage1())
          }
          override def onNothingSelected(adapterView: AdapterView[_]) { Log.i("Nothing selected:", "Nothin!") }
        })

        previousButton.setOnClickListener(new View.OnClickListener() {
          override def onClick(view: View) {
            arrayAdapter.clear()
            arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListPreviousPage())
          }
        })

        nextButton.setOnClickListener(new View.OnClickListener() {
          override def onClick(view: View) {
            arrayAdapter.clear()
            arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListNextPage())
          }
        })
      }
    }
  }
}
