/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.vmplugin.v5;

import groovy.lang.IntRange;
import groovy.lang.EmptyRange;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;

import java.util.Arrays;
import java.lang.reflect.Method;

/**
 * This class defines new Java 5 specific groovy methods which extend the normal
 * JDK classes inside the Groovy environment. Static methods are used with the
 * first parameter the destination class.
 */
public class PluginDefaultGroovyMethods extends DefaultGroovyMethodsSupport {
    private static final Object[] NO_ARGS = new Object[0];

    /**
     * This method is called by the ++ operator for enums. It will invoke
     * Groovy's default next behaviour for enums do not have their own
     * next method (Only works with JDK1.5 or later).
     *
     * @param self an Enum
     * @return the next defined enum from the enum class
     */
    public static Object next(Enum self) {
        final Method[] methods = self.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals("next") && method.getParameterTypes().length == 0) {
                return InvokerHelper.invokeMethod(self, "next", NO_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", NO_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index < values.length - 1 ? index + 1 : 0];
    }

    /**
     * This method is called by the -- operator for enums. It will invoke
     * Groovy's default previous behaviour for enums that do not have
     * their own previous method (Only works with JDK1.5 or later).
     *
     * @param self an Enum
     * @return the previous defined enum from the enum class
     */
    public static Object previous(Enum self) {
        final Method[] methods = self.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals("previous") && method.getParameterTypes().length == 0) {
                return InvokerHelper.invokeMethod(self, "previous", NO_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", NO_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index > 0 ? index - 1 : values.length - 1];
    }

    /**
     * Standard Groovy size() method for StringBuilders
     * (Only works with JDK1.5 or later).
     *
     * @param builder a StringBuilder
     * @return the length of the StringBuilder
     */
    public static int size(StringBuilder builder) {
        return builder.length();
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a StringBuilder (Only works with JDK1.5 or later).
     *
     * @param self  a StringBuilder
     * @param value a value to append
     * @return the StringBuilder on which this operation was invoked
     */
    public static StringBuilder leftShift(StringBuilder self, Object value) {
        if (value instanceof CharSequence)
            return self.append((CharSequence)value);
        else
            return self.append(value);
    }

    /**
     * Support the range subscript operator for StringBuilder (Only works with JDK1.5 or later).
     * Index values are treated as characters within the builder.
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     */
    public static void putAt(StringBuilder self, IntRange range, Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Support the range subscript operator for StringBuilder (Only works with JDK1.5 or later).
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     */
    public static void putAt(StringBuilder self, EmptyRange range, Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Appends a String to this StringBuilder (Only works with JDK1.5 or later).
     *
     * @param self  a StringBuilder
     * @param value a String
     * @return a String
     */
    public static String plus(StringBuilder self, String value) {
        return self + value;
    }

}
