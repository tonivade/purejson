/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.Sequence;

import java.util.List;
import java.util.Map;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

public sealed interface JsonElement permits
    JsonElement.JsonNull, JsonElement.JsonPrimitive, JsonElement.JsonArray, JsonElement.JsonObject {

  JsonElement NULL = new JsonNull();
  JsonElement TRUE = new JsonPrimitive.JsonBoolean(true);
  JsonElement FALSE = new JsonPrimitive.JsonBoolean(false);

  final class JsonNull implements JsonElement {

    private JsonNull() {}

    @Override
    public String toString() {
      return "JsonNull";
    }
  }

  record JsonArray(Sequence<? extends JsonElement> elements) implements JsonElement {
    public JsonArray {
      checkNonNull(elements);
    }
  }

  record JsonObject(ImmutableMap<String, ? extends JsonElement> values) implements JsonElement {
    public JsonObject {
      checkNonNull(values);
    }
  }

  sealed interface JsonPrimitive extends JsonElement permits
      JsonPrimitive.JsonString, JsonPrimitive.JsonNumber, JsonPrimitive.JsonBoolean {

    record JsonString(String value) implements JsonPrimitive {
      public JsonString {
        checkNonNull(value);
      }
    }

    record JsonNumber(Number value) implements JsonPrimitive {
      public JsonNumber {
        checkNonNull(value);
      }
    }

    record JsonBoolean(boolean value) implements JsonPrimitive {}
  }

  static JsonElement string(String string) {
    return string == null ? NULL : new JsonPrimitive.JsonString(string);
  }

  static JsonElement number(long value) {
    return new JsonPrimitive.JsonNumber(value);
  }

  static JsonElement number(double value) {
    return new JsonPrimitive.JsonNumber(value);
  }

  static JsonElement bool(boolean value) {
    return value ? TRUE : FALSE;
  }

  static JsonElement emptyObject() {
    return new JsonObject(ImmutableMap.empty());
  }

  static JsonElement object(Map<String, JsonElement> elements) {
    return elements == null ? NULL : new JsonObject(ImmutableMap.from(elements));
  }

  @SafeVarargs
  static JsonElement object(Tuple2<String, JsonElement>... elements) {
    return elements == null ? NULL : new JsonObject(ImmutableMap.of(elements));
  }

  static JsonElement emptyArray() {
    return new JsonArray(ImmutableList.empty());
  }

  static JsonElement array(List<JsonElement> elements) {
    return elements == null ? NULL : new JsonArray(ImmutableList.from(elements));
  }

  static JsonElement array(JsonElement... elements) {
    return elements == null ? NULL : new JsonArray(ImmutableList.of(elements));
  }
}

