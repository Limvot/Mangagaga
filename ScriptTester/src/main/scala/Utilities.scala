/**
 * Created by marcus on 12/17/14.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.luaj.vm2.LuaTable;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io._;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.Date;
import java.nio.ByteBuffer;

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object Utilities {
    //val executor = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
    /*val executor = if (true) {
      new Executor  {
        def execute(r: Runnable) {
          new Thread(r).start();
        }
      }
    } else {
      //AsyncTask.THREAD_POOL_EXECUTOR
    }
    implicit val exec = ExecutionContext.fromExecutor(executor)*/
    var gson : Gson = null


    def getGson() : Gson = {
        if (gson == null)
            gson = new GsonBuilder().registerTypeAdapter(classOf[LuaTable], new LuaTableSerializer()).setPrettyPrinting().create()
        return gson
    }

    var id = 0
    def getID() : Int = this.synchronized { id += 1; id }

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
                error("DownloadSource: URL is: "+source)
                sourceSite = new URL(source)
                if (sourceSite == null) {
                  error("HUGE ERROR!!!")
                }
                var urlcon : URLConnection = sourceSite.openConnection()
                //Use custom user agent to prevent http 403 response
                //If custom user agent doesn't work use actual one
                //urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:41.0) Gecko/20100101 Firefox/41");
                urlcon.setRequestProperty("User-Agent", "Mangagaga");
                error("TYPE IS... "+urlcon.getContentType)       
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

                var dest : String = "./Mangagaga/Cache/"

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

                debug("DownloadSource: Making Image fos")
                var fos : FileOutputStream = new FileOutputStream(file)
                info("DownloadSource: FOS is made!")
                //Error on this line...
                var is : InputStream = urlcon.getInputStream()
                info("DownloadSource: IS is made!")
                var bis : BufferedInputStream = new BufferedInputStream(is)
                info("DownloadSource: BIS is made!")
                var buffer : ByteBuffer = ByteBuffer.allocate(1024 * 1024 * 16)
                info("DownloadSource: buffer is made!")

                var chunk : Int = bis.read()

                while (chunk != -1) {
                  buffer.put(chunk.asInstanceOf[Byte])
                  chunk = bis.read()
                }
                debug("DownloadSource: Writing Image")
                fos.write(buffer.array)
                fos.flush()
                fos.close()
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
        var cache : File = new File("./Mangagaga/Cache/")
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

    def error(in : String) {
      System.out.println(in)
    }

    def info(in : String) {
      System.out.println(in)
    }

    def debug(in : String) {
      System.out.println(in)
    }
}