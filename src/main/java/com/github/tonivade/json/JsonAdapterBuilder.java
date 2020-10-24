/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonAdapter.listAdapter;
import static com.github.tonivade.json.JsonElement.object;
import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.tonivade.purefun.Function1;

public final class JsonAdapterBuilder<T> {

  private final Map<String, JsonEncoder<T>> encoders = new LinkedHashMap<>();
  private final Map<String, JsonDecoder<?>> decoders = new LinkedHashMap<>();

  private final Class<T> type;

  public JsonAdapterBuilder(Class<T> type) {
    this.type = checkNonNull(type);
  }

  public JsonAdapterBuilder<T> addInteger(String name, Function1<T, Integer> accessor) {
    return add(name, accessor, JsonAdapter.INTEGER);
  }

  public JsonAdapterBuilder<T> addLong(String name, Function1<T, Long> accessor) {
    return add(name, accessor, JsonAdapter.LONG);
  }

  public JsonAdapterBuilder<T> addFloat(String name, Function1<T, Float> accessor) {
    return add(name, accessor, JsonAdapter.FLOAT);
  }

  public JsonAdapterBuilder<T> addDouble(String name, Function1<T, Double> accessor) {
    return add(name, accessor, JsonAdapter.DOUBLE);
  }

  public JsonAdapterBuilder<T> addBoolean(String name, Function1<T, Boolean> accessor) {
    return add(name, accessor, JsonAdapter.BOOLEAN);
  }

  public JsonAdapterBuilder<T> addString(String name, Function1<T, String> accessor) {
    return add(name, accessor, JsonAdapter.STRING);
  }

  public <R> JsonAdapterBuilder<T> addObject(String name, Function1<T, R> accessor, JsonAdapter<R> other) {
    return add(name, accessor, other);
  }

  public <R> JsonAdapterBuilder<T> addIterable(String name, Function1<T, Iterable<R>> accessor, JsonAdapter<R> other) {
    return add(name, accessor, listAdapter(other));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public JsonAdapter<T> build() {
    Constructor<?> constructor1 = Arrays.stream(type.getDeclaredConstructors())
        .filter(constructor -> constructor.getParameterCount() == decoders.size()).findFirst()
        .orElseThrow();
    return JsonAdapter.of(

        value -> {
          Map<String, JsonElement> entries = new LinkedHashMap<>();
          for (Map.Entry<String, JsonEncoder<T>> entry : encoders.entrySet()) {
            entries.put(entry.getKey(), entry.getValue().encode(value));
          }
          return object(entries.entrySet());
        },

        json -> {
          if (json instanceof JsonElement.JsonObject o) {
            List params = new ArrayList<>();
            for (Map.Entry<String, JsonDecoder<?>> entry : decoders.entrySet()) {
              JsonElement element = o.values().get(entry.getKey());
              params.add(entry.getValue().decode(element));
            }

            try {
              return (T) constructor1.newInstance(params.toArray(Object[]::new));
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
              throw new IllegalStateException(e);
            }
          }

          throw new IllegalArgumentException();
        });
  }

  private <R> JsonAdapterBuilder<T> add(
      String name, Function1<T, R> accessor, JsonAdapter<R> adapter) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    checkNonNull(adapter);
    encoders.put(name, adapter.compose(accessor));
    decoders.put(name, adapter);
    return this;
  }
}
