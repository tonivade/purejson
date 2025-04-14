/*
 * Copyright (c) 2020-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.data.Sequence.listOf;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
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

import com.github.tonivade.purefun.Nullable;
import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Tuple2;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.ImmutableTreeMap;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purejson.JsonNode.Tuple;

@FunctionalInterface
public interface JsonDecoder<T> {

  @Nullable
  T decode(JsonNode json);

  default Try<T> tryDecode(JsonNode json) {
    return Try.of(() -> decode(json));
  }

  default <R> JsonDecoder<R> andThen(Function1<? super T, ? extends R> next) {
    return json -> next.apply(decode(json));
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
    var itemDecoder = decoder(type);
    return json -> {
      if (json instanceof JsonNode.JsonArray a) {
        var array = Array.newInstance(type, a.size());
        for (int i = 0; i < a.size(); i++) {
          Array.set(array, i, itemDecoder.decode(a.get(i)));
        }
        return (T[]) array;
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <T extends Enum<T>> JsonDecoder<T> enumDecoder(Class<T> type) {
    return create(String.class).andThen(string -> Enum.valueOf(type, string));
  }

  private static <T> JsonDecoder<T> recordDecoder(Class<T> clazz) {
    var fields = Arrays.stream(clazz.getRecordComponents())
        .map(f -> Tuple2.of(f, decoder(f.getGenericType())))
        .toList();
    var types = fields.stream()
        .map(Tuple2::get1)
        .map(RecordComponent::getType)
        .toList();
    var constructor = findCanonicalConstructor(clazz, types);
    var recordCreator = recordCreator(constructor, fields);
    return json -> {
      if (json instanceof JsonNode.JsonObject object) {
        return recordCreator.apply(object);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  private static <T> JsonDecoder<T> pojoDecoder(Class<T> clazz) {
    var fields = Arrays.stream(clazz.getDeclaredFields())
        .filter(f -> !isStatic(f.getModifiers()))
        .filter(f -> !f.isSynthetic())
        .filter(Field::trySetAccessible)
        .map(f -> Tuple2.of(f, decoder(f.getGenericType())))
        .toList();
    var constructor = findConstructor(clazz);
    var pojoCreator = pojoCreator(constructor, fields);
    return json -> {
      if (json instanceof JsonNode.JsonObject object) {
        return pojoCreator.apply(object);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  private static <T> Function1<JsonNode.JsonObject, T> recordCreator(
      Constructor<T> constructor, List<Tuple2<RecordComponent, JsonDecoder<Object>>> fields) {
    return object -> {
      var values = new ArrayList<>();
      for (var pair : fields) {
        JsonNode jsonElement = object.get(pair.get1().getName());
        values.add(pair.get2().decode(jsonElement));
      }
      try {
        return constructor.newInstance(values.toArray(Object[]::new));
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException("cannot create instance of record: " + constructor.getDeclaringClass().getName(), e);
      }
    };
  }

  private static <T> Function1<JsonNode.JsonObject, T>
      pojoCreator(Constructor<T> constructor, List<Tuple2<Field, JsonDecoder<Object>>> fields) {
    if (!constructor.trySetAccessible()) {
      throw new IllegalStateException("cannot access to constructor: " + constructor);
    }
    if (constructor.getParameterCount() > 0 && constructor.isAnnotationPresent(JsonCreator.class)) {
      return pojoCreatorFromAnnotatedConstructor(constructor, fields);
    }
    if (constructor.getParameterCount() == 0) {
      return pojoCreatorFromDefaultConstructor(constructor, fields);
    }
    throw new IllegalStateException("no suitable constructor for type " + constructor.getDeclaringClass().getName());
  }

  private static <T> Function1<JsonNode.JsonObject, T> pojoCreatorFromDefaultConstructor(
      Constructor<T> constructor, List<Tuple2<Field, JsonDecoder<Object>>> fields) {
    return object -> {
      try {
        T value = constructor.newInstance();
        for (var pair : fields) {
          var node = object.get(pair.get1().getName());
          pair.get1().set(value, pair.get2().decode(node));
        }
        return value;
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException("cannot create pojo using constructor " + constructor, e);
      }
    };
  }

  private static <T> Function1<JsonNode.JsonObject, T> pojoCreatorFromAnnotatedConstructor(
      Constructor<T> constructor, List<Tuple2<Field, JsonDecoder<Object>>> fields) {
    return object -> {
      try {
        var fieldsToDecode =
            fields.stream().collect(toUnmodifiableMap(t -> t.get1().getName(), Tuple2::get2));

        var values = Arrays.stream(constructor.getParameters())
            .map(p -> p.getAnnotation(JsonProperty.class))
            .map(JsonProperty::value)
            .map(name -> fieldsToDecode.getOrDefault(name, JsonDecoderModule.NULL).decode(object.get(name)))
            .toArray();

        return constructor.newInstance(values);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException("cannot create pojo using constructor " + constructor, e);
      }
    };
  }

  private static <T> Constructor<T> findCanonicalConstructor(Class<T> clazz, List<? extends Class<?>> types) {
    try {
      return clazz.getDeclaredConstructor(types.toArray(Class[]::new));
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("cannot create instance of record: " + clazz.getName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Constructor<T> findConstructor(Class<T> type) {
    return (Constructor<T>) findUniqueConstructor(type)
        .or(() -> findDefaultConstructor(type))
        .or(() -> findAnnotatedConstructor(type))
        .getOrElseThrow(() -> new IllegalStateException("no suitable constructor found for type: " + type.getName()));
  }

  static <T> Option<Constructor<?>> findUniqueConstructor(Class<T> type) {
    var list = listOf(type.getDeclaredConstructors());
    return list.tail().isEmpty() ? list.head() : Option.none();
  }

  static <T> Option<Constructor<?>> findAnnotatedConstructor(Class<T> type) {
    return listOf(type.getDeclaredConstructors()).filter(c -> c.isAnnotationPresent(JsonCreator.class)).head();
  }

  static <T> Option<Constructor<?>> findDefaultConstructor(Class<T> type) {
    return listOf(type.getDeclaredConstructors()).filter(c -> c.getParameterCount() == 0).head();
  }

  static <E> JsonDecoder<Iterable<E>> iterableDecoder(JsonDecoder<E> itemDecoder) {
    return json -> {
      if (json instanceof JsonNode.JsonArray array) {
        var list = new ArrayList<E>();
        for (var object : array) {
          list.add(itemDecoder.decode(object));
        }
        return unmodifiableList(list);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <V> JsonDecoder<Map<String, V>> mapDecoder(JsonDecoder<V> itemEncoder) {
    return json -> {
      if (json instanceof JsonNode.JsonObject object) {
        var map = new LinkedHashMap<String, V>();
        for (Tuple tuple : object) {
          map.put(tuple.key(), itemEncoder.decode(tuple.value()));
        }
        return unmodifiableMap(map);
      }
      throw new IllegalArgumentException(json.toString());
    };
  }

  static <T> JsonDecoder<T> nullSafe(JsonDecoder<T> decoder) {
    return json -> {
      if (json == null) {
        return null;
      }
      if (json instanceof JsonNode.JsonNull) {
        return null;
      }
      return decoder.decode(json);
    };
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonDecoder<T> create(Type type) {
    if (type instanceof Class clazz) {
      return create(clazz);
    }
    if (type instanceof ParameterizedType parameterizedType) {
      return create(parameterizedType);
    }
    if (type instanceof GenericArrayType genericArrayType) {
      return create(genericArrayType);
    }
    if (type instanceof WildcardType wildcardType) {
      return create(wildcardType);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonDecoder<T> create(ParameterizedType type) {
    if (type.getRawType() instanceof Class<?> c) {
      if (Collection.class.isAssignableFrom(c)) {
        var create = decoder(type.getActualTypeArguments()[0]);
        return (JsonDecoder<T>) iterableDecoder(create).andThen(toCollection(c));
      }
      if (Sequence.class.isAssignableFrom(c)) {
        var create = decoder(type.getActualTypeArguments()[0]);
        return (JsonDecoder<T>) iterableDecoder(create).andThen(toSequence(c));
      }
      if (Map.class.isAssignableFrom(c) && type.getActualTypeArguments()[0].equals(String.class)) {
        var create = decoder(type.getActualTypeArguments()[1]);
        return (JsonDecoder<T>) mapDecoder(create);
      }
      if (ImmutableMap.class.isAssignableFrom(c) && type.getActualTypeArguments()[0].equals(String.class)) {
        var create = decoder(type.getActualTypeArguments()[1]);
        return (JsonDecoder<T>) mapDecoder(create).andThen(toImmutableMap(c));
      }
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> JsonDecoder<T> create(GenericArrayType type) {
    Type genericComponentType = type.getGenericComponentType();
    if (genericComponentType instanceof Class<?> clazz) {
      return (JsonDecoder<T>) arrayDecoder(clazz);
    }
    throw new UnsupportedOperationException("not implemented yet: " + type.getTypeName());
  }

  private static <T> JsonDecoder<T> create(WildcardType type) {
    throw new UnsupportedOperationException("wildcard types are not supported: " + type.getTypeName());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static <T> JsonDecoder<T> create(Class<T> type) {
    if (type.isPrimitive()) {
      return primitiveDecoder(type);
    }
    if (type.equals(String.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.STRING;
    }
    if (type.equals(Character.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.CHAR;
    }
    if (type.equals(Byte.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BYTE;
    }
    if (type.equals(Short.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.SHORT;
    }
    if (type.equals(Integer.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.INTEGER;
    }
    if (type.equals(Long.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.LONG;
    }
    if (type.equals(BigDecimal.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BIG_DECIMAL;
    }
    if (type.equals(BigInteger.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BIG_INTEGER;
    }
    if (type.equals(Float.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.FLOAT;
    }
    if (type.equals(Double.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.DOUBLE;
    }
    if (type.equals(Boolean.class)) {
      return (JsonDecoder<T>) JsonDecoderModule.BOOLEAN;
    }
    if (type.isEnum()) {
      return enumDecoder((Class) type);
    }
    if (type.isArray()) {
      return arrayDecoder((Class) type.getComponentType());
    }
    if (type.isRecord()) {
      return recordDecoder(type);
    }
    return pojoDecoder(type);
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

  JsonDecoder<Object> NULL = ignore -> null;
  JsonDecoder<String> STRING = JsonNode::asString;
  JsonDecoder<Character> CHAR = JsonNode::asCharacter;
  JsonDecoder<Byte> BYTE = JsonNode::asByte;
  JsonDecoder<Short> SHORT = JsonNode::asShort;
  JsonDecoder<Integer> INTEGER = JsonNode::asInt;
  JsonDecoder<Long> LONG = JsonNode::asLong;
  JsonDecoder<Float> FLOAT = JsonNode::asFloat;
  JsonDecoder<Double> DOUBLE = JsonNode::asDouble;
  JsonDecoder<BigInteger> BIG_INTEGER = JsonNode::asBigInteger;
  JsonDecoder<BigDecimal> BIG_DECIMAL = JsonNode::asBigDecimal;
  JsonDecoder<Boolean> BOOLEAN = JsonNode::asBoolean;
}