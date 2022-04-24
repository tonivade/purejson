/*
 * Copyright (c) 2020-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import javax.tools.JavaFileObject;

import org.junit.jupiter.api.Test;

class JsonAnnotationProcessorTest {

  @Test
  void recordTest() {
    JavaFileObject file = forSourceLines("test.User",
        """
            package test;

            import com.github.tonivade.purejson.Json;
            import java.util.List;

            @Json
            public record User(int id, String name, List<String> roles) {}""");

    JavaFileObject expected = forSourceLines("test.UserAdapter",
        """
            package test;

            import com.github.tonivade.purejson.JsonAdapter;
            import com.github.tonivade.purejson.JsonDSL;
            import com.github.tonivade.purejson.JsonNode;
            import com.github.tonivade.purejson.TypeToken;
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import java.util.List;

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
            }""");

    assert_().about(javaSource()).that(file)
        .processedWith(new JsonAnnotationProcessor())
        .compilesWithoutError().and().generatesSources(expected);
  }

  @Test
  void withCustomAdapter() {
    JavaFileObject file = forSourceLines("test.User",
        """
            package test;

            import com.github.tonivade.purejson.Json;
            import java.util.List;

            @Json(UserAdapter.class)
            public record User(int id, String name, List<String> roles) {}

            class UserAdapter {}""");

    assert_().about(javaSource()).that(file)
        .processedWith(new JsonAnnotationProcessor())
        .compilesWithoutError();
  }

  @Test
  void pojoTest() {
    JavaFileObject file = forSourceLines("test.User",
        """
            package test;

            import com.github.tonivade.purejson.Json;
            import java.util.List;

            @Json
            public final class User {

              private final int id;
              private final String name;
              private final List<String> roles;

              public User(int id, String name, List<String> roles) {
                this.id = id;
                this.name = name;
                this.roles = roles;
              }

              public int getId() { return id; }

              public String getName() { return name; }

              public List<String> getRoles() { return roles; }
            }""");

    JavaFileObject expected = forSourceLines("test.UserAdapter",
        """
            package test;

            import com.github.tonivade.purejson.JsonAdapter;
            import com.github.tonivade.purejson.JsonDSL;
            import com.github.tonivade.purejson.JsonNode;
            import com.github.tonivade.purejson.TypeToken;
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import java.util.List;

            public enum UserAdapter implements JsonAdapter<User> {

              INSTANCE;

              private static final JsonAdapter<Integer> ID_ADAPTER = JsonAdapter.adapter(int.class);
              private static final JsonAdapter<String> NAME_ADAPTER = JsonAdapter.adapter(String.class);
              private static final JsonAdapter<List<String>> ROLES_ADAPTER = JsonAdapter.adapter(new TypeToken<List<String>>(){}.getType());

              @Override
              public JsonNode encode(User value) {
                var id = JsonDSL.entry("id", ID_ADAPTER.encode(value.getId()));
                var name = JsonDSL.entry("name", NAME_ADAPTER.encode(value.getName()));
                var roles = JsonDSL.entry("roles", ROLES_ADAPTER.encode(value.getRoles()));
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
            }""");

    assert_().about(javaSource()).that(file)
        .processedWith(new JsonAnnotationProcessor())
        .compilesWithoutError().and().generatesSources(expected);
  }

  @Test
  void pojoTestNoConstructor() {
    JavaFileObject file = forSourceLines("test.User",
        """
            package test;

            import com.github.tonivade.purejson.Json;
            import java.util.List;

            @Json
            public final class User {

              private final int id;
              private final String name;
              private final List<String> roles;

              public User() { }

              public int getId() { return id; }

              public String getName() { return name; }

              public List<String> getRoles() { return roles; }
            }""");

    assert_().about(javaSource()).that(file)
        .processedWith(new JsonAnnotationProcessor())
        .failsToCompile();
  }

  @Test
  void pojoTestNoAccessor() {
    JavaFileObject file = forSourceLines("test.User",
        """
            package test;

            import com.github.tonivade.purejson.Json;
            import java.util.List;

            @Json
            public final class User {

              private final int id;
              private final String name;
              private final List<String> roles;

              public User(int id, String name, List<String> roles) {
                this.id = id;
                this.name = name;
                this.roles = roles;
              }
            }""");

    assert_().about(javaSource()).that(file)
        .processedWith(new JsonAnnotationProcessor())
        .failsToCompile();
  }

  @Test
  void notSupported() {
    JavaFileObject file = forSourceLines("test.User",
        """
            package test;

            import com.github.tonivade.purejson.Json;

            @Json
            public interface User {}""");

    assert_().about(javaSource()).that(file)
        .processedWith(new JsonAnnotationProcessor())
        .failsToCompile();
  }
}
