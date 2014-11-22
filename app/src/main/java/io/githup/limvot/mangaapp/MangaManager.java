package io.githup.limvot.mangaapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedWriter;
import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

import org.luaj.vm2.LuaTable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nathan on 8/25/14.
 */
public class MangaManager {
    public static MangaManager mangaManager;
    public static void initMangaManager(Context ctx) {
        if (mangaManager == null)
            mangaManager = new MangaManager();
        mangaManager.setContext(ctx);
    }
    public static MangaManager getMangaManager() {
        return mangaManager;
    }

    private  Context mainContext;

    private Gson gson;

    private ArrayList<Chapter> chapterHistory;
    private ArrayList<Manga> favoriteManga;

    private boolean isOffline;
    private Manga currentManga;
    private Chapter currentChapter;
    private int currentPage;

    MangaManager() {
        gson = Utilities.getGson();
        chapterHistory = loadHistory();
        favoriteManga = loadFavorites();
    }

    private void setContext(Context ctx) {
        mainContext = ctx;
    }

    public void readingOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    private ArrayList<Chapter> loadHistory() {
        try {
            return gson.fromJson(Utilities.readFile(Environment.getExternalStorageDirectory() + "/Mangagaga/History.json"), new TypeToken<ArrayList<Chapter>>() {}.getType());
        } catch (Exception e) {
            return new ArrayList<Chapter>();
        }
    }

    private class HistorySaver extends AsyncTask<List<Chapter>, Void, Void> {
        @Override
        protected Void doInBackground(List<Chapter>... toDownloadAr) {
            Log.i("SAVE_HISTORY", "BEGINNING");
            try {
                File history = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "History.json");
                history.createNewFile();
                FileWriter fw = new FileWriter(history.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(gson.toJson(toDownloadAr[0]));
                bw.close();
                Log.i("SAVE_HISTORY", "SAVED");
            } catch (Exception e) {
                Log.i("SAVE_HISTORY", "Problem");
            }
            return null;
        }
    }

