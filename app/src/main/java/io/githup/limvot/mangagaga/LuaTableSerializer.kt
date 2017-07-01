package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

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

/**
 * Created by marcus on 12/19/14
 */

class LuaTableSerializer() : JsonSerializer<LuaTable>, JsonDeserializer<LuaTable>, AnkoLogger {
    companion object LuaTableSerializer : AnkoLogger {
        private fun typedKey(key : LuaValue) : String {
            var typedString = ""
            if (key.isboolean()) {
                typedString = "b"
            } else if (key.isnumber()) {
                typedString = "n"
            } else if (key.isstring()) {
                typedString = "s"
            } else if (key.istable()) {
                typedString = "TABLE_UNSUPPORTED"
                info("TYPED_KEY - DOES NOT SUPPORT TABLES AS KEYS")
            } else {
                typedString = "BAD_TYPE"
            }
            return typedString + key.toString()
        }

        fun decodeTypedString(typedString : String) : LuaValue {
            var typeChar = typedString[0]
            var valueString = typedString.substring(1)
            var value : LuaValue = LuaValue.NIL
            if (typeChar == 'b') {
                if (typedString.equals("true"))
                    value = LuaValue.TRUE
                else
                    value = LuaValue.FALSE
            } else if (typeChar == 'n') {
                value = LuaValue.valueOf(java.lang.Double.parseDouble(valueString));
            } else if (typeChar == 's') {
                value = LuaValue.valueOf(valueString);
            } else if (typeChar == 't') {
                value = LuaValue.NIL;
                info("DECODE_TYPED_KEYS - DOES NOT SUPPORT TABLES AS KEYS");
            }
            return value;
        }
    }
    
    override fun serialize(src : LuaTable, typeOfSrc : Type, context : JsonSerializationContext) : JsonElement {
        val jsonObj = JsonObject()
        var k : LuaValue = LuaValue.NIL
        while(true) {
            var n : Varargs = src.next(k)
            k = n.arg1() as LuaValue
            if (k.isnil()) break
            var v : LuaValue = n.arg(2)
            var jsonVal : JsonElement = JsonNull.INSTANCE;
            if (v.isboolean()) {
                jsonVal = JsonPrimitive(v.toboolean())
            } else if (v.isnumber()) {
                jsonVal = JsonPrimitive(v.todouble())
            } else if (v.isstring()) {
                jsonVal = JsonPrimitive(v.toString())
            } else if (v.istable()) {
                jsonVal = context.serialize(v.checktable(), LuaTable::class.java)
            } else {
                // We do not handle options such as userdata, functions, closures, etc
                // We may add some of these eventually
                jsonVal = JsonNull.INSTANCE

            }
            jsonObj.add(LuaTableSerializer.typedKey(k), jsonVal)
        }
        return jsonObj
    }

    override fun deserialize(json : JsonElement, typeOfT : Type, context : JsonDeserializationContext) : LuaTable {
        var tableObj : JsonObject = json.getAsJsonObject()
        var result : LuaTable = LuaTable()
        for (entry in tableObj.entrySet()) {
            var jsonVal : JsonElement = entry.value
            var value : LuaValue = LuaValue.NIL
            if (jsonVal.isJsonPrimitive()) {
                var primitive : JsonPrimitive = jsonVal.getAsJsonPrimitive()
                if (primitive.isBoolean()) {
                    value = LuaValue.valueOf(primitive.getAsBoolean())
                } else if (primitive.isNumber()) {
                    value = LuaValue.valueOf(primitive.getAsDouble())
                } else if (primitive.isString()) {
                    value = LuaValue.valueOf(primitive.getAsString())
                } else {
                    info("LUA_TABLE_SERILIZER - JsonPrimative is not a bool, number, or string")
                    value = LuaValue.NIL
                }
            } else if (jsonVal.isJsonObject()) {
                // JSON objects in the context of a LuaTable are other LuaTables
                // (This excludes Java objects saved in LuaTables, which we do not support)
                value = context.deserialize(jsonVal, LuaTable::class.java)
            } else {
                info("LUA_TABLE_SERILIZER - Is neither a JsonPrimitive or a JsonObject")
                value = LuaValue.NIL
            }
            result.set(LuaTableSerializer.decodeTypedString(entry.key as String), value)
        }
        return result
    }
}
