# purejson

Pure Functional Json Parser Library.

Built on top of ~~Gson~~ minimal-json parser, it defines an encoder/decoder machinery.

It supports java records, but the minimum required version is Java 11 (currently gson
doesn't work with records in java 16).

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
specific type, then the adapter is used instead of use reflection.

## Features

### Runtime Reflection

- Java POJOs: with empty constructor and setter/getters. Fields are populated using reflection.
- Java Value Objects: without empty constructor and setters (immutables). Constructor with parameters is used to create new instances.
- Java Records (Java 16): canonical constructor is used to create new instances.

### Annotation Processor

You can annotate your classes with `@Json` and an adapter for this class will be generated.

- Java Value Objects
- Java Recrods (Java 16)

## Performance

```
Performance parse
name  tot  min  max mean  p50 p90 p95  p99
refl  11680 14  510   23   15  38  38  204
buil  7982  13  102   15   14  18  18   62
adho  6630  12   22   13   12  13  13   21
gson  4016   6   99    8   7    9   9   21
```

```
Performance serialize
name  tot  min  max mean  p50 p90 p95  p99
refl  9793  13  265   19   14  21  21  111
buil  7210  13   37   14   13  14  14   23
adho  6940  13   46   13   13  13  13   19
gson  4853   8   72    9    9   9   9   26
```
