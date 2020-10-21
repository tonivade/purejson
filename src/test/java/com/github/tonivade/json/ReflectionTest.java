package com.github.tonivade.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ReflectionTest {

  @Test
  void test() {
    assertEquals("java.util.List<java.lang.String>", new Reflection<List<String>>() {}.getTypeName());
    assertEquals("java.lang.String", new Reflection<String>() {}.getTypeName());
    assertEquals("java.lang.String[]", new Reflection<String[]>() {}.getTypeName());
  }
}
