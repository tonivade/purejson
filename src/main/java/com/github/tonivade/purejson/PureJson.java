/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class PureJson {

  private final Map<String, JsonAdapter<?>> adapters = new HashMap<>();
  
  public static Try<String> serialize(JsonNode node) {
    return Try.of(node::toString);
  }

  public static Try<JsonNode> parse(String json) {
    return Option.of(json).fold(Try::<String>illegalArgumentException, Try::success)
        .flatMap(PureJson::tryParse)
        .map(JsonNode::from);
  }

  public <T> Try<Option<T>> fromJson(String json, Class<T> type) {
    return fromJson(json, (Type) type);
  }

  public <T> Try<Option<T>> fromJson(String json, Type type) {
    return parse(json).flatMap(node -> fromJson(node, type));
  }

  public <T> Try<Option<T>> fromJson(JsonNode node, Type type) {
    if (node instanceof JsonNode.Null) {
      return Try.success(Option.none());
    }
    return this.<T>getAdapter(type)
        .flatMap(adapter -> adapter.tryDecode(node)).map(Option::some);
  }

  public Try<String> toString(Object object) {
    return toString(object, object != null ? object.getClass() : Nothing.class);
  }

  public Try<String> toString(Object object, Type type) {
    return toJson(object, type).flatMap(PureJson::serialize);
  }

  public Try<JsonNode> toJson(Object object, Type type) {
    if (object == null) {
      return Try.success(JsonNode.NULL);
    }
    return getAdapter(type).flatMap(adapter -> adapter.tryEncode(object));
  }

  public <T> PureJson add(Type type, JsonAdapter<T> adapter) {
    adapters.put(type.getTypeName(), adapter);
    return this;
  }

  @SuppressWarnings("unchecked")
  private <T> Try<JsonAdapter<T>> getAdapter(Type type) {
    return (Try<JsonAdapter<T>>) Option.of(adapters.get(type.getTypeName()))
        .fold(() -> Try.of(() -> JsonAdapter.adapter(type)), Try::success);
  }

  private static Try<JsonElement> tryParse(String json) {
    return Try.of(() -> JsonParser.parseString(json));
  }
}
