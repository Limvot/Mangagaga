package io.githup.limvot.mangaapp;

import android.util.Log;

/**
 * Created by nathan on 8/20/14.
 */
public class APIObject {
    private static APIObject thisObj;
    private static Utilities downloader;
    public APIObject() {
        downloader = new Utilities();
    }
    public static APIObject getAPIObject() {
        if (thisObj == null)
            thisObj = new APIObject();
        return thisObj;
    }

    public static void note(String theNote) {
        Log.i("Noting the thing", theNote);
    }

    public static String download(String filePath) {
        Log.i("Downloading", filePath);
        downloader.SetSource(filePath);
        return downloader.Download();
    }

    public static String readFile(String absolutePath) {
        Log.i("Reader: ", "Path is: " + absolutePath);
        return Utilities.readFile(absolutePath);
    }
    public static String slice(String toSlice, int a, int b) {
        Log.i("Slicer: ", toSlice);
        return toSlice.substring(a,b);
    }


}
