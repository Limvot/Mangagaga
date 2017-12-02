package io.githup.limvot.mangagaga;

/**
 * Created by marcus on 12/17/14. (redone in Kotlin later by Nathan :D)
 */

import java.net.HttpURLConnection;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import java.util.Date;

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

object Utilities : GenericLogger {

    init {
        CookieHandler.setDefault(CookieManager())
    }

    var id = 0
    fun getID() = synchronized(this) { id += 1; id }

    fun getModifiedTime(source : String) : Date {
        info("Url to grab modified time from $source")
        var modifiedTime : Long = 0
        try {
            val connection = URL(source).openConnection() as HttpURLConnection
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
        var resultingPath : String = ""
        var resultingHeaders : MutableMap<String,List<String>>? = null
        info("DownloadSource: $source")
        var urlcon : HttpURLConnection? = null
        try {
            info("DownloadSource: URL is: $source")
            urlcon = URL(source).openConnection() as HttpURLConnection
            urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
            if (referer.isNotEmpty())
              urlcon.setRequestProperty("Referer", referer);
            //urlcon.setRequestProperty("User-Agent", "Mangagaga");
            info("user agent is ${urlcon.getRequestProperty("User-Agent")}")
            urlcon.setInstanceFollowRedirects(true)
            info("TYPE IS... ${urlcon.getContentType()}")       
            var filename : String = if (referer.isNotEmpty()) {
              info("Using referer instead: $referer")
              referer.substring((source.lastIndexOf('/') + 1))
            } else {
                source.substring((source.lastIndexOf('/') + 1))
            }

            filename = filename.replace('?', '_')
            info("filename: $filename")

            val dest = SettingsManager.mangagagaPath + "/Cache/"
            // Prepend an ID to the file name so different files do not conflict
            filename = "${getID()}_$filename"

            resultingPath = dest + filename
            val file = File(dest, filename)
            info("DownloadSource: resulting file: $file")
            file.createNewFile()
            
            resultingHeaders = urlcon.getHeaderFields()
            info("Request Headers: $resultingHeaders")
            info("here we go")
            info(urlcon.getResponseMessage())

            // RRRARRRARARGH you gave to do getErrorStream if it's returned an info
            // and getResponseMessage doesn't work if it returned an info
            val ist = if (urlcon.getResponseCode() in 200..299)
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
            urlcon?.disconnect()
        }

        return Pair(resultingPath, resultingHeaders!!)
    }

    fun clearCache() {
        val cache = File(SettingsManager.mangagagaPath + "/Cache/")
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

    private fun gitScriptHelper(f_script: File?, username: String, password: String, commit_msg: String) {
        val to = File(SettingsManager.mangagagaPath, "Scripts/")
        val git_temp = File(SettingsManager.mangagagaPath, "GitTemp/")
        Utilities.deleteFolder(git_temp)
        val result = Git.cloneRepository().setURI(SettingsManager.getGitURL())
                                          .setDirectory(git_temp)
                                          .call()

        if(f_script == null) {
            //gitToScripts()
            for (script in git_temp.listFiles().filter { it.isFile() }) {
                File(to, script.name).writeText(File(script.absolutePath).readText())
            }
        } else {
            //scriptToGit()
            val script = f_script
            File(git_temp, script.name).writeText(script.readText())
            result.add().addFilepattern(script.name).call()
            result.commit().setMessage(commit_msg).call()
            result.push().setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(username, password)).call()
        }
    }
    fun gitToScripts() {
        gitScriptHelper(null,"","","")
    }

    fun scriptToGit(script: File, username: String, password: String, commit_msg: String) {
        gitScriptHelper(script, username, password, commit_msg)
    }
}
