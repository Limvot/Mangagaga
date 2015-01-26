package io.githup.limvot.mangaapp;

import android.annotation.TargetApi;

import android.app.ActionBar;
import android.os.Build;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import scala.concurrent.Future;
import scala.concurrent.ExecutionContext;
import org.scaloid.common._
import scala.collection.JavaConversions._


class ImageViewerActivity extends SActivity with GestureDetector.OnGestureListener {
    implicit val exec = ExecutionContext.fromExecutor(Utilities.executor)

    private var detector:GestureDetectorCompat = null;
    private var image:ImageView = null;

    onCreate {
        contentView = new SFrameLayout() { image = SImageView().<<.fill.>> }

        // Hide the action bar.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) { 
          var actionBar: ActionBar = getActionBar();
          var d = new ColorDrawable(0);
          d.setAlpha(1);
          if (actionBar != null) {
            actionBar.setBackgroundDrawable(d);
            actionBar.hide();
          }
        }

        detector = new GestureDetectorCompat(this,this);

        image.setOnTouchListener(new View.OnTouchListener() {
            override def onTouch(view: View, motionEvent: MotionEvent): Boolean =  {
                detector.onTouchEvent(motionEvent);
                return true;
            }
        });

        this.updateImage();
    }

    /**
     * Gesture Listener method implementations.
     */
    override def onSingleTapUp(event: MotionEvent): Boolean = {
        var x: Float = 0;
        x = event.getX();
        if(x < 350) {
            var total: Int = MangaManager.getNumPages();
            var i: Int = MangaManager.getCurrentPageNum();
            if(i < total-1) {
                MangaManager.setCurrentPageNum(i+1);
            } else {
                MangaManager.nextChapter();
                MangaManager.setCurrentPageNum(0);
            }
        } else {
            var i: Int = MangaManager.getCurrentPageNum();
            if(i > 0) {
                MangaManager.setCurrentPageNum(i-1);
            } else {
                MangaManager.previousChapter();
                MangaManager.setCurrentPageNum(MangaManager.getNumPages() - 1);
            }
        }
        updateImage();
        Log.d("onSingleTapUp","X: "+x.toFloat.toString);
        return true;
    }

    override def onShowPress(event: MotionEvent) = {}
    override def onLongPress(event: MotionEvent) = {}
    override def onScroll(e1: MotionEvent, e2:MotionEvent, distancex: Float, distancey: Float)= true

    override def onFling(e1: MotionEvent, e2: MotionEvent, velx: Float, vely: Float): Boolean = {
        Log.d("onFling", "Velx: "+velx.toFloat.toString);
        if(velx > 0) {
            //swiped from left to right
            var total: Int = MangaManager.getNumPages();
            var i: Int = MangaManager.getCurrentPageNum();
            Log.d("onClick", Integer.toString(total));
            if(i < total-1) {
                MangaManager.setCurrentPageNum(i+1);
            } else {
                // Returns true if successful (has a next chapter)
                if (MangaManager.nextChapter())
                    MangaManager.setCurrentPageNum(0);
            }
        } else {
            //swiped from right to left
            var i: Int = MangaManager.getCurrentPageNum();
            if(i > 0) {
                MangaManager.setCurrentPageNum(i-1);
            } else {
                // Returns true if successful (has a previous chapter)
                if (MangaManager.previousChapter())
                    MangaManager.setCurrentPageNum(MangaManager.getNumPages() - 1);
            }
        }
        updateImage();
        true
    }

    def onDown(event: MotionEvent) = true

    // Do image updates async to maintain responsiveness
    def updateImage() = {
        // THE FUTURE IS NOW
        Future {
            Log.i("ASYNC DO IN BACKGROUND", "STARTING")
            var imagepath = MangaManager.getCurrentPage()
            Log.i("Display image!", imagepath)
            Log.i("ASYNC DO IN BACKGROUND", "ENDING")
            var bm: Bitmap = ImageManager.getNext(imagepath)
            Log.i("ASYNC DO IN BACKGROUND", "Post Execute!")
            runOnUiThread(image.setImageBitmap(bm))
        }
        // NOW IT'S THE PRESENT
    }
}
