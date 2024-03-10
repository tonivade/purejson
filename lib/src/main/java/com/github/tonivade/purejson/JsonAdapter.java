/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.core.Matcher1.is;
import static com.github.tonivade.purejson.JsonDecoder.decoder;
import static com.github.tonivade.purejson.JsonEncoder.encoder;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.github.tonivade.purefun.Nullable;
import com.github.tonivade.purefun.type.Option;

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
   * It creates an adapter builder. You can define each field step by step with its proper
   * accessor and create an adapter.
   *
   * @param <T>
   * @param target
   * @return
   */
  static <T> JsonAdapterBuilder<T> builder(Class<T> target) {
    return new JsonAdapterBuilder<>(target);
  }

  /**
   * Alias for {@link #adapter(Type)} but, for {@code Class<T>} in order to help type inference.
   *
   * @param <T>
   * @return
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  static <T> JsonAdapter<T> adapter(T... reified) {
    if (reified.length > 0) {
      throw new IllegalArgumentException("do not pass arguments to this function, it's just a trick to get refied types");
    }
    return adapter((Class<T>) reified.getClass().getComponentType());
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
  @SuppressWarnings("unchecked")
  static <T> Option<JsonAdapter<T>> load(Type type) {
    if (type instanceof Class<?> clazz && clazz.isAnnotationPresent(Json.class)) {
      return Option.<Class<?>>of(() -> clazz.getAnnotation(Json.class).value())
          .filterNot(is(Void.class))
          .toTry()
          .recover(error -> Class.forName(type.getTypeName() + "Adapter"))
          .filter(Class::isEnum)
          .map(c -> c.getEnumConstants()[0])
          .map(e -> (JsonAdapter<T>) e)
          .map(JsonAdapter::nullSafe)
          .toOption();
    }
    return Option.none();
  }

  /**
   * It creates an adapter with the given encoder and decoder.
   *
   * @param <T>
   * @param encoder
   * @param decoder
   * @return
   */
  static <T> JsonAdapter<T> of(JsonEncoder<T> encoder, JsonDecoder<T> decoder) {
    return new JsonAdapter<>() {

      @Override
      public JsonNode encode(T value) {
        return encoder.encode(value);
      }

      @Override
      @Nullable
      public T decode(JsonNode json) {
        return decoder.decode(json);
      }
    };
  }

  /**
   * It creates an adapter for any class that implements {@link java.lang.Iterable}.
   *
   * @param <E>
   * @param itemAdapter the adapter for the item type
   * @return
   */
  static <E> JsonAdapter<Iterable<E>> iterableAdapter(JsonAdapter<E> itemAdapter) {
    return of(JsonEncoder.iterableEncoder(itemAdapter), JsonDecoder.iterableDecoder(itemAdapter));
  }


  /**
   * It creates an adapter for a {@code Map}
   *
   * @param <V>
   * @param valueAdapter the adapter for the value type
   * @return
   */
  static <V> JsonAdapter<Map<String, V>> mapAdapter(JsonAdapter<V> valueAdapter) {
    return of(JsonEncoder.mapEncoder(valueAdapter), JsonDecoder.mapDecoder(valueAdapter));
  }

  /**
   * Helper function to convert any adapter to null safe, in case of receive a null value it will
   * generate a correct ADT value.
   *
   * @param <T>
   * @param adapter
   * @return
   */
  static <T> JsonAdapter<T> nullSafe(JsonAdapter<T> adapter) {
    return of(JsonEncoder.nullSafe(adapter), JsonDecoder.nullSafe(adapter));
  }
}