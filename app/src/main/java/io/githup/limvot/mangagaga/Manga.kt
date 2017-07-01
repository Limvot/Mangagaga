package io.githup.limvot.mangagaga;

import org.luaj.vm2.LuaTable;

class Manga(val sourceNumber:Int, val table:LuaTable) {
  
    override fun toString() : String = table.get("title").toString()

    fun getTitle() : String = table.get("title").toString()

    fun getDescription() : String {
        val description = table.get("description").toString()
        return if (description == "nil")
            "No Description"
        else
            description
    }
}
