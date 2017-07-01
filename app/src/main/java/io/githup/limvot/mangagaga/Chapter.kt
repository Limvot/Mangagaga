package io.githup.limvot.mangagaga;

import org.luaj.vm2.LuaTable;

class Chapter(val parentManga:Manga , val table:LuaTable, val num:Int) {

    override fun toString(): String = getTitle()
    fun getTitle(): String = table.get("title").toString()

    override fun equals(other:Any?) : Boolean {
        // One shudders at the horror that this is
        return toString().equals(other.toString())
    }
}
