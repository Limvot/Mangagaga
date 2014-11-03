package io.githup.limvot.mangaapp;

/**
 * Created by marcus on 8/19/14.
 * modified by pratik on 8/24/14
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.util.ByteArrayBuffer;
import org.luaj.vm2.LuaTable;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.Date;

public class Utilities {

    static Gson gson;

    private static class DownloadSource extends AsyncTask<String,Void,String>
    {
        public void onPostExecute(String s) {}
        public String doInBackground(String... sl)
        {
            return DownloadSource(sl[0]);
        }
    }
    private static class CheckUpdates extends AsyncTask<Context,Void,Void>
    {
        public void onPostExecute() {}
        public Void doInBackground(Context... ctx)
        {
            checkForUpdatesAsync(ctx[0]);
            return null;
        }
    }
    public Utilities()
    {
    }

    public static Gson getGson() {
        if (gson == null)
            gson = new GsonBuilder()
                    .registerTypeAdapter(LuaTable.class, new LuaTableSerializer())
                    .setPrettyPrinting()
                    .create();
        return gson;
    }

    public static void checkForUpdates(Context ctx) {
        new CheckUpdates().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ctx);
    }
    public static void checkForUpdatesAsync(Context ctx) {
        String updateURL = "http://nathanbraswell.com/~nathan/Mangagaga/apk/app-debug.apk";
        Date siteApkDate = getModifiedTime(updateURL);
        Log.i("Does this need updates?", siteApkDate.toString());
        SettingsManager settingsManager = SettingsManager.getSettingsManager();
        if (siteApkDate.after(settingsManager.getApkDate())) {
            File downloadedApk = new File(download(updateURL));
            Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(downloadedApk),
                            "application/vnd.android.package-archive");
            ctx.startActivity(promptInstall);
            settingsManager.setApkDate(siteApkDate);
        }
    }

    public static Date getModifiedTime(String source) {
        Log.i("Url to grab modified time from", source);
        long modifiedTime;
        try {
            URL url = new URL(source);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            modifiedTime = connection.getLastModified();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("getModifiedTime", e.toString());
            modifiedTime = 0;
        }
        if (modifiedTime == 0)
            Log.e("Could not get modified time", "Nope couldn't get it");
        return new Date(modifiedTime);
    }

    public static String download(String source)
    {
        try {
            return new DownloadSource().execute(source).get();
        }
        catch (ExecutionException e)
        {
            Log.e("Download","Error with Async Source Download!!!");
                    return "Error Async!";
        }
        catch(InterruptedException e)
        {
            Log.e("Download","Interrupted Exception!!!");
            return "Error Interrupted!";
        }
    }


    public static String DownloadSource(String source)
    {
        String filename = "";
        String resultingPath = "";
        URL sourceSite;
        if(!source.isEmpty())
        {
            Log.d("DownloadSource", source);
            try {
                sourceSite = new URL(source);
                URLConnection urlcon = sourceSite.openConnection();

                String extension;
                if (source.contains(".jpg")) {
                    extension = ".jpg";
                } else if (source.contains(".jpeg")) {
                    extension = ".jpeg";
                } else if (source.contains(".png")) {
                    extension = ".png";
                } else if (source.contains(".zip")) {
                    extension = ".zip";
                } else if (source.contains(".apk")) {
                    extension = ".apk";
                } else {
                    extension = ".html";
                }
                Log.i("DONLSDFING", source);
                int extensionIndex = source.lastIndexOf(extension);
                if (extensionIndex != -1)
                    filename = source.substring((source.lastIndexOf('/') + 1), extensionIndex + extension.length());
                else
                    filename = source.substring((source.lastIndexOf('/') + 1)) + extension;

                String dest = Environment.getExternalStorageDirectory() + "/Mangagaga/Cache/";

                if (filename.contains("?")) {
                    filename = filename.replace('?', '_');
                    Log.d("DownloadSource", "Removed '?' and renamed file to: " + filename);
                }

                resultingPath = dest + filename;
                File file = new File(dest, filename);
                Log.i("DownloadSource", file.toString());
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Log.e("DownloadSource", "Couldn't make file!!!");
                    Log.e("DownloadSource", e.toString());
                }

                if (extension.equals(".jpeg") || extension.equals(".jpg") || extension.equals(".png")
                        || extension.equals(".zip") || extension.equals(".apk")) {

                    Log.d("DownloadSource", "Making Image fos");
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = urlcon.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ByteArrayBuffer buffer = new ByteArrayBuffer(500);

                    int chunk = 0;

                    while ((chunk = bis.read()) != -1) {
                        buffer.append((byte) chunk);
                    }
                    Log.d("DownloadSource", "Writing Image");
                    fos.write(buffer.toByteArray());
                    fos.flush();
                    fos.close();
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(sourceSite.openStream()));
                    FileWriter fw = new FileWriter(resultingPath);
                    BufferedWriter writer = new BufferedWriter(fw);
                    String input;
                    while ((input = reader.readLine()) != null) {
                        writer.write(input);
                    }
                    reader.close();
                    writer.close();
                }

            }
            catch (MalformedURLException e)
            {
                Log.e("DownloadSource", "Error opening page!");
                return "Error 1";
            }
            catch(IOException e)
            {
                Log.e("DownloadSource", "Error With Reader/Writer!");
                Log.e("DownloadSource", e.toString());
                return "Error 2";

            }
        }

        return resultingPath;
    }

    public static String readFile(String absolutePath) throws IOException {
        return readFile(new FileInputStream(absolutePath));
    }

    public static String readFile(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine())
            sb.append(line).append("\n");
        return sb.toString();
    }

    public static void copyStreams(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int readBytes;
        while( (readBytes = in.read(buffer)) != -1)
            out.write(buffer, 0, readBytes);
    }

    public static void clearCache()
    {
        File cache = new File(Environment.getExternalStorageDirectory()+"/Mangagaga/Cache/");
        clearFolder(cache);
    }

    public static void clearFolder(File folder) {

        if(!folder.exists())
        {
            Log.e("clear saved", "Error clearing saved, directory doesn't exist");
        }else {
            for (String child : folder.list()) {
                File childFile = new File(folder, child);
                if (childFile.isDirectory())
                    clearFolder(childFile);
                childFile.delete();
            }
        }
    }
}
