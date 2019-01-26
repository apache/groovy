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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains utility methods to determine which class called the
 * current class to multiple levels of depth.  Calls used to handle the
 * groovy MOP are excluded from the level counting.
 */
public class ReflectionUtils {

    // these are packages in the call stack that are only part of the groovy MOP
    private static final Set<String> IGNORED_PACKAGES = new HashSet<>();

    static {
        //IGNORED_PACKAGES.add("java.lang.reflect");
        IGNORED_PACKAGES.add("groovy.lang");
        IGNORED_PACKAGES.add("org.codehaus.groovy.reflection");
        IGNORED_PACKAGES.add("org.codehaus.groovy.runtime.callsite");
        IGNORED_PACKAGES.add("org.codehaus.groovy.runtime.metaclass");
        IGNORED_PACKAGES.add("org.codehaus.groovy.runtime");
        IGNORED_PACKAGES.add("sun.reflect");
        IGNORED_PACKAGES.add("java.lang.invoke");
        IGNORED_PACKAGES.add("org.codehaus.groovy.vmplugin.v7");
    }

    private static final ClassContextHelper HELPER = new ClassContextHelper();

    /**
     * Determine whether or not the getCallingClass methods will return
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
     * Get the immediate calling class, ignoring MOP frames.
     *
     * @return The Class of the caller
     */
    public static Class getCallingClass() {
        return getCallingClass(1);
    }

    /**
     * Get the called that is matchLevel stack frames before the call,
     * ignoring MOP frames.
     *
     * @param matchLevel how may call stacks down to look.
     *                   If it is less than 1 it is treated as though it was 1.
     * @return The Class of the matched caller, or null if there aren't
     *         enough stackframes to satisfy matchLevel
     */
    public static Class getCallingClass(int matchLevel) {
        return getCallingClass(matchLevel, Collections.EMPTY_SET);
    }

    /**
     * Get the called that is matchLevel stack frames before the call,
     * ignoring MOP frames and desired exclude packages.
     *
     * @param matchLevel           how may call stacks down to look.
     *                             If it is less than 1 it is treated as though it was 1.
     * @param extraIgnoredPackages A collection of string names of packages to exclude
     *                             in addition to the MOP packages when counting stack frames.
     * @return The Class of the matched caller, or null if there aren't
     *         enough stackframes to satisfy matchLevel
     */
    public static Class getCallingClass(int matchLevel, Collection<String> extraIgnoredPackages) {
        Class[] classContext = HELPER.getClassContext();

        int depth = 0;
        try {
            Class c;
            // this super class stuff is for Java 1.4 support only
            // it isn't needed on a 5.0 VM
            Class sc;
            do {
                do {
                    c = classContext[depth++];
                    if (c != null) {
                        sc = c.getSuperclass();
                    } else {
                        sc = null;
                    }
                } while (classShouldBeIgnored(c, extraIgnoredPackages)
                        || superClassShouldBeIgnored(sc));
            } while (c != null && matchLevel-- > 0 && depth<classContext.length);
            return c;
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean superClassShouldBeIgnored(Class sc) {
        return ((sc != null) && (sc.getPackage() != null) && "org.codehaus.groovy.runtime.callsite".equals(sc.getPackage().getName()));
    }

    private static boolean classShouldBeIgnored(Class c, Collection<String> extraIgnoredPackages) {
        return ((c != null)
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
}
