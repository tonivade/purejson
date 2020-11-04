/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purecheck.PerfCase.ioPerfCase;
import static com.github.tonivade.purefun.Validator.equalsTo;
import static com.github.tonivade.purefun.Validator.instanceOf;
import static com.github.tonivade.purefun.data.Sequence.arrayOf;
import static com.github.tonivade.purefun.data.Sequence.emptyArray;
import static com.github.tonivade.purefun.data.Sequence.emptyList;
import static com.github.tonivade.purefun.data.Sequence.emptySet;
import static com.github.tonivade.purefun.data.Sequence.listOf;
import static com.github.tonivade.purefun.data.Sequence.setOf;
import static com.github.tonivade.purefun.data.Sequence.treeOf;
import static com.github.tonivade.purefun.data.SequenceOf.toSequence;
import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static com.github.tonivade.purefun.type.Option.none;
import static com.github.tonivade.purefun.type.Option.some;
import static com.github.tonivade.purefun.type.Try.success;
import static com.github.tonivade.purejson.JsonAdapter.INTEGER;
import static com.github.tonivade.purejson.JsonAdapter.STRING;
import static com.github.tonivade.purejson.JsonAdapter.iterableAdapter;
import static com.github.tonivade.purejson.JsonDSL.entry;
import static com.github.tonivade.purejson.JsonDSL.object;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purecheck.spec.IOTestSpec;
import com.github.tonivade.purefun.Equal;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.instances.SequenceInstances;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("preview")
class PureJsonTest extends IOTestSpec<String> {

  record User(Integer id, String name) {}

  static final class Pojo {
    
    @SuppressWarnings("unused")
    private static final int x = 1;
    
    private Integer id;
    private String name;
    
