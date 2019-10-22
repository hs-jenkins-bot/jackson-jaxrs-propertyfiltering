package com.hubspot.jackson.jaxrs.benchmark;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.hubspot.jackson.jaxrs.PropertyFilter;
import com.hubspot.jackson.jaxrs.PropertyFilteringJsonGenerator;

public class PropertyFilterBenchmark {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final PropertyFilter PROPERTY_FILTER = new PropertyFilter(Arrays.asList("id", "name"));
  private static final List<TestPojo> VALUES = generateTestData(1_000, 3);
  private static final OutputStream OUTPUT_STREAM = new OutputStream() {

    @Override
    public void write(int b) {}

    @Override
    public void write(byte[] b) {}

    @Override
    public void write(byte[] b, int off, int len) {}
  };

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public ObjectWriter baseline() throws IOException {
    ObjectWriter writer = MAPPER.writer();

    JsonGenerator generator = writer.getFactory().createGenerator(OUTPUT_STREAM);
    generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

    boolean ok = false;

    try {
      writer.writeValue(generator, VALUES);
      ok = true;

      return writer;
    } finally {
      if (ok) {
        generator.close();
      } else {
        try {
          generator.close();
        } catch (Exception ignored) {}
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public ObjectWriter filterStreaming() throws IOException {
    ObjectWriter writer = MAPPER.writer();

    JsonGenerator generator = writer.getFactory().createGenerator(OUTPUT_STREAM);
    generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    generator = new PropertyFilteringJsonGenerator(generator, PROPERTY_FILTER);

    boolean ok = false;

    try {
      writer.writeValue(generator, VALUES);
      ok = true;

      return writer;
    } finally {
      if (ok) {
        generator.close();
      } else {
        try {
          generator.close();
        } catch (Exception ignored) {}
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public ObjectWriter filterNonStreaming() throws IOException {
    ObjectWriter writer = MAPPER.writer();

    JsonNode tree = valueToTree(MAPPER, writer, VALUES);
    PROPERTY_FILTER.filter(tree);
    JsonGenerator generator = writer.getFactory().createGenerator(OUTPUT_STREAM);
    generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

    boolean ok = false;

    try {
      writer.writeValue(generator, tree);
      ok = true;

      return writer;
    } finally {
      if (ok) {
        generator.close();
      } else {
        try {
          generator.close();
        } catch (Exception ignored) {}
      }
    }
  }

  private JsonNode valueToTree(ObjectMapper mapper, ObjectWriter writer, Object o) {
    if (o == null) {
      return null;
    }

    TokenBuffer buf = new TokenBuffer(mapper, false);
    JsonNode result;
    try {
      writer.writeValue(buf, o);
      JsonParser jp = buf.asParser();
      result = mapper.readTree(jp);
      jp.close();
    } catch (IOException e) { // should not occur, no real i/o...
      throw new IllegalArgumentException(e.getMessage(), e);
    }

    return result;
  }

  private static List<TestPojo> generateTestData(int count, int nesting) {
    List<TestPojo> values = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      TestPojo pojo = new TestPojo()
          .setId(ThreadLocalRandom.current().nextInt())
          .setName(randomString(16))
          .setField3(randomString(16))
          .setField4(randomString(16))
          .setField5(randomString(16))
          .setField6(randomString(16))
          .setField7(randomString(16))
          .setField8(randomString(16))
          .setField9(randomString(16))
          .setField10(randomString(16))
          .setField11(randomString(16))
          .setField12(randomString(16))
          .setField13(randomString(16))
          .setField14(randomString(16))
          .setField15(randomString(16));

      if (nesting > 0) {
        TestPojo nested = generateTestData(1, nesting - 1).get(0);
        pojo.setNested(nested);
      }

      values.add(pojo);
    }

    return values;
  }

  private static String randomString(int length) {
    StringBuilder name = new StringBuilder();

    String availableChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    for (int i = 0; i < length; i++) {
      int charIndex = ThreadLocalRandom.current().nextInt(availableChars.length());
      name.append(availableChars.charAt(charIndex));
    }

    return name.toString();
  }
}
