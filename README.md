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
- cpu: Intel Core i7-1065G7
- ram: 32GB
- cores: 8

```
Performance parse pojo
name  tot   min max mean  p50 p90 p95 p99
refl  29230 10  117 14    11  23  27  82
buil  13499 3   104 6     4   10  11  26
adho  12194 2   105 6     4   8   9   23
gson  7877  1   65  3     3   5   5   16
```

```
Performance serialize pojo
name  tot   min max mean  p50 p90 p95 p99
refl  16256 2   213 3     2   2   2   4
buil  18380 2   213 3     3   3   3   5
adho  16643 2   213 3     2   2   3   5
gson  33139 4   217 6     5   7   7   8
```

```
Performance parse record
name  tot   min max mean  p50 p90 p95 p99
refl  17600 2   210 3     2   2   3   6
buil  18339 2   284 3     2   3   4   6
adho  23875 2   212 4     4   4   4   8
gson  20800 2   211 4     3   3   4   7
```

```
Performance serialize record
name  tot   min max mean  p50 p90 p95 p99
refl  17966 2   233 3     2   4   4   6
buil  18709 2   233 3     2   4   5   6
adho  18750 1   234 3     2   4   4   6
gson  34345 4   237 6     5   7   8   9
```
