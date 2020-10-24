/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonDecoder.listDecoder;
import static com.github.tonivade.json.JsonElement.object;
import static com.github.tonivade.json.JsonEncoder.listEncoder;
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
    checkNonEmpty(name);
    checkNonNull(accessor);
    encoders.put(name, JsonEncoder.INTEGER.compose(accessor));
    decoders.put(name, JsonDecoder.INTEGER);
    return this;
  }

  public JsonAdapterBuilder<T> addLong(String name, Function1<T, Long> accessor) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    encoders.put(name, JsonEncoder.LONG.compose(accessor));
    decoders.put(name, JsonDecoder.LONG);
    return this;
  }

  public JsonAdapterBuilder<T> addFloat(String name, Function1<T, Float> accessor) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    encoders.put(name, JsonEncoder.FLOAT.compose(accessor));
    decoders.put(name, JsonDecoder.FLOAT);
    return this;
  }

  public JsonAdapterBuilder<T> addDouble(String name, Function1<T, Double> accessor) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    encoders.put(name, JsonEncoder.DOUBLE.compose(accessor));
    decoders.put(name, JsonDecoder.DOUBLE);
    return this;
  }

  public JsonAdapterBuilder<T> addBoolean(String name, Function1<T, Boolean> accessor) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    encoders.put(name, JsonEncoder.BOOLEAN.compose(accessor));
    decoders.put(name, JsonDecoder.BOOLEAN);
    return this;
  }

  public JsonAdapterBuilder<T> addString(String name, Function1<T, String> accessor) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    encoders.put(name, JsonEncoder.STRING.compose(accessor));
    decoders.put(name, JsonDecoder.STRING);
    return this;
  }

  public <R> JsonAdapterBuilder<T> addObject(String name, Function1<T, R> accessor, JsonAdapter<R> other) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    checkNonNull(other);
    encoders.put(name, other.compose(accessor));
    decoders.put(name, other::decode);
    return this;
  }

  public <R> JsonAdapterBuilder<T> addIterable(String name, Function1<T, Iterable<R>> accessor, JsonAdapter<R> other) {
    checkNonEmpty(name);
    checkNonNull(accessor);
    checkNonNull(other);
    encoders.put(name, listEncoder(other).compose(accessor));
    decoders.put(name, listDecoder(other));
    return this;
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
}
