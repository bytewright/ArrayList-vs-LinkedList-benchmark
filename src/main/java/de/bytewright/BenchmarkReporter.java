package de.bytewright;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Utility class to parse and format JMH benchmark results in more readable formats
 */
public class BenchmarkReporter {

    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "([\\w\\.]+)\\.(\\w+)(\\s+)([\\w]+)(\\s+)(\\d+)(\\s+)(\\d+\\.\\d+)(\\s+±\\s+)(\\d+\\.\\d+)(\\s+)(\\w+/op)"
    );

    private static class BenchmarkResult implements Comparable<BenchmarkResult> {
        String benchmarkClass;
        String operation;
        int paramValue;
        double score;
        double error;
        String units;
        boolean isArrayList;

        @Override
        public int compareTo(BenchmarkResult other) {
            int classCompare = this.benchmarkClass.compareTo(other.benchmarkClass);
            if (classCompare != 0) return classCompare;

            int operationBaseCompare = getOperationBase().compareTo(other.getOperationBase());
            if (operationBaseCompare != 0) return operationBaseCompare;

            int paramCompare = Integer.compare(this.paramValue, other.paramValue);
            if (paramCompare != 0) return paramCompare;

            return Boolean.compare(this.isArrayList, other.isArrayList);
        }

        private String getOperationBase() {
            // Strip "ArrayList" or "LinkedList" from operation name
            return operation.replace("ArrayList", "").replace("LinkedList", "");
        }
    }

    public static void generateReport(String jmhResultsFile, String outputFile) throws IOException {
        List<BenchmarkResult> results = parseResults(jmhResultsFile);
        generateComparisonReport(results, outputFile);
    }

    private static List<BenchmarkResult> parseResults(String jmhResultsFile) throws IOException {
        List<BenchmarkResult> results = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(jmhResultsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = RESULT_PATTERN.matcher(line);
                if (matcher.find()) {
                    BenchmarkResult result = new BenchmarkResult();
                    result.benchmarkClass = matcher.group(1);
                    result.operation = matcher.group(2);
                    result.paramValue = Integer.parseInt(matcher.group(6));
                    result.score = Double.parseDouble(matcher.group(8));
                    result.error = Double.parseDouble(matcher.group(10));
                    result.units = matcher.group(12);
                    result.isArrayList = result.operation.contains("ArrayList");

                    results.add(result);
                }
            }
        }

        Collections.sort(results);
        return results;
    }

    private static void generateComparisonReport(List<BenchmarkResult> results, String outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("# ArrayList vs LinkedList Performance Comparison");
            writer.println("\nThis report compares the performance of ArrayList and LinkedList across various operations and data sizes.\n");

            Map<String, List<BenchmarkResult>> resultsByClass = groupResultsByClass(results);

            for (Map.Entry<String, List<BenchmarkResult>> entry : resultsByClass.entrySet()) {
                String benchmarkClass = entry.getKey();
                List<BenchmarkResult> classResults = entry.getValue();

                writer.println("## " + getSimpleClassName(benchmarkClass));
                writer.println();

                Map<String, List<BenchmarkResult>> resultsByOperation = groupResultsByOperationBase(classResults);

                for (Map.Entry<String, List<BenchmarkResult>> opEntry : resultsByOperation.entrySet()) {
                    String operationBase = opEntry.getKey();
                    List<BenchmarkResult> opResults = opEntry.getValue();

                    writer.println("### " + formatOperationName(operationBase));
                    writer.println();
                    writer.println("| Size | ArrayList | LinkedList | Difference | Winner |");
                    writer.println("|------|-----------|------------|------------|--------|");

                    Map<Integer, List<BenchmarkResult>> resultsBySize = groupResultsBySize(opResults);

                    for (Map.Entry<Integer, List<BenchmarkResult>> sizeEntry : resultsBySize.entrySet()) {
                        int size = sizeEntry.getKey();
                        List<BenchmarkResult> sizeResults = sizeEntry.getValue();

                        if (sizeResults.size() != 2) continue; // Skip if we don't have both ArrayList and LinkedList

                        BenchmarkResult arrayListResult = null;
                        BenchmarkResult linkedListResult = null;

                        for (BenchmarkResult r : sizeResults) {
                            if (r.isArrayList) {
                                arrayListResult = r;
                            } else {
                                linkedListResult = r;
                            }
                        }

                        if (arrayListResult != null && linkedListResult != null) {
                            double difference = ((linkedListResult.score - arrayListResult.score) / arrayListResult.score) * 100;
                            String winner = difference > 0 ? "ArrayList" : (difference < 0 ? "LinkedList" : "Tie");

                            writer.printf("| %d | %.2f %s | %.2f %s | %.2f%% | %s |%n",
                                    size,
                                    arrayListResult.score, arrayListResult.units,
                                    linkedListResult.score, linkedListResult.units,
                                    Math.abs(difference),
                                    winner);
                        }
                    }

                    writer.println();
                }

                writer.println();
            }

            // Add summary section
            writer.println("## Summary");
            writer.println();
            writer.println("### Operations where ArrayList performs better");
            writer.println();
            listOperationsByWinner(results, true, writer);

            writer.println();
            writer.println("### Operations where LinkedList performs better");
            writer.println();
            listOperationsByWinner(results, false, writer);

            writer.println();
            writer.println("## Conclusion");
            writer.println();
            writer.println("This benchmark comparison helps illustrate the strengths and weaknesses of ArrayList and LinkedList implementations.");
            writer.println("When choosing between them, consider your specific use case and the most frequent operations you'll perform.");
        }
    }

    private static String getSimpleClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot != -1 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }

    private static Map<String, List<BenchmarkResult>> groupResultsByClass(List<BenchmarkResult> results) {
        Map<String, List<BenchmarkResult>> map = new TreeMap<>();
        for (BenchmarkResult result : results) {
            map.computeIfAbsent(result.benchmarkClass, k -> new ArrayList<>()).add(result);
        }
        return map;
    }

    private static Map<String, List<BenchmarkResult>> groupResultsByOperationBase(List<BenchmarkResult> results) {
        Map<String, List<BenchmarkResult>> map = new TreeMap<>();
        for (BenchmarkResult result : results) {
            String operationBase = result.getOperationBase();
            map.computeIfAbsent(operationBase, k -> new ArrayList<>()).add(result);
        }
        return map;
    }

    private static Map<Integer, List<BenchmarkResult>> groupResultsBySize(List<BenchmarkResult> results) {
        Map<Integer, List<BenchmarkResult>> map = new TreeMap<>();
        for (BenchmarkResult result : results) {
            map.computeIfAbsent(result.paramValue, k -> new ArrayList<>()).add(result);
        }
        return map;
    }

    private static String formatOperationName(String operationName) {
        StringBuilder formatted = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : operationName.toCharArray()) {
            if (capitalizeNext) {
                formatted.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else if (Character.isUpperCase(c)) {
                formatted.append(' ').append(c);
            } else {
                formatted.append(c);
            }
        }

        return formatted.toString();
    }

    private static void listOperationsByWinner(List<BenchmarkResult> results, boolean arrayListWins, PrintWriter writer) {
        // Group by operation base
        Map<String, Map<Integer, Double>> differenceByOperationAndSize = new HashMap<>();

        for (int i = 0; i < results.size() - 1; i++) {
            BenchmarkResult r1 = results.get(i);
            BenchmarkResult r2 = results.get(i + 1);

            // Check if they are the same operation but different list types
            if (r1.getOperationBase().equals(r2.getOperationBase()) && r1.paramValue == r2.paramValue
                    && r1.isArrayList != r2.isArrayList) {

                BenchmarkResult arrayListResult = r1.isArrayList ? r1 : r2;
                BenchmarkResult linkedListResult = r1.isArrayList ? r2 : r1;

                // Calculate difference percentage
                double difference = ((linkedListResult.score - arrayListResult.score) / arrayListResult.score) * 100;

                // If we're looking for operations where ArrayList wins and difference is positive OR
                // we're looking for operations where LinkedList wins and difference is negative
                if ((arrayListWins && difference > 0) || (!arrayListWins && difference < 0)) {
                    String opBase = r1.getOperationBase();
                    differenceByOperationAndSize
                            .computeIfAbsent(opBase, k -> new HashMap<>())
                            .put(r1.paramValue, Math.abs(difference));
                }
            }
        }

        // Sort operations by the maximum difference
        List<Map.Entry<String, Map<Integer, Double>>> sortedEntries = new ArrayList<>(differenceByOperationAndSize.entrySet());
        sortedEntries.sort((e1, e2) -> {
            double max1 = e1.getValue().values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double max2 = e2.getValue().values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            return Double.compare(max2, max1); // Descending order
        });

        // Output the results
        if (sortedEntries.isEmpty()) {
            writer.println("No clear winner found in any operation.");
            return;
        }

        writer.println("| Operation | Size | Performance Difference |");
        writer.println("|-----------|------|------------------------|");

        for (Map.Entry<String, Map<Integer, Double>> entry : sortedEntries) {
            String operation = formatOperationName(entry.getKey());
            Map<Integer, Double> sizeEntries = entry.getValue();

            List<Map.Entry<Integer, Double>> sortedSizes = new ArrayList<>(sizeEntries.entrySet());
            sortedSizes.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            for (Map.Entry<Integer, Double> sizeEntry : sortedSizes) {
                writer.printf("| %s | %d | %.2f%% |%n",
                        operation, sizeEntry.getKey(), sizeEntry.getValue());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java BenchmarkReporter <jmh-results-file> <output-markdown-file>");
            return;
        }

        try {
            generateReport(args[0], args[1]);
            System.out.println("Report generated successfully: " + args[1]);
        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}