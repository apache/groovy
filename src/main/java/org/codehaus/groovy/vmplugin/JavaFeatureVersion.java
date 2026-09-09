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
package org.codehaus.groovy.vmplugin;

/**
 * The Java feature version of the running VM, e.g. {@code 17} (GROOVY-12382).
 * <p>
 * {@code Runtime.version()} is a Java 9 API that some runtimes built on the
 * JDK class library do not provide (Android's ART, for one). Where it is
 * missing, the {@code java.specification.version} property is parsed instead,
 * and when that is unusable the lowest release Groovy supports is assumed.
 * <p>
 * Deliberately tiny and free of lambdas: it is consulted from static
 * initialisers that can run while the system class loader is being
 * instantiated ({@code -Djava.system.class.loader=groovy.lang.GroovyClassLoader}),
 * where an {@code invokedynamic} bootstrap is not yet permitted, and it must
 * not trigger the creation of the VM plugin.
 */
public final class JavaFeatureVersion {

    /** The lowest Java release Groovy runs on. */
    public static final int MINIMUM = 17;

    private JavaFeatureVersion() {
    }

    /**
     * Returns the feature version of the running VM.
     *
     * @return the feature version, never below {@link #MINIMUM}
     */
    public static int current() {
        try {
            return Runtime.version().feature();
        } catch (Throwable ignore) {
            // NoSuchMethodError on a runtime without Runtime.version()
        }
        return parse(System.getProperty("java.specification.version"), MINIMUM);
    }

    /**
     * Parses a {@code java.specification.version} value ({@code "1.8"},
     * {@code "17"}, {@code "0.9"} on Android) into a feature version.
     *
     * @param specificationVersion the property value, possibly {@code null}
     * @param minimum the value returned when the property is missing, unparseable or below it
     * @return the feature version
     */
    public static int parse(final String specificationVersion, final int minimum) {
        if (specificationVersion == null) return minimum;
        String spec = specificationVersion.trim();
        if (spec.startsWith("1.")) spec = spec.substring(2);
        int dot = spec.indexOf('.');
        if (dot > 0) spec = spec.substring(0, dot);
        try {
            int version = Integer.parseInt(spec);
            return Math.max(version, minimum);
        } catch (NumberFormatException e) {
            return minimum;
        }
    }
}
