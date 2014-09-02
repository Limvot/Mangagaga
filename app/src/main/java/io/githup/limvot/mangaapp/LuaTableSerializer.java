package io.githup.limvot.mangaapp;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by nathan on 8/29/14.
 */
public class LuaTableSerializer implements JsonSerializer<LuaTable>, JsonDeserializer<LuaTable> {
    private static String typedKey(LuaValue key) {
        String typedString;
        if (key.isboolean()) {
            typedString = "b";
        } else if (key.isnumber()) {
            typedString = "n";
        } else if (key.isstring()) {
            typedString = "s";
        } else if (key.istable()) {
            typedString = "TABLE_UNSUPPORTED";
            Log.i("TYPED_KEY", "DOES NOT SUPPORT TABLES AS KEYS");
        } else {
            typedString = "BAD_TYPE";
        }
        return typedString + key.toString();
    }
    public static LuaValue decodeTypedString(String typedString) {
        char typeChar = typedString.charAt(0);
        String valueString = typedString.substring(1);
        LuaValue value;
        if (typeChar == 'b') {
            if (typedString.equals("true"))
                value = LuaBoolean.TRUE;
            else
                value = LuaBoolean.FALSE;
        } else if (typeChar == 'n') {
            value = LuaValue.valueOf(Double.parseDouble(valueString));
        } else if (typeChar == 's') {
            value = LuaValue.valueOf(valueString);
        } else if (typeChar == 't') {
            value = LuaValue.NIL;
            Log.i("DECODE_TYPED_KEYS", "DOES NOT SUPPORT TABLES AS KEYS");
        } else {
            value = LuaValue.NIL;
        }
        return value;
    }
    public JsonElement serialize(LuaTable src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObj = new JsonObject();
        LuaValue k = LuaValue.NIL;
        while(true) {
            Varargs n = src.next(k);
            if ((k = n.arg1()).isnil())
                break;
            LuaValue v = n.arg(2);
            JsonElement jsonVal;
            if (v.isboolean()) {
                jsonVal = new JsonPrimitive(v.toboolean());
            } else if (v.isnumber()) {
                jsonVal = new JsonPrimitive(v.todouble());
            } else if (v.isstring()) {
                jsonVal = new JsonPrimitive(v.toString());
            } else if (v.istable()) {
                jsonVal = context.serialize(v.checktable(), LuaTable.class);
            } else {
                // We do not handle options such as userdata, functions, closures, etc
                // We may add some of these eventually
                jsonVal = JsonNull.INSTANCE;

            }
            jsonObj.add(typedKey(k), jsonVal);
        }
        return jsonObj;
    }

    public LuaTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject tableObj = json.getAsJsonObject();
        LuaTable result = new LuaTable();
        for (Map.Entry<String, JsonElement> entry : tableObj.entrySet()) {
            JsonElement jsonVal = entry.getValue();
            LuaValue value;
            if (jsonVal.isJsonPrimitive()) {
                JsonPrimitive primitive = jsonVal.getAsJsonPrimitive();
                if (primitive.isBoolean()) {
                    value = LuaValue.valueOf(primitive.getAsBoolean());
                } else if (primitive.isNumber()) {
                    value = LuaValue.valueOf(primitive.getAsDouble());
                } else if (primitive.isString()) {
                    value = LuaValue.valueOf(primitive.getAsString());
                } else {
                    Log.i("LUA_TABLE_SERILIZER", "JsonPrimative is not a bool, number, or string");
                    value = LuaValue.NIL;
                }
            } else if (jsonVal.isJsonObject()) {
                // JSON objects in the context of a LuaTable are other LuaTables
                // (This excludes Java objects saved in LuaTables, which we do not support)
                value = context.deserialize(jsonVal, LuaTable.class);
            } else {
                Log.i("LUA_TABLE_SERILIZER", "Is neither a JsonPrimitive or a JsonObject");
                value = LuaValue.NIL;
            }
            result.set(decodeTypedString(entry.getKey()), value);
        }
        return result;
    }
}
