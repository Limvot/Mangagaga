package io.githup.limvot.mangagaga;

import org.luaj.vm2.LuaTable;

class Chapter(val parentManga:Manga , val backingTable:LuaTable?, val num:Int) {
    // for compatibility with old saved json
    val table: LuaTable?
        get() { return backingTable }

    override fun toString(): String = getTitle()
    fun getTitle(): String = table?.get("title")?.toString() ?: "title of chapter with null table"

    override fun equals(other:Any?) : Boolean {
        // One shudders at the horror that this is
        return toString().equals(other.toString())
    }
}
