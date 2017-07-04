package io.githup.limvot.mangagaga;

import org.jetbrains.anko.*

import com.google.gson.*

import org.luaj.vm2.*

import java.lang.reflect.Type

/**
 * Created by marcus on 12/19/14
 * Ported to Kotlin by nathan on 6/30/17 - 7/4/17
 */

class LuaTableSerializer() : JsonSerializer<LuaTable>, JsonDeserializer<LuaTable>, AnkoLogger {
    companion object LuaTableSerializer : AnkoLogger {
        private fun typedKey(key : LuaValue) = when {
            key.isboolean() -> "b"
            key.isnumber()  -> "n"
            key.isstring()  -> "s"
            else            -> "BAD_TYPE"
        } + key.toString()

        fun decodeTypedString(typedString : String) : LuaValue {
            var valueString = typedString.substring(1)
            return when (typedString[0]) {
                'b' -> if (valueString == "true") LuaValue.TRUE else LuaValue. FALSE
                'n' -> LuaValue.valueOf(java.lang.Double.parseDouble(valueString))
                's' -> LuaValue.valueOf(valueString)
                else-> LuaValue.NIL
            }
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
            var jsonVal = when {
                v.isboolean() -> JsonPrimitive(v.toboolean())
                v.isnumber()  -> JsonPrimitive(v.todouble())
                v.isstring()  -> JsonPrimitive(v.toString())
                v.istable()   -> context.serialize(v.checktable(), LuaTable::class.java)
                else          -> JsonNull.INSTANCE
            }
            jsonObj.add(LuaTableSerializer.typedKey(k), jsonVal)
        }
        return jsonObj
    }

    override fun deserialize(json : JsonElement, typeOfT : Type, context : JsonDeserializationContext) : LuaTable {
        var result : LuaTable = LuaTable()
        for (entry in json.getAsJsonObject().entrySet()) {
            var jsonVal = entry.value
            var value  = when {
                jsonVal.isJsonPrimitive() -> { val primitive = jsonVal.getAsJsonPrimitive(); when {
                            primitive.isBoolean() -> LuaValue.valueOf(primitive.getAsBoolean())
                            primitive.isNumber()  -> LuaValue.valueOf(primitive.getAsDouble())
                            primitive.isString()  -> LuaValue.valueOf(primitive.getAsString())
                            else                  -> LuaValue.NIL
                    } }
                jsonVal.isJsonObject()    -> context.deserialize(jsonVal, LuaTable::class.java)
                else                      -> LuaValue.NIL
            }
            result.set(LuaTableSerializer.decodeTypedString(entry.key as String), value)
        }
        return result
    }
}
