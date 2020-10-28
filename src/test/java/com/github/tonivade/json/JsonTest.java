/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.purefun.data.Sequence.arrayOf;
import static com.github.tonivade.purefun.data.Sequence.emptyArray;
import static com.github.tonivade.purefun.data.Sequence.emptyList;
import static com.github.tonivade.purefun.data.Sequence.emptySet;
import static com.github.tonivade.purefun.data.Sequence.listOf;
import static com.github.tonivade.purefun.data.Sequence.setOf;
import static com.github.tonivade.purefun.data.Sequence.treeOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Equal;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.Sequence;

@SuppressWarnings("preview")
class JsonTest {

  record User(Integer id, String name) {}

  final class Pojo {
    
    @SuppressWarnings("unused")
    private static final int x = 1;
    
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
    var result = new Json().toString(new User(1, "toni"));

    var expected = """
        {"id":1,"name":"toni"} 
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void serializeRecordNull() {
    var result = new Json().toString(new User(1, null));

    var expected = """
        {"id":1,"name":null} 
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void serializePojo() {
    var result = new Json().toString(new Pojo(1, "toni"));

    var expected = """
        {"id":1,"name":"toni"} 
        """.strip();

    assertEquals(expected, result);
  }

  @Test
  void serializePojoNull() {
    var result = new Json().toString(new Pojo(1, null));

    var expected = """
        {"id":1,"name":null} 
        """.strip();

    assertEquals(expected, result);
  }
  
