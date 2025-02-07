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
- cpu: Intel Core i7-1065G7 @ 8x 3.9GHz
- ram: 32GB
- cores: 4

```
Performance parse pojo
name  tot   min max mean  p50 p90 p95 p99 rps
refl  37575 5   124 7     6   8   9   25  133
buil  21722 3   108 4     3   4   5   8   230
adho  19488 2   107 3     3   4   5   7   256
gson  13707 1   103 2     2   2   3   6   364
```

```
Performance parse record
name  tot   min max mean  p50 p90 p95 p99 rps
refl  16017 1   174 3     2   3   5   5   312
buil  16382 1   175 3     2   4   5   6   305
adho  18214 1   176 3     2   3   5   6   274
gson  15766 1   172 3     2   4   5   5   317
```

```
Performance serialize pojo
name  tot   min max mean  p50 p90 p95 p99 rps
refl  26114 3   617 5     3   6   7   28  191
buil  26299 2   614 5     3   6   8   21  190
adho  24785 3   152 4     3   6   7   18  201
gson  31217 3   619 6     5   6   7   25  160
```

```
Performance serialize record
name  tot   min max mean  p50 p90 p95 p99 rps
refl  29586 2   361 5     3   7   8   15  168
buil  28702 3   360 5     3   7   7   15  174
adho  28602 3   360 5     3   7   7   14  174
gson  33846 3   360 6     5   7   7   16  147
```
