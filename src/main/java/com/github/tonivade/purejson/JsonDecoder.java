/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

@FunctionalInterface
@SuppressWarnings("preview")
public interface JsonDecoder<T> {

  T decode(JsonContext context, JsonNode json);

  default Try<T> tryDecode(JsonContext context, JsonNode json) {
    return Try.of(() -> decode(context, json));
  }
  
  default <R> JsonDecoder<R> andThen(Function1<? super T, ? extends R> next) {
    return (context, json) -> next.apply(decode(context, json));
  }
  
  static <T> JsonDecoder<T> decoder(Type type) {
    return nullSafe(JsonDecoder.<T>load(type).getOrElse(() -> create(type)));
  }
  
  @SuppressWarnings("unchecked")
  static <T> Option<JsonDecoder<T>> load(Type type) {
    return JsonAdapter.load(type).map(d -> (JsonDecoder<T>) d);
  }

  @SuppressWarnings("unchecked")
  static <T> JsonDecoder<T[]> arrayDecoder(Class<T> type) {
    return (context, json) -> {
      if (json instanceof JsonNode.Array a) {
        var array = Array.newInstance(type, a.size());
        for (int i = 0; i < a.size(); i++) {
          JsonNode element = a.get(i);
          Array.set(array, i, context.decode(element, type));
        }
        return (T[]) array;
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <T extends Enum<T>> JsonDecoder<T> enumDecoder(Class<T> type) {
    return create(String.class).andThen(string -> Enum.valueOf(type, string));
  }

  static <T> JsonDecoder<T> recordDecoder(Class<T> type) {
    return (context, json) -> {
      if (json instanceof JsonNode.Object o) {
        var types = new ArrayList<Class<?>>();
        var values = new ArrayList<>();
        for (var component : type.getRecordComponents()) {
          types.add(component.getType());
          JsonNode jsonElement = o.get(component.getName());
          values.add(context.decode(jsonElement, component.getGenericType()));
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
    return (context, json) -> {
      if (json instanceof JsonNode.Object o) {
        try {
          T value = type.getDeclaredConstructor().newInstance();
          for (var field : type.getDeclaredFields()) {
            if (!isStatic(field.getModifiers()) && !field.isSynthetic() && field.trySetAccessible()) {
              JsonNode jsonElement = o.get(field.getName());
              field.set(value, context.decode(jsonElement, field.getGenericType()));
            }
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
    return (context, json) -> {
      if (json instanceof JsonNode.Array array) {
        var list = new ArrayList<E>();
        for (JsonNode object : array) {
          list.add(itemDecoder.decode(context, object));
        }
        return unmodifiableList(list);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <V> JsonDecoder<Map<String, V>> mapDecoder(JsonDecoder<V> itemEncoder) {
    return (context, json) -> {
      if (json instanceof JsonNode.Object object) {
        var map = new LinkedHashMap<String, V>();
        for (Map.Entry<String, JsonNode> entry : object) {
          map.put(entry.getKey(), itemEncoder.decode(context, entry.getValue()));
        }
        return unmodifiableMap(map);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }
  
  static <T> JsonDecoder<T> nullSafe(JsonDecoder<T> decoder) {
    return (context, json) -> json instanceof JsonNode.Null ? null : decoder.decode(context, json);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static <T> JsonDecoder<T> create(Type type) {
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
  private static <T> JsonDecoder<T> create(ParameterizedType type) {
    if (type.getRawType() instanceof Class<?> c && Collection.class.isAssignableFrom(c)) {
      var create = decoder(type.getActualTypeArguments()[0]);
      return (JsonDecoder<T>) iterableDecoder(create).andThen(toCollection(c));
    }
    if (type.getRawType() instanceof Class<?> c && Sequence.class.isAssignableFrom(c)) {
      var create = decoder(type.getActualTypeArguments()[0]);
      return (JsonDecoder<T>) iterableDecoder(create).andThen(toSequence(c));
    }
    if (type.getRawType() instanceof Class<?> c 
        && Map.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      var create = decoder(type.getActualTypeArguments()[1]);
      return (JsonDecoder<T>) mapDecoder(create);
    }
    if (type.getRawType() instanceof Class<?> c 
        && ImmutableMap.class.isAssignableFrom(c)
        && type.getActualTypeArguments()[0].equals(String.class)) {
      var create = decoder(type.getActualTypeArguments()[1]);
      return (JsonDecoder<T>) mapDecoder(create).andThen(toImmutableMap(c));
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonDecoder<T> create(GenericArrayType type) {
    Type genericComponentType = type.getGenericComponentType();
    if (genericComponentType instanceof Class<?> c) {
      return (JsonDecoder<T>) arrayDecoder(c);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  private static <T> JsonDecoder<T> create(WildcardType type) {
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static <T> JsonDecoder<T> create(Class<T> type) {
    if (type.isPrimitive()) {
      return primitiveDecoder(type);
    } else if (type.equals(String.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.STRING;
    } else if (type.equals(Character.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.CHAR;
    } else if (type.equals(Byte.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BYTE;
    } else if (type.equals(Short.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.SHORT;
    } else if (type.equals(Integer.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.INTEGER;
    } else if (type.equals(Long.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.LONG;
    } else if (type.equals(BigDecimal.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BIG_DECIMAL;
    } else if (type.equals(BigInteger.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BIG_INTEGER;
    } else if (type.equals(Float.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.FLOAT;
    } else if (type.equals(Double.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.DOUBLE;
    } else if (type.equals(Boolean.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BOOLEAN;
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
  private static <T> JsonDecoder<T> primitiveDecoder(Class<T> type) {
    if (type.equals(char.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.CHAR;
    }
    if (type.equals(byte.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BYTE;
    }
    if (type.equals(short.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.SHORT;
    }
    if (type.equals(int.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.INTEGER;
    }
    if (type.equals(long.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.LONG;
    }
    if (type.equals(float.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.FLOAT;
    }
    if (type.equals(double.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.DOUBLE;
    }
    if (type.equals(boolean.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BOOLEAN;
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
}

interface JsonDecoderModule {
  
  JsonDecoder<String> STRING = primitive(JsonNode::asString);
  JsonDecoder<Character> CHAR = primitive(JsonNode::asCharacter);
  JsonDecoder<Byte> BYTE = primitive(JsonNode::asByte);
  JsonDecoder<Short> SHORT = primitive(JsonNode::asShort);
  JsonDecoder<Integer> INTEGER = primitive(JsonNode::asInt);
  JsonDecoder<Long> LONG = primitive(JsonNode::asLong);
  JsonDecoder<Float> FLOAT = primitive(JsonNode::asFloat);
  JsonDecoder<Double> DOUBLE = primitive(JsonNode::asDouble);
  JsonDecoder<BigInteger> BIG_INTEGER = primitive(JsonNode::asBigInteger);
  JsonDecoder<BigDecimal> BIG_DECIMAL = primitive(JsonNode::asBigDecimal);
  JsonDecoder<Boolean> BOOLEAN = primitive(JsonNode::asBoolean);
  
  static <T> JsonDecoder<T> primitive(Function1<JsonNode, T> method) {
    return (context, json) -> method.apply(json);
  }
}