package io.githup.limvot.mangagaga

import org.jetbrains.anko.*

import android.os.Bundle
import android.app.Activity
import android.widget.ImageView
import android.view.GestureDetector
import android.view.MotionEvent;
import android.graphics.BitmapFactory;

class ImageViewerActivity : Activity(), GenericLogger, GestureDetector.OnGestureListener {
    private var image : ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActionBar().hide()
        getActionBar().title = ""
        APIObject.onStatus = { text -> doAsync { uiThread { getActionBar().show()
                                                            getActionBar().subtitle = text } } }
        frameLayout { image = imageView() }
        val detector = GestureDetector(this, this)
        image!!.setOnTouchListener({ view, motionEvent -> detector.onTouchEvent(motionEvent);true})
        updateImage()
    }
    private fun updateImage() {
        doAsync {
            val req = Request(manga = Boss.currentManga, chapter = Boss.currentChapter,
                              page = Boss.currentPage.toString())
            val page_list = Boss.getCurrentSource().makeRequest(req)
            val bm = BitmapFactory.decodeFile(page_list[0])
            uiThread { image!!.setImageBitmap(bm) }
        }
    }
    private fun move(forwards: Boolean): Boolean {
        doAsync {
            Boss.move(forwards)
            uiThread {
                updateImage()
            }
        }
        return true
    }
    override fun onShowPress(event: MotionEvent) {}
    override fun onLongPress(event: MotionEvent) {}
    override fun onDown(event: MotionEvent) = true
    override fun onScroll(e1: MotionEvent, e2:MotionEvent, dx: Float, y: Float) = true
    override fun onSingleTapUp(event: MotionEvent) = move(event.getX() < 350)
    override fun onFling(m1: MotionEvent, m2: MotionEvent, vx: Float, vy: Float) = move(vx > 0)
}
