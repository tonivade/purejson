/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purejson.JsonAdapter.adapter;

import java.lang.reflect.Type;

import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class PureJson<T> {

  private final JsonAdapter<T> adapter;
  
  public PureJson(Type type) {
    this(adapter(type));
  }
  
  public PureJson(Class<T> type) {
    this(adapter(type));
  }
  
  public PureJson(JsonAdapter<T> adapter) {
    this.adapter = checkNonNull(adapter);
  }
  
  public static Try<String> serialize(JsonNode node) {
    return Try.of(node::toString);
  }

  public static Try<JsonNode> parse(String json) {
    return Option.of(json).fold(Try::<String>illegalArgumentException, Try::success)
        .flatMap(PureJson::tryParse)
        .map(JsonNode::from);
  }

  public Try<Option<T>> fromJson(String json) {
    return parse(json).flatMap(node -> fromJson(node));
  }

  public Try<Option<T>> fromJson(JsonNode node) {
    if (node instanceof JsonNode.Null) {
      return Try.success(Option.none());
    }
    return adapter.tryDecode(node).map(Option::some);
  }

  public Try<String> toString(T object) {
    return toJson(object).flatMap(PureJson::serialize);
  }

  public Try<JsonNode> toJson(T object) {
    if (object == null) {
      return Try.success(JsonNode.NULL);
    }
    return adapter.tryEncode(object);
  }

  private static Try<JsonElement> tryParse(String json) {
    return Try.of(() -> JsonParser.parseString(json));
  }
}
