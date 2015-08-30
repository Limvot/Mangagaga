package io.githup.limvot.mangagaga;

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

import scala.util.control.Breaks._
import scala.collection.JavaConversions._

/**
 * Created by marcus on 12/19/14
 */

object LuaTableSerializer {
    private def typedKey(key : LuaValue) : String = {
        var typedString = ""
        if (key.isboolean()) {
            typedString = "b"
        } else if (key.isnumber()) {
            typedString = "n"
        } else if (key.isstring()) {
            typedString = "s"
        } else if (key.istable()) {
            typedString = "TABLE_UNSUPPORTED"
            Log.i("TYPED_KEY", "DOES NOT SUPPORT TABLES AS KEYS")
        } else {
            typedString = "BAD_TYPE"
        }
        return typedString + key.toString()
    }

    def decodeTypedString(typedString : String) : LuaValue = {
        var typeChar = typedString.charAt(0)
        var valueString = typedString.substring(1)
        var value : LuaValue = null
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
            Log.i("DECODE_TYPED_KEYS", "DOES NOT SUPPORT TABLES AS KEYS");
        } else {
            value = LuaValue.NIL;
        }
        return value;
    }
}

class LuaTableSerializer() extends JsonSerializer[LuaTable] with JsonDeserializer[LuaTable] {
    
    def serialize(src : LuaTable, typeOfSrc : Type, context : JsonSerializationContext) : JsonElement = {
        val jsonObj = new JsonObject()
        var k : LuaValue = LuaValue.NIL
        breakable { while(true) {
            var n : Varargs = src.next(k)
            k = n.arg1().asInstanceOf[LuaValue]
            if (k.isnil()) break
            var v : LuaValue = n.arg(2)
            var jsonVal : JsonElement = null;
            if (v.isboolean()) {
                jsonVal = new JsonPrimitive(v.toboolean())
            } else if (v.isnumber()) {
                jsonVal = new JsonPrimitive(v.todouble())
            } else if (v.isstring()) {
                jsonVal = new JsonPrimitive(v.toString())
            } else if (v.istable()) {
                jsonVal = context.serialize(v.checktable(), classOf[LuaTable])
            } else {
                // We do not handle options such as userdata, functions, closures, etc
                // We may add some of these eventually
                jsonVal = JsonNull.INSTANCE

            }
            jsonObj.add(LuaTableSerializer.typedKey(k), jsonVal)
        } }
        return jsonObj
    }

    def deserialize(json : JsonElement, typeOfT : Type, context : JsonDeserializationContext) : LuaTable = {
        var tableObj : JsonObject = json.getAsJsonObject()
        var result : LuaTable = new LuaTable()
        for (entry <- tableObj.entrySet()) {
            var jsonVal : JsonElement = entry.getValue().asInstanceOf[JsonElement]
            var value : LuaValue = null
            if (jsonVal.isJsonPrimitive()) {
                var primitive : JsonPrimitive = jsonVal.getAsJsonPrimitive()
                if (primitive.isBoolean()) {
                    value = LuaValue.valueOf(primitive.getAsBoolean())
                } else if (primitive.isNumber()) {
                    value = LuaValue.valueOf(primitive.getAsDouble())
                } else if (primitive.isString()) {
                    value = LuaValue.valueOf(primitive.getAsString())
                } else {
                    Log.i("LUA_TABLE_SERILIZER", "JsonPrimative is not a bool, number, or string")
                    value = LuaValue.NIL
                }
            } else if (jsonVal.isJsonObject()) {
                // JSON objects in the context of a LuaTable are other LuaTables
                // (This excludes Java objects saved in LuaTables, which we do not support)
                value = context.deserialize(jsonVal, classOf[LuaTable])
            } else {
                Log.i("LUA_TABLE_SERILIZER", "Is neither a JsonPrimitive or a JsonObject")
                value = LuaValue.NIL
            }
            result.set(LuaTableSerializer.decodeTypedString(entry.getKey().asInstanceOf[String]), value)
        }
        return result
    }
}
