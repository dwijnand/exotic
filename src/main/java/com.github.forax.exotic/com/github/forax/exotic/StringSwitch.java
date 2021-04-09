package com.github.forax.exotic;

import java.lang.invoke.MethodHandle;

/**
 * A StringSwitch allows to encode a switch on strings as a plain old switch on integers.
 * For that, a StringSwitch is {@link #create(boolean, String...) created} with an array of strings
 * and will try to find for a string the index of the same string in the array.
 *
 * An example of usage, instead of using a cascade of 'if equals'
 * <pre>
 * public static String owner(String s) {
 *   if (s == null)                  return "no owner";
 *   if (s.equals("bernie the dog")) return "john";
 *   if (s.equals(  "zara the cat")) return "jane";
 *                                   return "unknown owner";
 * }
 * </pre>
 *
 * a StringSwitch allows to use a plain old switch to switch on string
 * <pre>
 * private static final StringSwitch STRING_SWITCH = StringSwitch.create(true, "bernie the dog", "zara the cat");
 *
 * public static String owner(String s) {
 *   switch (STRING_SWITCH.stringSwitch(s)) {
 *     case StringSwitch.NULL_MATCH: return "no owner";
 *     case                       0: return "john";
 *     case                       1: return "jane";
 *                          default: return "unknown owner"; // StringSwitch.BAD_MATCH
 *   }
 * }
 * </pre>
 */
@FunctionalInterface
public interface StringSwitch {
  /**
   * Returns the index of the first element in {@code cases} that matches {@code value},
   * {@value #NULL_MATCH} if {@code value} is null or {@link #NO_MATCH}.
   */
  int stringSwitch(String value);

  /** The value of {@link #stringSwitch(String)} that indicates that no match was found. */
  int NO_MATCH = -2;

  /** The value of {@link #stringSwitch(String)} that indicates that null was found. */
  int NULL_MATCH = -1;

  /**
   * Creates a StringSwitch that returns for a string the index in the {@code cases} array or {@link #NO_MATCH}.
   *
   * @param nullMatch true is the StringSwitch should allow null.
   * @param cases the cases to match against.
   * @return a StringSwitch configured with the array of cases.
   * @throws NullPointerException is {@code cases is null} or one element of the array is null.
   * @throws IllegalStateException if the same string appears several times in the array.
   */
  static StringSwitch create(boolean nullMatch, String... cases) {
    MethodHandle mh = StringSwitchCallSite.wrapNullIfNecessary(nullMatch,
      StringSwitchCallSite.bootstrap(cases).dynamicInvoker());
    return value -> {
      try {
        return (int)mh.invokeExact(value);
      } catch (Throwable t) {
        throw Thrower.rethrow(t);
      }
    };
  }
}
