/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.reflection;

import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * This class contains utility methods to determine which class called the
 * current class to multiple levels of depth.  Calls used to handle the
 * groovy MOP are excluded from the level counting.
 */
@SuppressWarnings("rawtypes")
public class ReflectionUtils {
    private static final VMPlugin VM_PLUGIN = VMPluginFactory.getPlugin();

    /** The packages in the call stack that are only part of the Groovy MOP. */
    private static final Set<String> IGNORED_PACKAGES;
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    static {
        Set<String> set = new HashSet<>();

        set.add("groovy.lang");
        set.add("sun.reflect");
        set.add("java.security");
        set.add("java.lang.invoke");
      //set.add("java.lang.reflect");
        set.add("org.codehaus.groovy.reflection");
        set.add("org.codehaus.groovy.runtime");
        set.add("org.codehaus.groovy.runtime.callsite");
        set.add("org.codehaus.groovy.runtime.metaclass");
        set.add("org.codehaus.groovy.vmplugin.v5");
        set.add("org.codehaus.groovy.vmplugin.v6");
        set.add("org.codehaus.groovy.vmplugin.v7");
        set.add("org.codehaus.groovy.vmplugin.v8");
        set.add("org.codehaus.groovy.vmplugin.v9");
        set.add("org.codehaus.groovy.vmplugin.v10");
        set.add("org.codehaus.groovy.vmplugin.v16");

        IGNORED_PACKAGES = Collections.unmodifiableSet(set);
    }

    private static final ClassContextHelper HELPER = new ClassContextHelper();

    /**
     * Determines whether or not the getCallingClass methods will return
     * any sensible results.  On JVMs that are not Sun derived i.e.
     * (gcj, Harmony) this will likely return false.  When not available
     * all getCallingClass methods will return null.
     *
     * @return true if getCallingClass can return anything but null, false if
     *         it will only return null.
     */
    public static boolean isCallingClassReflectionAvailable() {
        return true;
    }

    /**
     * Gets the immediate calling class, ignoring MOP frames.
     *
     * @return The Class of the caller
     */
    public static Class getCallingClass() {
        return getCallingClass(1);
    }

    /**
     * Gets the called that is matchLevel stack frames before the call,
     * ignoring MOP frames.
     *
     * @param matchLevel how may call stacks down to look.
     *                   If it is less than 1 it is treated as though it was 1.
     * @return The Class of the matched caller, or null if there aren't
     *         enough stackframes to satisfy matchLevel
     */
    public static Class getCallingClass(final int matchLevel) {
        return getCallingClass(matchLevel, Collections.emptySet());
    }

    /**
     * Gets the called that is matchLevel stack frames before the call,
     * ignoring MOP frames and desired exclude packages.
     *
     * @param matchLevel           how may call stacks down to look.
     *                             If it is less than 1 it is treated as though it was 1.
     * @param extraIgnoredPackages A collection of string names of packages to exclude
     *                             in addition to the MOP packages when counting stack frames.
     * @return The Class of the matched caller, or null if there aren't
     *         enough stackframes to satisfy matchLevel
     */
    public static Class getCallingClass(final int matchLevel, final Collection<String> extraIgnoredPackages) {
        Class[] classContext = HELPER.getClassContext();
        int depth = 0, level = matchLevel;
        try {
            Class c;
            do {
                do {
                    c = classContext[depth++];
                } while (classShouldBeIgnored(c, extraIgnoredPackages));
            } while (c != null && level-- > 0 && depth < classContext.length);
            return c;
        } catch (Throwable ignore) {
            return null;
        }
    }

    public static List<Method> getDeclaredMethods(final Class<?> type, final String name, final Class<?>... parameterTypes) {
        return doGetMethods(type, name, parameterTypes, Class::getDeclaredMethods);
    }

    public static List<Method> getMethods(final Class<?> type, final String name, final Class<?>... parameterTypes) {
        return doGetMethods(type, name, parameterTypes, Class::getMethods);
    }

    private static List<Method> doGetMethods(final Class<?> type, final String name, final Class<?>[] parameterTypes, final Function<? super Class<?>, ? extends Method[]> f) {
        List<Method> methodList = new LinkedList<>();

        for (Method m : f.apply(type)) {
            if (!m.getName().equals(name)) {
                continue;
            }
            Class<?>[] methodParameterTypes = m.getParameterTypes();
            if (!parameterTypeMatches(methodParameterTypes, parameterTypes)) {
                continue;
            }

            methodList.add(m);
        }

        return methodList;
    }

