# purejson

Pure Functional Json Parser Library.

Built on top of Gson parser, it defines an alternative encoder/decoder machinery.

It supports java records, so the minimum required version is Java 15. (currently gson
doesn't work with records in java 15).

## Usage

If you are familiar with Gson, it will be pretty straightforward for you to use this
library.

```java
  record User(int id, String name) {}

  var json = """
     { "id": 1, "name": "toni" }
     """.strip();

  var user = new Json().fromJson(json, User.class);

  assertEquals(new User(1, "toni"), user);
```

## Performance

```
Performance parse
name	tot	min	max	mean	p50	p90	p95	p99
refl	8963	12	22	17	12	12	12	18
cach	6389	11	19	12	12	12	12	18
buil	5975	10	18	11	10	10	10	15
expl	4716	8	17	9	9	9	9	16
gson	4113	6	11	8	6	6	6	7
```

```
Performance serialize
name	tot	min	max	mean	p50	p90	p95	p99
refl	7548	11	17	15	11	11	11	16
cach	5708	11	17	11	11	11	11	16
buil	5801	11	17	11	11	11	11	16
expl	5239	10	20	10	10	10	10	16
gson	5046	9	12	10	9	9	9	9

```