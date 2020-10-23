/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonAdapter.listAdapter;
import static com.github.tonivade.json.JsonAdapter.mapAdapter;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonTest {

  record User(Integer id, String name) {}
  
  enum EnumTest { VAL1, VAL2 };

  private final JsonAdapter<User> adapter =
      JsonAdapter.builder(User.class)
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
  void serializeList() {
    Json json = new Json().add(User.class, adapter);
    String result = json.toString(List.of(new User(1, "toni")));

    String expected = """
        [{"id":1,"name":"toni"}]
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void serializeArray() {
    Json json = new Json().add(User.class, adapter);
    String result = json.toString(new User[] { new User(1, "toni") });

    String expected = """
        [{"id":1,"name":"toni"}]
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
  void parseList() {
    String string = """
        [{"id":1,"name":"toni"}]
        """.strip();

    Reflection<List<User>> listOfUsers = new Reflection<List<User>>() {};
    Json json = new Json().add(listOfUsers.getType(), listAdapter(adapter));
    List<User> array = json.fromJson(string, listOfUsers.getType());

    User expected = new User(1, "toni");
    assertEquals(List.of(expected), array);
  }

  @Test
  void parseArray() {
    String string = """
        [{"id":1,"name":"toni"}]
        """.strip();

    Reflection<User[]> listOfUsers = new Reflection<User[]>() {};
    Json json = new Json();
    User[] array = json.fromJson(string, listOfUsers.getType());

    User expected = new User(1, "toni");
    assertArrayEquals(new User[] { expected }, array);
  }

  @Test
  void parseMap() {
    String string = """
        {"toni":{"id":1,"name":"toni"}}
        """.strip();

    Reflection<Map<String, User>> mapOfUsers = new Reflection<Map<String, User>>() {};
    Json json = new Json().add(mapOfUsers.getType(), mapAdapter(adapter));
    Map<String, User> map = json.fromJson(string, mapOfUsers.getType());

    User expected = new User(1, "toni");
    assertEquals(Map.of("toni", expected), map);
  }
  
  @Test
  void serializePrimitives() {
    Json json = new Json();
    
    assertEquals("1", json.toString((byte)1));
    assertEquals("1", json.toString((short)1));
    assertEquals("1", json.toString(1));
    assertEquals("1", json.toString(1L));
    assertEquals("1.0", json.toString(1f));
    assertEquals("1.0", json.toString(1d));
    assertEquals("1", json.toString(BigInteger.ONE));
    assertEquals("1.0", json.toString(BigDecimal.ONE));
    assertEquals("\"asdfg\"", json.toString("asdfg"));
    assertEquals("\"VAL1\"", json.toString(EnumTest.VAL1));
  }
  
  @Test
  void parsePrimitives() {
    Json json = new Json();
    
    assertEquals(Byte.valueOf((byte)1), json.<Byte>fromJson("1", byte.class));
    assertEquals(Short.valueOf((short)1), json.<Short>fromJson("1", short.class));
    assertEquals(Integer.valueOf(1), json.<Integer>fromJson("1", int.class));
    assertEquals(Long.valueOf(1L), json.<Long>fromJson("1", long.class));
    assertEquals(Float.valueOf(1L), json.<Float>fromJson("1.0", float.class));
    assertEquals(Double.valueOf(1L), json.<Double>fromJson("1.0", double.class));
    assertEquals(BigInteger.ONE, json.fromJson("1", BigInteger.class));
    assertEquals(BigDecimal.valueOf(1.0), json.fromJson("1.0", BigDecimal.class));
    assertEquals("asdfg", json.fromJson("\"asdfg\"", String.class));
    assertEquals(EnumTest.VAL1, json.fromJson("\"VAL1\"", EnumTest.class));
  }
}