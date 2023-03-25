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
- Java Records (Java 17+): canonical constructor is used to create new instances.

### Annotation Processor

You can annotate your classes with `@Json` and an adapter for this class will be generated.

- Java Value Objects
- Java Records (Java 17+)

## Performance

Tested on my laptop: 
- cpu: Intel Core i7-1165G7
- ram: 64GB
- cores: 8

```
Performance parse pojo
name  tot   min max mean  p50 p90 p95 p99
refl  60349 9   59  12    11  13  15  23
buil  16858 2   39  3     2   5   5   7
adho  14996 2   38  2     2   4   5   6
gson  10485 1   36  2     1   3   3   4
```

```
Performance parse record
name  tot   min max mean p50 p90 p95 p99
refl  16542 2   50  3    2   5   5   6
buil  16306 2   50  3    2   5   5   6
adho  17670 2   51  3    3   3   5   7
gson  13903 2   50  2    2   3   5   5
```

```
Performance serialize pojo
name  tot   min max mean p50 p90 p95 p99
refl  31824 5   22  6    6   6   8   11
buil  34202 3   25  6    6   10  11  12
adho  32495 4   21  6    6   7   10  11
gson  26533 4   18  5    5   6   6   6
```

```
Performance serialize record
name  tot   min max mean p50 p90 p95 p99
refl  33065 4   23  6    6   8   10  12
buil  33042 4   24  6    6   8   10  12
adho  32888 5   23  6    5   8   10  12
gson  27768 4   25  5    5   6   6   6
```
