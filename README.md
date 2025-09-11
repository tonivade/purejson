# purejson

Pure Functional Json Parser Library.

Built on top of [minimal-json](https://github.com/tonivade/minimal-json) parser, it defines an encoder/decoder machinery.

It supports java records and the minimum required java version is Java 21.

## Usage

If you are familiar with Gson, it will be pretty straightforward for you to use this
library.

```java
  record User(int id, String name) {}

  var json = """
    { 
      "id": 1, 
      "name": "toni" 
    }""";

  var user = new PureJson<User>().fromJson(json);

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
name  tot	        min     max       mean    p50     p90     p95     p99      rps
refl	34134667821 5095046 106700365 6826933 6146382 6881559 7863734 22812745 146
buil	19284790789 2843116 103816200 3856958 3202927 3878235 4522538 7356144  259
anno	17259201156 2415316 103940391 3451840 2832043 3433976 4024925 6574702  289
gson	12650463617 1835867	97242144  2530092 2117968 2442139 2776042 4853924  395
```

```
Performance parse record
name  tot	        min     max       mean    p50     p90     p95     p99     rps
refl  18285321842 2258864 165923995 3657064 2612503 5474232 6144933 7096856 273
buil  17492340483 2320340 212767711 3498468 2632877 3831916 5741407 6684536 285
anno  20553653778 2301261 166565596 4110730 3158407 5984022 6830097 7909004 243
gson  17844532945 2288693 165625520 3568906 2615751 4772828 5758663 6632191 280
```

```
Performance serialize pojo
name  tot	        min     max       mean    p50     p90     p95     p99     rps
refl  7037073920  3474481 152323255 5407414 4104274 7370875 8258107 9935941 184
buil  27831064153 3298775 153296005 5566212 4214510 7764021 8566194 9995677 179
anno  26499119687 3476054 155194968 5299823 3965655 7423811 8174893 9489636 188
gson  13068220588 1744770 136853263 2613644 1959073 3741622 3986688 4298267 382
```

```
Performance serialize record
name  tot	        min     max       mean    p50     p90     p95     p99     rps
refl  24695419584 3078750 133890565 4939083 4011554 4748900 5340099 8269929 202
buil  23484748117 3310479 134497319 4696949 3805725 4493878 5001936 7749564 212
anno  23461111391 3293238 133091474 4692222 3754019 4592889 5120672 7544880 213
gson  11629669510 1603659 130652273 2325933 1842801 2102938 2377305 3753296 429
```
