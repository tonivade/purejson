/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static java.util.stream.Collectors.joining;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public sealed interface JsonNode {

  JsonNode NULL = new JsonNull();
  JsonNode TRUE = new JsonTrue();
  JsonNode FALSE = new JsonFalse();

  default JsonArray asArray() {
    throw new UnsupportedOperationException();
  }

  default JsonObject asObject() {
    throw new UnsupportedOperationException();
  }

  default JsonNull asNull() {
    throw new UnsupportedOperationException();
  }

  default boolean isArray() {
    throw new UnsupportedOperationException();
  }

  default boolean isObject() {
    throw new UnsupportedOperationException();
  }

  default boolean isString() {
    throw new UnsupportedOperationException();
  }

  default boolean isNumber() {
    throw new UnsupportedOperationException();
  }

  default boolean isBoolean() {
    throw new UnsupportedOperationException();
  }

  default boolean isNull() {
    throw new UnsupportedOperationException();
  }

  default boolean asBoolean() {
    throw new UnsupportedOperationException();
  }

  default Number asNumber() {
    throw new UnsupportedOperationException();
  }

  default String asString() {
    throw new UnsupportedOperationException();
  }

  default double asDouble() {
    throw new UnsupportedOperationException();
  }

  default float asFloat() {
    throw new UnsupportedOperationException();
  }

  default long asLong() {
    throw new UnsupportedOperationException();
  }

  default int asInt() {
    throw new UnsupportedOperationException();
  }

  default byte asByte() {
    throw new UnsupportedOperationException();
  }

  default short asShort() {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  default char asCharacter() {
    throw new UnsupportedOperationException();
  }

  default BigDecimal asBigDecimal() {
    throw new UnsupportedOperationException();
  }

  default BigInteger asBigInteger() {
    throw new UnsupportedOperationException();
  }

  public static final class JsonNull implements JsonNode {

    private JsonNull() {
    }

    @Override
    public boolean isNull() {
      return true;
    }

    @Override
    public JsonNull asNull() {
      return this;
    }

    @Override
    public String toString() {
      return "null";
    }
  }

  public static final class JsonTrue implements JsonNode {

    private JsonTrue() {
    }

    @Override
    public boolean isBoolean() {
      return true;
    }

    @Override
    public boolean asBoolean() {
      return true;
    }

    @Override
    public String toString() {
      return "true";
    }
  }

  public static final class JsonFalse implements JsonNode {

    private JsonFalse() {
    }

    @Override
    public boolean isBoolean() {
      return true;
    }

    @Override
    public boolean asBoolean() {
      return false;
    }

    @Override
    public String toString() {
      return "false";
    }
  }

  public static record JsonArray(List<JsonNode> value) implements JsonNode, Iterable<JsonNode> {

    public JsonArray {
      checkNonNull(value);
    }

    public JsonArray() {
      this(new ArrayList<>());
    }

    public int size() {
      return value.size();
    }

    @Override
    public Iterator<JsonNode> iterator() {
      return value.stream().iterator();
    }

    public JsonNode get(int i) {
      return value.get(i);
    }

    void add(JsonNode value) {
      this.value.add(value);
    }

    @Override
    public boolean isArray() {
      return true;
    }

    @Override
    public JsonArray asArray() {
      return this;
    }

    @Override
    public String toString() {
      return value.stream().map(JsonNode::toString).collect(joining(",", "[", "]"));
    }
  }

  public static record JsonObject(Map<String, JsonNode> value) implements JsonNode, Iterable<Map.Entry<String, JsonNode>> {

    public JsonObject {
      checkNonNull(value);
    }

    public JsonObject() {
      this(new LinkedHashMap<>());
    }

    public JsonNode get(String name) {
      return value.get(name);
    }

    @Override
    public Iterator<Map.Entry<String, JsonNode>> iterator() {
      return value.entrySet().stream().iterator();
    }

    void add(String name, JsonNode value) {
      this.value.put(name, value);
    }

    @Override
    public boolean isObject() {
      return true;
    }

    @Override
    public JsonObject asObject() {
      return this;
    }

    @Override
    public String toString() {
      return value.entrySet().stream().map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue()).collect(joining(",", "{", "}"));
    }
  }

  public record JsonString(String value) implements JsonNode {

    public JsonString {
      checkNonNull(value);
    }

    @Override
    public boolean isString() {
      return value instanceof String;
    }

    @Override
    public String asString() {
      return value;
    }

    @Override
    public char asCharacter() {
      return asString().charAt(0);
    }

    @Override
    public String toString() {
      return "\"" + value + "\"";
    }
  }

  public record JsonNumber(Number value) implements JsonNode {

    public JsonNumber {
      checkNonNull(value);
    }

    @Override
    public boolean isNumber() {
      return true;
    }

    @Override
    public int asInt() {
      return value.intValue();
    }

    @Override
    public long asLong() {
      return value.longValue();
    }

    @Override
    public float asFloat() {
      return value.floatValue();
    }

    @Override
    public double asDouble() {
      return value.doubleValue();
    }

    @Override
    public short asShort() {
      return value.shortValue();
    }

    @Override
    public byte asByte() {
      return value.byteValue();
    }

    @Override
    public BigDecimal asBigDecimal() {
      return BigDecimal.valueOf(asDouble());
    }

    @Override
    public BigInteger asBigInteger() {
      return BigInteger.valueOf(asLong());
    }

    @Override
    public Number asNumber() {
      return value;
    }

    @Override
    public String toString() {
      String string = value.toString();
      if (string.endsWith(".0")) {
        return string.substring(0, string.length() - 2);
      }
      return string;
    }
  }
}
