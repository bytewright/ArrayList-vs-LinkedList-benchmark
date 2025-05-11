package de.bytewright;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ComplexObjectBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private List<TestObject> arrayList;
    private List<TestObject> linkedList;
    private Random random;
    private TestObject newObject;

    public static class TestObject {
        private final int id;
        private final String value;
        private final double score;

        public TestObject(int id, String value, double score) {
            this.id = id;
            this.value = value;
            this.score = score;
        }

        public int getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public double getScore() {
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return id == that.id &&
                    Double.compare(that.score, score) == 0 &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value, score);
        }
    }

    @Setup
    public void setup() {
        arrayList = new ArrayList<>(size);
        linkedList = new LinkedList<>();
        random = new Random(42); // Fixed seed for reproducibility

        // Create complex objects
        for (int i = 0; i < size; i++) {
            TestObject obj = new TestObject(
                    i,
                    "Value-" + random.nextInt(1000),
                    random.nextDouble() * 100
            );
            arrayList.add(obj);
            linkedList.add(obj);
        }

        newObject = new TestObject(
                size + 1,
                "NewValue",
                random.nextDouble() * 100
        );
    }

    // APPEND OPERATIONS WITH COMPLEX OBJECTS

    @Benchmark
    public List<TestObject> appendToArrayList() {
        List<TestObject> list = new ArrayList<>(arrayList);
        list.add(new TestObject(
                random.nextInt(),
                "Value-" + random.nextInt(1000),
                random.nextDouble() * 100
        ));
        return list;
    }

    @Benchmark
    public List<TestObject> appendToLinkedList() {
        List<TestObject> list = new LinkedList<>(linkedList);
        list.add(new TestObject(
                random.nextInt(),
                "Value-" + random.nextInt(1000),
                random.nextDouble() * 100
        ));
        return list;
    }

    // ITERATION OPERATIONS

    @Benchmark
    public void iterateArrayList(Blackhole blackhole) {
        for (TestObject obj : arrayList) {
            blackhole.consume(obj.getId());
            blackhole.consume(obj.getValue());
            blackhole.consume(obj.getScore());
        }
    }

    @Benchmark
    public void iterateLinkedList(Blackhole blackhole) {
        for (TestObject obj : linkedList) {
            blackhole.consume(obj.getId());
            blackhole.consume(obj.getValue());
            blackhole.consume(obj.getScore());
        }
    }

    // FIND OPERATIONS

    @Benchmark
    public TestObject findByPropertyArrayList() {
        String targetValue = "Value-500";
        for (TestObject obj : arrayList) {
            if (targetValue.equals(obj.getValue())) {
                return obj;
            }
        }
        return null;
    }

    @Benchmark
    public TestObject findByPropertyLinkedList() {
        String targetValue = "Value-500";
        for (TestObject obj : linkedList) {
            if (targetValue.equals(obj.getValue())) {
                return obj;
            }
        }
        return null;
    }

    // STREAM OPERATIONS

    @Benchmark
    public List<TestObject> streamFilterArrayList() {
        return arrayList.stream()
                .filter(obj -> obj.getScore() > 50)
                .collect(Collectors.toList());
    }

    @Benchmark
    public List<TestObject> streamFilterLinkedList() {
        return linkedList.stream()
                .filter(obj -> obj.getScore() > 50)
                .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> streamMapArrayList() {
        return arrayList.stream()
                .map(TestObject::getValue)
                .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> streamMapLinkedList() {
        return linkedList.stream()
                .map(TestObject::getValue)
                .collect(Collectors.toList());
    }

    // REMOVAL OPERATIONS

    @Benchmark
    public List<TestObject> removeByPredicateArrayList() {
        List<TestObject> list = new ArrayList<>(arrayList);
        Predicate<TestObject> condition = obj -> obj.getScore() < 30;
        list.removeIf(condition);
        return list;
    }

    @Benchmark
    public List<TestObject> removeByPredicateLinkedList() {
        List<TestObject> list = new LinkedList<>(linkedList);
        Predicate<TestObject> condition = obj -> obj.getScore() < 30;
        list.removeIf(condition);
        return list;
    }

    // REAL-WORLD SCENARIO: PROCESS & FILTER

    @Benchmark
    public double complexProcessingArrayList() {
        return arrayList.stream()
                .filter(obj -> obj.getId() % 2 == 0)
                .filter(obj -> obj.getValue().contains("5"))
                .mapToDouble(TestObject::getScore)
                .average()
                .orElse(0);
    }

    @Benchmark
    public double complexProcessingLinkedList() {
        return linkedList.stream()
                .filter(obj -> obj.getId() % 2 == 0)
                .filter(obj -> obj.getValue().contains("5"))
                .mapToDouble(TestObject::getScore)
                .average()
                .orElse(0);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ComplexObjectBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}