    Pojo() { }

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
  void serializeObject() {

    suite("serialize object", 

        it.should("serialize a record")
          .given(new User(1, "toni"))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"id":1,"name":"toni"} 
              """.strip()))),

        it.should("serialize a record with null fields")
          .given(new User(1, null))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"id":1,"name":null} 
              """.strip()))),
          
        it.should("serialize a pojo")
          .given(new Pojo(1, "toni"))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"id":1,"name":"toni"} 
              """.strip()))),
          
        it.should("serialize a pojo with null fields")
          .given(new Pojo(1, null))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"id":1,"name":null} 
              """.strip())))

        ).run().assertion();
  }
  
  @Test
  void serializeInnerArray() {

    record Test(String[] values) {}

    suite("serialize inner array", 

        it.should("serialize a inner array")
          .given(new Test(List.of("hola", "adios").toArray(String[]::new)))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":["hola","adios"]} 
              """.strip()))),

        it.should("serialize a inner array with null values")
          .given(new Test(asList(null, "adios").toArray(String[]::new)))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":[null,"adios"]} 
              """.strip()))),

        it.should("serialize a null inner array")
          .given(new Test(null))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":null} 
              """.strip())))

        ).run().assertion();
  }
  
  @Test
  void serializeInnerList() {

    record Test(List<String> values) {}

    suite("serialize inner list", 

        it.should("serialize a inner list")
          .given(new Test(List.of("hola", "adios")))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":["hola","adios"]} 
              """.strip()))),

        it.should("serialize a inner list with null values")
          .given(new Test(asList(null, "adios")))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":[null,"adios"]} 
              """.strip()))),

        it.should("serialize a null inner list")
          .given(new Test(null))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":null} 
              """.strip())))

        ).run().assertion();
  }

  @Test
  void serializeInnerMap() {

    record Test(Map<String, String> values) {}

    suite("serialize inner map", 

        it.should("serialize a inner map")
          .given(new Test(Map.of("hola", "adios")))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":{"hola":"adios"}} 
              """.strip()))),

        it.should("serialize a inner map with null values")
          .given(new Test(singletonMap("hola", null)))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":{"hola":null}} 
              """.strip()))),

        it.should("serialize a null inner map")
          .given(new Test(null))
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("""
              {"values":null} 
              """.strip())))

        ).run().assertion();
  }

  @Test
  void serializeList() {
    Type listOfUsers = new TypeToken<List<User>>() {}.getType();

    suite("serialize list", 

        it.should("serialize a list of records")
          .given(List.of(new User(1, "toni")))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a list of records with null fields")
          .given(List.of(new User(1, null)))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null list of records")
          .given(listWithNull())
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("[null]")))
          
        ).run().assertion();
  }

  @Test
  void serializeImmutableList() {
    Type listOfUsers = new TypeToken<ImmutableList<User>>() {}.getType();

    suite("serialize immutable list", 

        it.should("serialize a immutable list of records")
          .given(listOf(new User(1, "toni")))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a immutable list of records with null fields")
          .given(listOf(new User(1, null)))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null immutable list")
          .given(emptyList().append(null))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("[null]")))
          
        ).run().assertion();
  }

  @Test
  void serializeImmutableArray() {
    Type listOfUsers = new TypeToken<ImmutableArray<User>>() {}.getType();

    suite("serialize immutable array", 

        it.should("serialize a immutable array of records")
          .given(arrayOf(new User(1, "toni")))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a immutable array of records with null fields")
          .given(arrayOf(new User(1, null)))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null immutable array")
          .given(emptyArray().append(null))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("[null]")))
          
        ).run().assertion();
  }

  @Test
  void serializeImmutableSet() {
    Type listOfUsers = new TypeToken<ImmutableSet<User>>() {}.getType();

    suite("serialize immutable set", 

        it.should("serialize a immutable set of records")
          .given(setOf(new User(1, "toni")))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a immutable set of records with null fields")
          .given(setOf(new User(1, null)))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null immutable set")
          .given(emptySet().append(null))
          .when(value -> new PureJson().toString(value, listOfUsers))
          .thenMustBe(equalsTo(success("[null]")))
          
        ).run().assertion();
  }

  @Test
  void serializeArray() {
    var json = new PureJson();
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

    assertEquals(success(expected1), result1);
    assertEquals(success(expected2), result2);
    assertEquals(success(expected3), result3);
  }
  
  @Test
  void serializeMap() {
    var json = new PureJson();
    Type mapOfUsers = new TypeToken<Map<String, User>>(){}.getType();
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

    assertEquals(success(expected1), result1);
    assertEquals(success(expected2), result2);
    assertEquals(success(expected3), result3);
  }
  
  @Test
  void serializeImmutableMap() {
    var json = new PureJson();
    Type mapOfUsers = new TypeToken<ImmutableMap<String, User>>(){}.getType();
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

    assertEquals(success(expected1), result1);
    assertEquals(success(expected2), result2);
    assertEquals(success(expected3), result3);
  }

  @Test
  void parseObject() {

    suite("parse object", 

        it.should("parse a record")
          .given("""
              {"id":1,"name":"toni"} 
              """.strip())
          .when(json -> new PureJson().fromJson(json, User.class))
          .thenMustBe(equalsTo(success(some(new User(1, "toni"))))),

        it.should("parse a record with null fields")
          .given("""
              {"id":1,"name":null} 
              """.strip())
          .when(json -> new PureJson().fromJson(json, User.class))
          .thenMustBe(equalsTo(success(some(new User(1, null))))),

        it.should("parse a null record")
          .given("null")
          .when(json -> new PureJson().fromJson(json, User.class))
          .thenMustBe(equalsTo(success(none()))),

        it.should("parse a pojo")
          .given("""
              {"id":1,"name":"toni"} 
              """.strip())
          .when(json -> new PureJson().fromJson(json, Pojo.class))
          .thenMustBe(equalsTo(success(some(new Pojo(1, "toni"))))),

        it.should("parse a pojo")
          .given("""
              {"id":1,"name":null} 
              """.strip())
          .when(json -> new PureJson().fromJson(json, Pojo.class))
          .thenMustBe(equalsTo(success(some(new Pojo(1, null))))),

        it.should("parse a null pojo")
          .given("null")
          .when(json -> new PureJson().fromJson(json, User.class))
          .thenMustBe(equalsTo(success(none())))
        
        ).run().assertion();
  }

  @Test
  void parseInnerSequence() {

    record Test(Sequence<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(listOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableArray() {

    record Test(ImmutableArray<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(arrayOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableList() {

    record Test(ImmutableList<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(listOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableSet() {

    record Test(ImmutableSet<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(setOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableTree() {

    record Test(ImmutableTree<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(treeOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerCollection() {

    record Test(Collection<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(List.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerArray() {

    record Test(String[] values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertArrayEquals(List.of("one", "two", "three").toArray(String[]::new), 
        result.getOrElseThrow().getOrElseThrow().values);
  }

  @Test
  void parseInnerList() {

    record Test(List<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(List.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerSet() {

    record Test(Set<String> values) {}
    
    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(Set.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerMap() {

    record Test(Map<String, String> values) {}
    
    var string = """
        {"values":{"one":"1","two":"2","three":"3"}}
        """.strip();

    Try<Option<Test>> result = new PureJson().fromJson(string, Test.class);

    assertSuccessSome(new Test(Map.of("one", "1", "two", "2", "three", "3")), result);
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

    var listOfUsers = new TypeToken<User[]>() {};
    var json = new PureJson();
    Try<Option<User[]>> array1 = json.fromJson(string1, listOfUsers.getType());
    Try<Option<User[]>> array2 = json.fromJson(string2, listOfUsers.getType());
    Try<Option<User[]>> array3 = json.fromJson(string3, listOfUsers.getType());
    Try<Option<User[]>> array4 = json.fromJson(string4, listOfUsers.getType());
    Try<Option<User[]>> array5 = json.fromJson(string5, listOfUsers.getType());

    assertArrayEquals(new User[] { new User(1, "toni") }, array1.getOrElseThrow().getOrElseThrow());
    assertArrayEquals(new User[] { new User(1, null) }, array2.getOrElseThrow().getOrElseThrow());
    assertArrayEquals(new User[] { new User(1, null) }, array3.getOrElseThrow().getOrElseThrow());
    assertArrayEquals(new User[] { null }, array4.getOrElseThrow().getOrElseThrow());
    assertEquals(success(none()), array5);
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

    var listOfUsers = new TypeToken<List<User>>() {};
    var json = new PureJson();
    Try<Option<List<User>>> list1 = json.fromJson(string1, listOfUsers.getType());
    Try<Option<List<User>>> list2 = json.fromJson(string2, listOfUsers.getType());
    Try<Option<List<User>>> list3 = json.fromJson(string3, listOfUsers.getType());
    Try<Option<List<User>>> list4 = json.fromJson(string4, listOfUsers.getType());
    Try<Option<List<User>>> list5 = json.fromJson(string5, listOfUsers.getType());

    assertSuccessSome(List.of(new User(1, "toni")), list1);
    assertSuccessSome(List.of(new User(1, null)), list2);
    assertSuccessSome(List.of(new User(1, null)), list3);
    assertSuccessSome(listWithNull(), list4);
    assertEquals(success(none()), list5);
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

    var mapOfUsers = new TypeToken<Map<String, User>>() {};
    var json = new PureJson();
    Try<Option<Map<String, User>>> map1 = json.fromJson(string1, mapOfUsers.getType());
    Try<Option<Map<String, User>>> map2 = json.fromJson(string2, mapOfUsers.getType());
    Try<Option<Map<String, User>>> map3 = json.fromJson(string3, mapOfUsers.getType());
    Try<Option<Map<String, User>>> map4 = json.fromJson(string4, mapOfUsers.getType());
    Try<Option<Map<String, User>>> map5 = json.fromJson(string5, mapOfUsers.getType());

    assertSuccessSome(Map.of("toni", new User(1, "toni")), map1);
    assertSuccessSome(Map.of("toni", new User(1, null)), map2);
    assertSuccessSome(Map.of("toni", new User(1, null)), map3);
    assertSuccessSome(singletonMap("toni", null), map4);
    assertEquals(success(none()), map5);
  }
  
  @Test
  void failure() {
    suite("failure", 
        
        it.should("fail when not supported")
          .given(List.of(1, 2, 3))
          .when(list -> new PureJson().toString(list))
          .thenMustBe(instanceOf(UnsupportedOperationException.class).compose(Try::getCause)),
        
        it.should("fail when invalid json syntax")
          .given("this is wrong")
          .when(json -> new PureJson().fromJson(json, User.class))
          .thenMustBe(instanceOf(JsonSyntaxException.class).compose(Try::getCause)),
        
        it.should("be none when empty string")
          .given("")
          .when(json -> new PureJson().fromJson(json, User.class))
          .thenMustBe(equalsTo(success(none()))),
        
        it.should("fail when null string")
          .<String>givenNull()
          .when(json -> new PureJson().fromJson(json, User.class))
          .thenMustBe(instanceOf(IllegalArgumentException.class).compose(Try::getCause)),
        
        it.should("fail when type doesn't match with json")
          .given("""
              {"id":1,"name":"toni"} 
              """)
          .when(json -> new PureJson().fromJson(json, new TypeToken<List<User>>() {}.getType()))
          .thenMustBe(instanceOf(IllegalArgumentException.class).compose(Try::getCause))
        
        ).run().assertion();
    
  }
  
  @Test
  void serializePrimitives() {
    suite("serialize primitives", 

        it.should("serialize null")
          .givenNull()
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("null"))),

        it.should("serialize a character")
          .given("A")
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("\"A\""))),

        it.should("serialize a unicode character")
          .given("Á")
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("\"Á\""))),

        it.should("serialize a byte")
          .given((byte) 1)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1"))),

        it.should("serialize a short")
          .given((short) 1)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1"))),

        it.should("serialize an integer")
          .given(1)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1"))),

        it.should("serialize a long")
          .given(1L)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1"))),

        it.should("serialize a float")
          .given(1F)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1.0"))),

        it.should("serialize a double")
          .given(1D)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1.0"))),

        it.should("serialize a big integer")
          .given(BigInteger.ONE)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1"))),

        it.should("serialize a big decimal")
          .given(BigDecimal.ONE)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("1"))),

        it.should("serialize a string")
          .given("asdfg")
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("\"asdfg\""))),

        it.should("serialize a enum")
          .given(EnumTest.VAL1)
          .when(value -> new PureJson().toString(value))
          .thenMustBe(equalsTo(success("\"VAL1\"")))

        ).run().assertion();
  }
  
  @Test
  void parsePrimitives() {
    suite("parse primitives", 

        it.should("parse null")
          .given("null")
          .when(json -> new PureJson().fromJson(json, String.class))
          .thenMustBe(equalsTo(success(none()))),

        it.should("parse char")
          .given("\"A\"")
          .when(json -> new PureJson().fromJson(json, char.class))
          .thenMustBe(equalsTo(success(some('A')))),

        it.should("parse unicode char")
          .given("\"Á\"")
          .when(json -> new PureJson().fromJson(json, char.class))
          .thenMustBe(equalsTo(success(some('Á')))),

        it.should("parse byte")
          .given("1")
          .when(json -> new PureJson().fromJson(json, byte.class))
          .thenMustBe(equalsTo(success(some((byte) 1)))),

        it.should("parse short")
          .given("1")
          .when(json -> new PureJson().fromJson(json, short.class))
          .thenMustBe(equalsTo(success(some((short) 1)))),

        it.should("parse int")
          .given("1")
          .when(json -> new PureJson().fromJson(json, int.class))
          .thenMustBe(equalsTo(success(some(1)))),

        it.should("parse long")
          .given("1")
          .when(json -> new PureJson().fromJson(json, long.class))
          .thenMustBe(equalsTo(success(some(1L)))),

        it.should("parse float")
          .given("1.0")
          .when(json -> new PureJson().fromJson(json, float.class))
          .thenMustBe(equalsTo(success(some(1F)))),

        it.should("parse double")
          .given("1.0")
          .when(json -> new PureJson().fromJson(json, double.class))
          .thenMustBe(equalsTo(success(some(1D)))),

        it.should("parse big integer")
          .given("1")
          .when(json -> new PureJson().fromJson(json, BigInteger.class))
          .thenMustBe(equalsTo(success(some(BigInteger.ONE)))),

        it.should("parse big decimal")
          .given("1.0")
          .when(json -> new PureJson().fromJson(json, BigDecimal.class))
          .thenMustBe(equalsTo(success(some(BigDecimal.valueOf(1.0))))),

        it.should("parse string")
          .given("\"asdfg\"")
          .when(json -> new PureJson().fromJson(json, String.class))
          .thenMustBe(equalsTo(success(some("asdfg")))),

        it.should("parse enum values")
          .given("\"VAL1\"")
          .when(json -> new PureJson().fromJson(json, EnumTest.class))
          .thenMustBe(equalsTo(success(some(EnumTest.VAL1))))

        ).run().assertion();
  }

  @Test
  void parsePerformance() {
    var listOfUsers = new TypeToken<List<Pojo>>() { }.getType();
    var json1 = new PureJson();
    var json2 = new PureJson().add(listOfUsers, JsonAdapter.adapter(listOfUsers));
    var json3 = new PureJson().add(listOfUsers, builderPojoAdapter());
    var json4 = new PureJson().add(listOfUsers, adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 500;
    var stats1 = ioPerfCase("reflection", parseTask(string -> json1.fromJson(string, listOfUsers))).run(times);
    var stats2 = ioPerfCase("cached", parseTask(string -> json2.fromJson(string, listOfUsers))).run(times);
    var stats3 = ioPerfCase("builder", parseTask(string -> json3.fromJson(string, listOfUsers))).run(times);
    var stats4 = ioPerfCase("explicit", parseTask(string -> json4.fromJson(string, listOfUsers))).run(times);
    var stats5 = ioPerfCase("gson", parseTask(string -> gson.fromJson(string, listOfUsers))).run(times);

    runPerf("parse", listOf(stats1, stats2, stats3, stats4, stats5));
  }

  @Test
  void serializePerformance() {
    var listOfUsers = new TypeToken<List<Pojo>>() { }.getType();
    var json1 = new PureJson();
    var json2 = new PureJson().add(listOfUsers, JsonAdapter.adapter(listOfUsers));
    var json3 = new PureJson().add(listOfUsers, builderPojoAdapter());
    var json4 = new PureJson().add(listOfUsers, adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 500;
    var stats1 = ioPerfCase("reflection", serializeTask(value -> json1.toString(value, listOfUsers))).run(times);
    var stats2 = ioPerfCase("cached", serializeTask(value -> json2.toString(value, listOfUsers))).run(times);
    var stats3 = ioPerfCase("builder", serializeTask(value -> json3.toString(value, listOfUsers))).run(times);
    var stats4 = ioPerfCase("explicit", serializeTask(value -> json4.toString(value, listOfUsers))).run(times);
    var stats5 = ioPerfCase("gson", serializeTask(value -> gson.toJson(value, listOfUsers))).run(times);

    runPerf("serialize", listOf(stats1, stats2, stats3, stats4, stats5));
  }

  private void runPerf(String name, Sequence<Kind<IO_, Stats>> stats) {
    printStats(name, SequenceInstances.traverse().sequence(
        IOInstances.applicative(), stats).fix(toIO()).unsafeRunSync().fix(toSequence()));
  }

  private <R> Producer<R> serializeTask(Function1<List<Pojo>, R> serializer) {
    var user = new Pojo(1, "toni");

    var listOfUsers = Stream.generate(() -> user).limit(3000).collect(toList());

    return () -> serializer.apply(listOfUsers);
  }

  private <R> Producer<R> parseTask(Function1<String, R> parser) {
    var user = """
        {"id":1,"name":"toni"}
        """.strip();
    
    var listOfUsers = Stream.generate(() -> user).limit(3000).collect(joining(",", "[", "]"));

    return () -> parser.apply(listOfUsers);
  }

  private JsonAdapter<Iterable<Pojo>> builderPojoAdapter() {
    return iterableAdapter(
        JsonAdapter.builder(Pojo.class)
          .addInteger("id", Pojo::getId)
          .addString("name", Pojo::getName)
          .build());
  }

  private JsonAdapter<Iterable<Pojo>> adhocPojoAdapter() {
    return iterableAdapter(JsonAdapter.of(
        (context, value) -> object(
            entry("id", INTEGER.encode(context, value.getId())),
            entry("name", STRING.encode(context, value.getName()))), 
        (context, json) -> {
          if (json instanceof JsonNode.Object o) {
            return new Pojo(
                INTEGER.decode(context, o.get("id")),
                STRING.decode(context, o.get("name")));
          }
          throw new IllegalArgumentException();
        }));
  }

  private void printStats(String name, Sequence<Stats> stats) {
    System.out.println("Performance " + name);
    System.out.println("name\ttot\tmin\tmax\tmean\tp50\tp90\tp95\tp99");
    for (var s : stats) {
      System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d%n", 
          s.getName().substring(0, 4),
          s.getTotal().toMillis(),
          s.getMin().toMillis(),
          s.getMax().toMillis(),
          s.getMean().toMillis(),
          s.getP50().toMillis(),
          s.getP90().toMillis(),
          s.getP95().toMillis(),
          s.getP99().toMillis());
    }
  }

  private <T> List<T> listWithNull() {
    var list = new ArrayList<T>();
    list.add(null);
    return list;
  }
  
  private <T> void assertSuccessSome(T valueOf, Try<Option<T>> fromJson) {
    assertEquals(success(some(valueOf)), fromJson);
  }
}