/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.reflection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains utility methods to determine which class called the
 * current class to multiple levels of depth.  Calls used to handle the
 * groovy MOP are excluded from the level counting.
 */
public class ReflectionUtils {

    // these are packages in the call stack that are only part of the groovy MOP
    private static final Set<String> ignoredPackages = new HashSet<String>();
    static {
        //ignoredPackages.add("java.lang.reflect");
        ignoredPackages.add("groovy.lang");
        ignoredPackages.add("org.codehaus.groovy.reflection");
        ignoredPackages.add("org.codehaus.groovy.runtime.callsite");
        ignoredPackages.add("org.codehaus.groovy.runtime.metaclass");
        ignoredPackages.add("org.codehaus.groovy.runtime");
        ignoredPackages.add("sun.reflect");
    }

    /**
     * Get the immediate calling class, ignoring MOP frames.
     * @return The Class of the caller
     */
    public static Class getCallingClass() {
        return getCallingClass(1);
    }

    /**
     * Get the called that is matchLevel stack frames before the call,
     * ignoring MOP frames.
     * @param matchLevel how may call stacks down to look.
     *      If it is less than 1 it is treated as though it was 1.
     * @return The Class of the matched caller, or null if there aren't
     *   enough stackframes to satisfy matchLevel
     */
    public static Class getCallingClass(int matchLevel) {
        int depth = 0;
        try {
            Class c;
            do {
                do {
                    c = sun.reflect.Reflection.getCallerClass(depth++);
                } while ((c != null)
                    && (c.isSynthetic()
                        || (c.getPackage() != null
                            && ignoredPackages.contains(c.getPackage().getName()))));
            } while (c != null && matchLevel-- > 0);
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the called that is matchLevel stack frames before the call,
     * ignoring MOP frames and desired exclude packages.
     * @param matchLevel how may call stacks down to look.
     *      If it is less than 1 it is treated as though it was 1.
     * @param extraIgnoredPackages A collection of string names of packages to exclude
     *   in addition to the MOP packages when counting stack frames.
     * @return The Class of the matched caller, or null if there aren't
     *   enough stackframes to satisfy matchLevel
     */
    public static Class getCallingClass(int matchLevel, Collection<String> extraIgnoredPackages) {
        int depth = 0;
        try {
            Class c;
            do {
                do {
                    c = sun.reflect.Reflection.getCallerClass(depth++);
                } while ((c != null)
                    && (c.isSynthetic()
                        || (c.getPackage() != null
                            && (ignoredPackages.contains(c.getPackage().getName())
                              || extraIgnoredPackages.contains(c.getPackage().getName())))));
            } while (c != null && matchLevel-- > 0);
            return c;
        } catch (Exception e) {
            return null;
        }
    }
}
