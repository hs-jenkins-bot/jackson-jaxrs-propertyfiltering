package com.hubspot.jackson.jaxrs.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class BenchmarkRunner {

  public static void main(String... args) throws Exception {
    Options options = new OptionsBuilder()
        .timeUnit(TimeUnit.MILLISECONDS)
        .warmupTime(TimeValue.seconds(5))
        .warmupIterations(2)
        .measurementTime(TimeValue.seconds(10))
        .measurementIterations(3)
        .threads(5)
        .forks(1)
        .resultFormat(ResultFormatType.CSV)
        .addProfiler(GCProfiler.class)
        .build();

    new Runner(options).run();
  }
}
