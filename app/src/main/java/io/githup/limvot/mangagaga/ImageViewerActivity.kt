package io.githup.limvot.mangagaga

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

import android.os.Bundle
import android.os.Environment;
import android.app.Activity

import android.widget.ImageView

class ImageViewerActivity : Activity(), AnkoLogger {
    var image : ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        frameLayout {
            image = imageView {
                onClick { toast("Touched!") }
            }
        }
        updateImage()
    }
    fun updateImage() {
        doAsync {
            val imagePath = MangaManager.getCurrentPage()
            val bm = ImageManager.getNext(imagePath)
            uiThread {
                image!!.setImageBitmap(bm)
            }
        }
    }
}
