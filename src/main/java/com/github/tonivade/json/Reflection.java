/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public abstract class Reflection<T> implements Type {
  
  private final Type param;
  
  protected Reflection() {
    this.param = 
        ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }
  
  @Override
  public String getTypeName() {
    return param.getTypeName();
  }
}
