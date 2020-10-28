/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonElement.array;
import static com.github.tonivade.json.JsonElement.entry;
import static com.github.tonivade.json.JsonElement.object;
import static com.github.tonivade.json.JsonPrimitive.bool;
import static com.github.tonivade.json.JsonPrimitive.number;
import static com.github.tonivade.json.JsonPrimitive.string;
import static com.github.tonivade.json.Stats.stats;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.effect.UIO;
import com.google.gson.JsonParser;

public class JsonParserTest {

  @Test
  void parseObject() {
    var json = """
        {"id":1,"name":"toni","active":true} 
        """.strip();

    JsonElement element = Json.parse(json);

    assertEquals(object(entry("id", number(1L)), entry("name", string("toni")), entry("active", bool(true))), element);
  }

  @Test
  void parseArray() {
    var json = """
        ["toni","olivia","vanessa"]
        """.strip();

    JsonElement element = Json.parse(json);

    assertEquals(array(string("toni"), string("olivia"), string("vanessa")), element);
  }

  @Test
  void parseError() {
    assertThrows(IllegalArgumentException.class, () -> Json.parse(""));
  }
  
  @Test
  void performance() {
    int times = 500;
    stats(times, "pureJson", parseTask(Json::parse));
    stats(times, "gson", parseTask(JsonParser::parseString));
  }
  
  private <T> UIO<T> parseTask(Function1<String, T> parser) {
    var json = """
        {"id":1,"name":"toni"} 
        """.strip();
    
    String collect = Stream.generate(() -> json).limit(3000).collect(joining(",", "[", "]"));
    
    return UIO.task(() -> parser.apply(collect));
  }
}
