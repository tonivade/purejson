# purejson

Pure Functional Json Parser Library.

Built on top of ~~Gson~~ minimal-json parser, it defines an encoder/decoder machinery.

It supports java records and the minimum required java version is Java 17.

## Usage

If you are familiar with Gson, it will be pretty straightforward for you to use this
library.

```java
  record User(int id, String name) {}

  var json = """
     { "id": 1, "name": "toni" }
     """.strip();

  var user = new PureJson<>(User.class).fromJson(json);

  assertEquals(new User(1, "toni"), user);
```

PureJson supports runtime reflection and annotation processors. If an adapter exists for a 
specific type, then the adapter is used instead of reflection.

## Features

### Runtime Reflection

- Java POJOs: with empty constructor and setter/getters. Fields are populated using reflection.
- Java Value Objects: without empty constructor and setters (immutables). Constructor with parameters is used to create new instances.
- Java Records (Java 16+): canonical constructor is used to create new instances.

### Annotation Processor

You can annotate your classes with `@Json` and an adapter for this class will be generated.

- Java Value Objects
- Java Records (Java 16+)

## Performance

Tested on my laptop: 
- cpu: Intel Core i7-1165G7
- ram: 64GB
- cores: 8

```
Performance parse pojo
name  tot   min max mean p50 p90 p95 p99
refl  85617 15  52  17   16  18  20  32
buil  20796 3   36   4    3   4   8  11
adho  17443 2   32   3    3   3   5  9
gson  10485 1   31   2    1   2   4  4
```

```
Performance serialize record
name  tot   min max mean p50 p90 p95 p99
refl  13004 2   20  2    2   2   3   4
buil  13115 2   19  2    2   3   4   4
adho  12195 2   18  2    2   2   3   4
gson  22594 4   21  4    4   5   5   6
```

```
Performance serialize pojo
name  tot   min max mean p50 p90 p95 p99
refl  12229 2   20  2    2   2   4   4
buil  13821 2   20  2    2   3   4   4
adho  12565 2   22  2    2   2   4   4
gson  22084 4   22  4    4   4   5   5
```

```
Performance parse record
name  tot   min max mean p50 p90 p95 p99
refl  12360 1   55  2    2   2   3   5
buil  12654 1   55  2    2   2   4   5
adho  17089 2   56  3    3   3   4   7
gson  10784 1   55  2    1   2   2   4
```
