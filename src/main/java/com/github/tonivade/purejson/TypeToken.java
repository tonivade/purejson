/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class TypeToken<T> {
  
  private final Class<?> rawType;
  private final Type type;
  
  protected TypeToken() {
    Type genericSuperType = getClass().getGenericSuperclass();
    this.type = genericType(genericSuperType);
    this.rawType = rawType(type);
  }
  
  public Class<?> getRawType() {
    return rawType;
  }
  
  public Type getType() {
    return type;
  }
  
  private static Type genericType(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      return parameterizedType.getActualTypeArguments()[0];
    }
    throw new UnsupportedOperationException("not supported " + type.getTypeName());
  }

  private static Class<?> rawType(Type type) {
    if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    }
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    }
    throw new UnsupportedOperationException("not supported " + type.getTypeName());
  }
}
