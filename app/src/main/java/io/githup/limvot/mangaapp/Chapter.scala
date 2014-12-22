package io.githup.limvot.mangaapp;

import org.luaj.vm2.LuaTable;

class Chapter(parentManga:Manga , backingTable:LuaTable, chapterNum:Int){

    override def toString(): String = getTitle()
    def getTitle(): String = backingTable.get("title").toString()

    def getTable() : LuaTable = backingTable

    def getNum() : Int = chapterNum
    
    def getParentManga() : Manga = parentManga

    override def equals(other:Any) : Boolean = {
        // One shudders at the horror that this is
        toString().equals(other.toString())
    }
}
