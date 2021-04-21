package com.github.forax.exotic.perf;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.util.concurrent.TimeUnit;
import java.util.function.*;

import com.github.forax.exotic.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

@SuppressWarnings("static-method")
@Warmup         (iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement    (iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork           (3)
@BenchmarkMode  (Mode.AverageTime)
@OutputTimeUnit (TimeUnit.NANOSECONDS)
@State          (Scope.Benchmark)
public class MethodCallBenchMark {
                 interface  I {                  int f();              }
  static class A implements I { @Override public int f() { return 1; } }
  static class B implements I { @Override public int f() { return 1; } }
  static class C implements I { @Override public int f() { return 1; } }

  private static final I[] ARRAY = new I[] { new A(), new B(), new C() };

  private static final ToIntFunction<I> MEMOIZER        = ConstantMemoizer.intMemoizer(I::f, I.class);
  private static final StructuralCall   STRUCTURAL_CALL = StructuralCall.create(lookup(), "f", methodType(int.class));

  @Benchmark public int    virtual_call() { int sum = 0; for (I i : ARRAY) sum += i.f();                           return sum; }
  @Benchmark public int        memoizer() { int sum = 0; for (I i : ARRAY) sum += MEMOIZER.applyAsInt(i);          return sum; }
  @Benchmark public int structural_call() { int sum = 0; for (I i : ARRAY) sum += (int) STRUCTURAL_CALL.invoke(i); return sum; }

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(MethodCallBenchMark.class.getName()).build()).run();
  }
}
