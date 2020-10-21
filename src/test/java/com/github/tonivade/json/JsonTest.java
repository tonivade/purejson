/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.Json.listAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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
        {"id":1,"name":"toni"} 
        """.strip();

    Json json = new Json().add(User.class, adapter);
    User user = json.fromJson(string, User.class);

    User expected = new User(1, "toni");
    assertEquals(expected, user);
  }

  @Test
  void parseArray() {
    String string = """
        [{"name":"toni","id":1}]
        """.strip();

    Reflection<List<User>> listOfUsers = new Reflection<List<User>>() {};
    Json json = new Json().add(listOfUsers, listAdapter(adapter));
    List<User> user = json.fromJson(string, listOfUsers);

    User expected = new User(1, "toni");
    assertEquals(List.of(expected), user);
  }
}