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
import java.util.Objects;

public sealed interface JsonNode {

  JsonNode NULL = new Null();
  JsonNode TRUE = new Primitive(true);
  JsonNode FALSE = new Primitive(false);

  default Array asArray() {
    throw new UnsupportedOperationException();
  }

  default Object asObject() {
    throw new UnsupportedOperationException();
  }

  default Primitive asPrimitive() {
    throw new UnsupportedOperationException();
  }

  default Null asNull() {
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

  public static final class Null implements JsonNode {

    private Null() {
    }
    
    @Override
    public boolean isNull() {
      return true;
    }
    
    @Override
    public Null asNull() {
      return this;
    }
  }

  public static final class Array implements JsonNode, Iterable<JsonNode> {
    
    private final List<JsonNode> value;
    
    public Array() {
      this(new ArrayList<>());
    }

    public Array(List<JsonNode> value) {
      this.value = checkNonNull(value);
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
    public Array asArray() {
      return this;
    }
  }

  public static final class Object implements JsonNode, Iterable<Map.Entry<String, JsonNode>> {

    private final Map<String, JsonNode> value;

    public Object() {
      this(new HashMap<>());
    }

    public Object(Map<String, JsonNode> value) {
      this.value = checkNonNull(value);
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
    public Object asObject() {
      return this;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(value);
    }

    @Override
    public boolean equals(java.lang.Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Object other = (Object) obj;
      return Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
      return "JsonObject [value=" + value + "]";
    }
  }

  public record Primitive(java.lang.Object value) implements JsonNode {
    
    public Primitive {
      checkNonNull(value);
    }
    
    public Primitive(String value) {
      this((java.lang.Object) value);
    }

    public Primitive(boolean value) {
      this((java.lang.Object) value);
    }

    public Primitive(int value) {
      this((java.lang.Object) value);
    }

    public Primitive(long value) {
      this((java.lang.Object) value);
    }

    public Primitive(float value) {
      this((java.lang.Object) value);
    }

    public Primitive(double value) {
      this((java.lang.Object) value);
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
      return (int) value;
    }
    
    @Override
    public long asLong() {
      return (long) value;
    }
    
    @Override
    public float asFloat() {
      return (float) value;
    }
    
    @Override
    public double asDouble() {
      return (double) value;
    }
    
    @Override
    public short asShort() {
      return (short) value;
    }
    
    @Override
    public byte asByte() {
      return (byte) value;
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
    public Primitive asPrimitive() {
      return this;
    }
  }
}
