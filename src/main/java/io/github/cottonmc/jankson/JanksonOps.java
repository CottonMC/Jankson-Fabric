package io.github.cottonmc.jankson;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A DynamicOps instance for Jankson.
 * Some methods are based on Mojang's JsonOps for Gson.
 */
public class JanksonOps implements DynamicOps<JsonElement> {
    public static final JanksonOps INSTANCE = new JanksonOps(false);
    public static final JanksonOps COMPRESSED = new JanksonOps(true);

    private final boolean compressed;

    /**
     * Constructs a JanksonOps instance.
     *
     * @param compressed true if strings should be treated as valid numbers,
     *                   other primitives should be treated as valid strings,
     *                   and maps {@linkplain DynamicOps#compressMaps() should be compressed}.
     */
    protected JanksonOps(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public JsonElement empty() {
        return JsonNull.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, JsonElement input) {
        if (input instanceof JsonObject) {
            return convertMap(outOps, input);
        } else if (input instanceof JsonArray) {
            return convertList(outOps, input);
        } else if (input instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) input).getValue();

            if (value instanceof Byte) {
                return outOps.createByte((Byte) value);
            } else if (value instanceof Short) {
                return outOps.createShort((Short) value);
            } else if (value instanceof Integer) {
                return outOps.createInt((Integer) value);
            } else if (value instanceof Long) {
                return outOps.createLong((Long) value);
            } else if (value instanceof Float) {
                return outOps.createFloat((Float) value);
            } else if (value instanceof Double) {
                return outOps.createDouble((Double) value);
            } else if (value instanceof Number) {
                return outOps.createNumeric((Number) value);
            } else if (value instanceof Boolean) {
                return outOps.createBoolean((Boolean) value);
            } else if (value instanceof String) {
                return outOps.createString((String) value);
            }
        }

