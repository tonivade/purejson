/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("preview")
public sealed interface JsonElement permits
    JsonElement.JsonNull, JsonElement.JsonObject, JsonElement.JsonArray, JsonPrimitive {

  JsonElement NULL = new JsonNull();
  JsonElement EMPTY_OBJECT = new JsonObject();
  JsonElement EMPTY_ARRAY = new JsonArray();

  final class JsonNull implements JsonElement {

    private JsonNull() {}

    @Override
    public String toString() {
      return "JsonNull";
    }
  }

  record JsonArray(ArrayList<JsonElement> elements) 
      implements JsonElement, Iterable<JsonElement> {

    public JsonArray() {
      this(new ArrayList<>());
    }

    public JsonArray {
      checkNonNull(elements);
    }
    
    @Override
    public Iterator<JsonElement> iterator() {
      return elements.iterator();
    }
  }

  record JsonObject(LinkedHashMap<String, JsonElement> values) 
      implements JsonElement, Iterable<Map.Entry<String, JsonElement>> {

    public JsonObject() {
      this(new LinkedHashMap<>());
    }

    public JsonObject {
      checkNonNull(values);
    }
    
    @Override
    public Iterator<Entry<String, JsonElement>> iterator() {
      return values.entrySet().iterator();
    }
  }

  static JsonElement object(Iterable<Map.Entry<String, JsonElement>> elements) {
    return elements == null ? NULL : new JsonObject(streamFrom(elements).collect(toLinkedHashMap()));
  }

  @SafeVarargs
  static JsonElement object(Map.Entry<String, JsonElement>... elements) {
    return elements == null ? NULL : new JsonObject(Arrays.stream(elements).collect(toLinkedHashMap()));
  }

  static JsonElement array(Iterable<JsonElement> elements) {
    return elements == null ? NULL : new JsonArray(streamFrom(elements).collect(toArrayList()));
  }

  static JsonElement array(JsonElement... elements) {
    return elements == null ? NULL : new JsonArray(stream(elements).collect(toArrayList()));
  }

  static <V> Map.Entry<String, V> entry(String key, V value) {
    return new AbstractMap.SimpleImmutableEntry<>(key, value);
  }

  private static Collector<JsonElement, ?, ArrayList<JsonElement>> toArrayList() {
    return toCollection(ArrayList::new);
  }

  private static <K, V> Collector<Entry<K, V>, ?, LinkedHashMap<K, V>> toLinkedHashMap() {
    return toMap(Map.Entry::getKey, Map.Entry::getValue, mergeConflict(), LinkedHashMap::new);
  }

  private static <T> Stream<T> streamFrom(Iterable<T> elements) {
    return StreamSupport.stream(elements.spliterator(), false);
  }

  private static <T> BinaryOperator<T> mergeConflict() {
    return (a, b) -> { throw new RuntimeException(); };
  }
}

