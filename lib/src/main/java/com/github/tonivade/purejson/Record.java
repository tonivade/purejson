/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

public final class Record<T> {

  private final Class<T> clazz;

  public Record(Class<T> clazz) {
    this.clazz = checkNonNull(clazz);
  }

  public String getName() {
    return clazz.getName();
  }

  public RecordComponent[] getRecordComponents() {
    return Arrays.stream(clazz.getRecordComponents()).map(NativeRecordComponent::new).toArray(RecordComponent[]::new);
  }

  interface RecordComponent {
    String getName();
    Method getAccessor();
    Class<?> getType();
    Type getGenericType();
  }

  static final class NativeRecordComponent implements RecordComponent {

    private final java.lang.reflect.RecordComponent component;

    public NativeRecordComponent(java.lang.reflect.RecordComponent component) {
      this.component = checkNonNull(component);
    }

    @Override
    public String getName() {
      return component.getName();
    }

    @Override
    public Class<?> getType() {
      return component.getType();
    }

    @Override
    public Method getAccessor() {
      return component.getAccessor();
    }

    @Override
    public Type getGenericType() {
      return component.getGenericType();
    }
  }

  public static boolean isRecord(Class<?> aClass) {
    return aClass.isRecord();
  }
}