/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.bytewright;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
public class ListBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private List<Integer> arrayList;
    private List<Integer> linkedList;
    private Random random;

    @Setup
    public void setup() {
        // Initialize lists with data
        arrayList = new ArrayList<>(size);
        linkedList = new LinkedList<>();
        random = new Random(42);

        for (int i = 0; i < size; i++) {
            int value = random.nextInt(size * 10);
            arrayList.add(value);
            linkedList.add(value);
        }
    }

    // APPEND OPERATIONS

    @Benchmark
    public List<Integer> appendToArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        list.add(random.nextInt());
        return list;
    }

    @Benchmark
    public List<Integer> appendToLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        list.add(random.nextInt());
        return list;
    }

    @Benchmark
    public List<Integer> appendManyToArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        for (int i = 0; i < size / 10; i++) {
            list.add(random.nextInt());
        }
        return list;
    }

    @Benchmark
    public List<Integer> appendManyToLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        for (int i = 0; i < size / 10; i++) {
            list.add(random.nextInt());
        }
        return list;
    }

    // INSERT OPERATIONS

    @Benchmark
    public List<Integer> insertAtBeginningArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        list.add(0, random.nextInt());
        return list;
    }

    @Benchmark
    public List<Integer> insertAtBeginningLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        list.add(0, random.nextInt());
        return list;
    }

    @Benchmark
    public List<Integer> insertAtMiddleArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        list.add(list.size() / 2, random.nextInt());
        return list;
    }

    @Benchmark
    public List<Integer> insertAtMiddleLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        list.add(list.size() / 2, random.nextInt());
        return list;
    }

    // ITERATION OPERATIONS

    @Benchmark
    public void iterateArrayList(Blackhole blackhole) {
        for (Integer value : arrayList) {
            blackhole.consume(value);
        }
    }

    @Benchmark
    public void iterateLinkedList(Blackhole blackhole) {
        for (Integer value : linkedList) {
            blackhole.consume(value);
        }
    }

    // STREAMING OPERATIONS

    @Benchmark
    public List<Integer> streamFilterArrayList() {
        return arrayList.stream()
                .filter(i -> i % 2 == 0)
                .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> streamFilterLinkedList() {
        return linkedList.stream()
                .filter(i -> i % 2 == 0)
                .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> streamMapArrayList() {
        return arrayList.stream()
                .map(i -> i * 2)
                .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> streamMapLinkedList() {
        return linkedList.stream()
                .map(i -> i * 2)
                .collect(Collectors.toList());
    }

    // REMOVAL OPERATIONS

    @Benchmark
    public List<Integer> removeFirstArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        if (!list.isEmpty()) {
            list.remove(0);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeFirstLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        if (!list.isEmpty()) {
            list.remove(0);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeMiddleArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        if (!list.isEmpty()) {
            list.remove(list.size() / 2);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeMiddleLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        if (!list.isEmpty()) {
            list.remove(list.size() / 2);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeLastArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        if (!list.isEmpty()) {
            list.remove(list.size() - 1);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeLastLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        if (!list.isEmpty()) {
            list.remove(list.size() - 1);
        }
        return list;
    }

    // REMOVAL DURING ITERATION

    @Benchmark
    public List<Integer> removeWhileIteratingArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            Integer value = iterator.next();
            if (value % 10 == 0) {
                iterator.remove();
            }
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeWhileIteratingLinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            Integer value = iterator.next();
            if (value % 10 == 0) {
                iterator.remove();
            }
        }
        return list;
    }

    // ACCESS OPERATIONS

    @Benchmark
    public void randomAccessArrayList(Blackhole blackhole) {
        for (int i = 0; i < 1000; i++) {
            int index = random.nextInt(arrayList.size());
            blackhole.consume(arrayList.get(index));
        }
    }

    @Benchmark
    public void randomAccessLinkedList(Blackhole blackhole) {
        for (int i = 0; i < 1000; i++) {
            int index = random.nextInt(linkedList.size());
            blackhole.consume(linkedList.get(index));
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ListBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}