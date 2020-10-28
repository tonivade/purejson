/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import java.util.AbstractMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class JsonDSL {
  
  public static final JsonElement NULL = JsonNull.INSTANCE;
  
  public static JsonElement array(JsonElement... elements) {
    var array = new JsonArray();
    for (JsonElement jsonElement : elements) {
      array.add(jsonElement);
    }
    return array;
  }

  @SafeVarargs
  public static JsonElement object(Map.Entry<String, JsonElement>... elements) {
    var object = new JsonObject();
    for (Map.Entry<String, JsonElement> entry : elements) {
      object.add(entry.getKey(), entry.getValue());
    }
    return object;
  }
  
  public static Map.Entry<String, JsonElement> entry(String name, JsonElement value) {
    return new AbstractMap.SimpleImmutableEntry<>(name, value);
  }

  public static JsonElement string(String value) {
    return new JsonPrimitive(value);
  }

  public static JsonElement number(Number value) {
    return new JsonPrimitive(value);
  }

  public static JsonElement bool(Boolean value) {
    return new JsonPrimitive(value);
  }
}
