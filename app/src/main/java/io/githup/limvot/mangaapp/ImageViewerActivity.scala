package io.githup.limvot.mangaapp;

import android.annotation.TargetApi;
import android.app.ActionBar;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import scala.concurrent.Future;
import scala.concurrent.ExecutionContext;
import org.scaloid.common._
import scala.collection.JavaConversions._


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
class ImageViewerActivity extends Activity with GestureDetector.OnGestureListener {
    implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private val AUTO_HIDE: Boolean = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private val AUTO_HIDE_DELAY_MILLIS: Int = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private val TOGGLE_ON_CLICK: Boolean = true;

    private var detector:GestureDetectorCompat = null;
    private var gestureListen:View.OnTouchListener = null;

    private var contentview:ImageView = null;


    override protected def onCreate(savedInstanceState: Bundle) = {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);
        contentview = findViewById(R.id.fullscreen_content).asInstanceOf[ImageView];


        // Hide the action bar.
        var actionBar: ActionBar = getActionBar();
        var d = new ColorDrawable(0);
        d.setAlpha(1);
        if (actionBar != null) {
          actionBar.setBackgroundDrawable(d);
          actionBar.hide();
        }

        val contentView: View = findViewById(R.id.fullscreen_content);

        detector = new GestureDetectorCompat(this,this);


        contentView.setOnTouchListener(new View.OnTouchListener() {
            override def onTouch(view: View, motionEvent: MotionEvent): Boolean =  {
                detector.onTouchEvent(motionEvent);
                return true;
            }
        });

        this.updateImage();
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.back_button).setOnTouchListener(mDelayHideTouchListener);
    }

    //override protected def onPostCreate(savedInstanceState: Bundle) = {
        //super.onPostCreate(savedInstanceState);

        //// Trigger the initial hide() shortly after the activity has been
        //// created, to briefly hint to the user that UI controls
        //// are available.
        ////delayedHide(100);
    //}

    /**
     * Gesture Listener method implementations.
     */
    override def onSingleTapUp(event: MotionEvent): Boolean = {
        var x: Float = 0;
        x = event.getX();
        if(x < 350)
        {
            var total: Int = MangaManager.getNumPages();
            var i: Int = MangaManager.getCurrentPageNum();
            if(i < total-1) {
                MangaManager.setCurrentPageNum(i+1);
            } else {
                MangaManager.nextChapter();
                MangaManager.setCurrentPageNum(0);
            }
        }
        else
        {
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

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_viewer, menu);
        return true;
    }

    override def onShowPress(event: MotionEvent) = {}

    override def onLongPress(event: MotionEvent) = {}

    override def onScroll(e1: MotionEvent, e2:MotionEvent, distancex: Float, distancey: Float): Boolean = {
        return true;
    }

    override def onFling(e1: MotionEvent, e2: MotionEvent, velx: Float, vely: Float): Boolean = {
        Log.d("onFling", "Velx: "+velx.toFloat.toString);
        if(velx > 0)
        {
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
        }
        else
        {
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
            runOnUiThread(contentview.setImageBitmap(bm))
        }
        // NOW IT'S THE PRESENT
    }
}
