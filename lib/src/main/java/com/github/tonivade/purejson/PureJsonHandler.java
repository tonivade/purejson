/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import com.eclipsesource.json.JsonHandler;

class PureJsonHandler extends JsonHandler<JsonNode.JsonArray, JsonNode.JsonObject> {

  @SuppressWarnings("NullAway")
  private JsonNode value;

  @Override
  public JsonNode.JsonArray startArray() {
    return new JsonNode.JsonArray();
  }

  @Override
  public JsonNode.JsonObject startObject() {
    return new JsonNode.JsonObject();
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
    value = new JsonNode.JsonString(string);
  }

  @Override
  public void endNumber(String string) {
    try {
      value = new JsonNode.JsonNumber(Long.parseLong(string));
    } catch (NumberFormatException e) {
      value = new JsonNode.JsonNumber(Double.parseDouble(string));
    }
  }

  @Override
  public void endArray(JsonNode.JsonArray array) {
    value = array;
  }

  @Override
  public void endObject(JsonNode.JsonObject object) {
    value = object;
  }

  @Override
  public void endArrayValue(JsonNode.JsonArray array) {
    array.add(value);
  }

  @Override
  public void endObjectValue(JsonNode.JsonObject object, String name) {
    object.add(name, value);
  }

  JsonNode getValue() {
    return value;
  }
}
