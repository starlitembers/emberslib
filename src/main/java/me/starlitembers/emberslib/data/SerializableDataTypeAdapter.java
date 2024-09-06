package me.starlitembers.emberslib.data;

import com.google.gson.*;

import java.lang.reflect.Type;

public class SerializableDataTypeAdapter implements JsonSerializer<Object>, JsonDeserializer<Object> {

    private static final String CLASSNAME = "class";
    private static final String DATA = "data";

    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = prim.getAsString();
        Class<?> clazz = getClazz(className);
        return jsonDeserializationContext.deserialize(jsonObject.get(DATA), clazz);
    }
    public JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(CLASSNAME, object.getClass().getName());
        jsonObject.add(DATA, jsonSerializationContext.serialize(object, object.getClass()));
        return jsonObject;
    }
    public Class<?> getClazz(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | ClassCastException e){
            throw new JsonParseException(e);
        }
    }
}
