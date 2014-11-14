package io.githup.limvot.mangaapp

import org.scaloid.common._
import android.graphics.Color

class ScalaActivity extends SActivity {
  onCreate {
    contentView = new SVerticalLayout {
      STextView("Hellooo")
      SButton("O ooo")
    }
  }
}


object ScalaClassThing {
  implicit val tag = LoggerTag("Mangagaga")
  warn("It is time")
  def doThing() {
    warn("Scala and Scaloid for the win")
  }
}
