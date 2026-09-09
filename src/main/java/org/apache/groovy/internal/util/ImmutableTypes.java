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
package org.apache.groovy.internal.util;

import groovy.transform.Internal;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * The types and marker annotations Groovy treats as immutable, shared by the
 * {@code @Immutable} family of transforms and by the runtime (GROOVY-12390).
 * <p>
 * Lives outside the AST packages on purpose: {@code GStringImpl} consults it
 * for every GString with a non-trivial value, and reaching it through the
 * transform utilities would initialise the compiler's {@code ClassHelper},
 * with its reflective model of the JDK types, in an application that never
 * compiles anything. Internal to the runtime, not a public API.
 */
@Internal
public final class ImmutableTypes {

    /**
     * Currently leaving BigInteger and BigDecimal in list but see:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6348370
     *
     * Also, Color is not final so while not normally used with child
     * classes, it isn't strictly immutable. Use at your own risk.
     *
     * This list can be extended by providing "known immutable" classes
     * via Immutable.knownImmutableClasses
     */
    private static final Set<String> BUILTIN_IMMUTABLES = Set.of(
            "boolean",
            "byte",
            "char",
            "double",
            "float",
            "int",
            "long",
            "short",
            "java.lang.Class",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.String",
            "java.math.BigInteger",
            "java.math.BigDecimal",
            "java.awt.Color",
            "java.net.URI",
            "java.util.UUID",
            "java.util.regex.Pattern",
            "java.time.DayOfWeek",
            "java.time.Duration",
            "java.time.Instant",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.Month",
            "java.time.MonthDay",
            "java.time.OffsetDateTime",
            "java.time.OffsetTime",
            "java.time.Period",
            "java.time.Year",
            "java.time.YearMonth",
            "java.time.ZonedDateTime",
            "java.time.ZoneOffset",
            "java.time.ZoneRegion",
            "java.time.chrono.ChronoLocalDate",
            "java.time.chrono.ChronoLocalDateTime",
            "java.time.chrono.Chronology",
            "java.time.chrono.ChronoPeriod",
            "java.time.chrono.ChronoZonedDateTime",
            "java.time.chrono.Era",
            "java.time.format.DecimalStyle",
            "java.time.format.FormatStyle",
            "java.time.format.ResolverStyle",
            "java.time.format.SignStyle",
            "java.time.format.TextStyle",
            "java.time.temporal.IsoFields",
            "java.time.temporal.JulianFields",
            "java.time.temporal.ValueRange",
            "java.time.temporal.WeekFields",
            "java.io.File"
    );

    private static final Set<String> BUILTIN_IMMUTABLE_ANNOTATIONS = Set.of(
            "groovy.transform.Immutable",
            "groovy.transform.KnownImmutable",
          //"javax.annotation.concurrent.Immutable", // its RetentionPolicy is CLASS, can not be got via reflection
            "net.jcip.annotations.Immutable" // supported by Findbugs and IntelliJ IDEA
    );

    private ImmutableTypes() {
    }

    /**
     * Whether a type name is one of the JDK types Groovy treats as immutable.
     *
     * @param typeName the fully qualified type name
     * @return {@code true} for a built-in immutable type
     */
    public static boolean isBuiltinImmutable(final String typeName) {
        return BUILTIN_IMMUTABLES.contains(typeName);
    }

    /**
     * Whether an annotation name marks its target as immutable.
     *
     * @param annotationName the fully qualified annotation type name
     * @return {@code true} for a recognised immutable marker
     */
    public static boolean isImmutableMarker(final String annotationName) {
        return BUILTIN_IMMUTABLE_ANNOTATIONS.contains(annotationName);
    }

    /**
     * Whether a class carries a recognised immutable marker annotation.
     *
     * @param clazz the class to inspect
     * @return {@code true} when a marker is present
     */
    public static boolean hasImmutableAnnotation(final Class<?> clazz) {
        for (Annotation next : clazz.getAnnotations()) {
            if (isImmutableMarker(next.annotationType().getName())) return true;
        }
        return false;
    }

    /**
     * Whether a class is built in as immutable or carries an immutable marker annotation.
     *
     * @param clazz the class to inspect
     * @return {@code true} when the class is considered immutable
     */
    public static boolean builtinOrMarkedImmutableClass(final Class<?> clazz) {
        return isBuiltinImmutable(clazz.getName()) || hasImmutableAnnotation(clazz);
    }
}
