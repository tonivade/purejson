/*
 * Copyright (c) 2020-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.github.tonivade.purefun.type.Try;

public final class Record<T> {

  private static final Method IS_RECORD;
  private static final Method GET_RECORD_COMPONENTS;
  private static final Method GET_NAME;
  private static final Method GET_TYPE;
  private static final Method GET_GENERIC_TYPE;
  private static final Method GET_ACCESSOR;
  
  private final Class<T> clazz;

  static {
    Method isRecord;
    Method getRecordComponents;
    Method getName;
    Method getType;
    Method getGenericType;
    Method getAccessor;
    try {
      isRecord = Class.class.getDeclaredMethod("isRecord");
      getRecordComponents = Class.class.getMethod("getRecordComponents");
      Class<?> c = Class.forName("java.lang.reflect.RecordComponent");
      getName = c.getMethod("getName");
      getType = c.getMethod("getType");
      getGenericType = c.getMethod("getGenericType");
      getAccessor = c.getMethod("getAccessor");
    } catch (ClassNotFoundException| NoSuchMethodException e) {
      // pre-Java-14
      isRecord = null;
      getRecordComponents = null;
      getName = null;
      getType = null;
      getGenericType = null;
      getAccessor = null;
    }
    IS_RECORD = isRecord;
    GET_RECORD_COMPONENTS = getRecordComponents;
    GET_NAME = getName;
    GET_TYPE = getType;
    GET_GENERIC_TYPE = getGenericType;
    GET_ACCESSOR = getAccessor;
  }

  public Record(Class<T> clazz) {
    this.clazz = checkNonNull(clazz);
  }
  
  public String getName() {
    try {
      return (String) GET_NAME.invoke(clazz);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new AssertionError();
    }
  }
  
  public RecordComponent[] getRecordComponents() {
    try {
      Object[] components = (Object[]) GET_RECORD_COMPONENTS.invoke(clazz);
      if (components != null) {
        return Arrays.stream(components).map(ReflectionRecordComponent::new).toArray(RecordComponent[]::new);
      }
      return new RecordComponent[] {};
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new AssertionError();
    }
  }

  interface RecordComponent {
    String getName();
    Method getAccessor();
    Class<?> getType();
    Type getGenericType();
  }
  
  static final class ReflectionRecordComponent implements RecordComponent {
    
    private final Object component;
    
    public ReflectionRecordComponent(Object component) {
      this.component = checkNonNull(component);
    }

    @Override
    public String getName() {
      return invoke(GET_NAME);
    }

    @Override
    public Class<?> getType() {
      return invoke(GET_TYPE);
    }

    @Override
    public Method getAccessor() {
      return invoke(GET_ACCESSOR);
    }

    @Override
    public Type getGenericType() {
      return invoke(GET_GENERIC_TYPE);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T invoke(Method method) {
      return (T) Try.of(() -> method.invoke(component)).getOrElseThrow();
    }
    
  }

  public static boolean isRecord(Class<?> aClass) {
    try {
      return IS_RECORD == null ? false : (boolean) IS_RECORD.invoke(aClass);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new AssertionError();
    }
  }

}