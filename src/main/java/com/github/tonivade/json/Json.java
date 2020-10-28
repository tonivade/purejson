/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonElement.EMPTY_ARRAY;
import static com.github.tonivade.json.JsonElement.EMPTY_OBJECT;
import static com.github.tonivade.json.JsonElement.NULL;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.petitparser.context.Result;

import com.github.tonivade.json.JsonElement.JsonArray;
import com.github.tonivade.json.JsonElement.JsonNull;
import com.github.tonivade.json.JsonElement.JsonObject;
import com.github.tonivade.json.JsonPrimitive.JsonBoolean;
import com.github.tonivade.json.JsonPrimitive.JsonNumber;
import com.github.tonivade.json.JsonPrimitive.JsonString;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.Sequence;

@SuppressWarnings("preview")
public final class Json {

  private final Map<String, JsonAdapter<?>> adapters = new HashMap<>();
  
  public static String serialize(JsonElement element) {
    if (element instanceof JsonNull) {
      return "null";
    }
    if (element instanceof JsonObject o) {
      return o.values().entrySet().stream()
          .map(entry -> "\"%s\":%s".formatted(entry.getKey(), serialize(entry.getValue())))
          .collect(joining(",", "{", "}"));
    }
    if (element instanceof JsonArray a) {
      return a.elements().stream()
          .map(Json::serialize)
          .collect(joining(",", "[", "]"));
    }
    if (element instanceof JsonString s) {
      return "\"%s\"".formatted(s.value());
    }
    if (element instanceof JsonNumber n) {
      return String.valueOf(n.value());
    }
    if (element instanceof JsonBoolean b) {
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

  @SuppressWarnings("unchecked")
  public <T> T fromJson(JsonElement element, Type type) {
    if (element instanceof JsonNull) {
      return null;
    }
    var jsonAdapter = getAdapter(type);
    if (jsonAdapter != null) {
      return (T) jsonAdapter.decode(element);
    }
    throw new IllegalArgumentException("this should not happen");
  }

  public String toString(Object object) {
    return toString(object, object != null ? object.getClass() : null);
  }

  public String toString(Object object, Type type) {
    return serialize(toJson(object, type));
  }

  public JsonElement toJson(Object object, Type type) {
    if (object == null) {
      return NULL;
    }
    if (object instanceof Collection<?> collection && collection.isEmpty()) {
      return EMPTY_ARRAY;
    }
    if (object instanceof Sequence<?> sequence && sequence.isEmpty()) {
      return EMPTY_ARRAY;
    }
    if (object instanceof Map<?, ?> map && map.isEmpty()) {
      return EMPTY_OBJECT;
    }
    if (object instanceof ImmutableMap<?, ?> map && map.isEmpty()) {
      return EMPTY_OBJECT;
    }
    var jsonAdapter = getAdapter(type);
    if (jsonAdapter != null) {
      return jsonAdapter.encode(object);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private <T> JsonAdapter<T> getAdapter(Type type) {
    var jsonAdapter = adapters.get(type.getTypeName());
    if (jsonAdapter == null) {
      jsonAdapter = JsonAdapter.create(type);
    }
    return (JsonAdapter<T>) jsonAdapter;
  }

  public <T> Json add(Type type, JsonAdapter<T> adapter) {
    adapters.put(type.getTypeName(), adapter);
    return this;
  }
}
