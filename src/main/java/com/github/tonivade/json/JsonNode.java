/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonDSL.entry;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

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
public sealed interface JsonNode extends Wrapper
    permits JsonNode.Null, JsonNode.Array, JsonNode.Object, JsonNode.Primitive {
  
  JsonNode NULL = new Null();

  static JsonNode from(JsonElement element) {
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
  
  final class Null implements JsonNode {

    private Null() {}

    @Override
    public JsonElement unwrap() {
      return JsonNull.INSTANCE;
    }
    
    @Override
    public int hashCode() {
      return 1;
    }
    
    @Override
    public boolean equals(java.lang.Object obj) {
      return obj instanceof Null;
    }
    
    @Override
    public String toString() {
      return "Null";
    }
  }

  final class Array implements JsonNode, Iterable<JsonNode> {
    
    private final JsonArray array;
    
    public Array(JsonArray array) {
      this.array = checkNonNull(array);
    }

    int size() {
      return array.size();
    }
    
    @Override
    public Iterator<JsonNode> iterator() {
      return StreamSupport.stream(array.spliterator(), false).map(JsonNode::from).iterator();
    }

    JsonNode get(int i) {
      return JsonNode.from(array.get(i));
    }
    
    @Override
    public JsonElement unwrap() {
      return array;
    }

    @Override
    public int hashCode() {
      return array.hashCode();
    }

    @Override
    public boolean equals(java.lang.Object obj) {
      return array.equals(obj);
    }

    @Override
    public String toString() {
      return array.toString();
    }
  }

  final class Object implements JsonNode, Iterable<Map.Entry<String, JsonNode>> {
    
    private final JsonObject object;
    
    public Object(JsonObject object) {
      this.object = checkNonNull(object);
    }

    JsonNode get(String name) {
      return JsonNode.from(object.get(name));
    }
    
    @Override
    public Iterator<Map.Entry<String, JsonNode>> iterator() {
      return object.entrySet().stream()
          .map(entry -> entry(entry.getKey(), JsonNode.from(entry.getValue()))).iterator();
    }
    
    @Override
    public JsonElement unwrap() {
      return object;
    }

    @Override
    public int hashCode() {
      return object.hashCode();
    }

    @Override
    public boolean equals(java.lang.Object obj) {
      return object.equals(obj);
    }

    @Override
    public String toString() {
      return object.toString();
    }
  }
  
  final class Primitive implements JsonNode {
    
    private final JsonPrimitive value;

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
      this.value = checkNonNull(value);
    }

    @Override
    public JsonElement unwrap() {
      return value;
    }

    boolean isString() {
      return value.isString();
    }

    boolean isNumber() {
      return value.isNumber();
    }

    boolean isBoolean() {
      return value.isBoolean();
    }

    String asString() {
      return value.getAsString();
    }

    Character asCharacter() {
      return value.getAsCharacter();
    }

    Byte asByte() {
      return value.getAsByte();
    }

    Short asShort() {
      return value.getAsShort();
    }

    Integer asInt() {
      return value.getAsInt();
    }

    Long asLong() {
      return value.getAsLong();
    }

    Float asFloat() {
      return value.getAsFloat();
    }

    Double asDouble() {
      return value.getAsDouble();
    }

    BigInteger asBigInteger() {
      return value.getAsBigInteger();
    }

    BigDecimal asBigDecimal() {
      return value.getAsBigDecimal();
    }

    Boolean asBoolean() {
      return value.getAsBoolean();
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }

    @Override
    public boolean equals(java.lang.Object obj) {
      return value.equals(obj);
    }

    @Override
    public String toString() {
      return value.toString();
    }
  }
}

interface Wrapper {
  JsonElement unwrap();
}