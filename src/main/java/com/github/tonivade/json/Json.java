/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.google.gson.JsonParser;

public final class Json {

  private final Map<String, JsonAdapter<?>> adapters = new HashMap<>();
  
  public static Try<String> serialize(JsonNode node) {
    return Try.of(node::toString);
  }

  public static Try<JsonNode> parse(String json) {
    return Try.of(() -> JsonParser.parseString(json)).map(JsonNode::from);
  }

  public <T> Try<Option<T>> fromJson(String json, Type type) {
    return parse(json).flatMap(node -> fromJson(node, type));
  }

  public <T> Try<Option<T>> fromJson(JsonNode node, Type type) {
    if (node instanceof JsonNode.Null) {
      return Try.success(Option.none());
    }
    return this.<T>getAdapter(type).map(adapter -> Option.of(adapter.decode(node)));
  }

  public Try<String> toString(Object object) {
    return toString(object, object != null ? object.getClass() : Void.class);
  }

  public Try<String> toString(Object object, Type type) {
    return toJson(object, type).flatMap(Json::serialize);
  }

  public Try<JsonNode> toJson(Object object, Type type) {
    if (object == null) {
      return Try.success(JsonNode.NULL);
    }
    return getAdapter(type).map(adapter -> adapter.encode(object));
  }

  public <T> Json add(Type type, JsonAdapter<T> adapter) {
    adapters.put(type.getTypeName(), adapter);
    return this;
  }

  @SuppressWarnings("unchecked")
  private <T> Try<JsonAdapter<T>> getAdapter(Type type) {
    return (Try<JsonAdapter<T>>) Option.of(adapters.get(type.getTypeName()))
        .fold(() -> Try.of(() -> JsonAdapter.create(type)), Try::success);
  }
}
