package io.githup.limvot.mangaapp;

import org.luaj.vm2.LuaTable;

class Manga(sourceNumber:Int, backingTable:LuaTable) {
  
    override def toString() : String = backingTable.get("title").toString()

    def getTitle() : String = backingTable.get("title").toString()

    def getDescription() : String = {
        val description = backingTable.get("description").toString()
        if (description == "nil")
            "No Description"
        else
            description
    }

    def getTable() : LuaTable = backingTable

    def getSourceNumber() : Int = sourceNumber
}
