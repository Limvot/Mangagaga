package io.githup.limvot.mangaapp;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.io._;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
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
        var updateURL : String = "http://mangagaga.nathanbraswell.com/app-debug.apk"
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

    def download(source : String) : String = {
        try {

            val f : Future[String] = Future[String] {
                DownloadSource(source)
            }
            //Should Probably make the app handle timeouts a bit better
            return Await.result(f,60 seconds).asInstanceOf[String]
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
                return "Error Async!"
            }
            case e : InterruptedException => {
                error("Download: Interrupted Exception!!!")
                error(e.getMessage())
                return "Error Interrupted!"
            }
        }
    }


    def DownloadSource(source : String) : String = {
        var filename : String = ""
        var resultingPath : String = ""
        var sourceSite : URL = null
        if(!source.isEmpty())
        {
            debug("DownloadSource: "+source)
            try {
                sourceSite = new URL(source)
                var urlcon : URLConnection = sourceSite.openConnection()

                var extension : String = ""
                if (source.contains(".jpg")) {
                    extension = ".jpg"
                } else if (source.contains(".jpeg")) {
                    extension = ".jpeg"
                } else if (source.contains(".png")) {
                    extension = ".png"
                } else if (source.contains(".zip")) {
                    extension = ".zip"
                } else if (source.contains(".apk")) {
                    extension = ".apk"
                } else {
                    extension = ".html"
                }
                info("DONLSDFING: "+source)
                var extensionIndex : Int = source.lastIndexOf(extension)
                if (extensionIndex != -1)
                    filename = source.substring((source.lastIndexOf('/') + 1), extensionIndex + extension.length())
                else
                    filename = source.substring((source.lastIndexOf('/') + 1)) + extension

                var dest : String = Environment.getExternalStorageDirectory() + "/Mangagaga/Cache/"

                if (filename.contains("?")) {
                    filename = filename.replace('?', '_')
                    debug("DownloadSource: Removed '?' and renamed file to: " + filename)
                }
                // Prepend an ID to the file name so different files do not conflict
                filename = getID().toString + "_" + filename

                resultingPath = dest + filename
                var file : File = new File(dest, filename)
                info("DownloadSource: "+file.toString())
                try {
                    file.createNewFile()
                } catch {
                    case e : IOException => {
                        error("DownloadSource: Couldn't make file!!!")
                        error("DownloadSource: "+e.toString())
                    }
                }

                if (extension.equals(".jpeg") || extension.equals(".jpg") || extension.equals(".png")
                        || extension.equals(".zip") || extension.equals(".apk")) {

                    debug("DownloadSource: Making Image fos")
                    var fos : FileOutputStream = new FileOutputStream(file)
                    var is : InputStream = urlcon.getInputStream()
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
                } else {
                    var reader : BufferedReader = new BufferedReader(new InputStreamReader(sourceSite.openStream()))
                    var fw : FileWriter = new FileWriter(resultingPath)
                    var writer : BufferedWriter = new BufferedWriter(fw)
                    var input : String = reader.readLine()
                    while (input != null) {
                        writer.write(input)
                        input = reader.readLine()
                    }
                    reader.close()
                    writer.close()
                }

            }
            catch {
                case e : MalformedURLException => {
                    error("DownloadSource: Error opening page!")
                    return "Error 1"
                }
                case e : IOException => {
                    error("DownloadSource: Error With Reader/Writer!")
                    error("DownloadSource: "+e.toString())
                    return "Error 2"
                }
            }
        }

        return resultingPath
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