    public static boolean parameterTypeMatches(final Class<?>[] parameterTypes, final Class<?>[] argTypes) {
        if (parameterTypes.length != argTypes.length) {
            return false;
        }

        for (int i = 0, n = parameterTypes.length; i < n; i += 1) {
            Class<?> parameterType = parameterTypes[i];
            if (Object.class == parameterType) continue;

            Class<?> argType = argTypes[i];
            if (null == argType) return false;
            if (parameterType == argType) continue;

            Class<?> boxedArgType = TypeUtil.autoboxType(argType);
            Class<?> boxedParameterType = TypeUtil.autoboxType(parameterType);
            if (!boxedParameterType.isAssignableFrom(boxedArgType)) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkCanSetAccessible(final AccessibleObject accessibleObject, final Class<?> caller) {
        return VM_PLUGIN.checkCanSetAccessible(accessibleObject, caller);
    }

    public static boolean checkAccessible(final Class<?> callerClass, final Class<?> declaringClass, final int memberModifiers, final boolean allowIllegalAccess) {
        return VM_PLUGIN.checkAccessible(callerClass, declaringClass, memberModifiers, allowIllegalAccess);
    }

    public static boolean trySetAccessible(final AccessibleObject ao) {
        try {
            return VM_PLUGIN.trySetAccessible(ao);
        } catch (Throwable ignore) {
            // swallow for strict security managers, module systems, android or others
        }
        return false;
    }

    public static Optional<AccessibleObject> makeAccessibleInPrivilegedAction(final AccessibleObject ao) {
        return AccessController.doPrivileged((PrivilegedAction<Optional<AccessibleObject>>) () -> makeAccessible(ao));
    }

    // to be run in PrivilegedAction!
    public static Optional<AccessibleObject> makeAccessible(final AccessibleObject ao) {
        try {
            if (ao.isAccessible() || trySetAccessible(ao)) {
                return Optional.of(ao);
            }
        } catch (Throwable ignore) {
            // swallow for strict security managers, module systems, android, etc.
        }
        return Optional.empty();
    }

    // to be run in PrivilegedAction!
    public static AccessibleObject[] makeAccessible(final AccessibleObject[] aoa) {
        try {
            AccessibleObject.setAccessible(aoa, true);
            return aoa;
        } catch (Throwable ignore) {
            // swallow for strict security managers, module systems, android or others,
            // but try one-by-one to get the allowed ones at least
            List<AccessibleObject> ret = new ArrayList<>(aoa.length);
            for (final AccessibleObject ao : aoa) {
                boolean accessible = trySetAccessible(ao);
                if (accessible) {
                    ret.add(ao);
                }
            }
            return ret.toArray((AccessibleObject[]) Array.newInstance(aoa.getClass().getComponentType(), 0));
        }
    }

    public static boolean isSealed(Class<?> clazz) {
        if (null == IS_SEALED_METHODHANDLE) return false;
        if (null == clazz) return false;

        boolean sealed = false;
        try {
            sealed = (boolean) IS_SEALED_METHODHANDLE.invokeExact(clazz);
        } catch (Throwable ignored) {
        }
        return sealed;
    }

    public static Class<?>[] getPermittedSubclasses(Class<?> clazz) {
        if (null == GET_PERMITTED_SUBCLASSES_METHODHANDLE) return EMPTY_CLASS_ARRAY;
        if (null == clazz) return EMPTY_CLASS_ARRAY;

        Class<?>[] result = EMPTY_CLASS_ARRAY;
        try {
            result = (Class<?>[]) GET_PERMITTED_SUBCLASSES_METHODHANDLE.invokeExact(clazz);
        } catch (Throwable ignored) {
        }
        return result;
    }

    private static boolean classShouldBeIgnored(final Class c, final Collection<String> extraIgnoredPackages) {
        return (c != null
                && (c.isSynthetic()
                    || (c.getPackage() != null
                        && (IGNORED_PACKAGES.contains(c.getPackage().getName())
                          || extraIgnoredPackages.contains(c.getPackage().getName())))));
    }

    private static class ClassContextHelper extends SecurityManager {
        @Override
        public Class[] getClassContext() {
            return super.getClassContext();
        }
    }

    private static final MethodHandle IS_SEALED_METHODHANDLE;
    private static final MethodHandle GET_PERMITTED_SUBCLASSES_METHODHANDLE;
    static {
        MethodHandle isSealedMethodHandle = null;
        try {
            isSealedMethodHandle = MethodHandles.lookup().findVirtual(Class.class, "isSealed", MethodType.methodType(boolean.class, new Class[0]));
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }
        IS_SEALED_METHODHANDLE = isSealedMethodHandle;

        MethodHandle getPermittedSubclassesMethodHandle = null;
        try {
            getPermittedSubclassesMethodHandle = MethodHandles.lookup().findVirtual(Class.class, "getPermittedSubclasses", MethodType.methodType(Class[].class, new Class[0]));
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }
        GET_PERMITTED_SUBCLASSES_METHODHANDLE = getPermittedSubclassesMethodHandle;
    }
}
