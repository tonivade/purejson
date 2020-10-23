/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public abstract class Reflection<T> {
  
  private final Type param;
  
  protected Reflection() {
    this.param = 
        ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }
  
  public String getTypeName() {
    return param.getTypeName();
  }
  
  public Type getType() {
    return param;
  }
}
