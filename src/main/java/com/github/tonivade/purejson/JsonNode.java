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
public abstract class JsonNode extends JsonElement {

  public static final JsonNode NULL = new Null();

  private final JsonElement element;

  private JsonNode(JsonElement element) {
    this.element = checkNonNull(element);
  }

  @Override
  public JsonElement deepCopy() {
    return from(element.deepCopy());
  }

  @Override
  public boolean isJsonArray() {
    return element.isJsonArray();
  }

  @Override
  public boolean isJsonObject() {
    return element.isJsonObject();
  }

  @Override
  public boolean isJsonPrimitive() {
    return element.isJsonPrimitive();
  }

  @Override
  public boolean isJsonNull() {
    return element.isJsonNull();
  }

  @Override
  public JsonObject getAsJsonObject() {
    return element.getAsJsonObject();
  }

  @Override
  public JsonArray getAsJsonArray() {
    return element.getAsJsonArray();
  }

  @Override
  public JsonPrimitive getAsJsonPrimitive() {
    return element.getAsJsonPrimitive();
  }

  @Override
  public JsonNull getAsJsonNull() {
    return element.getAsJsonNull();
  }

  @Override
  public boolean getAsBoolean() {
    return element.getAsBoolean();
  }

  @Override
  public Number getAsNumber() {
    return element.getAsNumber();
  }

  @Override
  public String getAsString() {
    return element.getAsString();
  }

  @Override
  public double getAsDouble() {
    return element.getAsDouble();
  }

  @Override
  public float getAsFloat() {
    return element.getAsFloat();
  }

  @Override
  public long getAsLong() {
    return element.getAsLong();
  }

  @Override
  public int getAsInt() {
    return element.getAsInt();
  }

  @Override
  public byte getAsByte() {
    return element.getAsByte();
  }

  @Override
  @Deprecated
  public char getAsCharacter() {
    return element.getAsCharacter();
  }

  @Override
  public BigDecimal getAsBigDecimal() {
    return element.getAsBigDecimal();
  }

  @Override
  public BigInteger getAsBigInteger() {
    return element.getAsBigInteger();
  }

  @Override
  public short getAsShort() {
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
    throw new IllegalArgumentException();
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

    int size() {
      return getAsJsonArray().size();
    }
    
    @Override
    public Iterator<JsonNode> iterator() {
      return StreamSupport.stream(getAsJsonArray().spliterator(), false).map(JsonNode::from).iterator();
    }

    JsonNode get(int i) {
      return JsonNode.from(getAsJsonArray().get(i));
    }
  }

  public static final class Object extends JsonNode implements Iterable<Map.Entry<String, JsonNode>> {
    
    public Object(JsonObject object) {
      super(object);
    }

    JsonNode get(String name) {
      return JsonNode.from(getAsJsonObject().get(name));
    }
    
    @Override
    public Iterator<Map.Entry<String, JsonNode>> iterator() {
      return getAsJsonObject().entrySet().stream()
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

    boolean isString() {
      return getAsJsonPrimitive().isString();
    }

    boolean isNumber() {
      return getAsJsonPrimitive().isNumber();
    }

    boolean isBoolean() {
      return getAsJsonPrimitive().isBoolean();
    }
  }
}
