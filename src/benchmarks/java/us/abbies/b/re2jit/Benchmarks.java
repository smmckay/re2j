// Copyright 2011 The Go Authors.  All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Uncreative and literal-minded port from Go to Java by
// Alan Donovan <adonovan@google.com>, 2011.
//
// Original Go source here:
// http://code.google.com/p/go/source/browse/src/pkg/regexp/syntax/exec_test.go

package us.abbies.b.re2jit;

import com.google.caliper.BeforeExperiment;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.api.VmOptions;
import com.google.caliper.runner.CaliperMain;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;

import java.util.regex.Pattern;

import static org.junit.Assert.fail;

/**
 * Benchmarks for common RE2J operations. The easiest way to run these benchmarks is to run
 *
 * <pre>
 *   ./gradlew benchmarks
 * </pre>
 *
 * from the project root directory. You can pass arguments to the benchmark program using the
 * following invocation of {@code gradlew}:
 *
 * <pre>
 *   ./gradlew benchmarks --args="--help"
 * </pre>
 */
@VmOptions({
  "-XX:-TieredCompilation", // http://stackoverflow.com/questions/29199509
  "-Xms8G",
  "-Xmx8G"
}) // These seem to be necessary to avoid GC during the JDK benchmarks.
// A GC during an experiment causes Caliper to discard the results.
public class Benchmarks {
  private enum Implementation {
    RE2J,
    RE2JIT,
    JDK,
    AUTOMATON,
    AUTOMATON_RUN,
    LUCENE
  }

  @Param({"RE2J", "JDK", "AUTOMATON", "AUTOMATON_RUN", "LUCENE"})
  private Implementation implementation;

  private static final String LONG_DATA;

  static {
    StringBuilder sb = new StringBuilder(26 << 15);
    for (int i = 0; i < 1 << 15; i++) {
      sb.append("abcdefghijklmnopqrstuvwxyz");
    }
    LONG_DATA = sb.toString();
  }

  private Matcher pathologicalBacktracking;
  private Matcher literal;
  private Matcher notLiteral;
  private Matcher matchClassMatcher;
  private Matcher inRangeMatchClassMatcher;
  private Matcher anchoredLiteralNonMatchingMatcher;
  private Matcher anchoredMatchingMatcher;

  private interface Matcher {
    boolean match(String input);
  }

  @BeforeExperiment
  public void setupExpressions() {
    pathologicalBacktracking =
        compile("a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?" + "aaaaaaaaaaaaaaaaaaaaaaaa");
    literal = compile(".*y");
    notLiteral = compile(".*.y");
    matchClassMatcher = compile(".*[abcdw]");
    inRangeMatchClassMatcher = compile(".*[ac]");
    anchoredLiteralNonMatchingMatcher = compile("zbc(d|e).*");
    anchoredMatchingMatcher = compile(".bc(d|e).*");

    System.gc();
  }

  private Matcher compile(String re) {
    switch (implementation) {
      case JDK: {
        final Pattern p = Pattern.compile(re);
        return input -> p.matcher(input).matches();
      }
      case RE2J: {
        final com.google.re2j.Pattern p = com.google.re2j.Pattern.compile(re);
        return input -> p.matcher(input).matches();
      }
      case RE2JIT: {
        final us.abbies.b.re2jit.Pattern p = us.abbies.b.re2jit.Pattern.compile(re);
        return input -> p.matcher(input).matches();
      }
      case AUTOMATON: {
        RegExp regExp = new RegExp(re);
        final Automaton automaton = regExp.toAutomaton();
        return automaton::run;
      }
      case AUTOMATON_RUN: {
        RegExp regExp = new RegExp(re);
        Automaton automaton = regExp.toAutomaton();
        final RunAutomaton runAutomaton = new RunAutomaton(automaton);
        return runAutomaton::run;
      }
      case LUCENE: {
        org.apache.lucene.util.automaton.RegExp regExp = new org.apache.lucene.util.automaton.RegExp(re);
        org.apache.lucene.util.automaton.Automaton automaton = regExp.toAutomaton(Integer.MAX_VALUE);
        final CharacterRunAutomaton runAutomaton = new CharacterRunAutomaton(automaton);
        return runAutomaton::run;
      }
      default:
        throw new IllegalStateException("Can't handle " + implementation);
    }
  }

  // See http://swtch.com/~rsc/regexp/regexp1.html.
  @Benchmark
  public void benchmarkPathologicalBacktracking() {
    if (!pathologicalBacktracking.match("aaaaaaaaaaaaaaaaaaaaaaaa")) {
      fail("no match!");
    }
  }

  // The following benchmarks were ported from
  // http://code.google.com/p/go/source/browse/src/pkg/regexp/all_test.go

  @Benchmark
  public void benchmarkLiteral(long nreps) {
    String x = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxy";
    for (long i = 0; i < nreps; i++) {
      if (!literal.match(x)) {
        fail("no match!");
      }
    }
  }

  @Benchmark
  public void benchmarkNotLiteral(long nreps) {
    String x = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxy";
    for (long i = 0; i < nreps; i++) {
      if (!notLiteral.match(x)) {
        fail("no match!");
      }
    }
  }

  @Benchmark
  public void benchmarkMatchClass(long nreps) {
    String x =
        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
            + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
            + "w";
    for (long i = 0; i < nreps; i++) {
      if (!matchClassMatcher.match(x)) {
        fail("no match!");
      }
    }
  }

  @Benchmark
  public void benchmarkMatchClass_InRange(long nreps) {
    // 'b' is between 'a' and 'c', so the charclass
    // range checking is no help here.
    String x =
        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
            + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
            + "c";
    for (long i = 0; i < nreps; i++) {
      if (!inRangeMatchClassMatcher.match(x)) {
        fail("no match!");
      }
    }
  }

  @Benchmark
  public void benchmarkAnchoredLiteralShortNonMatch(long nreps) {
    String x = "abcdefghijklmnopqrstuvwxyz";
    for (long i = 0; i < nreps; i++) {
      if (anchoredLiteralNonMatchingMatcher.match(x)) {
        fail("match!");
      }
    }
  }

  @Benchmark
  public void benchmarkAnchoredLiteralLongNonMatch(long nreps) {
    for (long i = 0; i < nreps; i++) {
      if (anchoredLiteralNonMatchingMatcher.match(LONG_DATA)) {
        fail("match!");
      }
    }
  }

  @Benchmark
  public void benchmarkAnchoredShortMatch(long nreps) {
    String x = "abcdefghijklmnopqrstuvwxyz";
    for (long i = 0; i < nreps; i++) {
      if (!anchoredMatchingMatcher.match(x)) {
        fail("no match!");
      }
    }
  }

  @Benchmark
  public void benchmarkAnchoredLongMatch(long nreps) {
    for (long i = 0; i < nreps; i++) {
      if (!anchoredMatchingMatcher.match(LONG_DATA)) {
        fail("no match!");
      }
    }
  }

  public static void main(String[] args) {
    CaliperMain.main(Benchmarks.class, args);
  }
}
