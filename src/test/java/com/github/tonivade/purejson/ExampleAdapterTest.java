/*
 * Copyright (c) 2020-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purejson.JsonAdapter.adapter;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ExampleAdapterTest {
  
  @Json(RoleAdapter.class)
  record Role(String name) {}
  @Json(UserAdapter.class)
  record User(int id, String name, List<Role> roles) {}
  
  public enum RoleAdapter implements JsonAdapter<Role> {
    INSTANCE;

    private static final JsonAdapter<String> NAME_ADAPTER = JsonAdapter.<String>adapter();
    
    @Override
    public JsonNode encode(Role value) {
      return NAME_ADAPTER.encode(value.name());
    }
    
    @Override
    public Role decode(JsonNode json) {
      return new Role(NAME_ADAPTER.decode(json));
    }
  }

  public enum UserAdapter implements JsonAdapter<User> {
    INSTANCE;

    private static final JsonAdapter<Integer> ID_ADAPTER = JsonAdapter.<Integer>adapter();
    private static final JsonAdapter<String> NAME_ADAPTER = JsonAdapter.<String>adapter();
    private static final JsonAdapter<List<Role>> ROLES_ADAPTER = adapter(new TypeToken<List<Role>>(){}.getType());

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
    User user1 = new User(1, "toni", List.of(new Role("admin")));
    User user2 = new User(1, "toni", List.of(new Role("admin")));
    User user3 = new User(1, null, null);

    JsonAdapter<User> adapter = JsonAdapter.adapter(User.class);

    assertEquals(user1, adapter.decode(adapter.encode(user1)));
    assertEquals(user2, adapter.decode(adapter.encode(user2)));
    assertEquals(user3, adapter.decode(adapter.encode(user3)));
    assertNull(adapter.decode(adapter.encode(null)));
  }
}
