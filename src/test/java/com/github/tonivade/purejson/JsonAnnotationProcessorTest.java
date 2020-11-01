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
  void test() {
    JavaFileObject file = forSourceLines("test.Foo",
        "package test;",

        "import com.github.tonivade.purejson.Json;",

        "@Json",
        "public record Foo(int id, String name) {}");
    
    Compilation compile = Compiler.javac()
        .withOptions("--enable-preview", "-source", 15)
        .withProcessors(new JsonAnnotationProcessor())
        .compile(file);
    
    assertTrue(compile.errors().isEmpty());
  }
}
