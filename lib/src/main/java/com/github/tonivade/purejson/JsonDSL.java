/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.util.List;
import java.util.Map;

import com.github.tonivade.purejson.JsonNode.JsonArray;
import com.github.tonivade.purejson.JsonNode.JsonNumber;
import com.github.tonivade.purejson.JsonNode.JsonObject;
import com.github.tonivade.purejson.JsonNode.JsonString;
import com.github.tonivade.purejson.JsonNode.Tuple;

public final class JsonDSL {

  private JsonDSL() {}

  public static JsonNode array(JsonNode... elements) {
    return array(List.of(elements));
  }

  public static JsonNode array(Iterable<JsonNode> elements) {
    var array = new JsonArray();
    elements.forEach(array::add);
    return array;
  }

  public static JsonNode object(Tuple... elements) {
    return object(List.of(elements));
  }

  public static JsonNode object(Map<String, JsonNode> elements) {
    var object = new JsonObject();
    elements.forEach(object::add);
    return object;
  }

  public static JsonNode object(Iterable<Tuple> elements) {
    var object = new JsonObject();
    elements.forEach(object::add);
    return object;
  }

  public static Tuple entry(String name, JsonNode value) {
    return new Tuple(name, value);
  }

  public static JsonNode string(String value) {
    return new JsonString(value);
  }

  public static JsonNode number(int value) {
    return new JsonNumber(value);
  }

  public static JsonNode number(long value) {
    return new JsonNumber(value);
  }

  public static JsonNode number(float value) {
    return new JsonNumber(value);
  }

  public static JsonNode number(double value) {
    return new JsonNumber(value);
  }

  public static JsonNode bool(boolean value) {
    return value ? JsonNode.TRUE : JsonNode.FALSE;
  }
}