    private void saveHistory() {
        new HistorySaver().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, chapterHistory);
    }

    public void clearHistory() {
        chapterHistory.clear();
        saveHistory();
    }

    private void saveFavorites() {
        Log.i("SAVE_FAVORITES", "BEGINNING");
        try {
            File favorites = new File(Environment.getExternalStorageDirectory() + "/Mangagaga", "Favorites.json");
            favorites.createNewFile();
            FileWriter fw = new FileWriter(favorites.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(gson.toJson(favoriteManga));
            bw.close();
            Log.i("SAVE_FAVORITES", "SAVED");
        } catch (Exception e) {
            Log.i("SAVE_FAVORITES", "Problem");
        }
    }

    private ArrayList<Manga> loadFavorites() {
        try {
            return gson.fromJson(Utilities.readFile(Environment.getExternalStorageDirectory() + "/Mangagaga/Favorites.json"), new TypeToken<ArrayList<Manga>>() {}.getType());
        } catch (Exception e) {
            return new ArrayList<Manga>();
        }
    }

    public ArrayList<Manga> getFavoriteList() {
        return favoriteManga;
    }

    public boolean isFavorite(Manga manga) {
        return favoriteManga.contains(manga);
    }

    public void addFavorite(Manga manga) {
        favoriteManga.add(manga);
        saveFavorites();
    }

    public void removeFavorite(Manga manga) {
        favoriteManga.remove(manga);
        saveFavorites();
    }

    public void clearFavorites() {
        favoriteManga.clear();
        saveFavorites();
    }

    public boolean isSaved(Chapter chapter) {
        /*
        File savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/");
        File mangaDir = new File(savedDir, chapter.getParentManga().getTitle());
        if (mangaDir == null || mangaDir.list() == null)
            return false;
        for (String chapterDirStr :  mangaDir.list()) {
            File chapterDir = new File(mangaDir, chapterDirStr);
            if (chapterDir.isFile())
                continue;
            File chapterFile = new File(chapterDir, "chapter.json");
            try {
                Chapter possibleChapter = gson.fromJson(Utilities.readFile(chapterFile.getAbsolutePath()), Chapter.class);
                if (chapter.equals(possibleChapter))
                    return true;
            } catch (Exception e) {
                Log.e("isSaved", e.toString());
            }
        }
*/
        return false;
    }
    public void addSaved(Chapter chapter) {
        if (isSaved(chapter))
            return;

        new ChapterDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Arrays.asList(new Chapter[]{chapter}));
    }

    public void removeSaved(Chapter chapter) {
        //
    }

    public List<Manga> getSavedManga() {
        ArrayList<Manga> list = new ArrayList<Manga>();
        File savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/");
        for (String dir : savedDir.list()) {
            Log.i("Get Saved Manga", dir);
            File mangaDir = new File(savedDir, dir);
            File mangaFile = new File(mangaDir, "manga.json");
            try {
                Manga manga = gson.fromJson(Utilities.readFile(mangaFile.getAbsolutePath()), Manga.class);
                list.add(manga);
            } catch (Exception e) {
                Log.i("GetSaved", "Exception");
            }
        }
        return list;
    }
    public void clearSaved() {
        Log.i("MANGA_MANAGER", "Clearing Saved!");
        File downloaded = new File(Environment.getExternalStorageDirectory()+"/Mangagaga/Downloaded/");
        Utilities.clearFolder(downloaded);
    }

    private class ChapterDownloader extends AsyncTask<List<Chapter>, Void, Void> {
        @Override
        protected Void doInBackground(List<Chapter>... toDownloadAr) {
            List<Chapter> toDownload = toDownloadAr[0];
            // Notification
            Context context = mainContext;
            int notificationID = 0;
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Downloading Chapter")
                    .setContentText("Downloading " + toDownload.size() + " chapters...");
            Intent resultIntent = new Intent(context, DownloadedActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(DownloadedActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationID, builder.build());



            for (Chapter chapter : toDownload) {
                Manga parentManga = chapter.getParentManga();

                File savedDir = new File(Environment.getExternalStorageDirectory() + "/Mangagaga/Downloaded/");
                File mangaDir = new File(savedDir, parentManga.getTitle());
                File chapterDir = new File(mangaDir, Integer.toString(chapter.getNum()));
                mangaDir.mkdir();
                chapterDir.mkdir();

                // Save the Manga object and the Chapter object
                Log.i("SAVE_CHAPTER", "SAVING MANGA");
                try {
                    File mangaJson = new File(mangaDir, "manga.json");
                    if (!mangaJson.exists()) {
                        mangaJson.createNewFile();
                        FileWriter fw = new FileWriter(mangaJson.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(gson.toJson(parentManga));
                        bw.close();
                        Log.i("SAVE_CHAPTER_MANgA", "new SAVED");

                    } else {
                        Log.i("SAVE_CHAPTER_MANgA", "already existed");
                    }

                    File chapterJson = new File(chapterDir, "chapter.json");
                    if (!chapterJson.exists()) {
                        chapterJson.createNewFile();
                        FileWriter fw = new FileWriter(chapterJson.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(gson.toJson(chapter));
                        bw.close();
                        Log.i("SAVE_CHAPTER_Chapter", "new SAVED");

                    } else {
                        Log.i("SAVE_CHAPTER_Chapter", "already existed");
                    }


                    Log.i("SAVE_CHAPTER_MANgA_Chapter", "SAVED success");
                } catch (Exception e) {
                    Log.i("SAVE_CHAPTER_MANgA_chapter", "problem/exception");
                }

                for (int i = 0; i < getNumPages(parentManga, chapter); i++) {
                    builder.setContentText("Downloading page " + (i+1) + ".");
                    notificationManager.notify(notificationID, builder.build());
                    String fromFile = getCurrentPage(parentManga, chapter, i);
                    try {
                        FileInputStream is = new FileInputStream(fromFile);
                        String filename = Integer.toString(i) + fromFile.substring(fromFile.lastIndexOf("."));
                        FileOutputStream os = new FileOutputStream(chapterDir.getAbsolutePath() + "/" + filename);
                        Utilities.copyStreams(is, os);
                    } catch (Exception e) {
                        Log.e("Save Chapter ERROR", fromFile);
                    }
                }
            }

            builder.setContentTitle("Done!");
            builder.setContentText("Downloaded " + toDownload.size() + " chapters.");
            notificationManager.notify(notificationID, builder.build());
            return null;
        }
    }

    public void setCurrentManga(Manga manga) {
        ScriptManager.getCurrentSource().initManga(manga);
        currentManga = manga;
    }

    Manga getCurrentManga() {
        return currentManga;
    }

    void setCurrentChapter(Chapter current) {
        currentChapter = current;
        chapterHistory.add(0, current);
        for (int i = SettingsManager.getHistorySize(); i < chapterHistory.size(); i++)
            chapterHistory.remove(i);
        saveHistory();
    }

    List<Chapter> getMangaChapterList() {
        return ScriptManager.getCurrentSource().getMangaChapterList(currentManga);
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

    int getNumPages(Manga manga, Chapter chapter) {
        return ScriptManager.getCurrentSource().getNumPages(manga, chapter);
    }

    int getNumPages() {
        return getNumPages(currentManga, currentChapter);
    }

    void setCurrentPageNum(int page) {
        currentPage = page;
    }

    int getCurrentPageNum() {
        return currentPage;
    }

    String getCurrentPage() {
        return getCurrentPage(currentManga, currentChapter, currentPage);
    }

    String getCurrentPage(Manga manga, Chapter chapter, int page) {
        return ScriptManager.getCurrentSource().downloadPage(manga, chapter, page);
    }
}

