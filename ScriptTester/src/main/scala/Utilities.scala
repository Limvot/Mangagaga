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
import java.nio.ByteBuffer

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer

object Utilities {
    var gson : Gson = null


    def getGson() : Gson = {
        if (gson == null)
            gson = new GsonBuilder().registerTypeAdapter(classOf[LuaTable], new LuaTableSerializer()).setPrettyPrinting().create()
        return gson
    }

    def download(source : String) : String = {
        try {

            val f : Future[String] = Future[String] {
                DownloadSource(source)
            }
            //Should Probably make the app handle timeouts a bit better
            return Await.result(f,60 seconds).asInstanceOf[String]
            //return new DownloadSource().execute(source).get()
        }
        catch {
            case e : ExecutionException => {
                System.out.println("Download: Error with Async Source Download!!!")
                System.out.println(e.getMessage())
                var err = ""
                for(x <- e.getStackTrace()) {
                    err += x.toString()+"\n"
                }
                System.out.println(err)
                return "Error Async!"
            }
            case e : InterruptedException => {
                System.out.println("Download: Interrupted Exception!!!")
                System.out.println(e.getMessage())
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
            System.out.println("DownloadSource: "+source)
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
                System.out.println("DONLSDFING: "+source)
                var extensionIndex : Int = source.lastIndexOf(extension)
                if (extensionIndex != -1)
                    filename = source.substring((source.lastIndexOf('/') + 1), extensionIndex + extension.length())
                else
                    filename = source.substring((source.lastIndexOf('/') + 1)) + extension

                var dest : String = "./Mangagaga/Cache/"

                if (filename.contains("?")) {
                    filename = filename.replace('?', '_')
                    System.out.println("DownloadSource: Removed '?' and renamed file to: " + filename)
                }

                resultingPath = dest + filename
                var file : File = new File(dest, filename)
                System.out.println("DownloadSource: "+file.toString())
                try {
                    file.createNewFile()
                } catch {
                    case e : IOException => {
                        System.out.println("DownloadSource: Couldn't make file!!!")
                        System.out.println("DownloadSource: "+e.toString())
                    }
                }

                if (extension.equals(".jpeg") || extension.equals(".jpg") || extension.equals(".png")
                        || extension.equals(".zip") || extension.equals(".apk")) {

                    var fos : FileOutputStream = new FileOutputStream(file)
                    var is : InputStream = urlcon.getInputStream()
                    var bis : BufferedInputStream = new BufferedInputStream(is)
                    println("DownloadSource: Making buffer")
                    //var buffer : ByteBuffer = ByteBuffer.allocate(500)
                    var buffer : ByteBuffer = ByteBuffer.allocate(1024 * 1024 * 16)

                    var chunk : Int = bis.read()
                    println("Looping through chunks!")
                    while (chunk != -1) {
                        //buffer.put(chunk.asInstanceOf[Byte])
                        //println("Adding chunk to buffer!")
                        buffer.put(chunk.asInstanceOf[Byte])
                        chunk = bis.read()
                    }
                    println("DownloadSource: Writing Image")
                    fos.write(buffer.array)
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
                    System.out.println("DownloadSource: Error opening page!")
                    return "Error 1"
                }
                case e : IOException => {
                    System.out.println("DownloadSource: Error With Reader/Writer!")
                    System.out.println("DownloadSource: "+e.toString())
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
            System.out.println("clear saved: Error clearing saved, directory doesn't exist")
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
