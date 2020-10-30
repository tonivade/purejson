/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonParser;

public final class Json {

  private final Map<String, JsonAdapter<?>> adapters = new HashMap<>();
  
  public static String serialize(JsonNode element) {
    return element.toString();
  }

  public static JsonNode parse(String json) {
    return JsonNode.from(JsonParser.parseString(json));
  }

  public <T> T fromJson(String json, Type type) {
    return fromJson(parse(json), type) ;
  }

  @SuppressWarnings("unchecked")
  public <T> T fromJson(JsonNode element, Type type) {
    if (element instanceof JsonNode.Null) {
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

  public JsonNode toJson(Object object, Type type) {
    if (object == null) {
      return JsonNode.NULL;
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
