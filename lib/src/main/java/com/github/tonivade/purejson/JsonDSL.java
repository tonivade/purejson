/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonDSL {

  private JsonDSL() {}

  public static JsonNode array(JsonNode... elements) {
    return array(List.of(elements));
  }

  public static JsonNode array(Iterable<JsonNode> elements) {
    var array = new ArrayList<JsonNode>();
    for (var node : elements) {
      array.add(node);
    }
    return new JsonNode.JsonArray(array);
  }

  @SafeVarargs
  public static JsonNode object(Map.Entry<String, JsonNode>... elements) {
    return object(List.of(elements));
  }

  public static JsonNode object(Map<String, JsonNode> elements) {
    return object(elements.entrySet());
  }

  public static JsonNode object(Iterable<Map.Entry<String, JsonNode>> elements) {
    var object = new HashMap<String, JsonNode>();
    for (var entry : elements) {
      object.put(entry.getKey(), entry.getValue());
    }
    return new JsonNode.JsonObject(object);
  }

  public static Map.Entry<String, JsonNode> entry(String name, JsonNode value) {
    return new AbstractMap.SimpleImmutableEntry<>(name, value);
  }

  public static JsonNode string(String value) {
    return new JsonNode.JsonString(value);
  }

  public static JsonNode number(int value) {
    return new JsonNode.JsonNumber(value);
  }

  public static JsonNode number(long value) {
    return new JsonNode.JsonNumber(value);
  }

  public static JsonNode number(float value) {
    return new JsonNode.JsonNumber(value);
  }

  public static JsonNode number(double value) {
    return new JsonNode.JsonNumber(value);
  }

  public static JsonNode bool(boolean value) {
    return value ? JsonNode.TRUE : JsonNode.FALSE;
  }
}
