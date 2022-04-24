/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purecheck.PerfCase.ioPerfCase;
import static com.github.tonivade.purefun.data.Sequence.listOf;
import static com.github.tonivade.purefun.data.SequenceOf.toSequence;
import static com.github.tonivade.purefun.monad.IOOf.toIO;
import static com.github.tonivade.purejson.JsonAdapter.INTEGER;
import static com.github.tonivade.purejson.JsonAdapter.STRING;
import static com.github.tonivade.purejson.JsonAdapter.iterableAdapter;
import static com.github.tonivade.purejson.JsonDSL.entry;
import static com.github.tonivade.purejson.JsonDSL.object;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purefun.Equal;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.instances.SequenceInstances;
import com.github.tonivade.purefun.monad.IO_;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

@Tag("performance")
@Tag("slow")
class PureJsonPerformanceTest {
  
  private static record Value(Integer id, String name) {}

  private static final class Pojo {

    @SuppressWarnings("unused")
    private static final int x = 1;

    private Integer id;
    private String name;

    public Pojo() {}

    public Pojo(Integer id, String name) {
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
  
  static final class ValueAdapter implements JsonDeserializer<Value> {

    @Override
    public Value deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return new Value(
          json.getAsJsonObject().get("id").getAsInt(), 
          json.getAsJsonObject().get("name").getAsString());
    }
  }

  @Test
  void parsePerformanceRecord() {
    var listOfUsers = new TypeToken<List<Value>>() { }.getType();
    var json1 = new PureJson<>(listOfUsers);
    var json2 = new PureJson<>(builderValueAdapter());
    var json3 = new PureJson<>(adhocValueAdapter());
    var gson = new GsonBuilder().registerTypeAdapter(Value.class, new ValueAdapter()).create();

    int times = 5000;
    int warmup = 50;
    var stats1 = ioPerfCase("reflection", parseTask(string -> json1.fromJson(string))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", parseTask(string -> json2.fromJson(string))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", parseTask(string -> json3.fromJson(string))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", parseTask(string -> gson.fromJson(string, listOfUsers))).warmup(warmup).run(times);

    runPerf("parse value", listOf(stats1, stats2, stats3, stats4));
  }

  @Test
  void parsePerformancePojo() {
    var listOfUsers = new TypeToken<List<Pojo>>() { }.getType();
    var json1 = new PureJson<>(listOfUsers);
    var json2 = new PureJson<>(builderPojoAdapter());
    var json3 = new PureJson<>(adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 2000;
    int warmup = 20;
    var stats1 = ioPerfCase("reflection", parseTask(string -> json1.fromJson(string))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", parseTask(string -> json2.fromJson(string))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", parseTask(string -> json3.fromJson(string))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", parseTask(string -> gson.fromJson(string, listOfUsers))).warmup(warmup).run(times);

    runPerf("parse pojo", listOf(stats1, stats2, stats3, stats4));
  }

  @Test
  void serializePerformanceRecord() {
    var listOfUsers = new TypeToken<List<Value>>() { }.getType();
    var json1 = new PureJson<>(listOfUsers);
    var json2 = new PureJson<>(builderValueAdapter());
    var json3 = new PureJson<>(adhocValueAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    Producer<Value> supplier = () -> new Value(1, "name");
    var stats1 = ioPerfCase("reflection", serializeTask(supplier, value -> json1.toString(value))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", serializeTask(supplier, value -> json2.toString(value))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", serializeTask(supplier, value -> json3.toString(value))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", serializeTask(supplier, value -> gson.toJson(value, listOfUsers))).warmup(warmup).run(times);

    runPerf("serialize value", listOf(stats1, stats2, stats3, stats4));
  }

  @Test
  void serializePerformancePojo() {
    var listOfUsers = new TypeToken<List<Pojo>>() { }.getType();
    var json1 = new PureJson<>(listOfUsers);
    var json2 = new PureJson<>(builderPojoAdapter());
    var json3 = new PureJson<>(adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    Producer<Pojo> supplier = () -> new Pojo(1, "name");
    var stats1 = ioPerfCase("reflection", serializeTask(supplier, value -> json1.toString(value))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", serializeTask(supplier, value -> json2.toString(value))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", serializeTask(supplier, value -> json3.toString(value))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", serializeTask(supplier, value -> gson.toJson(value, listOfUsers))).warmup(warmup).run(times);

    runPerf("serialize pojo", listOf(stats1, stats2, stats3, stats4));
  }

  private void runPerf(String name, Sequence<Kind<IO_, Stats>> stats) {
    printStats(name, SequenceInstances.traverse().sequence(
      IOInstances.applicative(), stats).fix(toIO()).unsafeRunSync().fix(toSequence()));
  }

  private <T, R> Producer<R> serializeTask(Producer<T> supplier, Function1<List<T>, R> serializer) {
    var user = supplier.get();

    var listOfUsers = Stream.generate(() -> user).limit(10_000).collect(toList());

    return () -> serializer.apply(listOfUsers);
  }

  private <R> Producer<R> parseTask(Function1<String, R> parser) {
    var user = """
      {"id":1,"name":"toni"}
      """.strip();

    var listOfUsers = Stream.generate(() -> user).limit(10_000).collect(joining(",", "[", "]"));

    return () -> parser.apply(listOfUsers);
  }

  private JsonAdapter<Iterable<Pojo>> builderPojoAdapter() {
    return iterableAdapter(
      JsonAdapter.builder(Pojo.class)
      .addInteger("id", Pojo::getId)
      .addString("name", Pojo::getName)
      .build());
  }

  private JsonAdapter<Iterable<Value>> builderValueAdapter() {
    return iterableAdapter(
      JsonAdapter.builder(Value.class)
      .addInteger("id", Value::id)
      .addString("name", Value::name)
      .build());
  }

  private JsonAdapter<Iterable<Pojo>> adhocPojoAdapter() {
    return iterableAdapter(JsonAdapter.of(
      value -> object(
        entry("id", INTEGER.encode(value.getId())),
        entry("name", STRING.encode(value.getName()))), 
      json -> {
        if (json instanceof JsonNode.Object o) {
          return new Pojo(
            INTEGER.decode(o.get("id")),
            STRING.decode(o.get("name")));
        }
        throw new IllegalArgumentException();
      }));
  }

  private JsonAdapter<Iterable<Value>> adhocValueAdapter() {
    return iterableAdapter(JsonAdapter.of(
      value -> object(
        entry("id", INTEGER.encode(value.id())),
        entry("name", STRING.encode(value.name()))), 
      json -> {
        if (json instanceof JsonNode.Object o) {
          return new Value(
            INTEGER.decode(o.get("id")),
            STRING.decode(o.get("name")));
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
        s.getPercentile(50).getOrElseThrow().toMillis(),
        s.getPercentile(90).getOrElseThrow().toMillis(),
        s.getPercentile(95).getOrElseThrow().toMillis(),
        s.getPercentile(99).getOrElseThrow().toMillis());
    }
  }
}