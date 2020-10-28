/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.purefun.effect.Schedule.identity;

import java.time.Duration;

import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.Schedule;
import com.github.tonivade.purefun.effect.UIO;

class Stats {

  static <T> void stats(int times, String name, UIO<T> task) {
    var repeat = task.timed().map(Tuple2::get1).repeat(recursAndCollect(times));

    Sequence<Duration> result = repeat.unsafeRunSync();
    
    Duration totalDuration = result.reduce(Duration::plus).getOrElseThrow();
    Duration max = result.foldLeft(Duration.ZERO, (d1, d2) -> d1.compareTo(d2) > 0 ? d1 : d2);
    Duration min = result.foldLeft(Duration.ofDays(1), (d1, d2) -> d1.compareTo(d2) > 0 ? d2 : d1);
    
    System.out.println(name + " total: " + totalDuration.toMillis());
    System.out.println(name + " min: " + min.toMillis());
    System.out.println(name + " max: " + max.toMillis());
    System.out.println(name + " mean: " + totalDuration.dividedBy(result.size()).toMillis());
    System.out.println(name + " p50: " + percentile(50, result));
    System.out.println(name + " p90: " + percentile(90, result));
    System.out.println(name + " p95: " + percentile(95, result));
    System.out.println(name + " p99: " + percentile(99, result));
  }
  
  private static long percentile(double percentile, Sequence<Duration> results) {
    var array = results.asArray().sort(Duration::compareTo);
    
    return array.get((int) Math.round(percentile / 100.0 * (array.size() - 1))).toMillis();
  }

  private static <T> Schedule<Nothing, T, Sequence<T>> recursAndCollect(int times) {
    return Schedule.<Nothing, T>recurs(times).zipRight(identity()).collectAll();
  }
}
