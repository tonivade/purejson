/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonElement.NULL;
import static com.github.tonivade.json.JsonElement.array;
import static com.github.tonivade.json.JsonElement.emptyObject;
import static com.github.tonivade.json.JsonElement.object;
import static com.github.tonivade.json.JsonPrimitive.bool;
import static com.github.tonivade.json.JsonPrimitive.number;
import static com.github.tonivade.json.JsonPrimitive.string;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.petitparser.context.Result;

import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.data.ImmutableList;

public final class Json {

  private final Map<Class<?>, JsonAdapter<?>> adapters = new HashMap<>();

  public static String serialize(JsonElement element) {
    if (element instanceof JsonElement.JsonNull) {
      return "null";
    }
    if (element instanceof JsonElement.JsonObject obj) {
      return obj.values().entries().stream()
          .map(entry -> "\"%s\":%s".formatted(entry.get1(), serialize(entry.get2())))
          .collect(joining(",", "{", "}"));
    }
    if (element instanceof JsonElement.JsonArray array) {
      return array.elements().stream()
          .map(Json::serialize)
          .collect(joining(",", "[", "]"));
    }
    if (element instanceof JsonPrimitive.JsonString string) {
      return "\"%s\"".formatted(string.value());
    }
    if (element instanceof JsonPrimitive.JsonNumber number) {
      return String.valueOf(number.value());
    }
    if (element instanceof JsonPrimitive.JsonBoolean bool) {
      return String.valueOf(bool.value());
    }
    throw new IllegalArgumentException("this should not happen");
  }

  public static JsonElement parse(String json) {
    Result result = new JsonParser().parse(json);

    if (result.isSuccess()) {
      return result.get();
    }

    throw new IllegalArgumentException(result.getMessage());
  }

  public <T> T fromJson(String json, Class<T> type) {
    return fromJson(parse(json), type) ;
  }

  @SuppressWarnings("unchecked")
  public <T> T fromJson(JsonElement element, Class<T> type) {
    if (element instanceof JsonElement.JsonNull) {
      return null;
    }
    if (element instanceof JsonElement.JsonObject) {
      JsonAdapter<T> jsonAdapter = (JsonAdapter<T>) adapters.get(type);
      if (jsonAdapter != null) {
        return jsonAdapter.decode(element);
      }
    }
    throw new IllegalArgumentException("this should not happen");
  }

  public String toString(Object object) {
    return serialize(toJson(object));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public JsonElement toJson(Object object) {
    if (object == null) {
      return NULL;
    }
    if (object instanceof String s) {
      return string(s);
    }
    if (object instanceof Integer i) {
      return number(i);
    }
    if (object instanceof Double d) {
      return number(d);
    }
    if (object instanceof Boolean b) {
      return bool(b);
    }
    if (object instanceof Iterable<?> iterable) {
      List<JsonElement> items = new ArrayList<>();
      for (Object item : iterable) {
        items.add(toJson(item));
      }
      return array(items);
    }
    if (object instanceof Map<?, ?> map && map.isEmpty()) {
      return emptyObject();
    }
    if (object instanceof Map<?, ?> map && map.keySet().stream().allMatch(key -> key instanceof String)) {
      Map<String, JsonElement> entries = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        entries.put((String) entry.getKey(), toJson(entry.getValue()));
      }
      return object(entries);
    }
    JsonAdapter jsonAdapter = adapters.get(object.getClass());
    if (jsonAdapter != null) {
      return jsonAdapter.encode(object);
    }
    throw new UnsupportedOperationException("not implemented yet");
  }

  public <T> Json add(Class<T> type, JsonAdapter<T> adapter) {
    adapters.put(type, adapter);
    return this;
  }

  public static <E> JsonAdapterBuilder<E> adapter(Class<E> target) {
    return new JsonAdapterBuilder<>(target);
  }

  public static <E> JsonAdapter<Iterable<E>> listAdapter(JsonAdapter<E> itemAdapter) {
    return new JsonAdapter<>() {
      @Override
      public Iterable<E> decode(JsonElement json) {
        if (json instanceof JsonElement.JsonArray array) {
          return array.elements().map(itemAdapter::decode);
        }
        throw new IllegalArgumentException();
      }

      @Override
      public JsonElement encode(Iterable<E> value) {
        return array(ImmutableList.from(value).map(itemAdapter::encode).toList());
      }
    };
  }

  public static <V> JsonAdapter<Map<String, V>> mapAdapter(JsonAdapter<V> valueAdapter) {
    return new JsonAdapter<>() {
      @Override
      public Map<String, V> decode(JsonElement json) {
        if (json instanceof JsonElement.JsonObject object) {
          return object.values().entries().stream()
              .map(entry -> entry(entry.get1(), valueAdapter.decode(entry.get2())))
              .collect(toUnmodifiableMap(Tuple2::get1, Tuple2::get2));
        }
        throw new IllegalArgumentException();
      }

      @Override
      public JsonElement encode(Map<String, V> value) {
        return object(
            value.entrySet().stream()
                .map(entry -> entry(entry.getKey(), valueAdapter.encode(entry.getValue())))
                .collect(toUnmodifiableMap(Tuple2::get1, Tuple2::get2))
        );
      }
    };
  }

  public static <V> Tuple2<String, V> entry(String key, V value) {
    return Tuple2.of(key, value);
  }
}
