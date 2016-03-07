package io.githup.limvot.mangagaga;

/**
 * Created by marcus on 12/17/14.
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
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.io._;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException; 
import java.util.concurrent.Executor;
import java.util.Date;

import scala.collection.JavaConversions._
import org.scaloid.common._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration._

object Utilities {
    implicit val tag = LoggerTag("ScalaUtilities")
    //val executor = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
    val executor = if (true) {
      new Executor  {
        def execute(r: Runnable) {
          new Thread(r).start();
        }
      }
    } else {
      AsyncTask.THREAD_POOL_EXECUTOR
    }
    implicit val exec = ExecutionContext.fromExecutor(executor)
    var gson : Gson = null


    def getGson() : Gson = {
        if (gson == null)
            gson = new GsonBuilder().registerTypeAdapter(classOf[LuaTable], new LuaTableSerializer()).setPrettyPrinting().create()
        return gson
    }

    var id = 0
    def getID() : Int = this.synchronized { id += 1; id }

    def checkForUpdates(ctx : Context) {
        Future {
            checkForUpdatesAsync(ctx)
        }
    }
    def checkForUpdatesAsync(ctx : Context) {
        var updateURL : String = "http://mangagaga.room409.xyz/app-debug.apk"
        var siteApkDate : Date = getModifiedTime(updateURL)
        
        //This is needed to load the settings file from memory
        //and to make sure SettingsManager isn't null
        SettingsManager.loadSettings();
        
        info("Does this need updates? "+siteApkDate.toString())
        if (siteApkDate.after(SettingsManager.getApkDate())) {
            info("Ask user to update after downloading new apk!")
            var downloadedApk : File = new File(download(updateURL))
            var promptInstall : Intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(downloadedApk),
                            "application/vnd.android.package-archive")
            ctx.startActivity(promptInstall)
            SettingsManager.setApkDate(siteApkDate)
        }
    }

    def getModifiedTime(source : String) : Date = {
        info("Url to grab modified time from "+ source)
        var modifiedTime : Long = 0
        try {
            var url : URL = new URL(source)
            var connection : HttpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection]
            connection.setRequestMethod("HEAD")
            connection.connect()
            modifiedTime = connection.getLastModified()
            connection.disconnect()
        } catch {
            case e : Exception => {
                error("getModifiedTime "+e.toString())
                modifiedTime = 0
            }
        }
        if (modifiedTime == 0)
            error("Could not get modified time, Nope couldn't get it")
        return new Date(modifiedTime)
    }

    def download(source : String) : String = downloadWithRequestHeadersAndReferrer(source, "")._1
    CookieHandler.setDefault(new CookieManager())
    def downloadWithRequestHeadersAndReferrer(source : String, referer: String) : (String, java.util.Map[String,java.util.List[String]]) = {
        try {

            val f = Future[(String, java.util.Map[String,java.util.List[String]])] {
                DownloadSource(source, referer)
            }
            //Should Probably make the app handle timeouts a bit better
            return Await.result(f,20 seconds).asInstanceOf[(String, java.util.Map[String,java.util.List[String]])]
            //return Await.result(f,100 millis).asInstanceOf[String]
        }
        catch {
            case e : ExecutionException => {
                error("Download: Error with Async Source Download!!!")
                error(e.getMessage())
                var err = ""
                for(x <- e.getStackTrace()) {
                    err += x.toString()+"\n"
                }
                error(err)
                return ("Error Async!", null)
            }
            case e : InterruptedException => {
                error("Download: Interrupted Exception!!!")
                error(e.getMessage())
                return ("Error Interrupted!", null)
            }
            case e : TimeoutException => {
                error("Download: Timeout Exception!!!")
                error(e.getMessage())
                return ("Error Timeout!", null)
            }
        }
    }


    def DownloadSource(source : String, referer: String) : (String, java.util.Map[String,java.util.List[String]]) = {
        var filename : String = ""
        var resultingPath : String = ""
        var resultingHeaders : java.util.Map[String,java.util.List[String]] = null
        var sourceSite : URL = null
        if(!source.isEmpty()) {
            debug("DownloadSource: "+source)
            var urlcon : HttpURLConnection = null
            try {
                error("DownloadSource: URL is: "+source)
                sourceSite = new URL(source)
                if (sourceSite == null) {
                  error("HUGE ERROR!!!")
                }
                urlcon = sourceSite.openConnection().asInstanceOf[HttpURLConnection]
                urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
                if (referer.length() > 0)
                  urlcon.setRequestProperty("Referer", referer);
                //urlcon.setRequestProperty("User-Agent", "Mangagaga");
                error("user agent is " + urlcon.getRequestProperty("User-Agent"))
                error("following is " + urlcon.getInstanceFollowRedirects())
                urlcon.setInstanceFollowRedirects(true)
                error("following is " + urlcon.getInstanceFollowRedirects())
                error("TYPE IS... "+urlcon.getContentType)       
                filename = source.substring((source.lastIndexOf('/') + 1))
                if (referer.length() > 0) {
                  error("Using referer instead: " + referer)
                  filename = referer.substring((source.lastIndexOf('/') + 1))
                  error("filename:")
                  error(filename)
                }
                var dest : String = Environment.getExternalStorageDirectory() + "/Mangagaga/Cache/"

                if (filename.contains("?")) {
                    filename = filename.replace('?', '_')
                    debug("DownloadSource: Removed '?' and renamed file to: " + filename)
                }
                // Prepend an ID to the file name so different files do not conflict
                filename = getID().toString + "_" + filename

                resultingPath = dest + filename
                var file : File = new File(dest, filename)
                info("DownloadSource: resulting file: "+file.toString())
                try {
                    file.createNewFile()
                } catch {
                    case e : IOException => {
                        error("DownloadSource: Couldn't make file!!!")
                        error("DownloadSource: "+e.toString())
                    }
                }
                
                resultingHeaders = urlcon.getHeaderFields()
                error("Request Headers:")
                for (mapping <- resultingHeaders;
                     value <- mapping._2)
                  error(mapping._1 + " : " + value)
                error("here we go")
                error(urlcon.getResponseMessage())

                var fos : FileOutputStream = new FileOutputStream(file)
                var is : InputStream = null
                // RRRARRRARARGH you gave to do getErrorStream if it's returned an error
                // and getResponseMessage doesn't work if it returned an error
                if (200 <= urlcon.getResponseCode() && urlcon.getResponseCode() <= 299)
                  is = urlcon.getInputStream()
                else
                  is = urlcon.getErrorStream()
                var bis : BufferedInputStream = new BufferedInputStream(is)
                var buffer : ByteArrayBuffer = new ByteArrayBuffer(500)

                var chunk : Int = bis.read()
                while (chunk != -1) {
                  buffer.append(chunk.asInstanceOf[Byte])
                  chunk = bis.read()
                }
                debug("DownloadSource: Writing Image")
                fos.write(buffer.toByteArray())
                fos.flush()
                fos.close()
            }
            catch {
                case e : MalformedURLException => {
                    error("DownloadSource: Error opening page!")
                    return ("Error 1 - malformed url", resultingHeaders)
                }
                case e : IOException => {
                    error("DownloadSource: Error With Reader/Writer! (returning getResponseMessage as the string instead of the file path)")
                    error("DownloadSource: "+e.toString())
                    return ("Error 2 error with reader writer", resultingHeaders)
                }
            }
            finally {
              if (urlcon != null)
                urlcon.disconnect()
            }
        }

        return (resultingPath, resultingHeaders)
    }

    @throws(classOf[IOException])
    def readFile(absolutePath : String) : String = {
        return readFile(new FileInputStream(absolutePath))
    }

    @throws(classOf[IOException])
    def readFile(input : InputStream) : String = {
        var reader : BufferedReader = new BufferedReader(new InputStreamReader(input))
        var sb : StringBuilder = new StringBuilder()
        var line : String = reader.readLine()
        while (line != null) {
            sb.append(line).append("\n")
            line = reader.readLine()
        }
        /*for (var line : String = reader.readLine(); line != null; line = reader.readLine())
            sb.append(line).append("\n");*/
        return sb.toString()
    }

    @throws(classOf[IOException])
    def copyStreams(in : InputStream, out : OutputStream) {
        var buffer : Array[Byte] = new Array[Byte](1024);
        var readBytes : Int = in.read(buffer)
        while(readBytes != -1) {
            out.write(buffer, 0, readBytes)
            readBytes = in.read(buffer)
        }
        return
    }

    def clearCache()
    {
        var cache : File = new File(Environment.getExternalStorageDirectory()+"/Mangagaga/Cache/")
        clearFolder(cache)
    }

    def deleteFolder(folder : File) {
      if (folder.exists()) {
        clearFolder(folder)
        folder.delete()
      }
    }

    def clearFolder(folder : File) {

        if(!folder.exists())
        {
            error("clear saved: Error clearing saved, directory doesn't exist")
        }else {
            for (child <- folder.list()) {
                var childFile : File = new File(folder, child)
                if (childFile.isDirectory())
                    clearFolder(childFile)
                childFile.delete()
            }
        }
    }
}
