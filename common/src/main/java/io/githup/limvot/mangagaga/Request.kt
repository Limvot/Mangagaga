package io.githup.limvot.mangagaga;

class Request {
    var source : String = ""
    var filter : String = ""
    var manga : String = ""
    var chapter : String = ""
    var page : String = ""
    override fun toString() : String = source + ":" + filter + ":" + manga +":"+ chapter +":"+ page
}
