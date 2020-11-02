/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.tools.JavaFileObject;

import org.junit.jupiter.api.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

class JsonAnnotationProcessorTest {

  @Test
  void recordTest() {
    JavaFileObject file = forSourceLines("test.Foo",
        "package test;",

        "import com.github.tonivade.purejson.Json;",

        "@Json",
        "public record Foo(int id, String name) {}");

    JavaFileObject expected = forSourceLines("test.FooAdapter",
        "package test;",

        "import static com.github.tonivade.purejson.JsonAdapter.*;",

        "import javax.annotation.processing.Generated;",

        "@Generated(\"com.github.tonivade.purejson.JsonAnnotationProcessor\")",
        "public final class FooAdapter implements JsonAdapter<Foo> {",

          "public JsonNode encode(Foo value) {",
            "return object(",
              "entry(\"id\", INTEGER.encode(number(value.id())), ",
              "entry(\"name\", STRING.encode(value.name())));",
          "}",

          "public Foo decode(JsonNode node) {",
            "var object = node.asObject();",
            "return new Foo(",
              "INTEGER.decode(object.get(\"id\"),",
              "STRING.decode(object.get(\"name\")))",
          "}",
        "}");
    
    Compilation compile = Compiler.javac()
        .withOptions("--enable-preview", "-source", 15)
        .withProcessors(new JsonAnnotationProcessor())
        .compile(file);
    
    assertTrue(compile.errors().isEmpty());
  }

  @Test
  void pojoTest() {
    JavaFileObject file = forSourceLines("test.Foo",
        "package test;",

        "import com.github.tonivade.purejson.Json;",

        "@Json",
        "public final class Foo {",
          "private final int id;",
          "private final String name;",
          "public Foo(int id, String name) {",
            "this.id = id;",
            "this.name = name;",
          "}",
          "public int getId() { return id; }",
          "public String getName() { return name; }",
        "}");
    
    Compilation compile = Compiler.javac()
        .withOptions("--enable-preview", "-source", 15)
        .withProcessors(new JsonAnnotationProcessor())
        .compile(file);
    
    assertTrue(compile.errors().isEmpty());
  }

  @Test
  void notSupported() {
    JavaFileObject file = forSourceLines("test.Foo",
        "package test;",

        "import com.github.tonivade.purejson.Json;",

        "@Json",
        "public interface Foo {",
        "}");
    
    Compilation compile = Compiler.javac()
        .withOptions("--enable-preview", "-source", 15)
        .withProcessors(new JsonAnnotationProcessor())
        .compile(file);
    
    assertTrue(compile.errors().size() == 1);
  }
}
