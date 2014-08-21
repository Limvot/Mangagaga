package io.githup.limvot.mangaapp;

import android.util.Log;

import org.luaj.vm2.LuaValue;

/**
 * Created by nathan on 8/20/14.
 */
public class APIObject {
    private static APIObject thisObj;
    private static SourceDownloader downloader;
    public APIObject() {
        downloader = new SourceDownloader();
    }
    public static APIObject getAPIObject() {
        if (thisObj == null)
            thisObj = new APIObject();
        return thisObj;
    }

    public static void note() {
        Log.i("Noting the thing", "Woo the thing");
    }

    public static void download(String filePath) {
        downloader.SetSource(filePath);
        downloader.Download();
    }
}
