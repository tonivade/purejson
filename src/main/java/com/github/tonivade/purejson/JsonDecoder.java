/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Try;
import com.google.gson.internal.reflect.ReflectionAccessor;

@FunctionalInterface
@SuppressWarnings("preview")
public interface JsonDecoder<T> {
  
  JsonDecoder<String> STRING = JsonNode::getAsString;
  JsonDecoder<Character> CHAR = JsonNode::getAsCharacter;
  JsonDecoder<Byte> BYTE = JsonNode::getAsByte;
  JsonDecoder<Short> SHORT = JsonNode::getAsShort;
  JsonDecoder<Integer> INTEGER = JsonNode::getAsInt;
  JsonDecoder<Long> LONG = JsonNode::getAsLong;
  JsonDecoder<Float> FLOAT = JsonNode::getAsFloat;
  JsonDecoder<Double> DOUBLE = JsonNode::getAsDouble;
  JsonDecoder<BigInteger> BIG_INTEGER = JsonNode::getAsBigInteger;
  JsonDecoder<BigDecimal> BIG_DECIMAL = JsonNode::getAsBigDecimal;
  JsonDecoder<Boolean> BOOLEAN = JsonNode::getAsBoolean;

  T decode(JsonNode json);

  default Try<T> tryDecode(JsonNode json) {
    return Try.of(() -> decode(json));
  }
  
