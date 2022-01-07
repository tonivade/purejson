/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeToken<T> {
  
  private final Class<? super T> rawType;
  private final Type type;
  
  protected TypeToken() {
    this.type = genericType(getClass().getGenericSuperclass());
    this.rawType = rawType(type);
  }
  
  public Class<? super T> getRawType() {
    return rawType;
  }
  
  public Type getType() {
    return type;
  }
  
  private static Type genericType(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) type;
      return p.getActualTypeArguments()[0];
    }
    throw new UnsupportedOperationException("type not supported " + type.getTypeName());
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<? super T> rawType(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) type;
      return rawType(p.getRawType());
    }
    if (type instanceof Class<?>) {
      Class<?> c = (Class<?>) type;
      return (Class<? super T>) c;
    }
    throw new UnsupportedOperationException("type not supported " + type.getTypeName());
  }
}
