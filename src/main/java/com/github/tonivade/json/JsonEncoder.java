/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonElement.array;
import static com.github.tonivade.json.JsonElement.entry;
import static com.github.tonivade.json.JsonElement.object;
import static com.github.tonivade.json.JsonPrimitive.number;
import static com.github.tonivade.json.JsonPrimitive.string;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.github.tonivade.purefun.Function1;

@FunctionalInterface
@SuppressWarnings("preview")
public interface JsonEncoder<T> {
  
  JsonEncoder<String> STRING = JsonPrimitive::string;
  JsonEncoder<Character> CHAR = JsonPrimitive::number;
  JsonEncoder<Byte> BYTE = JsonPrimitive::number;
  JsonEncoder<Short> SHORT = JsonPrimitive::number;
  JsonEncoder<Integer> INTEGER = JsonPrimitive::number;
  JsonEncoder<Long> LONG = JsonPrimitive::number;
  JsonEncoder<BigDecimal> BIG_DECIMAL = value -> number(value.doubleValue());
  JsonEncoder<BigInteger> BIG_INTEGER = value -> number(value.longValue());
  JsonEncoder<Float> FLOAT = JsonPrimitive::number;
  JsonEncoder<Double> DOUBLE = JsonPrimitive::number;
  JsonEncoder<Boolean> BOOLEAN = JsonPrimitive::bool;
  JsonEncoder<Enum<?>> ENUM = value -> string(value.name());
  
  JsonElement encode(T value);
  
  default <R> JsonEncoder<R> compose(Function1<R, T> accesor) {
    return value -> encode(accesor.apply(value));
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <T> JsonEncoder<T> create(Type type) {
    if (type instanceof Class clazz) {
      return nullSafe(create(clazz));
    }
    if (type instanceof ParameterizedType paramType) {
      return nullSafe(create(paramType));
    }
    if (type instanceof WildcardType wildcardType) {
      return nullSafe(create(wildcardType));
    }
    throw new UnsupportedOperationException(type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  static <T> JsonEncoder<T> create(ParameterizedType type) {
    if (type.getRawType() instanceof Class<?> c && Iterable.class.isAssignableFrom(c)) {
      var create = create(type.getActualTypeArguments()[0]);
      return (JsonEncoder<T>) listEncoder(create);
    }
    if (type.getRawType() instanceof Class<?> c 
        && Map.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      var create = create(type.getActualTypeArguments()[1]);
      return (JsonEncoder<T>) mapEncoder(create);
    }
    throw new UnsupportedOperationException(type.getTypeName());
  }

  static <T> JsonEncoder<T> create(WildcardType type) {
    throw new UnsupportedOperationException(type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  static <T> JsonEncoder<T> create(Class<T> type) {
    if (type.isPrimitive()) {
      return primitiveEncoder(type);
    } else if (type.equals(String.class)) {
      return (JsonEncoder<T>) STRING;
    } else if (type.equals(Character.class)) {
      return (JsonEncoder<T>) CHAR;
    } else if (type.equals(Byte.class)) {
      return (JsonEncoder<T>) BYTE;
    } else if (type.equals(Short.class)) {
      return (JsonEncoder<T>) SHORT;
    } else if (type.equals(Integer.class)) {
      return (JsonEncoder<T>) INTEGER;
    } else if (type.equals(Long.class)) {
      return (JsonEncoder<T>) LONG;
    } else if (type.equals(BigDecimal.class)) {
      return (JsonEncoder<T>) BIG_DECIMAL;
    } else if (type.equals(BigInteger.class)) {
      return (JsonEncoder<T>) BIG_INTEGER;
    } else if (type.equals(Float.class)) {
      return (JsonEncoder<T>) FLOAT;
    } else if (type.equals(Double.class)) {
      return (JsonEncoder<T>) DOUBLE;
    } else if (type.equals(Boolean.class)) {
      return (JsonEncoder<T>) BOOLEAN;
    } else if (type.isEnum()) {
      return (JsonEncoder<T>) ENUM;
    } else if (type.isArray()) {
      return arrayEncoder(type);
    } else if (type.isRecord()) {
      return recordEncoder(type);
    } else {
      return pojoEncoder(type);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  static <T> JsonEncoder<T> arrayEncoder(Class<T> type) {
    return value -> {
      JsonEncoder arrayEncoder = create((Type) type.getComponentType());
      List<JsonElement> items = new LinkedList<>();
      for (Object object : (Object[]) value) {
        items.add(arrayEncoder.encode(object));
      }
      return array(items);
    };
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  static <T> JsonEncoder<T> pojoEncoder(Class<T> type) {
    return value -> {
      List<Map.Entry<String, JsonElement>> entries = new ArrayList<>();
      for (Field field : type.getDeclaredFields()) {
        if (!isStatic(field.getModifiers()) && !field.isSynthetic() && field.trySetAccessible()) {
          JsonEncoder fieldEncoder = create(field.getGenericType());
          try {
            entries.add(entry(field.getName(), fieldEncoder.encode(field.get(value))));
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
          }
        }
      }
      return object(entries);
    };
  }

  static <T> JsonEncoder<T> recordEncoder(Class<T> type) {
    return value -> {
      if (value == null) {
        new Exception().printStackTrace();
      }
      var entries = new ArrayList<Map.Entry<String, JsonElement>>();
      for (RecordComponent recordComponent : type.getRecordComponents()) {
        var fieldEncoder = create(recordComponent.getGenericType());
        try {
          var field = recordComponent.getAccessor().invoke(value);
          entries.add(entry(recordComponent.getName(), fieldEncoder.encode(field)));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          throw new IllegalStateException(e);
        }
      }
      return object(entries);
    };
  }

  static <E> JsonEncoder<Iterable<E>> listEncoder(JsonEncoder<E> itemEncoder) {
    return value -> {
      return array(StreamSupport.stream(value.spliterator(), false)
          .map(itemEncoder::encode).collect(toUnmodifiableList()));
    };
  }

  static <V> JsonEncoder<Map<String, V>> mapEncoder(JsonEncoder<V> valueEncoder) {
    return value -> {
      return object(
          value.entrySet().stream()
          .map(entry -> entry(entry.getKey(), valueEncoder.encode(entry.getValue())))
          .collect(toList()));
    };
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonEncoder<T> primitiveEncoder(Class<T> type) {
    if (type.equals(char.class)) {
      return (JsonEncoder<T>) CHAR;
    }
    if (type.equals(byte.class)) {
      return (JsonEncoder<T>) BYTE;
    }
    if (type.equals(short.class)) {
      return (JsonEncoder<T>) SHORT;
    }
    if (type.equals(int.class)) {
      return (JsonEncoder<T>) INTEGER;
    }
    if (type.equals(long.class)) {
      return (JsonEncoder<T>) LONG;
    }
    if (type.equals(float.class)) {
      return (JsonEncoder<T>) FLOAT;
    }
    if (type.equals(double.class)) {
      return (JsonEncoder<T>) DOUBLE;
    }
    if (type.equals(boolean.class)) {
      return (JsonEncoder<T>) BOOLEAN;
    }
    throw new IllegalArgumentException();
  }
  
  private static <T> JsonEncoder<T> nullSafe(JsonEncoder<T> encoder) {
    return value -> {
      if (value == null) {
        return JsonElement.NULL;
      }
      return encoder.encode(value);
    };
  }
}
