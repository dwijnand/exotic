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
public class FieldAccessBenchMark {
          static class A { final int x; public A(int x) { this.x = x; } public int x() { return x; } }
          static final A                       static_final           = new A(1_000);
  private static final MostlyConstant<Integer> MOSTLY_CONSTANT        = new MostlyConstant<>(1_000, int.class);
  private static final IntSupplier             MOSTLY_CONSTANT_GETTER = MOSTLY_CONSTANT.intGetter();
  private static final ToIntFunction<A>        STABLE_X               = StableField.intGetter(lookup(), A.class, "x");
  private static final ToIntFunction<A>        MEMOIZER               = ConstantMemoizer.intMemoizer(A::x, A.class);
  private static final StructuralCall          STRUCTURAL_CALL        = StructuralCall.create(lookup(), "x", methodType(int.class));

  @Benchmark public int field_access()    { return 1_000 / static_final.x;                             }
  @Benchmark public int mostly_constant() { return 1_000 / MOSTLY_CONSTANT_GETTER.getAsInt();          }
  @Benchmark public int stable_field()    { return 1_000 /          STABLE_X.applyAsInt(static_final); }
  @Benchmark public int memoizer()        { return 1_000 /          MEMOIZER.applyAsInt(static_final); }
  @Benchmark public int structural_call() { return 1_000 / (int) STRUCTURAL_CALL.invoke(static_final); }

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(FieldAccessBenchMark.class.getName()).build()).run();
  }
}
