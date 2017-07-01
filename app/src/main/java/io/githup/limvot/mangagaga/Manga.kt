package io.githup.limvot.mangagaga;

import org.luaj.vm2.LuaTable;

class Manga(val sourceNumber:Int, val backingTable:LuaTable?) {
    // for compatibility with old saved json
    val table: LuaTable?
        get() { return backingTable }
  
    override fun toString() : String = "source: $sourceNumber - ${getTitle()}"

    fun getTitle() : String = table?.get("title")?.toString() ?: "title of null table"

    fun getDescription() : String {
        val description = table?.get("description")?.toString()
        return if (description == null || description == "nil")
            "No Description"
        else
            description
    }
}
