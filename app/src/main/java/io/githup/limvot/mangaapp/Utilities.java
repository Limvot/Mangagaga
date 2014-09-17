package io.githup.limvot.mangaapp;

/**
 * Created by marcus on 8/19/14.
 * modified by pratik on 8/24/14
 */

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;


public class Utilities {

    private static class DownloadSource extends AsyncTask<String,Void,String>
    {

        public void onPostExecute(String s)
        {}

        public String doInBackground(String... sl)
        {
            return DownloadSource(sl[0]);
        }

    }
    public Utilities()
    {
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

                if (extension.equals(".jpeg") || extension.equals(".jpg") || extension.equals(".png") || extension.equals(".zip")) {

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
        if(!cache.exists())
        {
            Log.e("clearCache", "Error clearing cache, file doesn't exist");
        }
        else {
            File[] farr = cache.listFiles();
            for (int i = 0; i < farr.length; i++) {
                farr[i].delete();
            }
        }
    }

    public static void clearHistory()
    {
       MangaManager manager = MangaManager.getMangaManager();
        manager.clearHistory();
    }

    public static void clearAll()
    {
        clearCache();
        clearHistory();
    }
}
