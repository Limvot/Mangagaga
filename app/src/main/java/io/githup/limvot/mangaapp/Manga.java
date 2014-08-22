package io.githup.limvot.mangaapp;

import org.luaj.vm2.LuaTable;

/**
 * Created by nathan on 8/22/14.
 */
public class Manga {
    private LuaTable backingTable;
    public Manga(LuaTable tableIn) {
        backingTable = tableIn;
    }
    public String toString() {
        return backingTable.get("title").toString();
    }
    public LuaTable getTable() {
        return backingTable;
    }
}
