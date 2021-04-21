package com.github.forax.exotic;

import static com.github.forax.exotic.TypeSwitch.NO_MATCH;
import static com.github.forax.exotic.TypeSwitch.NULL_MATCH;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class TypeSwitchTests {
  interface I {}
  interface J {}
  class A implements I, J {}

  private static final TypeSwitch switch1 = TypeSwitch.create(false, Integer.class, String.class);
  private static final TypeSwitch switch2 = TypeSwitch.create(false, CharSequence.class, Object.class);
  private static final TypeSwitch switch3 = TypeSwitch.create(false, I.class, J.class);
  private static final TypeSwitch switch4 = TypeSwitch.create(false, J.class, I.class);
  private static final TypeSwitch switch5 = TypeSwitch.create(true, String.class);

  @Test void      simple1() { assertEquals(0,          switch1.typeSwitch(3));       }
  @Test void      simple2() { assertEquals(0,          switch1.typeSwitch(42));      }
  @Test void      simple3() { assertEquals(1,          switch1.typeSwitch("foo"));   }
  @Test void      simple4() { assertEquals(1,          switch1.typeSwitch("bar"));   }
  @Test void      simple5() { assertEquals(NO_MATCH,   switch1.typeSwitch(4.5));     }
  @Test void inheritance1() { assertEquals(1,          switch2.typeSwitch(3));       }
  @Test void inheritance2() { assertEquals(1,          switch2.typeSwitch(42));      }
  @Test void inheritance3() { assertEquals(0,          switch2.typeSwitch("foo"));   }
  @Test void inheritance4() { assertEquals(0,          switch2.typeSwitch("bar"));   }
  @Test void inheritance5() { assertEquals(1,          switch2.typeSwitch(4.5));     }
  @Test void interfacesA1() { assertEquals(0,          switch3.typeSwitch(new A())); }
  @Test void interfacesA2() { assertEquals(0,          switch3.typeSwitch(new A())); }
  @Test void interfacesA3() { assertEquals(NO_MATCH,   switch3.typeSwitch("bar"));   }
  @Test void interfacesB1() { assertEquals(0,          switch4.typeSwitch(new A())); }
  @Test void interfacesB2() { assertEquals(0,          switch4.typeSwitch(new A())); }
  @Test void interfacesB3() { assertEquals(NO_MATCH,   switch4.typeSwitch("bar"));   }
  @Test void    nullCase1() { assertEquals(0,          switch5.typeSwitch("foo"));   }
  @Test void    nullCase2() { assertEquals(NULL_MATCH, switch5.typeSwitch(null));    }
  @Test void    nullCase3() { assertEquals(  NO_MATCH, switch5.typeSwitch(3));       }

  @Test void nonNullSwitchCalledWithANull() { assertThrows(NullPointerException.class,  () -> TypeSwitch.create(false).typeSwitch(null)); }
  @Test void aCaseCanNotBeNull1()           { assertThrows(NullPointerException.class,  () -> TypeSwitch.create(false, (Class<?>) null)); }
  @Test void aCaseCanNotBeNull2()           { assertThrows(NullPointerException.class,  () -> TypeSwitch.create(true,  (Class<?>) null)); }
  @Test void invalidPartialOrder1()         { assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Object.class, String.class)); }
  @Test void invalidPartialOrder2()         { assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Comparable.class, String.class)); }
  @Test void invalidPartialOrder3()         { assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Object.class, Comparable.class)); }
  @Test void invalidPartialOrder4()         { assertThrows(IllegalStateException.class, () -> TypeSwitch.create(false, Serializable.class, Comparable.class, String.class)); }
}
