package io.github.cottonmc.jankson;

import blue.endless.jankson.*;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A DynamicOps instance for Jankson. Loosely based on Mojang's JsonOps for Gson.
 */
public class JanksonOps implements DynamicOps<JsonElement> {
    public static final JanksonOps INSTANCE = new JanksonOps();

    protected JanksonOps() {}

    @Override
    public JsonElement empty() {
        return JsonNull.INSTANCE;
    }

    @Override
    public Type<?> getType(JsonElement input) {
        if (input == null) {
            throw new NullPointerException("input is null");
        } else if (input instanceof JsonObject) {
            return DSL.compoundList(DSL.remainderType(), DSL.remainderType());
        } else if (input instanceof JsonArray) {
            return DSL.list(DSL.remainderType());
        } else if (input instanceof JsonNull) {
            return DSL.nilType();
        } else if (input instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) input).getValue();

            if (value instanceof String) {
                return DSL.string();
            } else if (value instanceof Boolean) {
                return DSL.bool();
            } else if (value instanceof Short) {
                return DSL.shortType();
            } else if (value instanceof Integer) {
                return DSL.intType();
            } else if (value instanceof Long) {
                return DSL.longType();
            } else if (value instanceof Float) {
                return DSL.floatType();
            } else if (value instanceof Double) {
                return DSL.doubleType();
            } else {
                throw new IllegalArgumentException("Value of JsonPrimitive '" + input + "' has an unknown type: " + value.getClass().getName());
            }
        } else {
            throw new IllegalArgumentException("JsonElement '" + input + "' has an unknown type: " + input.getClass().getName());
        }
    }

    @Override
    public Optional<Number> getNumberValue(JsonElement input) {
        if (input instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) input).getValue();
            if (value instanceof Number) {
                return Optional.of((Number) value);
            } else if (value instanceof Boolean) {
                return Optional.of((Boolean) value ? 1 : 0);
            }
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createNumeric(Number i) {
        return new JsonPrimitive(i);
    }

    @Override
    public JsonElement createBoolean(boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Optional<String> getStringValue(JsonElement input) {
        if (input instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) input).getValue();
            if (value instanceof String) {
                return Optional.of((String) value);
            }
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createString(String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public JsonElement mergeInto(JsonElement input, JsonElement value) {
        if (value instanceof JsonNull) {
            return value;
        } else if (input instanceof JsonNull) {
            throw new IllegalArgumentException("mergeInto called with null input.");
        }

        if (input instanceof JsonObject) {
            if (value instanceof JsonObject) {
                JsonObject result = new JsonObject();
                result.putAll((JsonObject) input);
                result.putAll((JsonObject) value);
                return result;
            }
            return input;
        } else if (input instanceof JsonArray) {
            JsonArray result = new JsonArray();
            result.addAll((JsonArray) input);
            result.add(value);
            return result;
        } else {
            return input;
        }
    }

    @Override
    public JsonElement mergeInto(JsonElement input, JsonElement key, JsonElement value) {
        JsonObject output = new JsonObject();
        if (input instanceof JsonObject) {
            output.putAll((JsonObject) input);
        } else if (!(input instanceof JsonNull)) {
            return input;
        }

        output.put(((JsonPrimitive) key).asString(), value);
        return output;
    }

    @Override
    public JsonElement merge(JsonElement first, JsonElement second) {
        if (first instanceof JsonNull) {
            return second;
        } else if (second instanceof JsonNull) {
            return first;
        }

        if (first instanceof JsonObject && second instanceof JsonObject) {
            JsonObject result = new JsonObject();
            result.putAll((JsonObject) first);
            result.putAll((JsonObject) second);
            return result;
        } else if (first instanceof JsonArray && second instanceof JsonArray) {
            JsonArray result = new JsonArray();
            result.addAll((JsonArray) first);
            result.addAll((JsonArray) second);
            return result;
        }

        throw new IllegalArgumentException("Could not merge " + first + " and " + second);
    }

    @Override
    public Optional<Map<JsonElement, JsonElement>> getMapValues(JsonElement input) {
        if (input instanceof JsonObject) {
            JsonObject inputObj = (JsonObject) input;
            ImmutableMap.Builder<JsonElement, JsonElement> builder = ImmutableMap.builder();
            for (Map.Entry<String, JsonElement> entry : inputObj.entrySet()) {
                builder.put(new JsonPrimitive(entry.getKey()), entry.getValue());
            }
            return Optional.of(builder.build());
        }
        return Optional.empty();
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
    public Optional<Stream<JsonElement>> getStream(JsonElement input) {
        if (input instanceof JsonArray) {
            return Optional.of(((JsonArray) input).stream());
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createList(Stream<JsonElement> input) {
        JsonArray result = new JsonArray();
        input.forEach(result::add);
        return result;
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
