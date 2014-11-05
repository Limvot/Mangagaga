package io.githup.limvot.mangaapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Matrix;
import android.opengl.GLES10;

/**
 * Created by marcus on 8/22/14.
 */
public class ImageManager {

    private static BitmapFactory bitmapMaker;
    private static ImageManager imageManager;

    ImageManager()
    {
        bitmapMaker = new BitmapFactory();
    }

    public static ImageManager getImageManager()
    {
        if(imageManager == null) {
            imageManager = new ImageManager();
        }
        return imageManager;
    }

    public Bitmap getNext(String path)
    {
        return this.load(path);
    }

    public Bitmap getPrevious(String path)
    {
        return this.load(path);
    }

    public Bitmap load(String path)
    {
        int[] textureSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, textureSize, 0);
        // Hardcode because sadness (the above won't work without an OpenGL context)
        textureSize[0] = 2048;

        Bitmap bm = bitmapMaker.decodeFile(path);
        int width = bm.getWidth();
        int height = bm.getHeight();
        float finalScale = 1.0f;
        if (width > textureSize[0] || height > textureSize[0]) {
            float scaledW = ((float) textureSize[0])/width;
            float scaledH = ((float) textureSize[0])/height;
            finalScale = scaledH < scaledW ? scaledH : scaledW;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(finalScale, finalScale);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }
}
