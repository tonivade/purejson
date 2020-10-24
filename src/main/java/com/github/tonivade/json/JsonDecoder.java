/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonElement.entry;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.tonivade.json.JsonElement.JsonArray;
import com.github.tonivade.json.JsonElement.JsonObject;
import com.github.tonivade.json.JsonPrimitive.JsonBoolean;
import com.github.tonivade.json.JsonPrimitive.JsonNumber;
import com.github.tonivade.json.JsonPrimitive.JsonString;
import com.github.tonivade.purefun.Function1;

@FunctionalInterface
public interface JsonDecoder<T> {
  
  JsonDecoder<String> STRING = json -> {
    if (json instanceof JsonString s) {
      return s.value();
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<Byte> BYTE = json -> {
    if (json instanceof JsonNumber n) {
      return n.value().byteValue();
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<Short> SHORT = json -> {
    if (json instanceof JsonNumber n) {
      return n.value().shortValue();
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<Integer> INTEGER = json -> {
    if (json instanceof JsonNumber n) {
      return n.value().intValue();
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<Long> LONG = json -> {
    if (json instanceof JsonNumber n) {
      return n.value().longValue();
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<Float> FLOAT = json -> {
    if (json instanceof JsonNumber n) {
      return n.value().floatValue();
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<Double> DOUBLE = json -> {
    if (json instanceof JsonNumber n) {
      return n.value().doubleValue();
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<BigInteger> BIG_INTEGER = json -> {
    if (json instanceof JsonNumber n) {
      return BigInteger.valueOf(n.value().longValue());
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<BigDecimal> BIG_DECIMAL = json -> {
    if (json instanceof JsonNumber n) {
      return BigDecimal.valueOf(n.value().doubleValue());
    }
    throw new IllegalArgumentException();
  };
  JsonDecoder<Boolean> BOOLEAN = json -> {
    if (json instanceof JsonBoolean b) {
      return b.value();
    }
    throw new IllegalArgumentException();
  };

  T decode(JsonElement json);
  
  @SuppressWarnings("unchecked")
  static <T> JsonDecoder<T> create(Type type) {
    if (type instanceof Class clazz) {
      return create(clazz);
    }
    if (type instanceof ParameterizedType paramType) {
      return create(paramType);
    }
    if (type instanceof WildcardType wildcardType) {
      return create(wildcardType);
    }
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  static <T> JsonDecoder<T> create(Class<T> type) {
    if (type.isPrimitive()) {
      return primitiveDecoder(type);
    } else if (type.equals(String.class)) {
      return (JsonDecoder<T>) STRING;
    } else if (type.equals(Byte.class)) {
      return (JsonDecoder<T>) BYTE;
    } else if (type.equals(Short.class)) {
      return (JsonDecoder<T>) SHORT;
    } else if (type.equals(Integer.class)) {
      return (JsonDecoder<T>) INTEGER;
    } else if (type.equals(Long.class)) {
      return (JsonDecoder<T>) LONG;
    } else if (type.equals(BigDecimal.class)) {
      return (JsonDecoder<T>) BIG_DECIMAL;
    } else if (type.equals(BigInteger.class)) {
      return (JsonDecoder<T>) BIG_INTEGER;
    } else if (type.equals(Float.class)) {
      return (JsonDecoder<T>) FLOAT;
    } else if (type.equals(Double.class)) {
      return (JsonDecoder<T>) DOUBLE;
    } else if (type.equals(Boolean.class)) {
      return (JsonDecoder<T>) BOOLEAN;
    } else if (type.isEnum()) {
      Class enumType = type;
      return enumDecoder(enumType);
    } else if (type.isArray()) {
      Class componentType = type.getComponentType();
      return arrayDecoder(componentType);
    } else if (type.isRecord()) {
      return recordDecoder(type);
    } else {
      return pojoDecoder(type);
    }
  }

  @SuppressWarnings("unchecked")
  static <T> JsonDecoder<T[]> arrayDecoder(Class<T> type) {
    return json -> {
      if (json instanceof JsonArray a) {
        Object array = Array.newInstance(type, a.elements().size());
        JsonDecoder<T> itemDecoder = create(type);
        for (int i = 0; i < a.elements().size(); i++) {
          JsonElement element = a.elements().get(i);
          Array.set(array, i, itemDecoder.decode(element));
        }
        return (T[]) array;
      }
      throw new IllegalArgumentException();
    };
  }

  static <T extends Enum<T>> JsonDecoder<T> enumDecoder(Class<T> type) {
    return json -> {
      if (json instanceof JsonString s) {
        return Enum.valueOf(type, s.value());
      }
      throw new IllegalArgumentException();
    };
  }

  static <T> JsonDecoder<T> recordDecoder(Class<T> type) {
    return json -> {
      if (json instanceof JsonObject o) {
        List<Class<?>> types = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (RecordComponent recordComponent : type.getRecordComponents()) {
          types.add(recordComponent.getType());
          JsonElement jsonElement = o.values().get(recordComponent.getName());
          Class<?> fieldType = recordComponent.getType();
          JsonDecoder fieldDecoder = create(fieldType);
          values.add(fieldDecoder.decode(jsonElement));
        }
        try {
          Constructor<T> constructor = type.getDeclaredConstructor(types.toArray(Class[]::new));
          return constructor.newInstance(values.toArray(Object[]::new));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new RuntimeException(e);
        }
      }
      throw new IllegalArgumentException();
    };
  }

  static <T> JsonDecoder<T> pojoDecoder(Class<T> type) {
    return json -> {
      if (json instanceof JsonObject o) {
        try {
          T value = type.getConstructor().newInstance();
          for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            JsonElement jsonElement = o.values().get(field.getName());
            JsonDecoder fieldDecoder = create(fieldType);
            field.set(value, fieldDecoder.decode(jsonElement));
          }
          return value;
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException 
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new RuntimeException(e);
        }
      }
      throw new IllegalArgumentException();
    };
  }

  static <E> JsonDecoder<Iterable<E>> listDecoder(JsonDecoder<E> itemDecoder) {
    return json -> {
      if (json instanceof JsonElement.JsonArray array) {
        return array.elements().stream().map(itemDecoder::decode).collect(toUnmodifiableList());
      }
      throw new IllegalArgumentException();
    };
  }

  static <V> JsonDecoder<Map<String, V>> mapDecoder(JsonDecoder<V> itemEncoder) {
    return json -> {
      if (json instanceof JsonElement.JsonObject object) {
        return object.values().entrySet().stream()
            .map(entry -> entry(entry.getKey(), itemEncoder.decode(entry.getValue())))
            .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
      }
      throw new IllegalArgumentException();
    };
  }
  
  private static <T> JsonDecoder<T> primitiveDecoder(Class<T> type) {
    if (type.equals(byte.class)) {
      return (JsonDecoder<T>) BYTE;
    }
    if (type.equals(short.class)) {
      return (JsonDecoder<T>) SHORT;
    }
    if (type.equals(int.class)) {
      return (JsonDecoder<T>) INTEGER;
    }
    if (type.equals(long.class)) {
      return (JsonDecoder<T>) LONG;
    }
    if (type.equals(float.class)) {
      return (JsonDecoder<T>) FLOAT;
    }
    if (type.equals(double.class)) {
      return (JsonDecoder<T>) DOUBLE;
    }
    if (type.equals(boolean.class)) {
      return (JsonDecoder<T>) BOOLEAN;
    }
    throw new IllegalArgumentException();
  }
}
