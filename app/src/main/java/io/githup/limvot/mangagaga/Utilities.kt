package io.githup.limvot.mangagaga;

/**
 * Created by marcus on 12/17/14. (redone in Kotlin later by Nathan :D)
 */

import org.jetbrains.anko.*

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
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException; 
import java.util.concurrent.Executor;
import java.util.Date;

object Utilities : AnkoLogger {

    init {
        CookieHandler.setDefault(CookieManager())
    }

    // Change to lazy?
    var gson_bac : Gson? = null
    fun getGson() : Gson {
        if (gson_bac == null)
            gson_bac = GsonBuilder().registerTypeAdapter(LuaTable::class.java, LuaTableSerializer()).setPrettyPrinting().create()
        return gson_bac!!
    }

    var id = 0
    fun getID() = synchronized(this) { id += 1; id }

    fun checkForUpdates(ctx : Context) {
        doAsync {
            checkForUpdatesAsync(ctx)
        }
    }
    fun checkForUpdatesAsync(ctx : Context) {
        var updateURL : String = "http://mangagaga.room409.xyz/app-debug.apk"
        var siteApkDate : Date = getModifiedTime(updateURL)
        
        //This is needed to load the settings file from memory
        //and to make sure SettingsManager isn't null
        SettingsManager.loadSettings();
        
        info("Does this need updates? $siteApkDate")
        if (siteApkDate.after(SettingsManager.getApkDate())) {
            info("Ask user to update after downloading new apk!")
            var downloadedApk : File = File(download(updateURL))
            var promptInstall : Intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(downloadedApk),
                            "application/vnd.android.package-archive")
            ctx.startActivity(promptInstall)
            SettingsManager.setApkDate(siteApkDate)
        }
    }

    fun getModifiedTime(source : String) : Date {
        info("Url to grab modified time from $source")
        var modifiedTime : Long = 0
        try {
            var url : URL = URL(source)
            var connection : HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("HEAD")
            connection.connect()
            modifiedTime = connection.getLastModified()
            connection.disconnect()
        } catch(e : Exception) {
            error("getModifiedTime "+e.toString())
            modifiedTime = 0
        }
        if (modifiedTime.toInt() == 0)
            error("Could not get modified time, Nope couldn't get it")
        return Date(modifiedTime)
    }

    fun download(source : String) : String = downloadWithRequestHeadersAndReferrer(source, "").first
    fun downloadWithRequestHeadersAndReferrer(source : String, referer: String) : Pair<String, MutableMap<String,List<String>>> {
        /*try {*/

            /*val f = Future[(String, java.util.Map[String,java.util.List[String]])] {*/
            val f = DownloadSource(source, referer)
            /*val f = bg {*/
                /*DownloadSource(source, referer)*/
            /*}*/
            //Should Probably make the app handle timeouts a bit better
            /*return Await.result(f,20 seconds).asInstanceOf[(String, java.util.Map[String,java.util.List[String]])]*/
            return f
            /*return f.await()*/
            //return Await.result(f,100 millis).asInstanceOf[String]
        /*}*/
        /*catch (e : ExecutionException) {*/
            /*error("Download: Error with Async Source Download!!!")*/
            /*error(e.getMessage())*/
            /*var err = ""*/
            /*for(x in e.getStackTrace()) {*/
                /*err += x.toString()+"\n"*/
            /*}*/
            /*error(err)*/
            /*return Pair("Error Async!", null)*/
        /*}*/
        /*catch (e : InterruptedException) {*/
            /*error("Download: Interrupted Exception!!!")*/
            /*error(e.getMessage())*/
            /*return Pair("Error Interrupted!", null)*/
        /*}*/
        /*catch (e : TimeoutException) {*/
            /*error("Download: Timeout Exception!!!")*/
            /*error(e.getMessage())*/
            /*return Pair("Error Timeout!", null)*/
        /*}*/
    }


    fun DownloadSource(source : String, referer: String) : Pair<String, MutableMap<String,List<String>>> {
        var filename : String = ""
        var resultingPath : String = ""
        /*var resultingHeaders : java.util.Map<java.lang.String,java.util.List<java.lang.String>>? = null*/
        var resultingHeaders : MutableMap<String,List<String>>? = null
        var sourceSite : URL? = null
        if(!source.isEmpty()) {
            debug("DownloadSource: $source")
            var urlcon : HttpURLConnection? = null
            try {
                error("DownloadSource: URL is: $source")
                sourceSite = URL(source)
                if (sourceSite == null) {
                  error("HUGE ERROR!!!")
                }
                urlcon = sourceSite.openConnection() as HttpURLConnection
                urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
                if (referer.length > 0)
                  urlcon.setRequestProperty("Referer", referer);
                //urlcon.setRequestProperty("User-Agent", "Mangagaga");
                error("user agent is ${urlcon.getRequestProperty("User-Agent")}")
                error("following is ${urlcon.getInstanceFollowRedirects()}")
                urlcon.setInstanceFollowRedirects(true)
                error("following is ${urlcon.getInstanceFollowRedirects()}")
                error("TYPE IS... ${urlcon.getContentType()}")       
                filename = source.substring((source.lastIndexOf('/') + 1))
                if (referer.length > 0) {
                  error("Using referer instead: $referer")
                  filename = referer.substring((source.lastIndexOf('/') + 1))
                  error("filename:")
                  error(filename)
                }
                var dest : String = SettingsManager.mangagagaPath + "/Cache/"

                if (filename.contains("?")) {
                    filename = filename.replace('?', '_')
                    debug("DownloadSource: Removed '?' and renamed file to: $filename")
                }
                // Prepend an ID to the file name so different files do not conflict
                filename = "${getID()}_$filename"

                resultingPath = dest + filename
                var file : File = File(dest, filename)
                info("DownloadSource: resulting file: $file")
                try {
                    file.createNewFile()
                }
                catch (e : IOException) {
                    error("DownloadSource: Couldn't make file!!!")
                    error("DownloadSource: $e")
                }
                
                resultingHeaders = urlcon.getHeaderFields()
                error("Request Headers: $resultingHeaders")
                error("here we go")
                error(urlcon.getResponseMessage())

                var fos : FileOutputStream = FileOutputStream(file)
                var ist : InputStream? = null
                // RRRARRRARARGH you gave to do getErrorStream if it's returned an error
                // and getResponseMessage doesn't work if it returned an error
                if (200 <= urlcon.getResponseCode() && urlcon.getResponseCode() <= 299)
                  ist = urlcon.getInputStream()
                else
                  ist = urlcon.getErrorStream()
                var bis : BufferedInputStream = BufferedInputStream(ist)
                var buffer : ByteArrayBuffer = ByteArrayBuffer(500)

                var chunk : Int = bis.read()
                while (chunk != -1) {
                  buffer.append(chunk)
                  chunk = bis.read()
                }
                debug("DownloadSource: Writing Image")
                fos.write(buffer.toByteArray())
                fos.flush()
                fos.close()
            }
            catch (e : MalformedURLException) {
                error("DownloadSource: Error opening page!")
                return Pair("Error 1 - malformed url", resultingHeaders!!)
            }
            catch (e : IOException) {
                error("DownloadSource: Error With Reader/Writer! (returning getResponseMessage as the string instead of the file path)")
                error("DownloadSource: $e")
                return Pair("Error 2 error with reader writer", resultingHeaders!!)
            }
            finally {
              if (urlcon != null)
                urlcon.disconnect()
            }
        }

        return Pair(resultingPath, resultingHeaders!!)
    }

    fun copyStreams(ins : InputStream, outs : OutputStream) {
        var buffer = ByteArray(1024);
        var readBytes : Int = ins.read(buffer)
        while(readBytes != -1) {
            outs.write(buffer, 0, readBytes)
            readBytes = ins.read(buffer)
        }
        return
    }

    fun clearCache()
    {
        var cache : File = File(SettingsManager.mangagagaPath + "/Cache/")
        clearFolder(cache)
    }

    fun deleteFolder(folder : File) {
      if (folder.exists()) {
        clearFolder(folder)
        folder.delete()
      }
    }

    fun clearFolder(folder : File) {

        if(!folder.exists())
        {
            error("clear saved: Error clearing saved, directory doesn't exist")
        }else {
            for (child in folder.list()) {
                val childFile : File = File(folder, child)
                if (childFile.isDirectory())
                    clearFolder(childFile)
                childFile.delete()
            }
        }
    }
}
