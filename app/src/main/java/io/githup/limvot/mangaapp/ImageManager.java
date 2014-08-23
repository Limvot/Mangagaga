package io.githup.limvot.mangaapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
        return bitmapMaker.decodeFile(path);
    }
}
