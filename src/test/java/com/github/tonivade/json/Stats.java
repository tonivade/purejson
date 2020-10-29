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

@SuppressWarnings("preview")
record Stats(String name, Duration total, Duration min, Duration max, Duration mean, 
    Duration p50, Duration p90, Duration p95, Duration p99) {

  public static <T> Stats stats(int times, String name, UIO<T> task) {
    var repeat = task.timed().map(Tuple2::get1).repeat(recursAndCollect(times));

    Duration totalDuration = repeat.unsafeRunSync().reduce(Duration::plus).getOrElseThrow();
    return new Stats(
        name,
        totalDuration, 
        repeat.unsafeRunSync().foldLeft(Duration.ofDays(1), (d1, d2) -> d1.compareTo(d2) > 0 ? d2 : d1), 
        repeat.unsafeRunSync().foldLeft(Duration.ZERO, (d1, d2) -> d1.compareTo(d2) > 0 ? d1 : d2), 
        totalDuration.dividedBy(repeat.unsafeRunSync().size()), 
        percentile(50, repeat.unsafeRunSync()), 
        percentile(90, repeat.unsafeRunSync()), 
        percentile(90, repeat.unsafeRunSync()), 
        percentile(99, repeat.unsafeRunSync()));
  }
  
  private static Duration percentile(double percentile, Sequence<Duration> results) {
    var array = results.asArray().sort(Duration::compareTo);
    
    return array.get((int) Math.round(percentile / 100.0 * (array.size() - 1)));
  }

  private static <T> Schedule<Nothing, T, Sequence<T>> recursAndCollect(int times) {
    return Schedule.<Nothing, T>recurs(times).zipRight(identity()).collectAll();
  }
}
