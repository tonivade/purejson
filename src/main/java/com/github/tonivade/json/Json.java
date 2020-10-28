/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;

public final class Json {

  private final Map<String, JsonAdapter<?>> adapters = new HashMap<>();
  
  public static String serialize(JsonElement element) {
    return element.toString();
  }

  public static JsonElement parse(String json) {
    return JsonParser.parseString(json);
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
    return toString(object, object != null ? object.getClass() : Void.class);
  }

  public String toString(Object object, Type type) {
    return serialize(toJson(object, type));
  }

  public JsonElement toJson(Object object, Type type) {
    if (object == null) {
      return JsonNull.INSTANCE;
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
