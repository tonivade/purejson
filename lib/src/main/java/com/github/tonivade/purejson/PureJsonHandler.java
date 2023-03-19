/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import com.eclipsesource.json.JsonHandler;

class PureJsonHandler extends JsonHandler<JsonNode.Array, JsonNode.Object> {
  
  private JsonNode value;

  @Override
  public JsonNode.Array startArray() {
    return new JsonNode.Array();
  }

  @Override
  public JsonNode.Object startObject() {
    return new JsonNode.Object();
  }

  @Override
  public void endNull() {
    value = JsonNode.NULL;
  }

  @Override
  public void endBoolean(boolean bool) {
    value = bool ? JsonNode.TRUE : JsonNode.FALSE;
  }

  @Override
  public void endString(String string) {
    value = new JsonNode.Primitive(string);
  }

  @Override
  public void endNumber(String string) {
    value = new JsonNode.Primitive(string);
  }

  @Override
  public void endArray(JsonNode.Array array) {
    value = array;
  }

  @Override
  public void endObject(JsonNode.Object object) {
    value = object;
  }

  @Override
  public void endArrayValue(JsonNode.Array array) {
    array.add(value);
  }

  @Override
  public void endObjectValue(JsonNode.Object object, String name) {
    object.add(name, value);
  }

  JsonNode getValue() {
    return value;
  }
}
