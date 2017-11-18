package io.githup.limvot.mangagaga;

/**
 * Created by marcus on 12/17/14. (redone in Kotlin later by Nathan :D)
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

object Utilities : GenericLogger {

    init {
        CookieHandler.setDefault(CookieManager())
    }

    // Change to lazy?
    var gson_bac : Gson? = null
    fun getGson() : Gson {
        if (gson_bac == null)
            gson_bac = GsonBuilder().setPrettyPrinting().create()
        return gson_bac!!
    }

    var id = 0
    fun getID() = synchronized(this) { id += 1; id }

    fun getModifiedTime(source : String) : Date {
        info("Url to grab modified time from $source")
        var modifiedTime : Long = 0
        try {
            var connection = URL(source).openConnection() as HttpURLConnection
            connection.setRequestMethod("HEAD")
            connection.connect()
            modifiedTime = connection.getLastModified()
            connection.disconnect()
        } catch(e : Exception) {
            info("getModifiedTime "+e.toString())
            modifiedTime = 0
        }
        if (modifiedTime.toInt() == 0)
            info("Could not get modified time, Nope couldn't get it")
        return Date(modifiedTime)
    }

    fun download(source : String) = downloadWithRequestHeadersAndReferrer(source, "").first
    fun downloadWithRequestHeadersAndReferrer(source : String, referer: String) : Pair<String, MutableMap<String,List<String>>> {
        // Removed a lot of try/catch/timeout stuff here.
        // We should add it back if we need it
        return DownloadSource(source, referer)
    }

    fun DownloadSource(source : String, referer: String) : Pair<String, MutableMap<String,List<String>>> {
        var filename : String = ""
        var resultingPath : String = ""
        var resultingHeaders : MutableMap<String,List<String>>? = null
        info("DownloadSource: $source")
        var urlcon : HttpURLConnection? = null
        try {
            info("DownloadSource: URL is: $source")
            urlcon = URL(source).openConnection() as HttpURLConnection
            urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
            if (referer.length > 0)
              urlcon.setRequestProperty("Referer", referer);
            //urlcon.setRequestProperty("User-Agent", "Mangagaga");
            info("user agent is ${urlcon.getRequestProperty("User-Agent")}")
            urlcon.setInstanceFollowRedirects(true)
            info("TYPE IS... ${urlcon.getContentType()}")       
            filename = source.substring((source.lastIndexOf('/') + 1))
            if (referer.length > 0) {
              info("Using referer instead: $referer")
              filename = referer.substring((source.lastIndexOf('/') + 1))
            }

            if (filename.contains("?"))
                filename = filename.replace('?', '_')
            info("filename: $filename")

            var dest = SettingsManager.mangagagaPath + "/Cache/"
            // Prepend an ID to the file name so different files do not conflict
            filename = "${getID()}_$filename"

            resultingPath = dest + filename
            var file = File(dest, filename)
            info("DownloadSource: resulting file: $file")
            file.createNewFile()
            
            resultingHeaders = urlcon.getHeaderFields()
            info("Request Headers: $resultingHeaders")
            info("here we go")
            info(urlcon.getResponseMessage())

            // RRRARRRARARGH you gave to do getErrorStream if it's returned an info
            // and getResponseMessage doesn't work if it returned an info
            val ist = if (200 <= urlcon.getResponseCode() && urlcon.getResponseCode() <= 299)
              urlcon.getInputStream()
            else
              urlcon.getErrorStream()
            file.writeBytes(ist.readBytes())
        }
        catch (e : MalformedURLException) {
            info("DownloadSource: Error opening page!")
            return Pair("Error 1 - malformed url", resultingHeaders!!)
        }
        catch (e : IOException) {
            info("DownloadSource: Error With Reader/Writer! (returning getResponseMessage as the string instead of the file path)")
            info("DownloadSource: $e")
            return Pair("Error 2 info with reader writer", resultingHeaders!!)
        }
        finally {
          if (urlcon != null)
            urlcon.disconnect()
        }

        return Pair(resultingPath, resultingHeaders!!)
    }

    fun clearCache() {
        var cache = File(SettingsManager.mangagagaPath + "/Cache/")
        clearFolder(cache)
    }

    fun deleteFolder(folder : File) {
      if (folder.exists()) {
        clearFolder(folder)
        folder.delete()
      }
    }

    fun clearFolder(folder : File) {
        for (child in folder.list()) {
            val childFile : File = File(folder, child)
            if (childFile.isDirectory())
                clearFolder(childFile)
            childFile.delete()
        }
    }

    fun gitToScripts() {
        val to = File(SettingsManager.mangagagaPath, "Scripts/")
        val git_temp = File(SettingsManager.mangagagaPath, "GitTemp/")
        Utilities.deleteFolder(git_temp)
        val result = Git.cloneRepository().setURI(SettingsManager.getGitURL())
                                          .setDirectory(git_temp)
                                          .call()

        for ((index, script) in git_temp.listFiles().filter { it.isFile() }.withIndex()) {
            File(to, script.name).writeText(File(script.absolutePath).readText())
        }
    }

    fun scriptToGit(script: File, username: String, password: String, commit_msg: String) {
        val from = File(SettingsManager.mangagagaPath, "Scripts/")
        val git_temp = File(SettingsManager.mangagagaPath, "GitTemp/")
        Utilities.deleteFolder(git_temp)
        val result = Git.cloneRepository().setURI(SettingsManager.getGitURL())
                                          .setDirectory(git_temp)
                                          .call()

        File(git_temp, script.name).writeText(script.readText())
        result.add().addFilepattern(script.name).call()
        result.commit().setMessage(commit_msg).call()
        result.push().setCredentialsProvider(
                UsernamePasswordCredentialsProvider(username, password)).call()
    }
}
