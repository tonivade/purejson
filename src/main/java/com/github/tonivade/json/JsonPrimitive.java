/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

public sealed interface JsonPrimitive extends JsonElement permits
    JsonPrimitive.JsonString, JsonPrimitive.JsonNumber, JsonPrimitive.JsonBoolean {

  JsonElement TRUE = new JsonPrimitive.JsonBoolean(true);
  JsonElement FALSE = new JsonPrimitive.JsonBoolean(false);

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

  static JsonElement string(String string) {
    return string == null ? NULL : new JsonString(string);
  }

  static JsonElement number(long value) {
    return new JsonNumber(value);
  }

  static JsonElement number(double value) {
    return new JsonNumber(value);
  }

  static JsonElement bool(boolean value) {
    return value ? TRUE : FALSE;
  }
}
