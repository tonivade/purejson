/*
 * Copyright (c) 2020-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.core.Validator.equalsTo;
import static com.github.tonivade.purefun.core.Validator.instanceOf;
import static com.github.tonivade.purefun.data.Sequence.arrayOf;
import static com.github.tonivade.purefun.data.Sequence.emptyArray;
import static com.github.tonivade.purefun.data.Sequence.emptyList;
import static com.github.tonivade.purefun.data.Sequence.emptySet;
import static com.github.tonivade.purefun.data.Sequence.listOf;
import static com.github.tonivade.purefun.data.Sequence.setOf;
import static com.github.tonivade.purefun.data.Sequence.treeOf;
import static com.github.tonivade.purefun.type.Option.none;
import static com.github.tonivade.purefun.type.Option.some;
import static com.github.tonivade.purefun.type.Try.success;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
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

import org.junit.jupiter.api.Test;

import com.eclipsesource.json.ParseException;
import com.github.tonivade.purecheck.spec.IOTestSpec;
import com.github.tonivade.purefun.core.Equal;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.ImmutableSet;
import com.github.tonivade.purefun.data.ImmutableTree;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

class PureJsonTest extends IOTestSpec<String> {

  record User(Integer id, String name) {}

  static final class Value {

    private static final Equal<Value> EQUAL = Equal.<Value>of().comparing(Value::getId).comparing(Value::getName);

    @SuppressWarnings("unused")
    private static final int x = 1;

    private final Integer id;
    private final String name;

    @JsonCreator
    Value(@JsonProperty("id") Integer id, @JsonProperty("name") String name) {
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
      return EQUAL.applyTo(this, obj);
    }

    @Override
    public String toString() {
      return "Value(id:%s,name:%s)".formatted(id, name);
    }
  }

  static final class Pojo {

    private static final Equal<Pojo> EQUAL = Equal.<Pojo>of().comparing(Pojo::getId).comparing(Pojo::getName);

    @SuppressWarnings("unused")
    private static final int x = 1;

    private Integer id;
    private String name;

    Pojo() {}

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
      return EQUAL.applyTo(this, obj);
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
          .when(value -> new PureJson<User>().toString(value))
          .then(equalsTo(success("""
              {"id":1,"name":"toni"}
              """.strip()))),

        it.should("serialize a record with null fields")
          .given(new User(1, null))
          .when(value -> new PureJson<User>().toString(value))
          .then(equalsTo(success("""
              {"id":1,"name":null}
              """.strip()))),

        it.should("serialize a value")
          .given(new Value(1, "toni"))
          .when(value -> new PureJson<Value>().toString(value))
          .then(equalsTo(success("""
              {"id":1,"name":"toni"}
              """.strip()))),

        it.should("serialize a value with null fields")
          .given(new Value(1, null))
          .when(value -> new PureJson<Value>().toString(value))
          .then(equalsTo(success("""
              {"id":1,"name":null}
              """.strip()))),

        it.should("serialize a pojo")
          .given(new Pojo(1, "toni"))
          .when(value -> new PureJson<Pojo>().toString(value))
          .then(equalsTo(success("""
              {"id":1,"name":"toni"}
              """.strip()))),

        it.should("serialize a pojo with null fields")
          .given(new Pojo(1, null))
          .when(value -> new PureJson<Pojo>().toString(value))
          .then(equalsTo(success("""
              {"id":1,"name":null}
              """.strip())))

        ).run().assertion();
  }

  @Test
  void serializeInnerArray() {

    record Test(String[] values) {}

    var json = new PureJson<Test>();

    suite("serialize inner array",

        it.should("serialize a inner array")
          .given(new Test(List.of("hola", "adios").toArray(String[]::new)))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":["hola","adios"]}
              """.strip()))),

        it.should("serialize a inner array with null values")
          .given(new Test(asList(null, "adios").toArray(String[]::new)))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":[null,"adios"]}
              """.strip()))),

        it.should("serialize a null inner array")
          .given(new Test(null))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":null}
              """.strip())))

        ).run().assertion();
  }

  @Test
  void serializeInnerList() {

    record Test(List<String> values) {}

    var json = new PureJson<Test>();

    suite("serialize inner list",

        it.should("serialize a inner list")
          .given(new Test(List.of("hola", "adios")))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":["hola","adios"]}
              """.strip()))),

        it.should("serialize a inner list with null values")
          .given(new Test(asList(null, "adios")))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":[null,"adios"]}
              """.strip()))),

        it.should("serialize a null inner list")
          .given(new Test(null))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":null}
              """.strip())))

        ).run().assertion();
  }

  @Test
  void serializeInnerMap() {

    record Test(Map<String, String> values) {}

    var json = new PureJson<Test>();

    suite("serialize inner map",

        it.should("serialize a inner map")
          .given(new Test(Map.of("hola", "adios")))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":{"hola":"adios"}}
              """.strip()))),

        it.should("serialize a inner map with null values")
          .given(new Test(singletonMap("hola", null)))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":{"hola":null}}
              """.strip()))),

        it.should("serialize a null inner map")
          .given(new Test(null))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              {"values":null}
              """.strip())))

        ).run().assertion();
  }

  @Test
  void serializeList() {
    Type listOfUsers = new TypeToken<List<User>>() {}.getType();

    var json = new PureJson<>(listOfUsers);

    suite("serialize list",

        it.should("serialize a list of records")
          .given(List.of(new User(1, "toni")))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a list of records with null fields")
          .given(List.of(new User(1, null)))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null list of records")
          .given(listWithNull())
          .when(value -> json.toString(value))
          .then(equalsTo(success("[null]")))

        ).run().assertion();
  }

  @Test
  void serializeImmutableList() {
    Type listOfUsers = new TypeToken<ImmutableList<User>>() {}.getType();

    var json = new PureJson<>(listOfUsers);

    suite("serialize immutable list",

        it.should("serialize a immutable list of records")
          .given(listOf(new User(1, "toni")))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a immutable list of records with null fields")
          .given(listOf(new User(1, null)))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null immutable list")
          .given(emptyList().append(null))
          .when(value -> json.toString(value))
          .then(equalsTo(success("[null]")))

        ).run().assertion();
  }

  @Test
  void serializeImmutableArray() {
    Type listOfUsers = new TypeToken<ImmutableArray<User>>() {}.getType();

    var json = new PureJson<>(listOfUsers);

    suite("serialize immutable array",

        it.should("serialize a immutable array of records")
          .given(arrayOf(new User(1, "toni")))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a immutable array of records with null fields")
          .given(arrayOf(new User(1, null)))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null immutable array")
          .given(emptyArray().append(null))
          .when(value -> json.toString(value))
          .then(equalsTo(success("[null]")))

        ).run().assertion();
  }

  @Test
  void serializeImmutableSet() {
    Type listOfUsers = new TypeToken<ImmutableSet<User>>() {}.getType();

    var json = new PureJson<>(listOfUsers);

    suite("serialize immutable set",

        it.should("serialize a immutable set of records")
          .given(setOf(new User(1, "toni")))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":"toni"}]
              """.strip()))),

        it.should("serialize a immutable set of records with null fields")
          .given(setOf(new User(1, null)))
          .when(value -> json.toString(value))
          .then(equalsTo(success("""
              [{"id":1,"name":null}]
              """.strip()))),

        it.should("serialize a null immutable set")
          .given(() -> emptySet().append(null))
          .when(value -> json.toString(value))
          .then(equalsTo(success("[null]"))).disable("set doesn't allow null values")

        ).run().assertion();
  }

  @Test
  void serializeArray() {
    var json = new PureJson<User[]>();
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
    Type mapOfUsers = new TypeToken<Map<String, User>>(){}.getType();

    var json = new PureJson<>(mapOfUsers);
    var result1 = json.toString(Map.of("toni", new User(1, "toni")));
    var result2 = json.toString(Map.of("toni", new User(1, null)));
    var result3 = json.toString(singletonMap("toni", null));

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
    Type mapOfUsers = new TypeToken<ImmutableMap<String, User>>(){}.getType();

    var json = new PureJson<>(mapOfUsers);
    var result1 = json.toString(ImmutableMap.empty().put("toni", new User(1, "toni")));
    var result2 = json.toString(ImmutableMap.empty().put("toni", new User(1, null)));
    var result3 = json.toString(ImmutableMap.empty().put("toni", null));

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
          .when(node -> new PureJson<User>().fromJson(node))
          .then(equalsTo(success(some(new User(1, "toni"))))),

        it.should("parse a record with null fields")
          .given("""
              {"id":1,"name":null}
              """.strip())
          .when(node -> new PureJson<User>().fromJson(node))
          .then(equalsTo(success(some(new User(1, null))))),

        it.should("parse a null record")
          .given("null")
          .when(node -> new PureJson<User>().fromJson(node))
          .then(equalsTo(success(none()))),

        it.should("parse a value")
          .given("""
              {"id":1,"name":"toni"}
              """.strip())
          .when(node -> new PureJson<Value>().fromJson(node))
          .then(equalsTo(success(some(new Value(1, "toni"))))),

        it.should("parse a value")
          .given("""
              {"id":1,"name":null}
              """.strip())
          .when(node -> new PureJson<Value>().fromJson(node))
          .then(equalsTo(success(some(new Value(1, null))))),

        it.should("parse a null value")
          .given("null")
          .when(node -> new PureJson<Value>().fromJson(node))
          .then(equalsTo(success(none()))),

        it.should("parse a pojo")
          .given("""
              {"id":1,"name":"toni"}
              """.strip())
          .when(node -> new PureJson<Pojo>().fromJson(node))
          .then(equalsTo(success(some(new Pojo(1, "toni"))))),

        it.should("parse a pojo")
          .given("""
              {"id":1,"name":null}
              """.strip())
          .when(node -> new PureJson<Pojo>().fromJson(node))
          .then(equalsTo(success(some(new Pojo(1, null))))),

        it.should("parse a null pojo")
          .given("null")
          .when(node -> new PureJson<Pojo>().fromJson(node))
          .then(equalsTo(success(none())))

        ).run().assertion();
  }

  @Test
  void parseInnerSequence() {

    record Test(Sequence<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(listOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableArray() {

    record Test(ImmutableArray<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(arrayOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableList() {

    record Test(ImmutableList<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(listOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableSet() {

    record Test(ImmutableSet<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(setOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerImmutableTree() {

    record Test(ImmutableTree<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(treeOf("one", "two", "three")), result);
  }

  @Test
  void parseInnerCollection() {

    record Test(Collection<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(List.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerArray() {

    record Test(String[] values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertArrayEquals(List.of("one", "two", "three").toArray(String[]::new),
        result.getOrElseThrow().getOrElseThrow().values);
  }

  @Test
  void parseInnerList() {

    record Test(List<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(List.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerSet() {

    record Test(Set<String> values) {}

    var string = """
        {"values":["one","two","three"]}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

    assertSuccessSome(new Test(Set.of("one", "two", "three")), result);
  }

  @Test
  void parseInnerMap() {

    record Test(Map<String, String> values) {}

    var string = """
        {"values":{"one":"1","two":"2","three":"3"}}
        """.strip();

    Try<Option<Test>> result = new PureJson<Test>().fromJson(string);

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

    var listOfUsers = new TypeToken<User[]>() {}.getType();
    var json = new PureJson<User[]>(listOfUsers);
    Try<Option<User[]>> array1 = json.fromJson(string1);
    Try<Option<User[]>> array2 = json.fromJson(string2);
    Try<Option<User[]>> array3 = json.fromJson(string3);
    Try<Option<User[]>> array4 = json.fromJson(string4);
    Try<Option<User[]>> array5 = json.fromJson(string5);

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

    var listOfUsers = new TypeToken<List<User>>() {}.getType();
    var json = new PureJson<List<User>>(listOfUsers);
    Try<Option<List<User>>> list1 = json.fromJson(string1);
    Try<Option<List<User>>> list2 = json.fromJson(string2);
    Try<Option<List<User>>> list3 = json.fromJson(string3);
    Try<Option<List<User>>> list4 = json.fromJson(string4);
    Try<Option<List<User>>> list5 = json.fromJson(string5);

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

    var mapOfUsers = new TypeToken<Map<String, User>>() {}.getType();
    var json = new PureJson<Map<String, User>>(mapOfUsers);
    Try<Option<Map<String, User>>> map1 = json.fromJson(string1);
    Try<Option<Map<String, User>>> map2 = json.fromJson(string2);
    Try<Option<Map<String, User>>> map3 = json.fromJson(string3);
    Try<Option<Map<String, User>>> map4 = json.fromJson(string4);
    Try<Option<Map<String, User>>> map5 = json.fromJson(string5);

    assertSuccessSome(Map.of("toni", new User(1, "toni")), map1);
    assertSuccessSome(Map.of("toni", new User(1, null)), map2);
    assertSuccessSome(Map.of("toni", new User(1, null)), map3);
    assertSuccessSome(singletonMap("toni", null), map4);
    assertEquals(success(none()), map5);
  }

  @Test
  void failure() {
    suite("failure",

//        it.should("fail when not supported")
//          .given(List.of(1, 2, 3))
//          .when(list -> new PureJson<>(new TypeToken<List<Integer>>() {}.getType()).toString(list))
//          .then(instanceOf(UnsupportedOperationException.class).compose(Try::getCause)),

        it.should("fail when invalid json syntax")
          .given("this is wrong")
          .when(json -> new PureJson<User>().fromJson(json))
          .then(instanceOf(ParseException.class).compose(Try::getCause)),

        it.should("fail when empty string")
          .given("")
          .when(json -> new PureJson<User>().fromJson(json))
          .then(instanceOf(ParseException.class).compose(Try::getCause)),

        it.should("fail when null string")
          .<String>givenNull()
          .when(json -> new PureJson<User>().fromJson(json))
          .then(instanceOf(IllegalArgumentException.class).compose(Try::getCause)),

        it.should("fail when type doesn't match with json")
          .given("""
              {"id":1,"name":"toni"}
              """)
          .when(json -> new PureJson<>(new TypeToken<List<User>>() {}.getType()).fromJson(json))
          .then(instanceOf(IllegalArgumentException.class).compose(Try::getCause))

        ).run().assertion();
  }

  @Test
  void serializePrimitives() {
    suite("serialize primitives",

        it.should("serialize null")
          .<String>givenNull()
          .when(value -> new PureJson<String>().toString(value))
          .then(equalsTo(success("null"))),

        it.should("serialize a character")
          .given('A')
          .when(value -> new PureJson<Character>().toString(value))
          .then(equalsTo(success("\"A\""))),

        it.should("serialize a unicode character")
          .given('\u00c1')
          .when(value -> new PureJson<Character>().toString(value))
          .then(equalsTo(success("\"\u00c1\""))),

        it.should("serialize a byte")
          .given((byte) 1)
          .when(value -> new PureJson<Byte>().toString(value))
          .then(equalsTo(success("1"))),

        it.should("serialize a short")
          .given((short) 1)
          .when(value -> new PureJson<Short>().toString(value))
          .then(equalsTo(success("1"))),

        it.should("serialize an integer")
          .given(1)
          .when(value -> new PureJson<Integer>().toString(value))
          .then(equalsTo(success("1"))),

        it.should("serialize a long")
          .given(1L)
          .when(value -> new PureJson<Long>().toString(value))
          .then(equalsTo(success("1"))),

        it.should("serialize a float")
          .given(1.1F)
          .when(value -> new PureJson<Float>().toString(value))
          .then(equalsTo(success("1.1"))),

        it.should("serialize a double")
          .given(1.1D)
          .when(value -> new PureJson<Double>().toString(value))
          .then(equalsTo(success("1.1"))),

        it.should("serialize a big integer")
          .given(BigInteger.ONE)
          .when(value -> new PureJson<BigInteger>().toString(value))
          .then(equalsTo(success("1"))),

        it.should("serialize a big decimal")
          .given(BigDecimal.ONE)
          .when(value -> new PureJson<BigDecimal>().toString(value))
          .then(equalsTo(success("1"))),

        it.should("serialize a string")
          .given("asdfg")
          .when(value -> new PureJson<String>().toString(value))
          .then(equalsTo(success("\"asdfg\""))),

        it.should("serialize a enum")
          .given(EnumTest.VAL1)
          .when(value -> new PureJson<EnumTest>().toString(value))
          .then(equalsTo(success("\"VAL1\"")))

        ).run().assertion();
  }

  @Test
  void parsePrimitives() {
    suite("parse primitives",

        it.should("parse null")
          .given("null")
          .when(json -> new PureJson<String>().fromJson(json))
          .then(equalsTo(success(none()))),

        it.should("parse char")
          .given("\"A\"")
          .when(json -> new PureJson<>(char.class).fromJson(json))
          .then(equalsTo(success(some('A')))),

        it.should("parse unicode char")
          .given("\"\u00c1\"")
          .when(json -> new PureJson<>(char.class).fromJson(json))
          .then(equalsTo(success(some('\u00c1')))),

        it.should("parse byte")
          .given("1")
          .when(json -> new PureJson<>(byte.class).fromJson(json))
          .then(equalsTo(success(some((byte) 1)))),

        it.should("parse short")
          .given("1")
          .when(json -> new PureJson<>(short.class).fromJson(json))
          .then(equalsTo(success(some((short) 1)))),

        it.should("parse int")
          .given("1")
          .when(json -> new PureJson<>(int.class).fromJson(json))
          .then(equalsTo(success(some(1)))),

        it.should("parse long")
          .given("1")
          .when(json -> new PureJson<>(long.class).fromJson(json))
          .then(equalsTo(success(some(1L)))),

        it.should("parse float")
          .given("1.0")
          .when(json -> new PureJson<>(float.class).fromJson(json))
          .then(equalsTo(success(some(1F)))),

        it.should("parse double")
          .given("1.0")
          .when(json -> new PureJson<>(double.class).fromJson(json))
          .then(equalsTo(success(some(1D)))),

        it.should("parse big integer")
          .given("1")
          .when(json -> new PureJson<BigInteger>().fromJson(json))
          .then(equalsTo(success(some(BigInteger.ONE)))),

        it.should("parse big decimal")
          .given("1.0")
          .when(json -> new PureJson<BigDecimal>().fromJson(json))
          .then(equalsTo(success(some(BigDecimal.valueOf(1.0))))),

        it.should("parse string")
          .given("\"asdfg\"")
          .when(json -> new PureJson<String>().fromJson(json))
          .then(equalsTo(success(some("asdfg")))),

        it.should("parse enum values")
          .given("\"VAL1\"")
          .when(json -> new PureJson<EnumTest>().fromJson(json))
          .then(equalsTo(success(some(EnumTest.VAL1))))

        ).run().assertion();
  }

  private static <T> List<T> listWithNull() {
    var list = new ArrayList<T>();
    list.add(null);
    return list;
  }

  private static <T> void assertSuccessSome(T valueOf, Try<Option<T>> fromJson) {
    if (fromJson.isFailure()) {
      fromJson.getCause().printStackTrace();
    }
    assertEquals(success(some(valueOf)), fromJson);
  }
}