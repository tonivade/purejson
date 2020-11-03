/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.util.List;

@SuppressWarnings("preview")
public class ExampleAdapterTest {
  
  record User(int id, String name, List<String> roles) {}

  public enum UserAdapter implements JsonAdapter<User> {
    INSTANCE;

    private static final JsonAdapter<Integer> ID_ADAPTER = JsonAdapter.adapter(int.class);
    private static final JsonAdapter<String> NAME_ADAPTER = JsonAdapter.adapter(String.class);
    private static final JsonAdapter<List<String>> ROLES_ADAPTER = JsonAdapter.adapter(new TypeToken<List<String>>(){}.getType());

    @Override
    public JsonNode encode(User value) {
      var id = JsonDSL.entry("id", ID_ADAPTER.encode(value.id()));
      var name = JsonDSL.entry("name", NAME_ADAPTER.encode(value.name()));
      var roles = JsonDSL.entry("roles", ROLES_ADAPTER.encode(value.roles()));
      return JsonDSL.object(id, name, roles);
    }

    @Override
    public User decode(JsonNode node) {
      var object = node.asObject();
      var id = ID_ADAPTER.decode(object.get("id"));
      var name = NAME_ADAPTER.decode(object.get("name"));
      var roles = ROLES_ADAPTER.decode(object.get("roles"));
      return new User(id, name, roles);
    }
  }

  @Test
  void testAdapter() {
    User user = new User(1, "toni", List.of("admin"));

    JsonAdapter<User> adapter = JsonAdapter.adapter(User.class);

    assertEquals(user, adapter.decode(adapter.encode(user)));
  }
}
