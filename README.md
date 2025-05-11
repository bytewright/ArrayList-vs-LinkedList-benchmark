# ArrayList vs LinkedList Benchmark
Test repo to compare throughput of ArrayLists vs LinkedList

This project provides comprehensive benchmarks to compare the performance of Java's ArrayList and LinkedList implementations across various operations and scenarios.

## Project Overview

This benchmark suite tests the following aspects:

1. **Basic Operations**
    - Insertion (beginning, middle, end)
    - Removal (beginning, middle, end)
    - Iteration
    - Random access

2. **Complex Object Handling**
    - Working with complex objects instead of primitives
    - Finding elements by property
    - Filtering and transforming

3. **Realistic Workflows**
    - Data processing pipelines
    - Batch updates with removals
    - Stream operations

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Building the Project

```bash
mvn clean package
```

This will create an executable JAR file `list-benchmark.jar` in the `target` directory.

### Running the Benchmarks

```bash
java -jar target/list-benchmark.jar
```

This will:
1. Run all benchmarks
2. Generate CSV files with raw results in the `benchmark-results` directory
3. Generate a markdown report with comparisons and analysis

For more accurate benchmarks (but longer execution time), you can modify the benchmark parameters in `ListBenchmarkRunner.java`:

```java
.warmupIterations(5)  // Increase for more stable results
.measurementIterations(10)
.forks(2)
```

## Benchmark Methodology

The benchmarks use JMH (Java Microbenchmark Harness) to ensure:

- Proper JVM warm-up
- Multiple measurement iterations
- Multiple JVM forks to avoid profile-guided optimizations
- Dead code elimination prevention
- Realistic data sets

## Interpreting Results

The generated report includes:

- Performance comparison tables for each operation
- Size-based analysis (how performance scales with collection size)
- Summary of operations where each list type excels
- Percentage differences to quantify the advantages

A negative percentage in the "Difference" column means LinkedList performed better, while a positive percentage means ArrayList performed better.

## Customizing Benchmarks

You can modify the benchmark parameters in each test class:

- `@Param`: To change the collection sizes
- `@Warmup`: To adjust warm-up iterations
- `@Measurement`: To adjust measurement iterations
- `@Fork`: To adjust the number of JVM forks

## Example Report

The report will look similar to this:

```
# ArrayList vs LinkedList Performance Comparison

## ListBenchmark

### Append

| Size | ArrayList | LinkedList | Difference | Winner |
|------|-----------|------------|------------|--------|
| 100  | 0.52 us   | 0.68 us    | 30.77%     | ArrayList |
| 1000 | 0.78 us   | 0.71 us    | 8.97%      | LinkedList |
...
```

## License

This project is open source and available under the MIT License.