package com.github.forax.exotic;

import static com.github.forax.exotic.StringSwitch.NO_MATCH;
import static com.github.forax.exotic.StringSwitch.NULL_MATCH;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class StringSwitchTests {
  private static StringSwitch newSwitch1() { return StringSwitch.create(false, "foo", "bar"); }
  private static StringSwitch newSwitch2() { return StringSwitch.create(true);                }

  @Test void simple1()   { assertEquals(0,                                newSwitch1().stringSwitch(           "foo" )); }
  @Test void simple2()   { assertEquals(0,                                newSwitch1().stringSwitch(new String("foo"))); }
  @Test void simple3()   { assertEquals(1,                                newSwitch1().stringSwitch(           "bar" )); }
  @Test void simple4()   { assertEquals(1,                                newSwitch1().stringSwitch(new String("bar"))); }
  @Test void simple5()   { assertEquals(NO_MATCH,                         newSwitch1().stringSwitch(           "baz" )); }
  @Test void simpleNPE() { assertThrows(NullPointerException.class, () -> newSwitch1().stringSwitch(           null  )); }
  @Test void nullCase1() { assertEquals(0,                                newSwitch2().stringSwitch(           "foo" )); }
  @Test void nullCase2() { assertEquals(NULL_MATCH,                       newSwitch2().stringSwitch(           null  )); }
  @Test void nullCase3() { assertEquals(NO_MATCH,                         newSwitch2().stringSwitch(           ""    )); }

  @Test void aCaseCanNotBeNull1    () { assertThrows(NullPointerException.class, () -> StringSwitch.create(false, (String  ) null)); }
  @Test void aCaseCanNotBeNull2    () { assertThrows(NullPointerException.class, () -> StringSwitch.create(true,  (String  ) null)); }
  @Test void casesArrayCanNotBeNull() { assertThrows(NullPointerException.class, () -> StringSwitch.create(false, (String[]) null)); }
}
