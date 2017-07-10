package io.githup.limvot.mangagaga

class Notification(title_in: String) {
    var title = title_in
    var text = "placeholder_text"
}

interface GenericLogger {
    fun info(it: String) {
    }
    fun notify(title: String) = Notification(title)
}
