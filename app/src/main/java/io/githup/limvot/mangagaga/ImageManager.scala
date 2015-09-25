package io.githup.limvot.mangagaga

import android.graphics.Bitmap
import android.graphics.BitmapFactory

import javax.microedition.khronos.opengles.GL10

import android.graphics.Matrix
import android.opengl.GLES10
import android.util.Log;

/**
 * Created by marcus on 8/22/14.
 * Converted to Scala by Pratik on 11/21/14.
 */
object ImageManager  {

    def instance() = this

    def getNext(path: String) : Bitmap = load(path)

    def getPrevious(path: String) : Bitmap = load(path)

    def load(path: String) : Bitmap = {
      Log.i("ImageManager - load", "begin")
      val textureSize = new Array[Int](1)
      GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, textureSize, 0)
      Log.i("ImageManager - load", "got textrue size")
      // Hardcode because sadness (the above won't work without an OpenGL context)
      textureSize.update(0, 2048)

      Log.i("ImageManager - load", "gonna decode  file")
      val bm = BitmapFactory.decodeFile(path)
      Log.i("ImageManager - load", "did decode  file")
      var width = bm.getWidth()
      var height = bm.getHeight()
      var finalScale:Float = 1.0f
      if (width > textureSize.apply(0) || height > textureSize.apply(0)) {
        var scaledW = (textureSize.apply(0))/width
        var scaledH = (textureSize.apply(0))/height
        finalScale = if (scaledH < scaledW) scaledH
        else scaledW
      }
      Log.i("ImageManager - load", "making matrix")
      val matrix = new Matrix();
      Log.i("ImageManager - load", "made matrix")
      matrix.postScale(finalScale, finalScale);
      Log.i("ImageManager - load", "scaled matrix, gonna create bitmap");

      //val b = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
      Log.i("ImageManager - load", "created bitmap");
      //b
      bm
    }
}
