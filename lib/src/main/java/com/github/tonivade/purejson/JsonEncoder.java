/*
 * Copyright (c) 2020-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

@FunctionalInterface
public interface JsonEncoder<T> {
  
  JsonNode encode(T value);
  
  default <R> JsonEncoder<R> contramap(Function1<? super R, ? extends T> contramap) {
    return value -> encode(contramap.apply(value));
  }

  default Try<JsonNode> tryEncode(T value) {
    return Try.of(() -> encode(value));
  }
  
  default <R> JsonEncoder<R> compose(Function1<? super R, ? extends T> accesor) {
    return value -> encode(accesor.apply(value));
  }
  
  static <T> JsonEncoder<T> encoder(Type type) {
    return nullSafe(JsonEncoder.<T>load(type).getOrElse(() -> create(type)));
  }
  
  @SuppressWarnings("unchecked")
  static <T> Option<JsonEncoder<T>> load(Type type) {
    return JsonAdapter.load(type).map(e -> (JsonEncoder<T>) e);
  }

  static <T> JsonEncoder<T> arrayEncoder(Type type) {
    var arrayEncoder = encoder(type);
    return value -> {
      var array = new JsonArray();
      for (var item : (Object[]) value) {
        array.add(arrayEncoder.encode(item).unwrap());
      }
      return new JsonNode.Array(array);
    };
  }

  static <T> JsonEncoder<T> pojoEncoder(Class<T> type) {
    var fields = Arrays.stream(type.getDeclaredFields())
        .filter(f -> !isStatic(f.getModifiers()))
        .filter(f -> !f.isSynthetic())
        .filter(Field::trySetAccessible)
        .map(f -> Tuple2.of(f, encoder(f.getGenericType())))
        .collect(toList());
    return value -> {
      var object = new JsonObject();
      for (var pair : fields) {
        try {
          object.add(pair.get1().getName(), pair.get2().encode(pair.get1().get(value)).unwrap());
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
      return new JsonNode.Object(object);
    };
  }

  static <T> JsonEncoder<T> recordEncoder(Class<T> type) {
    var record = new Record<T>(type);
    var fields = Arrays.stream(record.getRecordComponents())
        .map(f -> Tuple2.of(f, encoder(f.getGenericType())))
        .collect(toList());
    return value -> {
      var object = new JsonObject();
      for (var pair : fields) {
        try {
          var field = pair.get1().getAccessor().invoke(value);
          object.add(pair.get1().getName(), pair.get2().encode(field).unwrap());
        } catch (IllegalAccessException | InvocationTargetException e) {
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
        array.add(itemEncoder.encode(item).unwrap());
      }
      return new JsonNode.Array(array);
    };
  }

  static <V> JsonEncoder<Map<String, V>> mapEncoder(JsonEncoder<V> valueEncoder) {
    return value -> {
      var object = new JsonObject();
      for (var entry : value.entrySet()) {
        object.add(entry.getKey(), valueEncoder.encode(entry.getValue()).unwrap());
      }
      return new JsonNode.Object(object);
    };
  }

  static <V> JsonEncoder<ImmutableMap<String, V>> immutableMapEncoder(JsonEncoder<V> valueEncoder) {
    return mapEncoder(valueEncoder).compose(ImmutableMap::toMap);
  }
  
  static <T> JsonEncoder<T> nullSafe(JsonEncoder<T> encoder) {
    return value -> value == null ? JsonNode.NULL : encoder.encode(value);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static <T> JsonEncoder<T> create(Type type) {
    if (type instanceof Class) {
      return create((Class) type);
    }
    if (type instanceof ParameterizedType) {
      return create((ParameterizedType) type);
    }
    if (type instanceof GenericArrayType) {
      return create((GenericArrayType) type);
    }
    if (type instanceof WildcardType) {
      return create((WildcardType) type);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonEncoder<T> create(ParameterizedType type) {
    if (type.getRawType() instanceof Class<?>) {
      Class<?> c = (Class<?>) type.getRawType();
      if (ImmutableMap.class.isAssignableFrom(c) && type.getActualTypeArguments()[0].equals(String.class)) {
        var create = encoder(type.getActualTypeArguments()[1]);
        return (JsonEncoder<T>) immutableMapEncoder(create);
      }
      if (Map.class.isAssignableFrom(c) && type.getActualTypeArguments()[0].equals(String.class)) {
        var create = encoder(type.getActualTypeArguments()[1]);
        return (JsonEncoder<T>) mapEncoder(create);
      }
      if (Iterable.class.isAssignableFrom(c)) {
        var create = encoder(type.getActualTypeArguments()[0]);
        return (JsonEncoder<T>) iterableEncoder(create);
      }
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  private static <T> JsonEncoder<T> create(WildcardType type) {
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  private static <T> JsonEncoder<T> create(GenericArrayType type) {
    Type genericComponentType = type.getGenericComponentType();
    if (genericComponentType instanceof Class<?>) {
      return arrayEncoder((Class<?>) genericComponentType);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonEncoder<T> create(Class<T> type) {
    if (type.isPrimitive()) {
      return primitiveEncoder(type);
    } else if (type.equals(String.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.STRING;
    } else if (type.equals(Character.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.CHAR;
    } else if (type.equals(Byte.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.BYTE;
    } else if (type.equals(Short.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.SHORT;
    } else if (type.equals(Integer.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.INTEGER;
    } else if (type.equals(Long.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.LONG;
    } else if (type.equals(BigDecimal.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.BIG_DECIMAL;
    } else if (type.equals(BigInteger.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.BIG_INTEGER;
    } else if (type.equals(Float.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.FLOAT;
    } else if (type.equals(Double.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.DOUBLE;
    } else if (type.equals(Boolean.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.BOOLEAN;
    } else if (type.isEnum()) {
      return (JsonEncoder<T>) JsonEncoderModule.ENUM;
    } else if (type.isArray()) {
      return arrayEncoder(type.getComponentType());
    } else if (Record.isRecord(type)) {
      return recordEncoder(type);
    } else {
      return pojoEncoder(type);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonEncoder<T> primitiveEncoder(Class<T> type) {
    if (type.equals(char.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.CHAR;
    }
    if (type.equals(byte.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.BYTE;
    }
    if (type.equals(short.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.SHORT;
    }
    if (type.equals(int.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.INTEGER;
    }
    if (type.equals(long.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.LONG;
    }
    if (type.equals(float.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.FLOAT;
    }
    if (type.equals(double.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.DOUBLE;
    }
    if (type.equals(boolean.class)) {
      return (JsonEncoder<T>) JsonEncoderModule.BOOLEAN;
    }
    throw new IllegalArgumentException("a new primitive? " + type.getTypeName());
  }
}

interface JsonEncoderModule {
  
  JsonEncoder<String> STRING = JsonDSL::string;
  JsonEncoder<Character> CHAR = STRING.compose(Object::toString);
  JsonEncoder<Byte> BYTE = JsonDSL::number;
  JsonEncoder<Short> SHORT = JsonDSL::number;
  JsonEncoder<Integer> INTEGER = JsonDSL::number;
  JsonEncoder<Long> LONG = JsonDSL::number;
  JsonEncoder<Float> FLOAT = JsonDSL::number;
  JsonEncoder<Double> DOUBLE = JsonDSL::number;
  JsonEncoder<Boolean> BOOLEAN = JsonDSL::bool;
  JsonEncoder<Enum<?>> ENUM = STRING.compose(Enum::name);
  JsonEncoder<BigDecimal> BIG_DECIMAL = DOUBLE.compose(BigDecimal::doubleValue);
  JsonEncoder<BigInteger> BIG_INTEGER = LONG.compose(BigInteger::longValue);
}