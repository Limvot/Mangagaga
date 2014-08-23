package io.githup.limvot.mangaapp;

/**
 * Created by marcus on 8/19/14.
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
    public Utilities()
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
        String resultingPath = "";
        URL sourceSite;
        if(!source.isEmpty())
        {
            Log.d("DownloadSource", source);
            try {
                sourceSite = new URL(source);
                URLConnection urlcon = sourceSite.openConnection();

                if(source.contains(".jpeg") || source.contains(".jpg") || source.contains(".png") || source.contains(".zip"))
                {
                    filename = source.substring((source.lastIndexOf('/')+1));
                    if(source.contains(".jpg"))
                    {
                        filename = filename.substring(0, filename.lastIndexOf(".jpg")+4);
                    }
                    else if(source.contains(".png"))
                    {
                        filename = filename.substring(0, filename.lastIndexOf(".png")+4);
                    }
                    else
                    {
                        filename += ".zip";
                    }

                    resultingPath = Environment.getExternalStorageDirectory() +"/"+ Environment.DIRECTORY_DOWNLOADS+"/"+filename;
                    File file = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOWNLOADS+"/", filename);
                    Log.i("DownloadSource", file.toString());
                    try{
                        file.createNewFile();
                    }
                    catch(IOException e)
                    {
                        Log.e("DownloadSource", "Couldn't make file!!!");
                        Log.e("DownloadSource", e.toString());
                    }

                    Log.d("DownloadSource", "Making Image fos");
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = urlcon.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ByteArrayBuffer buffer = new ByteArrayBuffer(500);

                    int chunk = 0;

                    while( (chunk = bis.read()) != -1)
                    {
                        buffer.append((byte) chunk);
                    }
                    Log.d("DownloadSource", "Writing Image");
                    fos.write(buffer.toByteArray());
                    fos.flush();
                    fos.close();
                }
                else
                {
                    filename = source.substring((source.lastIndexOf('/') + 1)) + ".html";
                    if(filename.contains("?")) {
                        filename = filename.replace('?', '_');
                        Log.d("DownloadSource", "Removed '?' and renamed file to: "+filename);
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(sourceSite.openStream()));
                    File file = new File(Environment.DIRECTORY_DOWNLOADS,filename);
                    try{
                        file.createNewFile();
                    }
                    catch(IOException e)
                    {
                        Log.e("DownloadSource", "Couldn't make file!!!");
                        Log.e("DownloadSource", e.toString());
                    }

                    resultingPath = Environment.getExternalStorageDirectory() +"/"+ Environment.DIRECTORY_DOWNLOADS+"/"+filename;
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

    public static String readFile(String absolutePath) {
        String fileString = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)));
            StringBuilder sb = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine())
                sb.append(line).append("\n");
            fileString = sb.toString();
        } catch (Exception e) {
            Log.e("Script", e.toString());
        }
        return fileString;
    }
}
