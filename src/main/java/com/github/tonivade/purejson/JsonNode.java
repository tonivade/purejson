/*
 * Copyright (c) 2020-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.core.Precondition.checkNonNull;
import static com.github.tonivade.purefun.core.Unit.unit;
import static java.util.stream.Collectors.joining;

import java.io.Serial;
import java.io.Serializable;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.tonivade.purefun.core.Unit;
import com.github.tonivade.purefun.type.Try;

public sealed interface JsonNode extends Serializable {

  JsonNode NULL = JsonNull.NULL;
  JsonNode TRUE = JsonBoolean.TRUE;
  JsonNode FALSE = JsonBoolean.FALSE;

  default Try<Unit> writeTo(Writer writer) {
    return Try.of(() -> {
      writer.write(toString());
      return unit();
    });
  }

  default boolean isArray() {
    return false;
  }

  default boolean isObject() {
    return false;
  }

  default boolean isString() {
    return false;
  }

  default boolean isNumber() {
    return false;
  }

  default boolean isBoolean() {
    return false;
  }

  default boolean isNull() {
    return false;
  }

  default JsonArray asArray() {
    throw new UnsupportedOperationException();
  }

  default JsonObject asObject() {
    throw new UnsupportedOperationException();
  }

  default JsonNull asNull() {
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

  enum JsonNull implements JsonNode {
    NULL;

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

  enum JsonBoolean implements JsonNode {
    TRUE() {
      @Override
      public boolean asBoolean() {
        return true;
      }

      @Override
      public String toString() {
        return "true";
      }
    },
    FALSE() {
      @Override
      public boolean asBoolean() {
        return false;
      }

      @Override
      public String toString() {
        return "false";
      }
    };

    @Override
    public boolean isBoolean() {
      return true;
    }
  }

  final class JsonArray implements JsonNode, Iterable<JsonNode> {

    @Serial
    private static final long serialVersionUID = 2330798672175039020L;

    private final List<JsonNode> values = new ArrayList<>();

    public int size() {
      return values.size();
    }

    @Override
    public Iterator<JsonNode> iterator() {
      return values.stream().iterator();
    }

    public JsonNode get(int i) {
      return values.get(i);
    }

    void add(JsonNode value) {
      values.add(value);
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
    public int hashCode() {
      return Objects.hash(values);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof JsonArray other) {
        return Objects.equals(this.values, other.values);
      }
      return false;
    }

    @Override
    public String toString() {
      return values.stream().map(JsonNode::toString).collect(joining(",", "[", "]"));
    }
  }

  final class JsonObject implements JsonNode, Iterable<Tuple> {

    @Serial
    private static final long serialVersionUID = -5023192121266472804L;

    private final Map<String, JsonNode> values = new LinkedHashMap<>();

    public JsonNode get(String name) {
      return values.getOrDefault(name, NULL);
    }

    @Override
    public Iterator<Tuple> iterator() {
      return values.entrySet().stream().map(Tuple::new).iterator();
    }

    void add(Tuple tuple) {
      add(tuple.key(), tuple.value());
    }

    void add(String name, JsonNode value) {
      values.put(name, value);
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
    public int hashCode() {
      return Objects.hash(values);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof JsonObject other) {
        return Objects.equals(this.values, other.values);
      }
      return false;
    }

    @Override
    public String toString() {
      return values.entrySet().stream()
          .map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
          .collect(joining(",", "{", "}"));
    }
  }

  record JsonString(String value) implements JsonNode {

    public JsonString {
      checkNonNull(value);
    }

    @Override
    public boolean isString() {
      return true;
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

  record JsonNumber(Number value) implements JsonNode {

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

  record Tuple(String key, JsonNode value) {

    Tuple(Map.Entry<String, JsonNode> entry) {
      this(entry.getKey(), entry.getValue());
    }
  }
}
