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

import groovy.lang.IntRange;
import groovy.lang.EmptyRange;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.StringGroovyMethods;

/**
 * This class defines new Java 5 specific groovy methods which extend the normal
 * JDK classes inside the Groovy environment. Static methods are used with the
 * first parameter the destination class.
 *
 * @deprecated retained for compatibility
 */
@Deprecated
public class PluginDefaultGroovyMethods extends DefaultGroovyMethodsSupport {

    /**
     * This method is called by the ++ operator for enums. It will invoke
     * Groovy's default next behaviour for enums do not have their own
     * next method.
     *
     * @param self an Enum
     * @return the next defined enum from the enum class
     * @deprecated use {@link DefaultGroovyMethods#next(Enum)}
     */
    @Deprecated
    public static Object next(Enum self) {
        return DefaultGroovyMethods.next(self);
    }

    /**
     * This method is called by the -- operator for enums. It will invoke
     * Groovy's default previous behaviour for enums that do not have
     * their own previous method.
     *
     * @param self an Enum
     * @return the previous defined enum from the enum class
     * @deprecated use {@link DefaultGroovyMethods#previous(Enum)}
     */
    @Deprecated
    public static Object previous(Enum self) {
        return DefaultGroovyMethods.previous(self);
    }

    /**
     * Standard Groovy size() method for StringBuilders.
     *
     * @param builder a StringBuilder
     * @return the length of the StringBuilder
     * @deprecated use {@link StringGroovyMethods#size(StringBuilder)}
     */
    @Deprecated
    public static int size(StringBuilder builder) {
        return StringGroovyMethods.size(builder);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value a value to append
     * @return the StringBuilder on which this operation was invoked
     * @deprecated use {@link StringGroovyMethods#leftShift(StringBuilder, Object)}
     */
    @Deprecated
    public static StringBuilder leftShift(StringBuilder self, Object value) {
        return StringGroovyMethods.leftShift(self, value);
    }

    /**
     * Support the range subscript operator for StringBuilder.
     * Index values are treated as characters within the builder.
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     * @deprecated use {@link StringGroovyMethods#putAt(StringBuilder, IntRange, Object)}
     */
    @Deprecated
    public static void putAt(StringBuilder self, IntRange range, Object value) {
        StringGroovyMethods.putAt(self, range, value);
    }

    /**
     * Support the range subscript operator for StringBuilder.
     *
     * @param self  a StringBuilder
     * @param range a Range
     * @param value the object that's toString() will be inserted
     * @deprecated use {@link StringGroovyMethods#putAt(StringBuffer, EmptyRange, Object)}
     */
    @Deprecated
    public static void putAt(StringBuilder self, EmptyRange range, Object value) {
        StringGroovyMethods.putAt(self, range, value);
    }

    /**
     * Appends a String to this StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value a String
     * @return a String
     * @deprecated use {@link StringGroovyMethods#plus(StringBuilder, String)}
     */
    @Deprecated
    public static String plus(StringBuilder self, String value) {
        return StringGroovyMethods.plus(self, value);
    }

}
