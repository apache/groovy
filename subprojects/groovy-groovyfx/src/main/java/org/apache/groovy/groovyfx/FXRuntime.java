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
package org.apache.groovy.groovyfx;

/**
 * Checks that JavaFX is available before any class that links against it is
 * used, so that a missing runtime is reported with instructions rather than a
 * bare {@code NoClassDefFoundError}. This class deliberately references no
 * JavaFX type.
 */
public final class FXRuntime {

    static final String MISSING_MESSAGE = "JavaFX is not available on the classpath or module path. "
            + "groovy-groovyfx does not bundle it (JavaFX is GPL-2.0 with the Classpath Exception and published "
            + "per platform). Supply it in one of these ways: run on a JDK that includes JavaFX (for example "
            + "Azul Zulu FX or BellSoft Liberica Full); add the org.openjfx:javafx-controls artifact for your "
            + "platform to your build; put the JavaFX SDK on the module path with --module-path <sdk>/lib "
            + "--add-modules javafx.controls; or in a script use @Grab with the platform classifier, "
            + "e.g. @Grab(group='org.openjfx', module='javafx-controls', version='21.0.8', classifier='linux').";

    private FXRuntime() {
    }

    /**
     * Whether the JavaFX runtime can be loaded.
     *
     * @return {@code true} when {@code javafx.application.Platform} is loadable
     */
    public static boolean isAvailable() {
        try {
            Class.forName("javafx.application.Platform", false, FXRuntime.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    /**
     * Fails with instructions on how to obtain JavaFX when it is not available.
     *
     * @throws IllegalStateException when the JavaFX runtime cannot be loaded
     */
    public static void requireJavaFX() {
        if (!isAvailable()) {
            throw new IllegalStateException(MISSING_MESSAGE);
        }
    }
}
