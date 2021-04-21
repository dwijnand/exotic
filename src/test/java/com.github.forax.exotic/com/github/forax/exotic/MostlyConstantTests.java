package com.github.forax.exotic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class MostlyConstantTests {
  static final class ObjectSandbox1 { static final MostlyConstant<String>  VALUE = new MostlyConstant<>("hello", String.class); static final Supplier<String> VALUE_GETTER = VALUE.      getter(); }
  static final class ObjectSandbox2 { static final MostlyConstant<String>  VALUE = new MostlyConstant<>("hello", String.class); static final Supplier<String> VALUE_GETTER = VALUE.      getter(); }
  static final class    IntSandbox1 { static final MostlyConstant<Integer> VALUE = new MostlyConstant<>(42,         int.class); static final    IntSupplier   VALUE_GETTER = VALUE.   intGetter(); }
  static final class    IntSandbox2 { static final MostlyConstant<Integer> VALUE = new MostlyConstant<>(42,         int.class); static final    IntSupplier   VALUE_GETTER = VALUE.   intGetter(); }
  static final class   LongSandbox1 { static final MostlyConstant<Long>    VALUE = new MostlyConstant<>(42L,       long.class); static final   LongSupplier   VALUE_GETTER = VALUE.  longGetter(); }
  static final class   LongSandbox2 { static final MostlyConstant<Long>    VALUE = new MostlyConstant<>(42L,       long.class); static final   LongSupplier   VALUE_GETTER = VALUE.  longGetter(); }
  static final class DoubleSandbox1 { static final MostlyConstant<Double>  VALUE = new MostlyConstant<>(42.0,    double.class); static final DoubleSupplier   VALUE_GETTER = VALUE.doubleGetter(); }
  static final class DoubleSandbox2 { static final MostlyConstant<Double>  VALUE = new MostlyConstant<>(42.0,    double.class); static final DoubleSupplier   VALUE_GETTER = VALUE.doubleGetter(); }

  @Test void testObjectSimpleChange() { assertEquals("hello", ObjectSandbox1.VALUE_GETTER.get        ()); ObjectSandbox1.VALUE.setAndDeoptimize("hell"); assertEquals("hell", ObjectSandbox1.VALUE_GETTER.get        ()); }
  @Test void    testIntSimpleChange() { assertEquals(42,         IntSandbox1.VALUE_GETTER.getAsInt   ());    IntSandbox1.VALUE.setAndDeoptimize(43);     assertEquals(43,        IntSandbox1.VALUE_GETTER.getAsInt   ()); }
  @Test void   testLongSimpleChange() { assertEquals(42L,       LongSandbox1.VALUE_GETTER.getAsLong  ());   LongSandbox1.VALUE.setAndDeoptimize(43L);    assertEquals(43L,      LongSandbox1.VALUE_GETTER.getAsLong  ()); }
  @Test void testDoubleSimpleChange() { assertEquals(42.0,    DoubleSandbox1.VALUE_GETTER.getAsDouble()); DoubleSandbox1.VALUE.setAndDeoptimize(43.0);   assertEquals(43.0,   DoubleSandbox1.VALUE_GETTER.getAsDouble()); }

  static class FakeS { String test() { return ObjectSandbox2.VALUE_GETTER.get        (); } } FakeS fakeS = new FakeS();
  static class FakeI { int    test() { return    IntSandbox2.VALUE_GETTER.getAsInt   (); } } FakeI fakeI = new FakeI();
  static class FakeL { long   test() { return   LongSandbox2.VALUE_GETTER.getAsLong  (); } } FakeL fakeL = new FakeL();
  static class FakeD { double test() { return DoubleSandbox2.VALUE_GETTER.getAsDouble(); } } FakeD fakeD = new FakeD();

  @Test void testObjectSimpleChangeOptimized() { for (int i = 0; i < 1_000_000; i++) assertEquals("hello", fakeS.test()); ObjectSandbox2.VALUE.setAndDeoptimize("hell"); assertEquals("hell", fakeS.test()); }
  @Test void    testIntSimpleChangeOptimized() { for (int i = 0; i < 1_000_000; i++) assertEquals(42,      fakeI.test());    IntSandbox2.VALUE.setAndDeoptimize(43);     assertEquals(43,     fakeI.test()); }
  @Test void   testLongSimpleChangeOptimized() { for (int i = 0; i < 1_000_000; i++) assertEquals(42L,     fakeL.test());   LongSandbox2.VALUE.setAndDeoptimize(43L);    assertEquals(43L,    fakeL.test()); }
  @Test void testDoubleSimpleChangeOptimized() { for (int i = 0; i < 1_000_000; i++) assertEquals(42.0,    fakeD.test()); DoubleSandbox2.VALUE.setAndDeoptimize(43.0);   assertEquals(43.0,   fakeD.test()); }

  @Test void testConstructorWithVoidType() { assertThrows(IllegalArgumentException.class, () -> new MostlyConstant<>(null, void.class)); }
  @Test void testConstructorWithNullType() { assertThrows(    NullPointerException.class, () -> new MostlyConstant<>(null, null));       }

  @Test void testSpecializedGettersWithWrapperTypes() {
    assertThrows(IllegalStateException.class, () -> new MostlyConstant<>(0,   Integer.class).   intGetter());
    assertThrows(IllegalStateException.class, () -> new MostlyConstant<>(0L,     Long.class).  longGetter());
    assertThrows(IllegalStateException.class, () -> new MostlyConstant<>(0.0,  Double.class).doubleGetter());
    assertThrows(IllegalStateException.class, () -> new MostlyConstant<>(0,    Object.class).   intGetter());
    assertThrows(IllegalStateException.class, () -> new MostlyConstant<>(0L,   Object.class).  longGetter());
    assertThrows(IllegalStateException.class, () -> new MostlyConstant<>(0.0,  Object.class).doubleGetter());
  }
}
