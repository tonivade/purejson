/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.util.List;

@SuppressWarnings("preview")
public class ExampleAdapterTest {
  
  record Role(String name) {}
  record User(int id, String name, List<Role> roles) {}

  public enum UserAdapter implements JsonAdapter<User> {
    INSTANCE;

    private static final JsonAdapter<Integer> ID_ADAPTER = JsonAdapter.adapter(int.class);
    private static final JsonAdapter<String> NAME_ADAPTER = JsonAdapter.adapter(String.class);
    private static final JsonAdapter<List<Role>> ROLES_ADAPTER = JsonAdapter.adapter(new TypeToken<List<Role>>(){}.getType());

    @Override
    public JsonNode encode(JsonContext context, User value) {
      var id = JsonDSL.entry("id", ID_ADAPTER.encode(context, value.id()));
      var name = JsonDSL.entry("name", NAME_ADAPTER.encode(context, value.name()));
      var roles = JsonDSL.entry("roles", ROLES_ADAPTER.encode(context, value.roles()));
      return JsonDSL.object(id, name, roles);
    }

    @Override
    public User decode(JsonContext context, JsonNode node) {
      var object = node.asObject();
      var id = ID_ADAPTER.decode(context, object.get("id"));
      var name = NAME_ADAPTER.decode(context, object.get("name"));
      var roles = ROLES_ADAPTER.decode(context, object.get("roles"));
      return new User(id, name, roles);
    }
  }

  @Test
  void testAdapter() {
    User user1 = new User(1, "toni", List.of(new Role("admin")));
    User user2 = new User(1, "toni", List.of(new Role("admin")));
    User user3 = new User(1, null, null);

    JsonAdapter<User> adapter = JsonAdapter.adapter(User.class);
    
    PureJson json = new PureJson();

    assertEquals(user1, adapter.decode(json, adapter.encode(json, user1)));
    assertEquals(user2, adapter.decode(json, adapter.encode(json, user2)));
    assertEquals(user3, adapter.decode(json, adapter.encode(json, user3)));
    assertNull(adapter.decode(json, adapter.encode(json, null)));
  }
}
