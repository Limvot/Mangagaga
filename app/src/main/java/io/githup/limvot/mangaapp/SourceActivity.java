package io.githup.limvot.mangaapp;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Arrays;


public class SourceActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.source, menu);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return ScriptManager.numSources();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ScriptManager.getScript(position).getName();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            sectionNumber--; // Not zero indexed for some bizarre reason
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_source, container, false);

            TextView title = (TextView) rootView.findViewById(R.id.testSectionText);
            final int sourceNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            title.setText("Manga List");

            final Spinner mangaListTypeSpinner = (Spinner) rootView.findViewById(R.id.manga_list_type_spinner);
            final ListView mangaListView = (ListView) rootView.findViewById(R.id.mangaListView);
            final Button previousButton = (Button) rootView.findViewById(R.id.previousChapterPageButton);
            final Button nextButton = (Button) rootView.findViewById(R.id.nextChapterPageButton);

            final ArrayAdapter<Manga> arrayAdapter = new ArrayAdapter<Manga>(getActivity(), android.R.layout.simple_list_item_1,
                    ScriptManager.getScript(sourceNumber).getMangaListPage1());
            mangaListView.setAdapter(arrayAdapter);

            mangaListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.i("onItemClick", mangaListView.getItemAtPosition(i).toString());
                    Intent chapterView = new Intent(getActivity(), ChapterActivity.class);
                    ScriptManager.setCurrentSource(sourceNumber);
                    MangaManager.readingOffline(false);
                    MangaManager.setCurrentManga((Manga) mangaListView.getItemAtPosition(i));
                    startActivity(chapterView);
                }
            });

            final ArrayAdapter<String> mangaListTypes = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                    ScriptManager.getScript(sourceNumber).getMangaListTypes());
            mangaListTypeSpinner.setAdapter(mangaListTypes);
            mangaListTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String selected = adapterView.getItemAtPosition(i).toString();
                    Log.i("New selected:", selected);
                    // Set the new type and get its first page
                    ScriptManager.getCurrentSource().setMangaListType(selected);
                    arrayAdapter.clear();
                    arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListPage1());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.i("Nothing selected:", "Nothin!");
                }
            });

            previousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    arrayAdapter.clear();
                    arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListPreviousPage());
                }
            });

            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    arrayAdapter.clear();
                    arrayAdapter.addAll(ScriptManager.getScript(sourceNumber).getMangaListNextPage());
                }
            });

            return rootView;
        }
    }

}