        return outOps.empty();
    }

    @Override
    public DataResult<Number> getNumberValue(JsonElement input) {
        if (input instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) input).getValue();
            if (value instanceof Number) {
                return DataResult.success((Number) value);
            } else if (value instanceof Boolean) {
                return DataResult.success((Boolean) value ? 1 : 0);
            } else if (compressed && value instanceof String) {
                // See JsonOps.getNumberValue
                try {
                    return DataResult.success(Integer.parseInt((String) value));
                } catch (final NumberFormatException e) {
                    return DataResult.error("Not a number: " + e + " " + input);
                }
            }
        }

        return DataResult.error("Not a number: " + input);
    }

    @Override
    public JsonElement createNumeric(Number i) {
        return new JsonPrimitive(i);
    }

    @Override
    public DataResult<String> getStringValue(JsonElement input) {
        if (input instanceof JsonPrimitive) {
            JsonPrimitive primitive = (JsonPrimitive) input;
            if (compressed || primitive.getValue() instanceof String) {
                return DataResult.success(primitive.asString());
            }
        }

        return DataResult.error("Not a string: " + input);
    }

    @Override
    public JsonElement createString(String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public JsonElement createBoolean(boolean value) {
        return value ? JsonPrimitive.TRUE : JsonPrimitive.FALSE;
    }

    @Override
    public DataResult<JsonElement> mergeToList(JsonElement list, JsonElement value) {
        if (list instanceof JsonNull) {
            JsonArray output = new JsonArray();
            output.add(value);
            return DataResult.success(output);
        } else if (list instanceof JsonArray) {
            JsonArray output = new JsonArray();
            output.addAll((JsonArray) list);
            output.add(value);
            return DataResult.success(output);
        }

        return DataResult.error("Not an array: " + list);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(JsonElement map, JsonElement key, JsonElement value) {
        if (!(key instanceof JsonPrimitive) || (!(((JsonPrimitive) key).getValue() instanceof String) && !compressed)) {
            return DataResult.error("Key is not a string: " + key);
        }

        if (map instanceof JsonNull) {
            JsonObject output = new JsonObject();
            output.put(((JsonPrimitive) key).asString(), value);
            return DataResult.success(output);
        } else if (map instanceof JsonObject) {
            JsonObject output = new JsonObject();
            output.put(((JsonPrimitive) key).asString(), value);
            output.putAll((JsonObject) map);
            return DataResult.success(output);
        } else {
            return DataResult.error("Not a JSON object: " + map);
        }
    }

    @Override
    public DataResult<JsonElement> mergeToMap(JsonElement map, MapLike<JsonElement> values) {
        if (!(map instanceof JsonObject) && !(map instanceof JsonNull)) {
            return DataResult.error("Not a JSON object: " + map);
        }

        JsonObject result = new JsonObject();
        if (map instanceof JsonObject) {
            result.putAll((JsonObject) map);
        }

        List<JsonElement> invalidKeys = new ArrayList<>();

        values.entries().forEach(entry -> {
            JsonElement key = entry.getFirst();

            if (!(key instanceof JsonPrimitive) || (!(((JsonPrimitive) key).getValue() instanceof String && !compressed))) {
                invalidKeys.add(key);
                return;
            }

            result.put(((JsonPrimitive) key).asString(), entry.getSecond());
        });

        if (!invalidKeys.isEmpty()) {
            return DataResult.error("Some keys are not strings: " + invalidKeys, result);
        }

        return DataResult.success(result);
    }

    @Override
    public DataResult<Stream<Pair<JsonElement, JsonElement>>> getMapValues(JsonElement input) {
        if (input instanceof JsonObject) {
            return DataResult.success(((JsonObject) input).entrySet().stream()
                    .map(entry -> new Pair<>(new JsonPrimitive(entry.getKey()), entry.getValue())));
        }
        return DataResult.error("Not a JSON object: " + input);
    }

    @Override
    public JsonElement createMap(Map<JsonElement, JsonElement> map) {
        JsonObject result = new JsonObject();
        for (Map.Entry<JsonElement, JsonElement> entry : map.entrySet()) {
            result.put(((JsonPrimitive) entry.getKey()).asString(), entry.getValue());
        }
        return result;
    }

    @Override
    public JsonElement createMap(Stream<Pair<JsonElement, JsonElement>> map) {
        JsonObject result = new JsonObject();
        map.forEach(pair -> result.put(((JsonPrimitive) pair.getFirst()).asString(), pair.getSecond()));
        return result;
    }

    @Override
    public DataResult<MapLike<JsonElement>> getMap(JsonElement input) {
        if (!(input instanceof JsonObject)) {
            return DataResult.error("Not a JSON object: " + input);
        }

        JsonObject obj = (JsonObject) input;
        return DataResult.success(new MapLike<JsonElement>() {
            @Override
            public JsonElement get(JsonElement key) {
                return obj.get(((JsonPrimitive) key).asString());
            }

            @Override
            public JsonElement get(String key) {
                return obj.get(key);
            }

            @Override
            public Stream<Pair<JsonElement, JsonElement>> entries() {
                return obj.entrySet().stream()
                        .map(entry -> new Pair<>(new JsonPrimitive(entry.getKey()), entry.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + obj + "]";
            }
        });
    }

    @Override
    public DataResult<Stream<JsonElement>> getStream(JsonElement input) {
        if (input instanceof JsonArray) {
            return DataResult.success(((JsonArray) input).stream());
        }
        return DataResult.error("Not an array: " + input);
    }

    @Override
    public JsonElement createList(Stream<JsonElement> input) {
        JsonArray result = new JsonArray();
        input.forEach(result::add);
        return result;
    }

    @Override
    public DataResult<Consumer<Consumer<JsonElement>>> getList(JsonElement input) {
        if (!(input instanceof JsonArray)) {
            return DataResult.error("Not an array: " + input);
        }

        return DataResult.success(sink -> {
            for (JsonElement element : (JsonArray) input) {
                sink.accept(element);
            }
        });
    }

    @Override
    public boolean compressMaps() {
        return compressed;
    }

    @Override
    public JsonElement emptyList() {
        return new JsonArray();
    }

    @Override
    public JsonElement emptyMap() {
        return new JsonObject();
    }

    @Override
    public JsonElement remove(JsonElement input, String key) {
        if (input instanceof JsonObject) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : ((JsonObject) input).entrySet()) {
                if (!entry.getKey().equals(key)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }
        return input;
    }

    @Override
    public String toString() {
        return "Jankson";
    }
}
