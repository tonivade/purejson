/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JsonAdapterTest {

  @Test
  void checkCache() {
    var a1 = JsonAdapter.adapter(String.class);
    var a2 = JsonAdapter.adapter(String.class);
    
    assertSame(a1, a2);
  }
}
