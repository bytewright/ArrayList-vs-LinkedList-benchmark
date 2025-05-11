package de.bytewright;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class RealisticWorkflowBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private List<DataRecord> sourceData;
    private Random random;

    public static class DataRecord {
        private final int id;
        private final String category;
        private final double value;
        private final boolean active;

        public DataRecord(int id, String category, double value, boolean active) {
            this.id = id;
            this.category = category;
            this.value = value;
            this.active = active;
        }

        public int getId() {
            return id;
        }

        public String getCategory() {
            return category;
        }

        public double getValue() {
            return value;
        }

        public boolean isActive() {
            return active;
        }
    }

    @Setup
    public void setup() {
        random = new Random(42); // Fixed seed for reproducibility

        String[] categories = {"A", "B", "C", "D", "E"};
        sourceData = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            sourceData.add(new DataRecord(
                    i,
                    categories[random.nextInt(categories.length)],
                    random.nextDouble() * 1000,
                    random.nextBoolean()
            ));
        }
    }

    // SCENARIO 1: DATA PROCESSING PIPELINE
    // Filter -> Transform -> Aggregate

    @Benchmark
    public Map<String, Double> dataProcessingWithArrayList() {
        List<DataRecord> data = new ArrayList<>(sourceData);

        // Step 1: Filter active records
        List<DataRecord> activeRecords = new ArrayList<>();
        for (DataRecord record : data) {
            if (record.isActive()) {
                activeRecords.add(record);
            }
        }

        // Step 2: Transform data
        List<DataRecord> transformedRecords = new ArrayList<>();
        for (DataRecord record : activeRecords) {
            if (record.getValue() > 200) {
                transformedRecords.add(new DataRecord(
                        record.getId(),
                        record.getCategory(),
                        record.getValue() * 1.1, // Apply 10% increase
                        record.isActive()
                ));
            } else {
                transformedRecords.add(record);
            }
        }

        // Step 3: Aggregate by category
        Map<String, Double> result = new HashMap<>();
        for (DataRecord record : transformedRecords) {
            String category = record.getCategory();
            double currentSum = result.getOrDefault(category, 0.0);
            result.put(category, currentSum + record.getValue());
        }

        return result;
    }

    @Benchmark
    public Map<String, Double> dataProcessingWithLinkedList() {
        List<DataRecord> data = new LinkedList<>(sourceData);

        // Step 1: Filter active records
        List<DataRecord> activeRecords = new LinkedList<>();
        for (DataRecord record : data) {
            if (record.isActive()) {
                activeRecords.add(record);
            }
        }

        // Step 2: Transform data
        List<DataRecord> transformedRecords = new LinkedList<>();
        for (DataRecord record : activeRecords) {
            if (record.getValue() > 200) {
                transformedRecords.add(new DataRecord(
                        record.getId(),
                        record.getCategory(),
                        record.getValue() * 1.1, // Apply 10% increase
                        record.isActive()
                ));
            } else {
                transformedRecords.add(record);
            }
        }

        // Step 3: Aggregate by category
        Map<String, Double> result = new HashMap<>();
        for (DataRecord record : transformedRecords) {
            String category = record.getCategory();
            double currentSum = result.getOrDefault(category, 0.0);
            result.put(category, currentSum + record.getValue());
        }

        return result;
    }

    // SCENARIO 2: STREAM DATA PROCESSING PIPELINE

    @Benchmark
    public Map<String, Double> streamProcessingWithArrayList() {
        List<DataRecord> data = new ArrayList<>(sourceData);

        return data.stream()
                .filter(DataRecord::isActive)
                .map(record -> record.getValue() > 200
                        ? new DataRecord(record.getId(), record.getCategory(), record.getValue() * 1.1, record.isActive())
                        : record)
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.summingDouble(DataRecord::getValue)
                ));
    }

    @Benchmark
    public Map<String, Double> streamProcessingWithLinkedList() {
        List<DataRecord> data = new LinkedList<>(sourceData);

        return data.stream()
                .filter(DataRecord::isActive)
                .map(record -> record.getValue() > 200
                        ? new DataRecord(record.getId(), record.getCategory(), record.getValue() * 1.1, record.isActive())
                        : record)
                .collect(Collectors.groupingBy(
                        DataRecord::getCategory,
                        Collectors.summingDouble(DataRecord::getValue)
                ));
    }

    // SCENARIO 3: BATCH UPDATE WITH REMOVAL

    @Benchmark
    public List<DataRecord> batchUpdateWithArrayList() {
        List<DataRecord> data = new ArrayList<>(sourceData);
        List<DataRecord> result = new ArrayList<>(data.size());

        // Process records in batch, removing some and updating others
        Iterator<DataRecord> iterator = data.iterator();
        while (iterator.hasNext()) {
            DataRecord record = iterator.next();

            // Remove records with low values
            if (record.getValue() < 100) {
                iterator.remove();
                continue;
            }

            // Process and add to result
            if (record.isActive() && "A".equals(record.getCategory())) {
                result.add(new DataRecord(
                        record.getId(),
                        "Premium-" + record.getCategory(),
                        record.getValue() * 1.2,
                        true
                ));
            } else if (!record.isActive() && record.getValue() > 500) {
                result.add(new DataRecord(
                        record.getId(),
                        record.getCategory(),
                        record.getValue(),
                        true // Activate high-value inactive records
                ));
            } else {
                result.add(record);
            }
        }

        return result;
    }

    @Benchmark
    public List<DataRecord> batchUpdateWithLinkedList() {
        List<DataRecord> data = new LinkedList<>(sourceData);
        List<DataRecord> result = new LinkedList<>();

        // Process records in batch, removing some and updating others
        Iterator<DataRecord> iterator = data.iterator();
        while (iterator.hasNext()) {
            DataRecord record = iterator.next();

            // Remove records with low values
            if (record.getValue() < 100) {
                iterator.remove();
                continue;
            }

            // Process and add to result
            if (record.isActive() && "A".equals(record.getCategory())) {
                result.add(new DataRecord(
                        record.getId(),
                        "Premium-" + record.getCategory(),
                        record.getValue() * 1.2,
                        true
                ));
            } else if (!record.isActive() && record.getValue() > 500) {
                result.add(new DataRecord(
                        record.getId(),
                        record.getCategory(),
                        record.getValue(),
                        true // Activate high-value inactive records
                ));
            } else {
                result.add(record);
            }
        }

        return result;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RealisticWorkflowBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}