/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.lang.reflect.Type;

public interface JsonContext {

  <T> JsonAdapter<T> getAdapter(Type type);

  default <T> JsonNode encode(T value, Type type) {
    return getAdapter(type).encode(this, value);
  }

  @SuppressWarnings("unchecked")
  default <T> T decode(JsonNode json, Type type) {
    return (T) getAdapter(type).decode(this, json);
  }
}
