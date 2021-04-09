package com.github.forax.exotic;

import static com.github.forax.exotic.StringSwitch.NO_MATCH;
import static com.github.forax.exotic.StringSwitch.NULL_MATCH;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class StringSwitchCallSite extends MutableCallSite {
  private static final MethodType STRING_TO_INT = methodType(int.class, String.class);
  private static final MethodHandle FALLBACK, EQUALS, GET_OR_NO_MATCH, NULLCHECK;

  static {
    Lookup lookup = lookup();
    MethodHandle GET_OR_DEFAULT;
    try {
      FALLBACK        = lookup.findVirtual(lookup.lookupClass(), "fallback", STRING_TO_INT);
      EQUALS          = lookup.findVirtual( String.class,        "equals",       methodType(boolean.class, Object.class));
      GET_OR_DEFAULT  = lookup.findVirtual(HashMap.class,        "getOrDefault", methodType( Object.class, Object.class, Object.class));
      GET_OR_NO_MATCH = insertArguments(GET_OR_DEFAULT, 2, NO_MATCH).asType(     methodType(    int.class,    Map.class, String.class));
      NULLCHECK       = TypeSwitchCallSite.NULLCHECK.asType(                     methodType(boolean.class, String.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final String[]             cases;
  private final Map<String, Integer> stringToIndex;
  private final MutableCallSite      topCallsite;
  private final int                  depth;

  private StringSwitchCallSite(String[] cases, Map<String, Integer> stringToIndex, MutableCallSite topCallsite, int depth) {
    super(STRING_TO_INT);
    setTarget(FALLBACK.bindTo(this));
    this.cases         = cases;
    this.stringToIndex = stringToIndex;
    this.topCallsite   = topCallsite == null ? this : topCallsite;
    this.depth         = depth;
  }

  static StringSwitchCallSite bootstrap(String[] cases) {
    HashMap<String, Integer> stringToIndex = new HashMap<>();
    for (int i = 0; i < cases.length; i++)
      if (stringToIndex.put(Objects.requireNonNull(cases[i]), i) != null)
        throw new IllegalStateException(cases[i] + " value appear more than once");
    return new StringSwitchCallSite(cases, stringToIndex, null, 0);
  }

  static MethodHandle wrapNullIfNecessary(boolean nullMatch, MethodHandle mh) {
    return nullMatch ? guardWithTest(NULLCHECK, constInt(NULL_MATCH), mh) : mh;
  }

  @SuppressWarnings("unused")
  private int fallback(String value) {
    int index = stringToIndex.getOrDefault(Objects.requireNonNull(value), NO_MATCH);
    if (depth == 32) {
      topCallsite.setTarget(GET_OR_NO_MATCH.bindTo(stringToIndex));
    } else if (depth == cases.length) {
      MethodHandle next = constInt(NO_MATCH);
      for (int i = cases.length; --i >= 0; )
        next = valueToIndexOrElse(cases[i], i, next);
      topCallsite.setTarget(next);
    } else {
      MethodHandle next = new StringSwitchCallSite(cases, stringToIndex, topCallsite, depth + 1).dynamicInvoker();
      setTarget(valueToIndexOrElse(value, index, next));
    }
    return index;
  }

  private static MethodHandle valueToIndexOrElse(String value, int index, MethodHandle alt) {
    return guardWithTest(insertArguments(EQUALS, 1, value), constInt(index), alt);
  }

  private static MethodHandle constInt(int value) {
    return dropArguments(constant(int.class, value), 0, String.class);
  }
}
