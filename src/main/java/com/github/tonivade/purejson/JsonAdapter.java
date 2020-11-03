/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purejson.JsonDecoder.decoder;
import static com.github.tonivade.purejson.JsonEncoder.encoder;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface JsonAdapter<T> extends JsonEncoder<T>, JsonDecoder<T> {

  JsonAdapter<String> STRING = adapter(String.class);
  JsonAdapter<Character> CHAR = adapter(Character.class);
  JsonAdapter<Byte> BYTE = adapter(Byte.class);
  JsonAdapter<Short> SHORT = adapter(Short.class);
  JsonAdapter<Integer> INTEGER = adapter(Integer.class);
  JsonAdapter<Long> LONG = adapter(Long.class);
  JsonAdapter<BigDecimal> BIG_DECIMAL = adapter(BigDecimal.class);
  JsonAdapter<BigInteger> BIG_INTEGER = adapter(BigInteger.class);
  JsonAdapter<Float> FLOAT = adapter(Float.class);
  JsonAdapter<Double> DOUBLE = adapter(Double.class);
  JsonAdapter<Boolean> BOOLEAN = adapter(Boolean.class);

  static <T> JsonAdapterBuilder<T> builder(Class<T> target) {
    return new JsonAdapterBuilder<>(target);
  }

  static <T> JsonAdapter<T> adapter(Class<T> type) {
    return adapter((Type) type);
  }

  static <T> JsonAdapter<T> adapter(Type type) {
    return JsonAdapterCache.get(type);
  }
  
  static <T> JsonAdapter<T> of(JsonEncoder<T> encoder, JsonDecoder<T> decoder) {
    return new JsonAdapter<>() {

      @Override
      public JsonNode encode(T value) {
        return encoder.encode(value);
      }
      
      @Override
      public T decode(JsonNode json) {
        return decoder.decode(json);
      }
    };
  }

  static <E> JsonAdapter<Iterable<E>> iterableAdapter(JsonAdapter<E> itemAdapter) {
    return of(JsonEncoder.iterableEncoder(itemAdapter), JsonDecoder.iterableDecoder(itemAdapter));
  }

  static <V> JsonAdapter<Map<String, V>> mapAdapter(JsonAdapter<V> valueAdapter) {
    return of(JsonEncoder.mapEncoder(valueAdapter), JsonDecoder.mapDecoder(valueAdapter));
  }
}

@SuppressWarnings({"preview", "unchecked"})
interface JsonAdapterCache {
  
  ConcurrentHashMap<Type, JsonAdapter<?>> cache = new ConcurrentHashMap<>();
  
  static <T> JsonAdapter<T> get(Type type) {
    return (JsonAdapter<T>) cache.computeIfAbsent(type, JsonAdapterCache::load);
  }

  private static JsonAdapter<?> load(Type type) {
    if (type instanceof Class<?> c && !c.isPrimitive()) {
      try {
        Class<?> forName = Class.forName(type.getTypeName() + "Adapter");
        if (forName.isEnum() && forName.getEnumConstants().length == 1) {
          Object instance = forName.getEnumConstants()[0];
          System.out.println("found instance for type: " + type.getTypeName());
          return (JsonAdapter<?>) instance;
        }
      } catch (ClassNotFoundException e) {
        // instance not found
      }
    }
    return JsonAdapter.of(encoder(type), decoder(type));
  }
}