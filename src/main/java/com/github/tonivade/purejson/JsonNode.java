/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purejson.JsonDSL.entry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

public abstract class JsonNode {

  public static final JsonNode NULL = new Null();

  private final JsonValue element;

  private JsonNode(JsonValue element) {
    this.element = checkNonNull(element);
  }
  
  public Array asArray() {
    return (Array) this;
  }
  
  public Object asObject() {
    return (Object) this;
  }
  
  public Primitive asPrimitive() {
    return (Primitive) this;
  }
  
  public Null asNull() {
    return (Null) this;
  }

  public boolean isArray() {
    return element.isArray();
  }

  public boolean isObject() {
    return element.isObject();
  }

  public boolean isPrimitive() {
    return element.isBoolean() || element.isNumber() || element.isString();
  }

  public boolean isString() {
    return element.isString();
  }

  public boolean isNumber() {
    return element.isNumber();
  }

  public boolean isBoolean() {
    return element.isBoolean();
  }

  public boolean isNull() {
    return element.isNull();
  }
  
  JsonValue unwrap() {
    return element;
  }

  JsonObject asJsonObject() {
    return element.asObject();
  }

  JsonArray asJsonArray() {
    return element.asArray();
  }

  public boolean asBoolean() {
    return element.asBoolean();
  }

  public Number asNumber() {
    // TODO
    throw new UnsupportedOperationException();
  }

  public String asString() {
    return element.asString();
  }

  public double asDouble() {
    return element.asDouble();
  }

  public float asFloat() {
    return element.asFloat();
  }

  public long asLong() {
    return element.asLong();
  }

  public int asInt() {
    return element.asInt();
  }

  public byte asByte() {
    return (byte) element.asInt();
  }

  @Deprecated
  public char asCharacter() {
    return element.asString().charAt(0);
  }

  public BigDecimal asBigDecimal() {
    return BigDecimal.valueOf(element.asDouble());
  }

  public BigInteger asBigInteger() {
    return BigInteger.valueOf(element.asLong());
  }

  public short asShort() {
    return (short) element.asInt();
  }

  @Override
  public int hashCode() {
    return element.hashCode();
  }

  @Override
  public boolean equals(java.lang.Object obj) {
    return element.equals(obj);
  }

  @Override
  public String toString() {
    return element.toString();
  }

  public static JsonNode from(JsonValue element) {
    if (element == null) {
      return NULL;
    }
    if (element.isNull()) {
      return NULL;
    }
    if (element.isArray()) {
      return new Array(element.asArray());
    }
    if (element.isObject()) {
      return new Object(element.asObject());
    }
    if (element.isBoolean()) {
      return new Primitive(element);
    }
    if (element.isNumber()) {
      return new Primitive(element);
    }
    if (element.isString()) {
      return new Primitive(element);
    }
    throw new IllegalArgumentException(element.getClass().getName());
  }
  
  public static final class Null extends JsonNode {

    private Null() {
      super(Json.NULL);
    }
  }

  public static final class Array extends JsonNode implements Iterable<JsonNode> {

    public Array(JsonArray array) {
      super(array);
    }

    public int size() {
      return asJsonArray().size();
    }
    
    @Override
    public Iterator<JsonNode> iterator() {
      return StreamSupport.stream(asJsonArray().spliterator(), false).map(JsonNode::from).iterator();
    }

    public JsonNode get(int i) {
      return JsonNode.from(asJsonArray().get(i));
    }
  }

  public static final class Object extends JsonNode implements Iterable<Map.Entry<String, JsonNode>> {
    
    public Object(JsonObject object) {
      super(object);
    }

    public JsonNode get(String name) {
      return JsonNode.from(asJsonObject().get(name));
    }
    
    @Override
    public Iterator<Map.Entry<String, JsonNode>> iterator() {
      Stream<Member> stream = StreamSupport.stream(asJsonObject().spliterator(), false);
      return stream.map(member -> entry(member.getName(), JsonNode.from(member.getValue()))).iterator();
    }
  }
  
  public static final class Primitive extends JsonNode {

    public Primitive(String value) {
      super(Json.value(value));
    }

    public Primitive(boolean value) {
      super(Json.value(value));
    }

    public Primitive(int value) {
      super(Json.value(value));
    }

    public Primitive(long value) {
      super(Json.value(value));
    }

    public Primitive(float value) {
      super(Json.value(value));
    }

    public Primitive(double value) {
      super(Json.value(value));
    }

    private Primitive(JsonValue value) {
      super(value);
    }
  }
}
