/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings({"unused", "preview"})
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
    if (type instanceof ParameterizedType p) {
      return p.getActualTypeArguments()[0];
    }
    throw new UnsupportedOperationException("not supported " + type.getTypeName());
  }

  private static Class<?> rawType(Type type) {
    if (type instanceof ParameterizedType p) {
      return rawType(p.getRawType());
    }
    if (type instanceof Class<?> c) {
      return c;
    }
    throw new UnsupportedOperationException("not supported " + type.getTypeName());
  }
}