  @Test
  void serializeInnerArray() {

    record Test(String[] values) {}
    
    var json = new Json();
    var result1 = json.toString(new Test(List.of("hola", "adios").toArray(String[]::new)));
    var result2 = json.toString(new Test(asList(null, "adios").toArray(String[]::new)));
    var result3 = json.toString(new Test(null));

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
    
    var json = new Json();
    var result1 = json.toString(new Test(List.of("hola", "adios")));
    var result2 = json.toString(new Test(asList(null, "adios")));
    var result3 = json.toString(new Test(null));

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
    
    var json = new Json();
    var result1 = json.toString(new Test(Map.of("hola", "adios")));
    var result2 = json.toString(new Test(singletonMap("hola", null)));
    var result3 = json.toString(new Test(null));

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
    var result3 = json.toString(listWithNull(), listOfUsers);

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
  void serializeImmutableList() {
    var json = new Json();
    Type listOfUsers = new Reflection<ImmutableList<User>>() {}.getType();
    var result1 = json.toString(listOf(new User(1, "toni")), listOfUsers);
    var result2 = json.toString(listOf(new User(1, null)), listOfUsers);
    var result3 = json.toString(emptyList().append(null), listOfUsers);

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
  void serializeImmutableArray() {
    var json = new Json();
    Type listOfUsers = new Reflection<ImmutableArray<User>>() {}.getType();
    var result1 = json.toString(arrayOf(new User(1, "toni")), listOfUsers);
    var result2 = json.toString(arrayOf(new User(1, null)), listOfUsers);
    var result3 = json.toString(emptyArray().append(null), listOfUsers);

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
  void serializeImmutableSet() {
    var json = new Json();
    Type listOfUsers = new Reflection<ImmutableSet<User>>() {}.getType();
    var result1 = json.toString(setOf(new User(1, "toni")), listOfUsers);
    var result2 = json.toString(setOf(new User(1, null)), listOfUsers);
    var result3 = json.toString(emptySet().append(null), listOfUsers);

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
  void serializeImmutableMap() {
    var json = new Json();
    Type mapOfUsers = new Reflection<ImmutableMap<String, User>>(){}.getType();
    var result1 = json.toString(ImmutableMap.empty().put("toni", new User(1, "toni")), mapOfUsers);
    var result2 = json.toString(ImmutableMap.empty().put("toni", new User(1, null)), mapOfUsers);
    var result3 = json.toString(ImmutableMap.empty().put("toni", null), mapOfUsers);

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

    User user = new Json().fromJson(string, User.class);

    assertEquals(new User(1, "toni"), user);
  }

  @Test
  void parseInnerSequence() {

    record Test(Sequence<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(listOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableArray() {

    record Test(ImmutableArray<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(arrayOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableList() {

    record Test(ImmutableList<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(listOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableSet() {

    record Test(ImmutableSet<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(setOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableTree() {

    record Test(ImmutableTree<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(treeOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerCollection() {

    record Test(Collection<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(List.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerArray() {

    record Test(String[] values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertArrayEquals(List.of("one", "two", "three").toArray(String[]::new), result.values);
  }

  @Test
  void parseInnerList() {

    record Test(List<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(List.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerSet() {

    record Test(Set<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(Set.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerMap() {

    record Test(Map<String, String> values) {}
    
    var string = """
        {"values":{"one":"1","two":"2","three":"3"}}
        """.strip();

    Test result = new Json().fromJson(string, Test.class);

    assertEquals(new Test(Map.of("one", "1", "two", "2", "three", "3")), result);
  }

  @Test
  void parseArray() {
    var string1 = """
        [{"id":1,"name":"toni"}]
        """.strip();
    var string2 = """
        [{"id":1,"name":null}]
        """.strip();
    var string3 = """
        [{"id":1}]
        """.strip();
    var string4 = "[null]";
    var string5 = "null";

    var listOfUsers = new Reflection<User[]>() {};
    var json = new Json();
    User[] array1 = json.fromJson(string1, listOfUsers.getType());
    User[] array2 = json.fromJson(string2, listOfUsers.getType());
    User[] array3 = json.fromJson(string3, listOfUsers.getType());
    User[] array4 = json.fromJson(string4, listOfUsers.getType());
    User[] array5 = json.fromJson(string5, listOfUsers.getType());

    assertArrayEquals(new User[] { new User(1, "toni") }, array1);
    assertArrayEquals(new User[] { new User(1, null) }, array2);
    assertArrayEquals(new User[] { new User(1, null) }, array3);
    assertArrayEquals(new User[] { null }, array4);
    assertNull(array5);
  }

  @Test
  void parseList() {
    var string1 = """
        [{"id":1,"name":"toni"}]
        """.strip();
    var string2 = """
        [{"id":1,"name":null}]
        """.strip();
    var string3 = """
        [{"id":1}]
        """.strip();
    var string4 = """
        [null]
        """.strip();
    var string5 = "null";

    var listOfUsers = new Reflection<List<User>>() {};
    var json = new Json();
    List<User> list1 = json.fromJson(string1, listOfUsers.getType());
    List<User> list2 = json.fromJson(string2, listOfUsers.getType());
    List<User> list3 = json.fromJson(string3, listOfUsers.getType());
    List<User> list4 = json.fromJson(string4, listOfUsers.getType());
    List<User> list5 = json.fromJson(string5, listOfUsers.getType());

    assertEquals(List.of(new User(1, "toni")), list1);
    assertEquals(List.of(new User(1, null)), list2);
    assertEquals(List.of(new User(1, null)), list3);
    assertEquals(listWithNull(), list4);
    assertNull(list5);
  }

  @Test
  void parseMap() {
    var string1 = """
        {"toni":{"id":1,"name":"toni"}}
        """.strip();
    var string2 = """
        {"toni":{"id":1,"name":null}}
        """.strip();
    var string3 = """
        {"toni":{"id":1}}
        """.strip();
    var string4 = """
        {"toni":null}
        """.strip();
    var string5 = "null";

    var mapOfUsers = new Reflection<Map<String, User>>() {};
    var json = new Json();
    Map<String, User> map1 = json.fromJson(string1, mapOfUsers.getType());
    Map<String, User> map2 = json.fromJson(string2, mapOfUsers.getType());
    Map<String, User> map3 = json.fromJson(string3, mapOfUsers.getType());
    Map<String, User> map4 = json.fromJson(string4, mapOfUsers.getType());
    Map<String, User> map5 = json.fromJson(string5, mapOfUsers.getType());

    assertEquals(Map.of("toni", new User(1, "toni")), map1);
    assertEquals(Map.of("toni", new User(1, null)), map2);
    assertEquals(Map.of("toni", new User(1, null)), map3);
    assertEquals(singletonMap("toni", null), map4);
    assertNull(map5);
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

  private <T> ArrayList<T> listWithNull() {
    var list = new ArrayList<T>();
    list.add(null);
    return list;
  }
}