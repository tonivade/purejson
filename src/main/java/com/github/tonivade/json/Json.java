/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonElement.NULL;
import static com.github.tonivade.json.JsonElement.array;
import static com.github.tonivade.json.JsonElement.emptyObject;
import static com.github.tonivade.json.JsonElement.entry;
import static com.github.tonivade.json.JsonElement.object;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.petitparser.context.Result;

public final class Json {

  private final Map<String, JsonAdapter<?>> adapters = new HashMap<>();
  
  public static String serialize(JsonElement element) {
    if (element instanceof JsonElement.JsonNull) {
      return "null";
    }
    if (element instanceof JsonElement.JsonObject o) {
      return o.values().entrySet().stream()
          .map(entry -> "\"%s\":%s".formatted(entry.getKey(), serialize(entry.getValue())))
          .collect(joining(",", "{", "}"));
    }
    if (element instanceof JsonElement.JsonArray a) {
      return a.elements().stream()
          .map(Json::serialize)
          .collect(joining(",", "[", "]"));
    }
    if (element instanceof JsonPrimitive.JsonString s) {
      return "\"%s\"".formatted(s.value());
    }
    if (element instanceof JsonPrimitive.JsonNumber n) {
      return String.valueOf(n.value());
    }
    if (element instanceof JsonPrimitive.JsonBoolean b) {
      return String.valueOf(b.value());
    }
    throw new IllegalArgumentException("this should not happen");
  }

  public static JsonElement parse(String json) {
    Result result = new JsonParser().parse(json);

    if (result.isSuccess()) {
      return result.get();
    }

    throw new IllegalArgumentException(result.getMessage());
  }

  public <T> T fromJson(String json, Type type) {
    return fromJson(parse(json), type) ;
  }

  public <T> T fromJson(JsonElement element, Type type) {
    if (element instanceof JsonElement.JsonNull) {
      return null;
    }
    JsonAdapter<T> jsonAdapter = getAdapter(type);
    if (jsonAdapter != null) {
      return jsonAdapter.decode(element);
    }
    throw new IllegalArgumentException("this should not happen");
  }

  public String toString(Object object) {
    return serialize(toJson(object));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public JsonElement toJson(Object object) {
    if (object == null) {
      return NULL;
    }
    if (object instanceof Iterable<?> iterable) {
      List<JsonElement> items = new ArrayList<>();
      for (Object item : iterable) {
        items.add(toJson(item));
      }
      return array(items);
    }
    if (object instanceof Map<?, ?> map && map.isEmpty()) {
      return emptyObject();
    }
    if (object instanceof Map<?, ?> map && map.keySet().stream().allMatch(key -> key instanceof String)) {
      List<Map.Entry<String, JsonElement>> entries = new ArrayList<>();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        entries.add(entry((String) entry.getKey(), toJson(entry.getValue())));
      }
      return object(entries);
    }
    JsonAdapter jsonAdapter = getAdapter(object.getClass());
    if (jsonAdapter != null) {
      return jsonAdapter.encode(object);
    }
    throw new UnsupportedOperationException("not implemented yet");
  }

  @SuppressWarnings("unchecked")
  private <T> JsonAdapter<T> getAdapter(Type type) {
    JsonAdapter<T> jsonAdapter = (JsonAdapter<T>) adapters.get(type.getTypeName());
    if (jsonAdapter == null) {
      jsonAdapter = JsonAdapter.create(type);
      add(type, jsonAdapter);
    }
    return jsonAdapter;
  }

  public <T> Json add(Type type, JsonAdapter<T> adapter) {
    adapters.put(type.getTypeName(), adapter);
    return this;
  }
}
