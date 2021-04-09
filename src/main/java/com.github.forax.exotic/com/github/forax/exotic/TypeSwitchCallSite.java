package com.github.forax.exotic;

import static com.github.forax.exotic.TypeSwitch.NO_MATCH;
import static com.github.forax.exotic.TypeSwitch.NULL_MATCH;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;

final class TypeSwitchCallSite extends MutableCallSite {
  static void validatePartialOrder(Class<?>[] cases) {
    if (cases.length < 2) return;
    HashMap<Class<?>, Class<?>> map = new HashMap<>();
    for (int i = cases.length; --i >= 0; )
      validateClass(map, Objects.requireNonNull(cases[i]));
  }

  private static void validateClass(HashMap<Class<?>, Class<?>> map, Class<?> classCase) {
    Class<?> conflictingCase = map.putIfAbsent(classCase, classCase);
    if (conflictingCase != null)
      throw new IllegalStateException(
          "Case " + conflictingCase.getName() + " matches a subtype of what case " +
          classCase.getName() + " matches but is located after it");
    validateParents(map, classCase, classCase);
  }

  private static void validateParents(HashMap<Class<?>, Class<?>> map, Class<?> parent, Class<?> classCase) {
    if (parent == null || map.putIfAbsent(parent, classCase) != null || parent == Object.class) return;
    validateParents(map, parent.getSuperclass(), classCase);
    for (Class<?> superinterface : parent.getInterfaces())
      validateParents(map, superinterface, classCase);
  }

  private static abstract class Strategy {
    abstract int index(Class<?> receiverClass);
    abstract MethodHandle target();
  }

  private static final class IsInstanceStrategy extends Strategy {
    private final WeakReference<?>[] refs;

    IsInstanceStrategy(Class<?>[] cases) {
      refs = createRefArray(cases);
    }

    int index(Class<?> receiverClass) {
      for (int i = 0; i < refs.length; i++) {
        Class<?> case1 = (Class<?>) refs[i].get();
        if (case1 != null && case1.isAssignableFrom(receiverClass)) return i;
      }
      return NO_MATCH;
    }

    MethodHandle target() {
      MethodHandle next = constInt(NO_MATCH);
      for (int i = refs.length; --i >= 0; ) {
        Class<?> case1 = (Class<?>) refs[i].get();
        if (case1 != null) next = guardWithTest(IS_INSTANCE.bindTo(case1), constInt(i), next);
      }
      return next;
    }
  }

  private static final class ClassValueStrategy extends Strategy {
    private static final MethodHandle GET;

    static {
      Lookup lookup = lookup();
      try {
        GET = lookup.findStatic(lookup.lookupClass(), "get", methodType(int.class, ClassValue.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    private final ClassValue<Integer> classValue;

    ClassValueStrategy(Class<?>[] cases) {
      classValue = createClassValue(cases);
    }

    @SuppressWarnings("unused")
    static int get(ClassValue<Integer> classValue, Object value) {
      return classValue.get(value.getClass());
    }

    int index(Class<?> receiverClass) { return classValue.get(receiverClass); }
    MethodHandle target()             { return GET.bindTo(classValue); }
  }

  static WeakReference<Class<?>>[] createRefArray(Class<?>[] cases) {
    @SuppressWarnings("unchecked")
    WeakReference<Class<?>>[] refs = (WeakReference<Class<?>>[]) new WeakReference<?>[cases.length];
    for (int i = 0; i < cases.length; i++)
      refs[i] = new WeakReference<>(cases[i]);
    return refs;
  }

  static ClassValue<Integer> createClassValue(Class<?>[] cases) {
    ThreadLocal<Integer> local     = new ThreadLocal<>();
    ClassValue<Integer> classValue = new ClassValue<Integer>() {
      protected Integer computeValue(Class<?> type) {
        Integer index = local.get();
        if (index != null) return index;
        Class<?> superclass = type.getSuperclass();
        index = superclass == null ? NO_MATCH : get(superclass);
        for (Class<?> supertype: type.getInterfaces()) {
          int localIndex = get(supertype);
          if (localIndex != NO_MATCH)
            index = index == NO_MATCH ? localIndex : Math.min(index, localIndex);
        }
        return index;
      }
    };
    for (int i = 0; i < cases.length; i++) {
      local.set(i);
      classValue.get(cases[i]);
    }
    local.remove();
    return classValue;
  }

  private static final MethodType OBJECT_TO_INT = methodType(int.class, Object.class);
          static final MethodHandle FALLBACK, IS_INSTANCE, NULLCHECK;

  static {
    Lookup lookup = lookup();
    try {
      FALLBACK    = lookup.findVirtual(lookup.lookupClass(), "fallback", OBJECT_TO_INT);
      IS_INSTANCE = lookup.findVirtual( Class.class,         "isInstance", methodType(boolean.class, Object.class));
      NULLCHECK   = lookup.findStatic (Objects.class,        "isNull",     methodType(boolean.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final Strategy        strategy;
  private final MutableCallSite topCallsite;
  private final int             depth;

  TypeSwitchCallSite(Strategy strategy, MutableCallSite topCallsite, int depth) {
    super(OBJECT_TO_INT);
    setTarget(FALLBACK.bindTo(this));
    this.strategy    = strategy;
    this.topCallsite = topCallsite == null ? this : topCallsite;
    this.depth       = depth;
  }

  static TypeSwitchCallSite bootstrap(Class<?>[] cases) {
    validatePartialOrder(cases);
    Strategy strategy = cases.length < 5 ? new IsInstanceStrategy(cases) : new ClassValueStrategy(cases);
    return new TypeSwitchCallSite(strategy, null, 0);
  }

  static MethodHandle wrapNullIfNecessary(boolean nullMatch, MethodHandle mh) {
    return nullMatch ? guardWithTest(NULLCHECK, constInt(NULL_MATCH), mh) : mh;
  }

  @SuppressWarnings("unused")
  private int fallback(Object value) {
    Class<?> receiverClass = value.getClass();
    int index = strategy.index(receiverClass);
    if (depth == 8) {
      topCallsite.setTarget(strategy.target());
    } else {
      MethodHandle next = new TypeSwitchCallSite(strategy, topCallsite, depth + 1).dynamicInvoker();
      setTarget(guardWithTest(IS_INSTANCE.bindTo(receiverClass), constInt(index), next));
    }
    return index;
  }

  private static MethodHandle constInt(int value) {
    return dropArguments(constant(int.class, value), 0, Object.class);
  }
}