  default <R> JsonDecoder<R> andThen(Function1<T, R> next) {
    return json -> next.apply(decode(json));
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <T> JsonDecoder<T> create(Type type) {
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
  static <T> JsonDecoder<T> create(ParameterizedType type) {
    if (type.getRawType() instanceof Class<?> c && Collection.class.isAssignableFrom(c)) {
      var create = create(type.getActualTypeArguments()[0]);
      return (JsonDecoder<T>) iterableDecoder(create).andThen(toCollection(c));
    }
    if (type.getRawType() instanceof Class<?> c && Sequence.class.isAssignableFrom(c)) {
      var create = create(type.getActualTypeArguments()[0]);
      return (JsonDecoder<T>) iterableDecoder(create).andThen(toSequence(c));
    }
    if (type.getRawType() instanceof Class<?> c 
        && Map.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      var create = create(type.getActualTypeArguments()[1]);
      return (JsonDecoder<T>) mapDecoder(create);
    }
    if (type.getRawType() instanceof Class<?> c 
        && ImmutableMap.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      var create = create(type.getActualTypeArguments()[1]);
      return (JsonDecoder<T>) mapDecoder(create).andThen(toImmutableMap(c));
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  static <T> JsonDecoder<T> create(GenericArrayType type) {
    Type genericComponentType = type.getGenericComponentType();
    if (genericComponentType instanceof Class<?> c) {
      return (JsonDecoder<T>) arrayDecoder(c);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  static <T> JsonDecoder<T> create(WildcardType type) {
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  static <T> JsonDecoder<T> create(Class<T> type) {
    if (type.isPrimitive()) {
      return primitiveDecoder(type);
    } else if (type.equals(String.class)) {
      return (JsonDecoder<T>) STRING;
    } else if (type.equals(Character.class)) {
      return (JsonDecoder<T>) CHAR;
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
    var itemDecoder = create((Type) type);
    return json -> {
      if (json instanceof JsonNode.Array a) {
        var array = Array.newInstance(type, a.size());
        for (int i = 0; i < a.size(); i++) {
          JsonNode element = a.get(i);
          Array.set(array, i, itemDecoder.decode(element));
        }
        return (T[]) array;
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <T extends Enum<T>> JsonDecoder<T> enumDecoder(Class<T> type) {
    return STRING.andThen(string -> Enum.valueOf(type, string));
  }

  static <T> JsonDecoder<T> recordDecoder(Class<T> type) {
    var fields = Arrays.stream(type.getRecordComponents())
        .map(f -> Tuple2.of(f, create(f.getGenericType())))
        .collect(toList());
    return json -> {
      if (json instanceof JsonNode.Object o) {
        var types = new ArrayList<Class<?>>();
        var values = new ArrayList<>();
        for (var pair : fields) {
          types.add(pair.get1().getType());
          JsonNode jsonElement = o.get(pair.get1().getName());
          values.add(pair.get2().decode(jsonElement));
        }
        try {
          var constructor = type.getDeclaredConstructor(types.toArray(Class[]::new));
          return constructor.newInstance(values.toArray(Object[]::new));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new RuntimeException(e);
        }
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <T> JsonDecoder<T> pojoDecoder(Class<T> type) {
    var fields = Arrays.stream(type.getDeclaredFields())
        .filter(f -> !isStatic(f.getModifiers()))
        .filter(f -> !f.isSynthetic())
        .peek(f -> ReflectionAccessor.getInstance().makeAccessible(f))
        .map(f -> Tuple2.of(f, create(f.getGenericType())))
        .collect(toList());
    return json -> {
      if (json instanceof JsonNode.Object o) {
        try {
          T value = type.getDeclaredConstructor().newInstance();
          for (var pair : fields) {
            JsonNode jsonElement = o.get(pair.get1().getName());
            pair.get1().set(value, pair.get2().decode(jsonElement));
          }
          return value;
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException 
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new RuntimeException(e);
        }
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <E> JsonDecoder<Iterable<E>> iterableDecoder(JsonDecoder<E> itemDecoder) {
    return json -> {
      if (json instanceof JsonNode.Array array) {
        var list = new ArrayList<E>();
        for (JsonNode object : array) {
          list.add(itemDecoder.decode(object));
        }
        return unmodifiableList(list);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <V> JsonDecoder<Map<String, V>> mapDecoder(JsonDecoder<V> itemEncoder) {
    return json -> {
      if (json instanceof JsonNode.Object object) {
        var map = new LinkedHashMap<String, V>();
        for (Map.Entry<String, JsonNode> entry : object) {
          map.put(entry.getKey(), itemEncoder.decode(entry.getValue()));
        }
        return unmodifiableMap(map);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }
  
  @SuppressWarnings("unchecked")
  private static <T> JsonDecoder<T> primitiveDecoder(Class<T> type) {
    if (type.equals(char.class)) {
      return (JsonDecoder<T>) CHAR;
    }
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
    throw new IllegalArgumentException("a new primitive type?" + type.getTypeName());
  }

  private static <T> Function1<Iterable<T>, Collection<T>> toCollection(Class<?> type) {
    if (Deque.class.isAssignableFrom(type)) {
      return iterable -> ImmutableList.from(iterable).toList();
    }
    if (Queue.class.isAssignableFrom(type)) {
      return iterable -> ImmutableList.from(iterable).toList();
    }
    if (List.class.isAssignableFrom(type)) {
      return iterable -> ImmutableArray.from(iterable).toList();
    }
    if (NavigableSet.class.isAssignableFrom(type)) {
      return iterable -> ImmutableTree.from(iterable).toNavigableSet();
    }
    if (Set.class.isAssignableFrom(type)) {
      return iterable -> ImmutableSet.from(iterable).toSet();
    }
    return iterable -> ImmutableArray.from(iterable).toList();
  }

  private static <T> Function1<Iterable<T>, Sequence<T>> toSequence(Class<?> type) {
    if (ImmutableList.class.isAssignableFrom(type)) {
      return ImmutableList::from;
    }
    if (ImmutableArray.class.isAssignableFrom(type)) {
      return ImmutableArray::from;
    }
    if (ImmutableSet.class.isAssignableFrom(type)) {
      return ImmutableSet::from;
    }
    if (ImmutableTree.class.isAssignableFrom(type)) {
      return ImmutableTree::from;
    }
    return ImmutableList::from;
  }

  private static <T> Function1<Map<String, T>, ImmutableMap<String, T>> toImmutableMap(Class<?> type) {
    if (ImmutableTreeMap.class.isAssignableFrom(type)) {
      return ImmutableTreeMap::from;
    }
    return ImmutableMap::from;
  }
  
  private static <T> JsonDecoder<T> nullSafe(JsonDecoder<T> decoder) {
    return json -> json instanceof JsonNode.Null ? null : decoder.decode(json);
  }
}
