package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import android.graphics.Bitmap
import android.graphics.BitmapFactory

import javax.microedition.khronos.opengles.GL10

import android.graphics.Matrix
import android.opengl.GLES10
import android.util.Log;

/**
 * Created by marcus on 8/22/14.
 * Converted to Scala by Pratik on 11/21/14.
 * Converted to Kotlin by Nathan on 6/30/17.
 */
object ImageManager : AnkoLogger {

    fun instance() = this

    fun getNext(path: String) : Bitmap = load(path)

    fun getPrevious(path: String) : Bitmap = load(path)

    fun load(path: String) : Bitmap {
      info("ImageManager - load - begin")
      val textureSize = intArrayOf(0)
      GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, textureSize, 0)
      // Hardcode because sadness (the above won't work without an OpenGL context)
      textureSize[0] = 2048

      val bm = BitmapFactory.decodeFile(path)
      var width = bm.getWidth()
      var height = bm.getHeight()
      var finalScale:Float = 1.0f
      if (width > textureSize[0] || height > textureSize[0]) {
        var scaledW = textureSize[0].toFloat()/width
        var scaledH = textureSize[0].toFloat()/height
        finalScale = if (scaledH < scaledW) scaledH
        else scaledW
      }
      val matrix = Matrix();
      matrix.postScale(finalScale, finalScale);
      //val b = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
      info("ImageManager - load - created bitmap");
      //return b
      return bm
    }
}
