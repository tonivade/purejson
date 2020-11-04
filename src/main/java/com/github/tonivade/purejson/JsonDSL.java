/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class JsonDSL {
  
  public static JsonNode array(JsonNode... elements) {
    return array(List.of(elements));
  }

  public static JsonNode array(Iterable<JsonNode> elements) {
    var array = new JsonArray();
    for (var node : elements) {
      array.add(node.unwrap());
    }
    return new JsonNode.Array(array);
  }

  @SafeVarargs
  public static JsonNode object(Map.Entry<String, JsonNode>... elements) {
    return object(List.of(elements));
  }
  
  public static JsonNode object(Map<String, JsonNode> elements) {
    return object(elements.entrySet());
  }

  public static JsonNode object(Iterable<Map.Entry<String, JsonNode>> elements) {
    var object = new JsonObject();
    for (var entry : elements) {
      object.add(entry.getKey(), entry.getValue().unwrap());
    }
    return new JsonNode.Object(object);
  }
  
  public static Map.Entry<String, JsonNode> entry(String name, JsonNode value) {
    return new AbstractMap.SimpleImmutableEntry<>(name, value);
  }

  public static JsonNode string(String value) {
    return new JsonNode.Primitive(value);
  }

  public static JsonNode number(Number value) {
    return new JsonNode.Primitive(value);
  }

  public static JsonNode bool(Boolean value) {
    return new JsonNode.Primitive(value);
  }
}
