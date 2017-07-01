package io.githup.limvot.mangagaga

import android.os.Bundle
import android.os.Environment;
import android.app.Activity

import java.io.File;

import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class ImageViewerActivity : Activity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verticalLayout {
            button("placeholder") { onClick { toast("hi") } }
        }
    }
}
