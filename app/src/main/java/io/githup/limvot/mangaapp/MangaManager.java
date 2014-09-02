package io.githup.limvot.mangaapp;

import android.graphics.Path;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedWriter;
import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

import org.luaj.vm2.LuaTable;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nathan on 8/25/14.
 */
public class MangaManager {
    public static MangaManager mangaManager;
    public static MangaManager getMangaManager() {
        if (mangaManager == null)
            mangaManager = new MangaManager();
        return mangaManager;
    }

    private Gson gson;

    private ScriptManager scriptManager;
    private ArrayList<Chapter> chapterHistory;
    private Manga currentManga;
    private Chapter currentChapter;
    private int currentPage;

    MangaManager() {
        scriptManager = ScriptManager.getScriptManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(LuaTable.class, new LuaTableSerializer())
                .setPrettyPrinting()
                .create();
        chapterHistory = loadHistory();
    }

    private ArrayList<Chapter> loadHistory() {
        try {
            return gson.fromJson(Utilities.readFile(Environment.getExternalStorageDirectory() + "/Mangagaga/History.json"), new TypeToken<ArrayList<Chapter>>() {}.getType());
        } catch (Exception e) {
            return new ArrayList<Chapter>();
        }
    }

    private void saveHistory() {
        Log.i("SAVE_HISTORY", "BEGINNING");
        try {
            File history = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "History.json");
            history.createNewFile();
            FileWriter fw = new FileWriter(history.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(chapterHistory));
            bw.close();
            Log.i("SAVE_HISTORY", "SAVED");
        } catch (Exception e) {
            Log.i("SAVE_HISTORY", "Problem");
        }
    }

    public void setCurrentManga(Manga manga) {
        scriptManager.getCurrentSource().initManga(manga);
        currentManga = manga;
    }

    Manga getCurrentManga() {
        return currentManga;
    }

    void setCurrentChapter(Chapter current) {
        currentChapter = current;
        chapterHistory.add(0, current);
        saveHistory();
    }

    List<Chapter> getMangaChapterList() {
        return scriptManager.getCurrentSource().getMangaChapterList(currentManga);
    }

    List<Chapter> getChapterHistoryList() {
        return chapterHistory;
    }

    // THESE LOOK BACKWARDS
    // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
    // Make this script decidable later
    public boolean previousChapter() {
        List<Chapter> mangaChapterList = getMangaChapterList();
        Log.i("PREVOUS CHAPTER", Integer.toString(currentChapter.getNum()));
        Log.i("PREVIOUS CHAPTER: size", Integer.toString(mangaChapterList.size()-1));

        if (currentChapter.getNum() < mangaChapterList.size()-1)
            setCurrentChapter(mangaChapterList.get(currentChapter.getNum()+1));
        else
            return false;
        return true;
    }

    public void clearHistory() {
        chapterHistory.clear();
        saveHistory();
    }

    public boolean nextChapter() {
        Log.i("NEXT CHAPTER", Integer.toString(currentChapter.getNum()));
        if (currentChapter.getNum() > 0)
            setCurrentChapter(getMangaChapterList().get(currentChapter.getNum()-1));
        else
            return false;
        return true;
    }

    int getNumPages() {
        return scriptManager.getCurrentSource().getNumPages(currentManga, currentChapter);
    }

    void setCurrentPageNum(int page) {
        currentPage = page;
    }

    int getCurrentPageNum() {
        return currentPage;
    }

    String getCurrentPage() {
        return scriptManager.getCurrentSource().downloadPage(currentManga, currentChapter, currentPage);
    }
}

