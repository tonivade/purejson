/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import com.github.tonivade.purefun.Function1;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tonivade.json.JsonPrimitive.bool;
import static com.github.tonivade.json.JsonPrimitive.number;
import static com.github.tonivade.json.JsonPrimitive.string;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

public final class JsonAdapterBuilder<T> {

  private final Map<String, Function1<T, JsonElement>> encoders = new LinkedHashMap<>();
  private final Map<String, Function1<JsonElement, ?>> decoders = new LinkedHashMap<>();

  private final Class<T> type;

  public JsonAdapterBuilder(Class<T> type) {
    this.type = checkNonNull(type);
  }

  public JsonAdapterBuilder<T> addInteger(String name, Function1<T, Integer> accessor) {
    encoders.put(name, value -> number(accessor.apply(value)));
    decoders.put(name, element -> {
      if (element instanceof JsonPrimitive.JsonNumber n) {
        return n.value().intValue();
      }
      throw new IllegalArgumentException();
    });
    return this;
  }

  public JsonAdapterBuilder<T> addDouble(String name, Function1<T, Double> accessor) {
    encoders.put(name, value -> number(accessor.apply(value)));
    decoders.put(name, element -> {
      if (element instanceof JsonPrimitive.JsonNumber n) {
        return n.value().doubleValue();
      }
      throw new IllegalArgumentException();
    });
    return this;
  }

  public JsonAdapterBuilder<T> addBoolean(String name, Function1<T, Boolean> accessor) {
    encoders.put(name, value -> bool(accessor.apply(value)));
    decoders.put(name, element -> {
      if (element instanceof JsonPrimitive.JsonBoolean bool) {
        return bool.value();
      }
      throw new IllegalArgumentException();
    });
    return this;
  }

  public JsonAdapterBuilder<T> addString(String name, Function1<T, String> accessor) {
    encoders.put(name, value -> string(accessor.apply(value)));
    decoders.put(name, element -> {
      if (element instanceof JsonPrimitive.JsonString s) {
        return s.value();
      }
      throw new IllegalArgumentException();
    });
    return this;
  }

  public <R> JsonAdapterBuilder<T> addObject(String name, Function1<T, R> accessor, JsonAdapter<R> other) {
    encoders.put(name, value -> other.encode(accessor.apply(value)));
    decoders.put(name, other::decode);
    return this;
  }

  public <R> JsonAdapterBuilder<T> addIterable(String name, Function1<T, Iterable<R>> accessor, JsonAdapter<R> other) {
    encoders.put(name, value -> Json.listAdapter(other).encode(accessor.apply(value)));
    decoders.put(name, element -> Json.listAdapter(other).decode(element));
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public JsonAdapter<T> build() {
    return new JsonAdapter<>() {
      @Override
      public T decode(JsonElement json) {
        Constructor<?> constructor1 = Arrays.stream(type.getDeclaredConstructors())
            .filter(constructor -> constructor.getParameterCount() == decoders.size()).findFirst()
            .orElseThrow();

        if (json instanceof JsonElement.JsonObject o) {
          List params = new ArrayList<>();
          for (Map.Entry<String, Function1<JsonElement, ?>> entry : decoders.entrySet()) {
            JsonElement element = o.values().get(entry.getKey()).getOrElseNull();
            params.add(entry.getValue().apply(element));
          }

          try {
            return (T) constructor1.newInstance(params.toArray(Object[]::new));
          } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException();
          }
        }

        throw new IllegalArgumentException();
      }

      @Override
      public JsonElement encode(T value) {
        Map<String, JsonElement> entries = new HashMap<>();
        for (Map.Entry<String, Function1<T, JsonElement>> entry : encoders.entrySet()) {
          entries.put(entry.getKey(), entry.getValue().apply(value));
        }
        return JsonElement.object(entries);
      }
    };
  }
}
