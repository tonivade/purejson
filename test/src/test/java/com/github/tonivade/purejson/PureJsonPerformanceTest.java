/*
 * Copyright (c) 2020-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purefun.core.Equal;
import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.data.Sequence_;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.google.gson.GsonBuilder;

@Tag("performance")
@Tag("slow")
class PureJsonPerformanceTest {

  private static record Value(Integer id, String name) {}

  private static final class Pojo {

    @SuppressWarnings("unused")
    private static final int x = 1;

    private Integer id;
    private String name;

    @SuppressWarnings("unused")
    public Pojo() {
      // default constructor is need to instance using reflection
    }

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

  @Test
  void parsePerformanceRecord() {
    var listOfValues = new TypeToken<List<Value>>() { }.getType();
    var json1 = new PureJson<>(listOfValues);
    var json2 = new PureJson<>(builderValueAdapter());
    var json3 = new PureJson<>(adhocValueAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    var stats1 = ioPerfCase("reflection", parseTask(string -> json1.fromJson(string))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", parseTask(string -> json2.fromJson(string))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", parseTask(string -> json3.fromJson(string))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", parseTask(string -> gson.fromJson(string, listOfValues))).warmup(warmup).run(times);

    runPerf("parse record", listOf(stats1, stats2, stats3, stats4));
  }

  @Test
  void parsePerformancePojo() {
    var listOfPojos = new TypeToken<List<Pojo>>() { }.getType();
    var json1 = new PureJson<>(listOfPojos);
    var json2 = new PureJson<>(builderPojoAdapter());
    var json3 = new PureJson<>(adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    var stats1 = ioPerfCase("reflection", parseTask(string -> json1.fromJson(string))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", parseTask(string -> json2.fromJson(string))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", parseTask(string -> json3.fromJson(string))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", parseTask(string -> gson.fromJson(string, listOfPojos))).warmup(warmup).run(times);

    runPerf("parse pojo", listOf(stats1, stats2, stats3, stats4));
  }

  @Test
  void serializePerformanceRecord() {
    var listOfValues = new TypeToken<List<Value>>() { }.getType();
    var json1 = new PureJson<>(listOfValues);
    var json2 = new PureJson<>(builderValueAdapter());
    var json3 = new PureJson<>(adhocValueAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    Producer<Value> supplier = () -> new Value(1, "name");
    var stats1 = ioPerfCase("reflection", serializeTask(supplier, value -> json1.toString(value))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", serializeTask(supplier, value -> json2.toString(value))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", serializeTask(supplier, value -> json3.toString(value))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", serializeTask(supplier, value -> gson.toJson(value, listOfValues))).warmup(warmup).run(times);

    runPerf("serialize record", listOf(stats1, stats2, stats3, stats4));
  }

  @Test
  void serializePerformancePojo() {
    var listOfPojos = new TypeToken<List<Pojo>>() { }.getType();
    var json1 = new PureJson<>(listOfPojos);
    var json2 = new PureJson<>(builderPojoAdapter());
    var json3 = new PureJson<>(adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    Producer<Pojo> supplier = () -> new Pojo(1, "name");
    var stats1 = ioPerfCase("reflection", serializeTask(supplier, value -> json1.toString(value))).warmup(warmup).run(times);
    var stats2 = ioPerfCase("builder", serializeTask(supplier, value -> json2.toString(value))).warmup(warmup).run(times);
    var stats3 = ioPerfCase("adhoc", serializeTask(supplier, value -> json3.toString(value))).warmup(warmup).run(times);
    var stats4 = ioPerfCase("gson", serializeTask(supplier, value -> gson.toJson(value, listOfPojos))).warmup(warmup).run(times);

    runPerf("serialize pojo", listOf(stats1, stats2, stats3, stats4));
  }

  private void runPerf(String name, Sequence<IO<Stats>> stats) {
    printStats(name, Instances.traverse(Sequence_.class).sequence(
      Instances.applicative(IO_.class), stats).fix(toIO()).unsafeRunSync().fix(toSequence()));
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
        if (json instanceof JsonNode.JsonObject o) {
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
        if (json instanceof JsonNode.JsonObject o) {
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
        s.name().substring(0, 4),
        s.total().toMillis(),
        s.min().toMillis(),
        s.max().toMillis(),
        s.mean().toMillis(),
        s.getPercentile(50).getOrElseThrow().toMillis(),
        s.getPercentile(90).getOrElseThrow().toMillis(),
        s.getPercentile(95).getOrElseThrow().toMillis(),
        s.getPercentile(99).getOrElseThrow().toMillis());
    }
  }
}