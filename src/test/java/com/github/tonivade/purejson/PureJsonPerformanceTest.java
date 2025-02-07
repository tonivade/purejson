/*
 * Copyright (c) 2020-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purecheck.PerfCase.ioPerfCase;
import static com.github.tonivade.purefun.data.Sequence.listOf;
import static com.github.tonivade.purejson.JsonAdapter.INTEGER;
import static com.github.tonivade.purejson.JsonAdapter.STRING;
import static com.github.tonivade.purejson.JsonAdapter.iterableAdapter;
import static com.github.tonivade.purejson.JsonDSL.entry;
import static com.github.tonivade.purejson.JsonDSL.object;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import com.github.tonivade.purecheck.PerfCase.Stats;
import com.github.tonivade.purefun.core.Equal;
import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.data.SequenceOf;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
    var reflection = new PureJson<>(listOfValues);
    var builder = new PureJson<>(builderValueAdapter());
    var adhoc = new PureJson<>(adhocValueAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    var reflectionStats = ioPerfCase("reflection", parseTask(string -> reflection.fromJson(string))).warmup(warmup).run(times);
    var builderStats = ioPerfCase("builder", parseTask(string -> builder.fromJson(string))).warmup(warmup).run(times);
    var adhocStats = ioPerfCase("adhoc", parseTask(string -> adhoc.fromJson(string))).warmup(warmup).run(times);
    var gsonStats = ioPerfCase("gson", parseTask(string -> gson.fromJson(string, listOfValues))).warmup(warmup).run(times);

    runPerf("parse record", listOf(reflectionStats, builderStats, adhocStats, gsonStats));
  }

  @Test
  void parsePerformancePojo() {
    var listOfPojos = new TypeToken<List<Pojo>>() { }.getType();
    var reflection = new PureJson<>(listOfPojos);
    var builder = new PureJson<>(builderPojoAdapter());
    var adhoc = new PureJson<>(adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    var reflectionStats = ioPerfCase("reflection", parseTask(string -> reflection.fromJson(string))).warmup(warmup).run(times);
    var builderStats = ioPerfCase("builder", parseTask(string -> builder.fromJson(string))).warmup(warmup).run(times);
    var adhocStats = ioPerfCase("adhoc", parseTask(string -> adhoc.fromJson(string))).warmup(warmup).run(times);
    var gsonStats = ioPerfCase("gson", parseTask(string -> gson.fromJson(string, listOfPojos))).warmup(warmup).run(times);

    runPerf("parse pojo", listOf(reflectionStats, builderStats, adhocStats, gsonStats));
  }

  @Test
  void serializePerformanceRecord() {
    var listOfValues = new TypeToken<List<Value>>() { }.getType();
    var reflection = new PureJson<>(listOfValues);
    var builder = new PureJson<>(builderValueAdapter());
    var adhoc = new PureJson<>(adhocValueAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    Producer<Value> supplier = () -> new Value(1, "name");
    var reflectionStats = ioPerfCase("reflection", serializeTask(supplier, value -> reflection.toString(value))).warmup(warmup).run(times);
    var builderStats = ioPerfCase("builder", serializeTask(supplier, value -> builder.toString(value))).warmup(warmup).run(times);
    var adhocStats = ioPerfCase("adhoc", serializeTask(supplier, value -> adhoc.toString(value))).warmup(warmup).run(times);
    var gsonStats = ioPerfCase("gson", serializeTask(supplier, value -> gson.toJson(value, listOfValues))).warmup(warmup).run(times);

    runPerf("serialize record", listOf(reflectionStats, builderStats, adhocStats, gsonStats));
  }

  @Test
  void serializePerformancePojo() {
    var listOfPojos = new TypeToken<List<Pojo>>() { }.getType();
    var reflection = new PureJson<>(listOfPojos);
    var builder = new PureJson<>(builderPojoAdapter());
    var adhoc = new PureJson<>(adhocPojoAdapter());
    var gson = new GsonBuilder().create();

    int times = 5000;
    int warmup = 50;
    Producer<Pojo> supplier = () -> new Pojo(1, "name");
    var reflectionStats = ioPerfCase("reflection", serializeTask(supplier, value -> reflection.toString(value))).warmup(warmup).run(times);
    var builderStats = ioPerfCase("builder", serializeTask(supplier, value -> builder.toString(value))).warmup(warmup).run(times);
    var adhocStats = ioPerfCase("adhoc", serializeTask(supplier, value -> adhoc.toString(value))).warmup(warmup).run(times);
    var gsonStats = ioPerfCase("gson", serializeTask(supplier, value -> gson.toJson(value, listOfPojos))).warmup(warmup).run(times);

    runPerf("serialize pojo", listOf(reflectionStats, builderStats, adhocStats, gsonStats));
  }

  private void runPerf(String name, Sequence<IO<Stats>> stats) {
    printStats(name, Instances.<Sequence<?>>traverse().sequence(
      Instances.applicative(), stats).fix(IOOf::toIO).unsafeRunSync().fix(SequenceOf::toSequence));
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
    System.out.println("name\ttot\tmin\tmax\tmean\tp50\tp90\tp95\tp99\trps");
    for (var s : stats) {
      System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d%n",
        s.name().substring(0, 4),
        s.total().toMillis(),
        s.min().toMillis(),
        s.max().toMillis(),
        s.mean().toMillis(),
        s.getPercentile(50).toMillis(),
        s.getPercentile(90).toMillis(),
        s.getPercentile(95).toMillis(),
        s.getPercentile(99).toMillis(),
        s.getRequestsPerSeconds());
    }
  }
}