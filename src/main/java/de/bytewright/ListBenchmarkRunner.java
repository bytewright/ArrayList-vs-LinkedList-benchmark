package de.bytewright;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Main class to run all benchmarks and generate reports
 */
public class ListBenchmarkRunner {

    public static void main(String[] args) {
        try {
            // Create results directory if it doesn't exist
            File resultsDir = new File("benchmark-results");
            if (!resultsDir.exists()) {
                resultsDir.mkdir();
            }

            // Create timestamp for this run
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String resultFile = "benchmark-results/jmh-result-" + timestamp + ".csv";
            String reportFile = "benchmark-results/list-comparison-report-" + timestamp + ".md";

            // Run benchmarks
            runAllBenchmarks(resultFile);

            // Generate report
            BenchmarkReporter.generateReport(resultFile, reportFile);

            System.out.println("Benchmarks completed!");
            System.out.println("Raw results: " + resultFile);
            System.out.println("Report: " + reportFile);

        } catch (Exception e) {
            System.err.println("Error running benchmarks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runAllBenchmarks(String resultFile) throws RunnerException, IOException {
        System.out.println("Starting benchmark run...");

        // Configure benchmark options
        ChainedOptionsBuilder optionsBuilder = new OptionsBuilder()
                .include(ListBenchmark.class.getSimpleName())
                .include(ComplexObjectBenchmark.class.getSimpleName())
                .include(RealisticWorkflowBenchmark.class.getSimpleName())
                // Use these settings for quicker runs during development
                // For actual benchmarking, you might want to increase these values
                .warmupIterations(5)
                .measurementIterations(6)
                .forks(4)
                .threads(8)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.CSV)
                .result(resultFile);

        // Run benchmarks
        Options opt = optionsBuilder.build();
        Collection<RunResult> results = new Runner(opt).run();

        // Print summary to console
        System.out.println("Benchmark run completed with " + results.size() + " results.");
    }
}