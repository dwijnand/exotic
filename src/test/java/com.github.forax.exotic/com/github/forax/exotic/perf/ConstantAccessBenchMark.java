package com.github.forax.exotic.perf;

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
public class ConstantAccessBenchMark {
          static final int                     static_final_int               = 1_000;
          static final Integer                 static_final_Integer           = 1_000;
  private static final MostlyConstant<Integer> MOSTLY_CONSTANT_INT            = new MostlyConstant<>(1_000, int.class);
  private static final MostlyConstant<Integer> MOSTLY_CONSTANT_INTEGER        = new MostlyConstant<>(1_000, Integer.class);
  private static final IntSupplier             MOSTLY_CONSTANT_INT_GETTER     = MOSTLY_CONSTANT_INT.intGetter();
  private static final Supplier<Integer>       MOSTLY_CONSTANT_INTEGER_GETTER = MOSTLY_CONSTANT_INTEGER.getter();

  @Benchmark public int static_final_int()        { return 1_000 / static_final_int;                      }
  @Benchmark public int static_final_Integer()    { return 1_000 / static_final_Integer;                  }
  @Benchmark public int mostly_constant_int()     { return 1_000 / MOSTLY_CONSTANT_INT_GETTER.getAsInt(); }
  @Benchmark public int mostly_constant_Integer() { return 1_000 / MOSTLY_CONSTANT_INTEGER_GETTER.get();  }

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(ConstantAccessBenchMark.class.getName()).build()).run();
  }
}
