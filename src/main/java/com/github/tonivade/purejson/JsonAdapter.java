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

import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

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

  /**
   * It creates a builder to define an adapter for a concrete class
   *
   * @param target
   * @param <T>
   * @return
   */
  static <T> JsonAdapterBuilder<T> builder(Class<T> target) {
    return new JsonAdapterBuilder<>(target);
  }

  static <T> JsonAdapter<T> adapter(Class<T> type) {
    return adapter((Type) type);
  }

  /**
   * <p>First, it tries to load the instance of an adapter generated using annotation processor,
   * or else it will try to generate an adapter using reflection.
   * 
   * <p>if the type is not supported it will throw an {@code UnsupportedOperationException}.
   * 
   * @param <T>
   * @param type
   * @return
   */
  static <T> JsonAdapter<T> adapter(Type type) {
    return JsonAdapter.<T>load(type).getOrElse(() -> of(encoder(type), decoder(type)));
  }
  
  /**
   * Try to load the instance of an adapter generated using annotation processor via
   * {@code @Json}.
   * 
   * @param <T>
   * @param type
   * @return
   */
  @SuppressWarnings({ "unchecked", "preview" })
  static <T> Option<JsonAdapter<T>> load(Type type) {
    if (type instanceof Class<?> clazz && clazz.isAnnotationPresent(Json.class)) {
      return Try.of(() -> Class.forName(type.getTypeName() + "Adapter"))
          .filter(Class::isEnum)
          .map(c -> c.getEnumConstants()[0])
          .map(e -> (JsonAdapter<T>) e)
          .map(JsonAdapter::nullSafe)
          .toOption();
    }
    return Option.none();
  }

  /**
   * It will create an adapter with the given encoder and decoder
   * 
   * @param <T>
   * @param encoder
   * @param decoder
   * @return
   */
  static <T> JsonAdapter<T> of(JsonEncoder<T> encoder, JsonDecoder<T> decoder) {
    return new JsonAdapter<>() {

      @Override
      public JsonNode encode(JsonContext context, T value) {
        return encoder.encode(context, value);
      }
      
      @Override
      public T decode(JsonContext context, JsonNode json) {
        return decoder.decode(context, json);
      }
    };
  }

  static <E> JsonAdapter<Iterable<E>> iterableAdapter(Type componentType) {
    return of(JsonEncoder.iterableEncoder(componentType), JsonDecoder.iterableDecoder(componentType));
  }

  static <V> JsonAdapter<Map<String, V>> mapAdapter(Type componentType) {
    return of(JsonEncoder.mapEncoder(componentType), JsonDecoder.mapDecoder(componentType));
  }
  
  static <T> JsonAdapter<T> nullSafe(JsonAdapter<T> adapter) {
    return of(JsonEncoder.nullSafe(adapter), JsonDecoder.nullSafe(adapter));
  }
}