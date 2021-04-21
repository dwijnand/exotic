package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class StableFieldTests {
  static class A     { String  x;                                     }
  static class B     { int     y;                                     }
  static class C     { long    z;                                     }
  static class D     { double  z;                                     }
  static class Boxed { int     i; long j; double d;                   }
  static class Prim  { boolean z; byte b; char   c; short s; float f; }
  private static class Foo { private @SuppressWarnings("unused") Object a; private @SuppressWarnings("unused") int b; private @SuppressWarnings("unused") long c; private @SuppressWarnings("unused") double d; }

  @Test void testObjectFieldUninitialized() { Function<A, String> f = StableField.      getter(lookup(), A.class, "x", String.class); A a = new A();              assertNull(         f.apply(a));                         assertNull(           f.apply(a));                                                                    }
  @Test void    testIntFieldUninitialized() {    ToIntFunction<B> f = StableField.   intGetter(lookup(), B.class, "y");               B b = new B();              assertEquals(0,     f.applyAsInt(b));                    assertEquals(0,       f.applyAsInt(b));                                                               }
  @Test void   testLongFieldUninitialized() {   ToLongFunction<C> f = StableField.  longGetter(lookup(), C.class, "z");               C c = new C();              assertEquals(0,     f.applyAsLong(c));                   assertEquals(0,       f.applyAsLong(c));                                                              }
  @Test void testDoubleFieldUninitialized() { ToDoubleFunction<D> f = StableField.doubleGetter(lookup(), D.class, "z");               D d = new D();              assertEquals(0.0,   f.applyAsDouble(d));                 assertEquals(0.0,     f.applyAsDouble(d));                                                            }
  @Test void testObjectFieldStable       () { Function<A, String> f = StableField.      getter(lookup(), A.class, "x", String.class); A a = new A();              assertNull(         f.apply(a));         a.x = "hello";  assertEquals("hello", f.apply(a));                         assertEquals("hello", f.apply(a));         }
  @Test void    testIntFieldStable       () {    ToIntFunction<B> f = StableField.   intGetter(lookup(), B.class, "y");               B b = new B();              assertEquals(0,     f.applyAsInt(b));    b.y = 42;       assertEquals(42,      f.applyAsInt(b));                    assertEquals(42,      f.applyAsInt(b));    }
  @Test void   testLongFieldStable       () {   ToLongFunction<C> f = StableField.  longGetter(lookup(), C.class, "z");               C c = new C();              assertEquals(0L,    f.applyAsLong(c));   c.z = 42L;      assertEquals(42L,     f.applyAsLong(c));                   assertEquals(42L,     f.applyAsLong(c));   }
  @Test void testDoubleFieldStable       () { ToDoubleFunction<D> f = StableField.doubleGetter(lookup(), D.class, "z");               D d = new D();              assertEquals(0.0,   f.applyAsDouble(d)); d.z = 42.0;     assertEquals(42.0,    f.applyAsDouble(d));                 assertEquals(42.0,    f.applyAsDouble(d)); }
  @Test void testObjectFieldStableStill  () { Function<A, String> f = StableField.      getter(lookup(), A.class, "x", String.class); A a = new A();              assertNull(         f.apply(a));         a.x = "hello";  assertEquals("hello", f.apply(a));         a.x = "banzai"; assertEquals("hello", f.apply(a));         }
  @Test void    testIntFieldStableStill  () {    ToIntFunction<B> f = StableField.   intGetter(lookup(), B.class, "y");               B b = new B();              assertEquals(0,     f.applyAsInt(b));    b.y = 42;       assertEquals(42,      f.applyAsInt(b));    b.y = 777;      assertEquals(42,      f.applyAsInt(b));    }
  @Test void   testLongFieldStableStill  () {   ToLongFunction<C> f = StableField.  longGetter(lookup(), C.class, "z");               C c = new C();              assertEquals(0,     f.applyAsLong(c));   c.z = 42L;      assertEquals(42L,     f.applyAsLong(c));   c.z = 777L;     assertEquals(42L,     f.applyAsLong(c));   }
  @Test void testDoubleFieldStableStill  () { ToDoubleFunction<D> f = StableField.doubleGetter(lookup(), D.class, "z");               D d = new D();              assertEquals(0.0,   f.applyAsDouble(d)); d.z = 42;       assertEquals(42.0,    f.applyAsDouble(d)); d.z = 777;      assertEquals(42.0,    f.applyAsDouble(d)); }
  @Test void testObjectFieldNonConstant  () { Function<A, String> f = StableField.      getter(lookup(), A.class, "x", String.class); A a = new A(); a.x = "foo"; assertEquals("foo", f.apply(a));         A a2 = new A(); assertThrows(IllegalStateException.class, () -> f.apply(a2));                                         }
  @Test void    testIntFieldNonConstant  () {    ToIntFunction<B> f = StableField.   intGetter(lookup(), B.class, "y");               B b = new B(); b.y = 666;   assertEquals(666,   f.applyAsInt(b));    B b2 = new B(); assertThrows(IllegalStateException.class, () -> f.applyAsInt(b2));                                    }
  @Test void    tesLongFieldNonConstant  () {   ToLongFunction<C> f = StableField.  longGetter(lookup(), C.class, "z");               C c = new C(); c.z = 666;   assertEquals(666,   f.applyAsLong(c));   C c2 = new C(); assertThrows(IllegalStateException.class, () -> f.applyAsLong(c2));                                   }
  @Test void testDoubleFieldNonConstant  () { ToDoubleFunction<D> f = StableField.doubleGetter(lookup(), D.class, "z");               D d = new D(); d.z = 666.0; assertEquals(666.0, f.applyAsDouble(d)); D d2 = new D(); assertThrows(IllegalStateException.class, () -> f.applyAsDouble(d2));                                 }

  @Test void testBoxedIntField   () { Function<Boxed, Integer>  f = StableField.getter(lookup(), Boxed.class, "i",     int.class); Boxed boxed = new Boxed(); assertEquals(0,         (int)    f.apply(boxed)); boxed.i = 3;    assertEquals(3,             (int)    f.apply(boxed)); boxed.i = 5;    assertEquals(3,             (int)    f.apply(boxed)); assertThrows(IllegalStateException.class, () -> f.apply(new Boxed())); }
  @Test void testBoxedLongField  () { Function<Boxed, Long>     f = StableField.getter(lookup(), Boxed.class, "j",    long.class); Boxed boxed = new Boxed(); assertEquals(0L,        (long)   f.apply(boxed)); boxed.j = 3L;   assertEquals(3L,            (long)   f.apply(boxed)); boxed.j = 5L;   assertEquals(3L,            (long)   f.apply(boxed)); assertThrows(IllegalStateException.class, () -> f.apply(new Boxed())); }
  @Test void testBoxedDoubleField() { Function<Boxed, Double>   f = StableField.getter(lookup(), Boxed.class, "d",  double.class); Boxed boxed = new Boxed(); assertEquals(0.0,       (double) f.apply(boxed)); boxed.d = 3.0;  assertEquals(3.0,           (double) f.apply(boxed)); boxed.d = 5.0;  assertEquals(3.0,           (double) f.apply(boxed)); assertThrows(IllegalStateException.class, () -> f.apply(new Boxed())); }
  @Test void testPrimBooleanField() { Function<Prim, Boolean>   f = StableField.getter(lookup(),  Prim.class, "z", boolean.class); Prim  prim  = new Prim();  assertFalse(                     f.apply(prim));  prim.z = true;  assertTrue(                          f.apply(prim));  prim.z = false; assertTrue(                          f.apply(prim));  assertThrows(IllegalStateException.class, () -> f.apply(new Prim())); }
  @Test void testPrimByteField   () { Function<Prim, Byte>      f = StableField.getter(lookup(),  Prim.class, "b",    byte.class); Prim  prim  = new Prim();  assertEquals((byte) 0,  (byte)   f.apply(prim));  prim.b = 10;    assertEquals((byte) 10,     (byte)   f.apply(prim));  prim.b = 5;     assertEquals((byte) 10,     (byte)   f.apply(prim));  assertThrows(IllegalStateException.class, () -> f.apply(new Prim())); }
  @Test void testPrimCharField   () { Function<Prim, Character> f = StableField.getter(lookup(),  Prim.class, "c",    char.class); Prim  prim  = new Prim();  assertEquals((char) 0,  (char)   f.apply(prim));  prim.c = 'A';   assertEquals('A',           (char)   f.apply(prim));  prim.c = 'B';   assertEquals('A',           (char)   f.apply(prim));  assertThrows(IllegalStateException.class, () -> f.apply(new Prim())); }
  @Test void testPrimShortField  () { Function<Prim, Short>     f = StableField.getter(lookup(),  Prim.class, "s",   short.class); Prim  prim  = new Prim();  assertEquals((short) 0, (short)  f.apply(prim));  prim.s = 1_000; assertEquals((short) 1_000, (short)  f.apply(prim));  prim.s = 2_000; assertEquals((short) 1_000, (short)  f.apply(prim));  assertThrows(IllegalStateException.class, () -> f.apply(new Prim())); }
  @Test void testPrimFloatField  () { Function<Prim, Float>     f = StableField.getter(lookup(),  Prim.class, "f",   float.class); Prim  prim  = new Prim();  assertEquals(0.0f,      (float)  f.apply(prim));  prim.f = 0.2f;  assertEquals(0.2f,          (float)  f.apply(prim));  prim.f = 0.4f;  assertEquals(0.2f,          (float)  f.apply(prim));  assertThrows(IllegalStateException.class, () -> f.apply(new Prim())); }

  @Test
  void testNoSuchField() {
    assertThrows(NoSuchFieldError.class, () -> StableField.      getter(lookup(), Object.class, "foo", String.class));
    assertThrows(NoSuchFieldError.class, () -> StableField.   intGetter(lookup(), Object.class, "foo"));
    assertThrows(NoSuchFieldError.class, () -> StableField.  longGetter(lookup(), Object.class, "foo"));
    assertThrows(NoSuchFieldError.class, () -> StableField.doubleGetter(lookup(), Object.class, "foo"));
  }

  @Test
  void testNoAccess() {
    assertThrows(IllegalAccessError.class, () -> StableField.      getter(publicLookup(), Foo.class, "a", Object.class));
    assertThrows(IllegalAccessError.class, () -> StableField.   intGetter(publicLookup(), Foo.class, "b"));
    assertThrows(IllegalAccessError.class, () -> StableField.  longGetter(publicLookup(), Foo.class, "c"));
    assertThrows(IllegalAccessError.class, () -> StableField.doubleGetter(publicLookup(), Foo.class, "d"));
  }
}
