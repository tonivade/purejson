/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public sealed interface JsonNode {

  JsonNode NULL = new JsonNull();
  JsonNode TRUE = new JsonPrimitive(true);
  JsonNode FALSE = new JsonPrimitive(false);

  default JsonArray asArray() {
    throw new UnsupportedOperationException();
  }

  default JsonObject asObject() {
    throw new UnsupportedOperationException();
  }

  default JsonPrimitive asPrimitive() {
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

  default boolean isPrimitive() {
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
  }

  public static record JsonObject(Map<String, JsonNode> value) implements JsonNode, Iterable<Map.Entry<String, JsonNode>> {

    public JsonObject {
      checkNonNull(value);
    }

    public JsonObject() {
      this(new HashMap<>());
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
  }

  public record JsonPrimitive(java.lang.Object value) implements JsonNode {
    
    public JsonPrimitive {
      checkNonNull(value);
    }
    
    public JsonPrimitive(String value) {
      this((Object) value);
    }

    public JsonPrimitive(boolean value) {
      this((Boolean) value);
    }

    public JsonPrimitive(int value) {
      this(((Integer) value).longValue());
    }

    public JsonPrimitive(long value) {
      this((Long) value);
    }

    public JsonPrimitive(float value) {
      this(((Float) value).doubleValue());
    }

    public JsonPrimitive(double value) {
      this((Double) value);
    }
    
    @Override
    public boolean isPrimitive() {
      return true;
    }
    
    @Override
    public boolean isNumber() {
      return value instanceof Number;
    }
    
    @Override
    public boolean isString() {
      return value instanceof String;
    }
    
    @Override
    public boolean isBoolean() {
      return value instanceof Boolean;
    }
    
    @Override
    public int asInt() {
      return (int) asLong();
    }
    
    @Override
    public long asLong() {
      return (long) value;
    }
    
    @Override
    public float asFloat() {
      return (float) asDouble();
    }
    
    @Override
    public double asDouble() {
      return (double) value;
    }
    
    @Override
    public short asShort() {
      return (short) asInt();
    }
    
    @Override
    public byte asByte() {
      return (byte) asInt();
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
    public char asCharacter() {
      return asString().charAt(0);
    }
    
    @Override
    public boolean asBoolean() {
      return (boolean) value;
    }
    
    @Override
    public String asString() {
      return (String) value;
    }
    
    @Override
    public Number asNumber() {
      return (Number) value;
    }
    
    @Override
    public JsonPrimitive asPrimitive() {
      return this;
    }
  }
}
