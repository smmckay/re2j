// Copyright 2011 The Go Authors.  All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Uncreative and literal-minded port from Go to Java by
// Alan Donovan <adonovan@google.com>, 2011.
//
// Original Go source here:
// http://code.google.com/p/go/source/browse/src/pkg/regexp/syntax/exec_test.go

package us.abbies.b.re2jit;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarks for common RE2J operations. The easiest way to run these benchmarks is to run
 *
 * <pre>
 *   ./gradlew benchmarks
 * </pre>
 * <p>
 * from the project root directory. You can pass arguments to the benchmark program using the following invocation of
 * {@code gradlew}:
 *
 * <pre>
 *   ./gradlew benchmarks --args="--help"
 * </pre>
 */
@State(Scope.Benchmark)
public abstract class AbstractBenchmark {
    public enum Implementation {
        RE2J, RE2JIT, JDK, AUTOMATON, AUTOMATON_RUN, LUCENE
    }

    @Param({"RE2J", "JDK", "AUTOMATON", "AUTOMATON_RUN", "LUCENE"})
    Implementation implementation;

    private String re;
    private Predicate<String> matcher;
    private String text;
    private boolean expectMatch;

    protected AbstractBenchmark(String re, String text, boolean expectMatch) {
        this.re = re;
        this.text = text;
        this.expectMatch = expectMatch;
    }

    @Setup
    public void setup() {
        switch (implementation) {
        case JDK: {
            final Pattern p = Pattern.compile(re);
            matcher = input -> p.matcher(input).matches();
        }
            break;
        case RE2J: {
            final com.google.re2j.Pattern p = com.google.re2j.Pattern.compile(re);
            matcher = input -> p.matcher(input).matches();
        }
            break;
        case RE2JIT: {
            final us.abbies.b.re2jit.Pattern p = us.abbies.b.re2jit.Pattern.compile(re);
            matcher = input -> p.matcher(input).matches();
        }
            break;
        case AUTOMATON: {
            RegExp regExp = new RegExp(re);
            final Automaton automaton = regExp.toAutomaton();
            matcher = automaton::run;
        }
            break;
        case AUTOMATON_RUN: {
            RegExp regExp = new RegExp(re);
            Automaton automaton = regExp.toAutomaton();
            final RunAutomaton runAutomaton = new RunAutomaton(automaton);
            matcher = runAutomaton::run;
        }
            break;
        case LUCENE: {
            org.apache.lucene.util.automaton.RegExp regExp = new org.apache.lucene.util.automaton.RegExp(re);
            org.apache.lucene.util.automaton.Automaton automaton = regExp.toAutomaton(Integer.MAX_VALUE);
            final CharacterRunAutomaton runAutomaton = new CharacterRunAutomaton(automaton);
            matcher = runAutomaton::run;
        }
            break;
        default:
            throw new IllegalStateException("Can't handle " + implementation);
        }

        assert matcher.test(text) != expectMatch;
    }

    @Benchmark
    public boolean run() {
        return matcher.test(text);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                // .include(AbstractBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
