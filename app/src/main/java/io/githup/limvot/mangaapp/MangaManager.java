package io.githup.limvot.mangaapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static ScriptManager scriptManager;

    private Manga currentManga;
    private Chapter currentChapter;
    private int currentPage;
    ArrayList<Chapter> mangaChapterList;

    MangaManager() {
        scriptManager = ScriptManager.getScriptManager();
    }

    public void setCurrentManga(Manga manga) {
        currentManga = manga;
        scriptManager.getCurrentSource().initManga(currentManga);
    }

    Manga getCurrentManga() {
        return currentManga;
    }

    void setCurrentChapter(Chapter current) {
        currentChapter = current;
    }

    List<Chapter> getMangaChapterList() {
        return scriptManager.getCurrentSource().getMangaChapterList(currentManga);
    }

    // THESE LOOK BACKWARDS
    // But they're not. Or they are. (Because Chapter 1 is at the 'end' of the list.)
    // Make this script decidable later
    public void previousChapter() {
        Log.i("PREVOUS CHAPTER", Integer.toString(currentChapter.getNum()));
        if (currentChapter.getNum() < mangaChapterList.size()-1)
            currentChapter = mangaChapterList.get(currentChapter.getNum()+1);
    }

    public void nextChapter() {
        Log.i("NEXT CHAPTER", Integer.toString(currentChapter.getNum()));
        if (currentChapter.getNum() > 0)
            currentChapter = mangaChapterList.get(currentChapter.getNum()-1);
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

