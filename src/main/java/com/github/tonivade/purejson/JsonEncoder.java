/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@FunctionalInterface
@SuppressWarnings("preview")
public interface JsonEncoder<T> {
  
  JsonEncoder<String> STRING = JsonNode.Primitive::new;
  JsonEncoder<Character> CHAR = STRING.compose(Object::toString);
  JsonEncoder<Byte> BYTE = JsonNode.Primitive::new;
  JsonEncoder<Short> SHORT = JsonNode.Primitive::new;
  JsonEncoder<Integer> INTEGER = JsonNode.Primitive::new;
  JsonEncoder<Long> LONG = JsonNode.Primitive::new;
  JsonEncoder<BigDecimal> BIG_DECIMAL = JsonNode.Primitive::new;
  JsonEncoder<BigInteger> BIG_INTEGER = JsonNode.Primitive::new;
  JsonEncoder<Float> FLOAT = JsonNode.Primitive::new;
  JsonEncoder<Double> DOUBLE = JsonNode.Primitive::new;
  JsonEncoder<Boolean> BOOLEAN = JsonNode.Primitive::new;
  JsonEncoder<Enum<?>> ENUM = STRING.compose(Enum::name);
  
  JsonNode encode(T value);
  
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
    if (type instanceof GenericArrayType arrayType) {
      return nullSafe(create(arrayType));
    }
    if (type instanceof WildcardType wildcardType) {
      return nullSafe(create(wildcardType));
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  static <T> JsonEncoder<T> create(ParameterizedType type) {
    if (type.getRawType() instanceof Class<?> c 
        && ImmutableMap.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      var create = create(type.getActualTypeArguments()[1]);
      return (JsonEncoder<T>) immutableMapEncoder(create);
    }
    if (type.getRawType() instanceof Class<?> c 
        && Map.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      var create = create(type.getActualTypeArguments()[1]);
      return (JsonEncoder<T>) mapEncoder(create);
    }
    if (type.getRawType() instanceof Class<?> c && Iterable.class.isAssignableFrom(c)) {
      var create = create(type.getActualTypeArguments()[0]);
      return (JsonEncoder<T>) iterableEncoder(create);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  static <T> JsonEncoder<T> create(WildcardType type) {
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  static <T> JsonEncoder<T> create(GenericArrayType type) {
    Type genericComponentType = type.getGenericComponentType();
    if (genericComponentType instanceof Class<?> c) {
      return (JsonEncoder<T>) arrayEncoder(c);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
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
      return arrayEncoder(type.getComponentType());
    } else if (type.isRecord()) {
      return recordEncoder(type);
    } else {
      return pojoEncoder(type);
    }
  }

  static <T> JsonEncoder<T> arrayEncoder(Type type) {
    var arrayEncoder = create(type);
    return value -> {
      var array = new JsonArray();
      for (var item : (Object[]) value) {
        array.add(arrayEncoder.encode(item));
      }
      return new JsonNode.Array(array);
    };
  }

  static <T> JsonEncoder<T> pojoEncoder(Class<T> type) {
    var fields = Arrays.stream(type.getDeclaredFields())
        .filter(f -> !isStatic(f.getModifiers()))
        .filter(f -> !f.isSynthetic())
        .filter(f -> f.trySetAccessible())
        .map(f -> Tuple2.of(f, create(f.getGenericType())))
        .collect(toList());
    return value -> {
      var object = new JsonObject();
      for (var pair : fields) {
        try {
          object.add(pair.get1().getName(), pair.get2().encode(pair.get1().get(value)));
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
      return new JsonNode.Object(object);
    };
  }

  static <T> JsonEncoder<T> recordEncoder(Class<T> type) {
    var fields = Arrays.stream(type.getRecordComponents())
        .map(f -> Tuple2.of(f, create(f.getGenericType())))
        .collect(toList());
    return value -> {
      var object = new JsonObject();
      for (var pair : fields) {
        try {
          var field = pair.get1().getAccessor().invoke(value);
          object.add(pair.get1().getName(), pair.get2().encode(field));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          throw new IllegalStateException(e);
        }
      }
      return new JsonNode.Object(object);
    };
  }

  static <E> JsonEncoder<Iterable<E>> iterableEncoder(JsonEncoder<E> itemEncoder) {
    return value -> {
      var array = new JsonArray();
      for (E item : value) {
        array.add(itemEncoder.encode(item));
      }
      return new JsonNode.Array(array);
    };
  }

  static <V> JsonEncoder<Map<String, V>> mapEncoder(JsonEncoder<V> valueEncoder) {
    return value -> {
      var object = new JsonObject();
      for (var entry : value.entrySet()) {
        object.add(entry.getKey(), valueEncoder.encode(entry.getValue()));
      }
      return new JsonNode.Object(object);
    };
  }

  static <V> JsonEncoder<ImmutableMap<String, V>> immutableMapEncoder(JsonEncoder<V> valueEncoder) {
    return mapEncoder(valueEncoder).compose(ImmutableMap::toMap);
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
    throw new IllegalArgumentException("a new primitive? " + type.getTypeName());
  }
  
  private static <T> JsonEncoder<T> nullSafe(JsonEncoder<T> encoder) {
    return value -> value == null ? JsonNode.NULL : encoder.encode(value);
  }
}
