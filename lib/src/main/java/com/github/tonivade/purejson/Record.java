/*
 * Copyright (c) 2020-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public final class Record<T> {

  private final Class<T> clazz;

  public Record(Class<T> clazz) {
    this.clazz = checkNonNull(clazz);
  }

  public String getName() {
    return clazz.getName();
  }

  public RecordComponent[] getRecordComponents() {
    return new RecordComponent[] {};
  }

  interface RecordComponent {
    String getName();
    Method getAccessor();
    Class<?> getType();
    Type getGenericType();
  }

  public static boolean isRecord(Class<?> aClass) {
    return false;
  }
}