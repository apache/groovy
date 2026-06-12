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
package org.apache.groovy.groovysh.util

import org.apache.groovy.groovysh.jline.GroovyEngine

/**
 * Utility methods for probing whether classes are available to the shell or current JVM.
 */
class ClassUtils {
    private ClassUtils() {
        // utility class
    }

    /**
     * Determines whether the current JVM can resolve the named class.
     *
     * @param s fully qualified class name to resolve
     * @return {@code true} if the class can be loaded
     */
    static boolean lookFor(String s) {
        try {
            Class.forName(s)
            return true
        } catch (Exception ignore) {
            return false
        }
    }

    /**
     * Determines whether the shell script engine can resolve the named class.
     *
     * @param engine script engine used to evaluate the lookup
     * @param s fully qualified class name to resolve
     * @return {@code true} if the class can be loaded in the engine context
     */
    static boolean lookFor(GroovyEngine engine, String s) {
        try {
            engine.execute("Class.forName('$s')")
            return true
        } catch (Exception ignore) {
            return false
        }
    }
}
