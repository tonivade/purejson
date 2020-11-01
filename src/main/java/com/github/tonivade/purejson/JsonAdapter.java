/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public interface JsonAdapter<T> extends JsonEncoder<T>, JsonDecoder<T> {

  JsonAdapter<String> STRING = of(JsonEncoder.STRING, JsonDecoder.STRING);
  JsonAdapter<Character> CHAR = of(JsonEncoder.CHAR, JsonDecoder.CHAR);
  JsonAdapter<Byte> BYTE = of(JsonEncoder.BYTE, JsonDecoder.BYTE);
  JsonAdapter<Short> SHORT = of(JsonEncoder.SHORT, JsonDecoder.SHORT);
  JsonAdapter<Integer> INTEGER = of(JsonEncoder.INTEGER, JsonDecoder.INTEGER);
  JsonAdapter<Long> LONG = of(JsonEncoder.LONG, JsonDecoder.LONG);
  JsonAdapter<BigDecimal> BIG_DECIMAL = of(JsonEncoder.BIG_DECIMAL, JsonDecoder.BIG_DECIMAL);
  JsonAdapter<BigInteger> BIG_INTEGER = of(JsonEncoder.BIG_INTEGER, JsonDecoder.BIG_INTEGER);
  JsonAdapter<Float> FLOAT = of(JsonEncoder.FLOAT, JsonDecoder.FLOAT);
  JsonAdapter<Double> DOUBLE = of(JsonEncoder.DOUBLE, JsonDecoder.DOUBLE);
  JsonAdapter<Boolean> BOOLEAN = of(JsonEncoder.BOOLEAN, JsonDecoder.BOOLEAN);

  static <T> JsonAdapterBuilder<T> builder(Class<T> target) {
    return new JsonAdapterBuilder<>(target);
  }
  
  static <T> JsonAdapter<T> create(Type type) {
    return of(JsonEncoder.create(type), JsonDecoder.create(type));
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
