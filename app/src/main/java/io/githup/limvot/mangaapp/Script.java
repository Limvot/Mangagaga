package io.githup.limvot.mangaapp;

import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nathan on 8/20/14.
 */
class Script {
    private String name;
    private String luaCode;

    private Globals globals;
    private LuaValue luaGetMangaList;

    public Script(String name, String luaCode) {
        this.name = name;
        this.luaCode = luaCode;

        globals = JsePlatform.standardGlobals();
        globals.load(new StringReader(luaCode), name).call();
        // Call init function which normally saves this APIObject
        globals.get("init").call(CoerceJavaToLua.coerce(APIObject.getAPIObject()));
        luaGetMangaList = globals.get("getMangaList");
    }

    public String getName() {
        return name;
    }

    public List<String> getMangaList() {
        LuaValue result = luaGetMangaList.call();
        LuaTable resTable = result.checktable();

        ArrayList<String> mangaList = new ArrayList<String>();
        Log.i("getMangaList", "Woooooo: " + resTable.length());
        for (int i = 0; i <= resTable.length(); i++)
            mangaList.add(resTable.get(i).toString());

        return mangaList;

    }
}
