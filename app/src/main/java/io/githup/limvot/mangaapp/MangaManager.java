package io.githup.limvot.mangaapp;

import android.util.Log;
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

    private ScriptManager scriptManager;
    private ArrayList<Chapter> chapterHistory;
    private Manga currentManga;
    private Chapter currentChapter;
    private int currentPage;

    MangaManager() {
        scriptManager = ScriptManager.getScriptManager();
        chapterHistory = new ArrayList<Chapter>();
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
        chapterHistory.add(0, current);
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

