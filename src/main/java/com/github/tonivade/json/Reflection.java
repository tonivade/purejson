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
