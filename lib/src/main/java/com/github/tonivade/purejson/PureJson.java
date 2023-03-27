/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purejson.JsonAdapter.adapter;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import com.eclipsesource.json.JsonParser;
import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

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

  public static Try<Unit> serialize(JsonNode node, Writer writer) {
    return node.writeTo(writer);
  }

  public static Try<JsonNode> parse(String json) {
    return Option.of(json).fold(Try::<String>illegalArgumentException, Try::success)
        .flatMap(PureJson::tryParse);
  }

  public static Try<JsonNode> parse(Reader json) {
    return Option.of(json).fold(Try::<Reader>illegalArgumentException, Try::success)
        .flatMap(PureJson::tryParse);
  }

  public Try<Option<T>> fromJson(String json) {
    return parse(json).flatMap(this::fromJson);
  }

  public Try<Option<T>> fromJson(JsonNode node) {
    if (node instanceof JsonNode.JsonNull) {
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

  private static Try<JsonNode> tryParse(String json) {
    return Try.of(() -> {
      var handler = new PureJsonHandler();
      new JsonParser(handler).parse(json);
      return handler.getValue();
    });
  }

  private static Try<JsonNode> tryParse(Reader reader) {
    return Try.of(() -> {
      var handler = new PureJsonHandler();
      new JsonParser(handler).parse(reader);
      return handler.getValue();
    });
  }
}
