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
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SuppressWarnings("preview")
public abstract class JsonNode {

  public static final JsonNode NULL = new Null();

  private final JsonElement element;

  private JsonNode(JsonElement element) {
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
    return element.isJsonArray();
  }

  public boolean isObject() {
    return element.isJsonObject();
  }

  public boolean isPrimitive() {
    return element.isJsonPrimitive();
  }

  public boolean isNull() {
    return element.isJsonNull();
  }
  
  JsonElement unwrap() {
    return element;
  }

  JsonObject asJsonObject() {
    return element.getAsJsonObject();
  }

  JsonArray asJsonArray() {
    return element.getAsJsonArray();
  }

  JsonPrimitive asJsonPrimitive() {
    return element.getAsJsonPrimitive();
  }

  JsonNull asJsonNull() {
    return element.getAsJsonNull();
  }

  public boolean asBoolean() {
    return element.getAsBoolean();
  }

  public Number asNumber() {
    return element.getAsNumber();
  }

  public String asString() {
    return element.getAsString();
  }

  public double asDouble() {
    return element.getAsDouble();
  }

  public float asFloat() {
    return element.getAsFloat();
  }

  public long asLong() {
    return element.getAsLong();
  }

  public int asInt() {
    return element.getAsInt();
  }

  public byte asByte() {
    return element.getAsByte();
  }

  @Deprecated
  public char asCharacter() {
    return element.getAsCharacter();
  }

  public BigDecimal asBigDecimal() {
    return element.getAsBigDecimal();
  }

  public BigInteger asBigInteger() {
    return element.getAsBigInteger();
  }

  public short asShort() {
    return element.getAsShort();
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

  public static JsonNode from(JsonElement element) {
    if (element == null) {
      return NULL;
    }
    if (element instanceof JsonNull) {
      return NULL;
    }
    if (element instanceof JsonArray a) {
      return new Array(a);
    }
    if (element instanceof JsonObject o) {
      return new Object(o);
    }
    if (element instanceof JsonPrimitive p) {
      return new Primitive(p);
    }
    throw new IllegalArgumentException(element.getClass().getName());
  }
  
  public static final class Null extends JsonNode {

    private Null() {
      super(JsonNull.INSTANCE);
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
      return asJsonObject().entrySet().stream()
          .map(entry -> entry(entry.getKey(), JsonNode.from(entry.getValue()))).iterator();
    }
  }
  
  public static final class Primitive extends JsonNode {
    
    public Primitive(String value) {
      this(new JsonPrimitive(value));
    }
    
    public Primitive(Boolean value) {
      this(new JsonPrimitive(value));
    }
    
    public Primitive(Number value) {
      this(new JsonPrimitive(value));
    }

    private Primitive(JsonPrimitive value) {
      super(value);
    }

    public boolean isString() {
      return asJsonPrimitive().isString();
    }

    public boolean isNumber() {
      return asJsonPrimitive().isNumber();
    }

    public boolean isBoolean() {
      return asJsonPrimitive().isBoolean();
    }
  }
}
