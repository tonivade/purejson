/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Equal;

@SuppressWarnings("preview")
class JsonTest {

  record User(Integer id, String name) {}

  final class Pojo {
    
    private Integer id;
    private String name;

    Pojo(Integer id, String name) {
      this.id = id;
      this.name = name;
    }
    
    public Integer getId() {
      return id;
    }
    
    public String getName() {
      return name;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(id, name);
    }
    
    @Override
    public boolean equals(Object obj) {
      return Equal.<Pojo>of().comparing(Pojo::getId).comparing(Pojo::getName).applyTo(this, obj);
    }
    
    @Override
    public String toString() {
      return "Pojo(id:%s,name:%s)".formatted(id, name);
    }
  }
  
  enum EnumTest { VAL1, VAL2 }

  @Test
  void serializeRecord() {
    var json = new Json();
    var result = json.toString(new User(1, "toni"));

    var expected = """
        {"id":1,"name":"toni"} 
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void serializeRecordNull() {
    var json = new Json();
    var result = json.toString(new User(1, null));

    var expected = """
        {"id":1,"name":null} 
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void serializePojo() {
    var json = new Json();
    var result = json.toString(new Pojo(1, "toni"));

    var expected = """
        {"id":1,"name":"toni"} 
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void serializePojoNull() {
    var json = new Json();
    var result = json.toString(new Pojo(1, null));

    var expected = """
        {"id":1,"name":null} 
        """.strip();

    assertEquals(expected, result);
  }
  
  @Test
  void serializeInnerArray() {

    record Test(String[] values) {}
    
    var result1 = new Json().toString(new Test(List.of("hola", "adios").toArray(String[]::new)));
    var result2 = new Json().toString(new Test(asList(null, "adios").toArray(String[]::new)));
    var result3 = new Json().toString(new Test(null));

    var expected1 = """
        {"values":["hola","adios"]} 
        """.strip();
    var expected2 = """
        {"values":[null,"adios"]} 
        """.strip();
    var expected3 = """
        {"values":null} 
        """.strip();
    
    assertEquals(expected1, result1);
    assertEquals(expected2, result2);
    assertEquals(expected3, result3);
  }
  
  @Test
  void serializeInnerList() {

    record Test(List<String> values) {}
    
    var result1 = new Json().toString(new Test(List.of("hola", "adios")));
    var result2 = new Json().toString(new Test(asList(null, "adios")));
    var result3 = new Json().toString(new Test(null));

    var expected1 = """
        {"values":["hola","adios"]} 
        """.strip();
    var expected2 = """
        {"values":[null,"adios"]} 
        """.strip();
    var expected3 = """
        {"values":null} 
        """.strip();
    
    assertEquals(expected1, result1);
    assertEquals(expected2, result2);
    assertEquals(expected3, result3);
  }

  @Test
  void serializeInnerMap() {

    record Test(Map<String, String> values) {}
    
    var result1 = new Json().toString(new Test(Map.of("hola", "adios")));
    var result2 = new Json().toString(new Test(singletonMap("hola", null)));
    var result3 = new Json().toString(new Test(null));

    var expected1 = """
        {"values":{"hola":"adios"}} 
        """.strip();
    var expected2 = """
        {"values":{"hola":null}} 
        """.strip();
    var expected3 = """
        {"values":null} 
        """.strip();
    
    assertEquals(expected1, result1);
    assertEquals(expected2, result2);
    assertEquals(expected3, result3);
  }

  @Test
  void serializeList() {
    var json = new Json();
    Type listOfUsers = new Reflection<List<User>>() {}.getType();
    var result1 = json.toString(List.of(new User(1, "toni")), listOfUsers);
    var result2 = json.toString(List.of(new User(1, null)), listOfUsers);
    var list = new ArrayList<User>();
    list.add(null);
    var result3 = json.toString(list, listOfUsers);

    var expected1 = """
        [{"id":1,"name":"toni"}]
        """.strip();
    var expected2 = """
        [{"id":1,"name":null}]
        """.strip();
    var expected3 = """
        [null]
        """.strip();

    assertEquals(expected1, result1);
    assertEquals(expected2, result2);
    assertEquals(expected3, result3);
  }

  @Test
  void serializeArray() {
    var json = new Json();
    var result1 = json.toString(new User[] { new User(1, "toni") });
    var result2 = json.toString(new User[] { new User(1, null) });
    var result3 = json.toString(new User[] { null });

    var expected1 = """
        [{"id":1,"name":"toni"}]
        """.strip();
    var expected2 = """
        [{"id":1,"name":null}]
        """.strip();
    var expected3 = """
        [null]
        """.strip();

    assertEquals(expected1, result1);
    assertEquals(expected2, result2);
    assertEquals(expected3, result3);
  }
  
  @Test
  void serializeMap() {
    var json = new Json();
    Type mapOfUsers = new Reflection<Map<String, User>>(){}.getType();
    var result1 = json.toString(Map.of("toni", new User(1, "toni")), mapOfUsers);
    var result2 = json.toString(Map.of("toni", new User(1, null)), mapOfUsers);
    var result3 = json.toString(singletonMap("toni", null), mapOfUsers);

    var expected1 = """
        {"toni":{"id":1,"name":"toni"}}
        """.strip();
    var expected2 = """
        {"toni":{"id":1,"name":null}}
        """.strip();
    var expected3 = """
        {"toni":null}
        """.strip();

    assertEquals(expected1, result1);
    assertEquals(expected2, result2);
    assertEquals(expected3, result3);
    
  }

  @Test
  void parse() {
    var string = """
        {"id":1,"name":"toni"} 
        """.strip();

    var json = new Json();
    User user = json.fromJson(string, User.class);

    var expected = new User(1, "toni");
    assertEquals(expected, user);
  }

  @Test
  void parseList() {
    var string = """
        [{"id":1,"name":"toni"}]
        """.strip();

    var listOfUsers = new Reflection<List<User>>() {};
    var json = new Json();
    List<User> array = json.fromJson(string, listOfUsers.getType());

    var expected = new User(1, "toni");
    assertEquals(List.of(expected), array);
  }

  @Test
  void parseInnerList() {
    record Test(List<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    var json = new Json();
    Test result = json.fromJson(string, Test.class);

    var expected = new Test(List.of("one", "two", "three"));
    assertEquals(expected, result);
  }

  @Test
  void parseArray() {
    var string = """
        [{"id":1,"name":"toni"}]
        """.strip();

    var listOfUsers = new Reflection<User[]>() {};
    var json = new Json();
    User[] array = json.fromJson(string, listOfUsers.getType());

    var expected = new User(1, "toni");
    assertArrayEquals(new User[] { expected }, array);
  }

  @Test
  void parseMap() {
    String string = """
        {"toni":{"id":1,"name":"toni"}}
        """.strip();

    var mapOfUsers = new Reflection<Map<String, User>>() {};
    var json = new Json();
    Map<String, User> map = json.fromJson(string, mapOfUsers.getType());

    var expected = new User(1, "toni");
    assertEquals(Map.of("toni", expected), map);
  }
  
  @Test
  void serializePrimitives() {
    var json = new Json();
    
    assertEquals("null", json.toString(null));
    assertEquals("65", json.toString('A'));
    assertEquals("193", json.toString('Á'));
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
    var json = new Json();
    
    assertNull(json.fromJson("null", String.class));
    assertEquals(Byte.valueOf((byte)1), json.<Byte>fromJson("1", byte.class));
    assertEquals(Short.valueOf((short)1), json.<Short>fromJson("1", short.class));
    assertEquals(Character.valueOf('A'), json.<Character>fromJson("65", char.class));
    assertEquals(Character.valueOf('Á'), json.<Character>fromJson("193", char.class));
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