/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.Json.entry;
import static com.github.tonivade.json.JsonElement.array;
import static com.github.tonivade.json.JsonElement.object;
import static com.github.tonivade.json.JsonPrimitive.bool;
import static com.github.tonivade.json.JsonPrimitive.number;
import static com.github.tonivade.json.JsonPrimitive.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class JsonParserTest {

  @Test
  void parseObject() {
    String json = """
        {"name":"toni","id":1,"active":true} 
        """.strip();

    JsonElement element = Json.parse(json);

    assertEquals(object(entry("name", string("toni")), entry("id", number(1L)), entry("active", bool(true))), element);
  }

  @Test
  void parseArray() {
    String json = """
        ["toni","olivia"]
        """.strip();

    JsonElement element = Json.parse(json);

    assertEquals(array(string("toni"), string("olivia")), element);
  }

  @Test
  void parseError() {
    assertThrows(IllegalArgumentException.class, () -> Json.parse(""));
  }

}
