package io.githup.limvot.mangaapp;

/**
 * Created by marcus on 8/19/14.
 */

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import java.util.concurrent.ExecutionException;


public class SourceDownloader {

    private String source;

    private class DownloadSource extends AsyncTask<String,Void,String>
    {

        public void onPostExecute(String s)
        {}

        public String doInBackground(String... sl)
        {
            return DownloadSource(sl[0]);
        }

    }
    public SourceDownloader()
    {
    }

    public String Download()
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

    public void SetSource(String s)
    {
        this.source = s;
    }

    public String GetSource()
    {
        return this.source;
    }

    public static String DownloadSource(String source)
    {
        String filename = "";
        URL sourceSite;
        if(!source.isEmpty())
        {
            try {
                sourceSite = new URL(source);
                Log.i("DownloadSource", "Make URL object!");
                BufferedReader reader = new BufferedReader(new InputStreamReader(sourceSite.openStream()));
                Log.i("DownloadSource", "Open Source Reader!");
                filename = source.substring((source.lastIndexOf('/') + 1)) + ".html";
                Log.i("filename", filename);
                new File(Environment.DIRECTORY_DOWNLOADS,filename);
                Log.i("path", Environment.DIRECTORY_DOWNLOADS);
                Log.i("file", "File made!");

                //Throws exception here!
                FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory() +"/"+ Environment.DIRECTORY_DOWNLOADS+"/"+filename);
                Log.i("DownloadSource", "Make file writer");
                //----------

                BufferedWriter writer = new BufferedWriter(fw);
                Log.i("DownloadSource", "Make buffered writer!");

                String input;
                while ((input = reader.readLine()) != null) {
                        writer.write(input);
                }
                Log.i("DownloadSource", "Finished Writing File!");

                reader.close();
                writer.close();
            }
            catch (MalformedURLException e)
            {
                Log.e("DownloadSource", "Error opening page!");
                return "Error 1";
            }
            catch(IOException e)
            {
                Log.e("DownloadSource", "Error With Reader/Writer!");
                return "Error 2";

            }
        }

        return filename;
    }
}
