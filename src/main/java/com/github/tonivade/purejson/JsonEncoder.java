/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@FunctionalInterface
@SuppressWarnings("preview")
public interface JsonEncoder<T> {
  
  JsonNode encode(JsonContext context, T value);

  default Try<JsonNode> tryEncode(JsonContext context, T value) {
    return Try.of(() -> encode(context, value));
  }
  
  default <R> JsonEncoder<R> compose(Function1<? super R, ? extends T> accesor) {
    return (context, value) -> encode(context, accesor.apply(value));
  }
  
  static <T> JsonEncoder<T> encoder(Type type) {
    return nullSafe(JsonEncoder.<T>load(type).getOrElse(() -> create(type)));
  }
  
  @SuppressWarnings("unchecked")
  static <T> Option<JsonEncoder<T>> load(Type type) {
    return JsonAdapter.load(type).map(e -> (JsonEncoder<T>) e);
  }

  static <T> JsonEncoder<T> arrayEncoder(Type type) {
    return (context, value) -> {
      var array = new JsonArray();
      for (var item : (Object[]) value) {
        array.add(context.encode(item, type).unwrap());
      }
      return new JsonNode.Array(array);
    };
  }

  static <T> JsonEncoder<T> pojoEncoder(Class<T> type) {
    return (context, value) -> {
      var object = new JsonObject();
      for (var field : type.getDeclaredFields()) {
        if (!isStatic(field.getModifiers()) && !field.isSynthetic() && field.trySetAccessible()) {
          try {
            object.add(field.getName(), context.encode(field.get(value), field.getGenericType()).unwrap());
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
          }
        }
      }
      return new JsonNode.Object(object);
    };
  }

  static <T> JsonEncoder<T> recordEncoder(Class<T> type) {
    return (context, value) -> {
      var object = new JsonObject();
      for (var component : type.getRecordComponents()) {
        try {
          var field = component.getAccessor().invoke(value);
          object.add(component.getName(), context.encode(field, component.getGenericType()).unwrap());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          throw new IllegalStateException(e);
        }
      }
      return new JsonNode.Object(object);
    };
  }

  static <E> JsonEncoder<Iterable<E>> iterableEncoder(Type componentType) {
    return (context, value) -> {
      var array = new JsonArray();
      for (E item : value) {
        array.add(context.encode(item, componentType).unwrap());
      }
      return new JsonNode.Array(array);
    };
  }

  static <V> JsonEncoder<Map<String, V>> mapEncoder(Type componentType) {
    return (context, value) -> {
      var object = new JsonObject();
      for (var entry : value.entrySet()) {
        object.add(entry.getKey(), context.encode(entry.getValue(), componentType).unwrap());
      }
      return new JsonNode.Object(object);
    };
  }

  static <V> JsonEncoder<ImmutableMap<String, V>> immutableMapEncoder(Type componentType) {
    return JsonEncoder.<V>mapEncoder(componentType).compose(ImmutableMap::toMap);
  }
  
  static <T> JsonEncoder<T> nullSafe(JsonEncoder<T> encoder) {
    return (context, value) -> value == null ? JsonNode.NULL : encoder.encode(context, value);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static <T> JsonEncoder<T> create(Type type) {
    if (type instanceof Class clazz) {
      return create(clazz);
    }
    if (type instanceof ParameterizedType paramType) {
      return create(paramType);
    }
    if (type instanceof GenericArrayType arrayType) {
      return create(arrayType);
    }
    if (type instanceof WildcardType wildcardType) {
      return create(wildcardType);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonEncoder<T> create(ParameterizedType type) {
    if (type.getRawType() instanceof Class<?> c 
        && ImmutableMap.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      return (JsonEncoder<T>) immutableMapEncoder(type.getActualTypeArguments()[1]);
    }
    if (type.getRawType() instanceof Class<?> c 
        && Map.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      return (JsonEncoder<T>) mapEncoder(type.getActualTypeArguments()[1]);
    }
    if (type.getRawType() instanceof Class<?> c && Iterable.class.isAssignableFrom(c)) {
      return (JsonEncoder<T>) iterableEncoder(type.getActualTypeArguments()[0]);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  private static <T> JsonEncoder<T> create(WildcardType type) {
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonEncoder<T> create(GenericArrayType type) {
    Type genericComponentType = type.getGenericComponentType();
    if (genericComponentType instanceof Class<?> c) {
      return (JsonEncoder<T>) arrayEncoder(c);
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
    } else if (type.isRecord()) {
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
  
  JsonEncoder<String> STRING = primitive(JsonNode.Primitive::new);
  JsonEncoder<Character> CHAR = STRING.compose(Object::toString);
  JsonEncoder<Byte> BYTE = primitive(JsonNode.Primitive::new);
  JsonEncoder<Short> SHORT = primitive(JsonNode.Primitive::new);
  JsonEncoder<Integer> INTEGER = primitive(JsonNode.Primitive::new);
  JsonEncoder<Long> LONG = primitive(JsonNode.Primitive::new);
  JsonEncoder<BigDecimal> BIG_DECIMAL = primitive(JsonNode.Primitive::new);
  JsonEncoder<BigInteger> BIG_INTEGER = primitive(JsonNode.Primitive::new);
  JsonEncoder<Float> FLOAT = primitive(JsonNode.Primitive::new);
  JsonEncoder<Double> DOUBLE = primitive(JsonNode.Primitive::new);
  JsonEncoder<Boolean> BOOLEAN = primitive(JsonNode.Primitive::new);
  JsonEncoder<Enum<?>> ENUM = STRING.compose(Enum::name);
  
  static <T> JsonEncoder<T> primitive(Function1<T, JsonNode> method) {
    return (context, value) -> method.apply(value);
  }
}