/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JsonTest {

  record User(Integer id, String name) {}

  private final JsonAdapter<User> adapter =
      Json.adapter(User.class)
          .addInteger("id", User::id)
          .addString("name", User::name).build();

  @Test
  void serialize() {
    Json json = new Json().add(User.class, adapter);
    String result = json.toString(new User(1, "toni"));

    String expected = """
        {"id":1,"name":"toni"} 
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void parse() {
    String string = """
        {"name":"toni","id":1} 
        """.strip();

    Json json = new Json().add(User.class, adapter);
    User user = json.fromJson(string, User.class);

    User expected = new User(1, "toni");
    assertEquals(expected, user);
  }
}