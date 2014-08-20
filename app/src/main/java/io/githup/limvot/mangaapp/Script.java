package io.githup.limvot.mangaapp;

import java.util.Arrays;
import java.util.List;

/**
 * Created by nathan on 8/20/14.
 */
class Script {
    private String name;
    private String luaCode;

    public Script(String name, String luaCode) {
        this.name = name;
        this.luaCode = luaCode;
    }

    public String getName() {
        return name;
    }

    public List<String> getMangaList() {
        String[] chapterList = new String[] { "One Piece" + name,
                "Naruto" + name, "Bleach" + name,
                "Belezebub" + name,
                "History's Strongest Disciple Kinichi" + name};
        return Arrays.asList(chapterList);
    }

}
