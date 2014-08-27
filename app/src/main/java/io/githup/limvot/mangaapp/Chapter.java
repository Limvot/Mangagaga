package io.githup.limvot.mangaapp;

import org.luaj.vm2.LuaTable;

/**
 * Created by nathan on 8/22/14.
 */
public class Chapter {
    private Manga parentManga;
    private LuaTable backingTable;
    private int chapterNum;
    public Chapter(Manga parent, LuaTable tableIn, int num) {
        parentManga = parent;
        backingTable = tableIn;
        chapterNum = num;
    }
    public String toString() {
        return backingTable.get("title").toString();
    }
    public LuaTable getTable() {
        return backingTable;
    }
    public int getNum() { return chapterNum; }
    public Manga getParentManga() {
        return parentManga;
    }
}
