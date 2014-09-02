package io.githup.limvot.mangaapp;

import org.luaj.vm2.LuaTable;

/**
 * Created by nathan on 8/22/14.
 */
public class Manga {
    private int sourceNumber;
    private LuaTable backingTable;
    public Manga(int sourceNum, LuaTable tableIn) {
        sourceNumber = sourceNum;
        backingTable = tableIn;
    }
    public String toString() {
        return backingTable.get("title").toString();
    }
    public String getTitle() {
        return backingTable.get("title").toString();
    }
    public String getDescription() {
        String description =  backingTable.get("description").toString();
        if (description == "nil")
            return "No Description";
        return description;
    }
    public LuaTable getTable() {
        return backingTable;
    }
    public int getSourceNumber() {
        return sourceNumber;
    }
}
