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
package org.codehaus.groovy.vmplugin.v5;

import groovy.lang.EmptyRange;
import groovy.lang.GString;
import groovy.lang.IntRange;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.RangeInfo;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This class defines new Java 5 specific groovy methods which extend the normal
 * JDK classes inside the Groovy environment. Static methods are used with the
 * first parameter the destination class.
 */
public class PluginDefaultGroovyMethods extends DefaultGroovyMethodsSupport {

    /**
     * This method is called by the ++ operator for enums. It will invoke
     * Groovy's default next behaviour for enums do not have their own
     * next method.
     *
     * @param self an Enum
     * @return the next defined enum from the enum class
     */
    public static Object next(final Enum self) {
        for (Method method : self.getClass().getMethods()) {
            if (method.getName().equals("next") && method.getParameterCount() == 0) {
                return InvokerHelper.invokeMethod(self, "next", InvokerHelper.EMPTY_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", InvokerHelper.EMPTY_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index < values.length - 1 ? index + 1 : 0];
    }

    /**
     * This method is called by the -- operator for enums. It will invoke
     * Groovy's default previous behaviour for enums that do not have
     * their own previous method.
     *
     * @param self an Enum
     * @return the previous defined enum from the enum class
     */
    public static Object previous(final Enum self) {
        for (Method method : self.getClass().getMethods()) {
            if (method.getName().equals("previous") && method.getParameterCount() == 0) {
                return InvokerHelper.invokeMethod(self, "previous", InvokerHelper.EMPTY_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", InvokerHelper.EMPTY_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index > 0 ? index - 1 : values.length - 1];
    }

    /**
     * Standard Groovy size() method for StringBuilders.
     *
     * @param builder a StringBuilder
     * @return the length of the StringBuilder
     */
    public static int size(final StringBuilder builder) {
        return builder.length();
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value a value to append
     * @return the StringBuilder on which this operation was invoked
     */
    public static StringBuilder leftShift(final StringBuilder self, final Object value) {
        if (value instanceof GString) {
            // Force the conversion of the GString to string now, or appending
            // is going to be extremely expensive, due to calls to GString#charAt,
            // which is going to re-evaluate the GString for each character!
            return self.append(value.toString());
        }
        if (value instanceof CharSequence) {
            return self.append((CharSequence)value);
        }
        return self.append(value);
    }

    /**
     * Support the range subscript operator for StringBuilder.
     * Index values are treated as characters within the builder.
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     */
    public static void putAt(final StringBuilder self, final IntRange range, final Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Support the range subscript operator for StringBuilder.
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     */
    public static void putAt(final StringBuilder self, final EmptyRange range, final Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Appends a String to this StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value a String
     * @return a String
     */
    public static String plus(final StringBuilder self, final String value) {
        return self + value;
    }
}
