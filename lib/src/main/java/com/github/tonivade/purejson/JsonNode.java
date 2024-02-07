/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.Unit.unit;
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

import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.type.Try;

public sealed interface JsonNode extends Serializable {

  JsonNode NULL = JsonNull.NULL;
  JsonNode TRUE = JsonBoolean.TRUE;
  JsonNode FALSE = JsonBoolean.TRUE;

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
    },
    FALSE() {
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
    };
  }

  final class JsonArray implements JsonNode, Iterable<JsonNode> {

    @Serial
    private static final long serialVersionUID = 2330798672175039020L;

    private final List<JsonNode> value = new ArrayList<>();

    public JsonArray() { }

    public JsonArray(Iterable<JsonNode> values) {
      checkNonNull(values).forEach(value::add);
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
    public int hashCode() {
      return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      JsonArray other = (JsonArray) obj;
      return Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
      return value.stream().map(JsonNode::toString).collect(joining(",", "[", "]"));
    }
  }

  final class JsonObject implements JsonNode, Iterable<Tuple> {

    @Serial
    private static final long serialVersionUID = -5023192121266472804L;

    private final Map<String, JsonNode> value = new LinkedHashMap<>();

    public JsonObject() { }

    public JsonObject(Iterable<Tuple> values) {
      checkNonNull(values).forEach(t -> value.put(t.key(), t.value()));
    }

    public JsonNode get(String name) {
      return value.get(name);
    }

    @Override
    public Iterator<Tuple> iterator() {
      return value.entrySet().stream().map(entry -> new Tuple(entry.getKey(), entry.getValue())).iterator();
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
    public int hashCode() {
      return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      JsonObject other = (JsonObject) obj;
      return Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
      return value.entrySet().stream()
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

  record Tuple(String key, JsonNode value) { }
}
