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
package org.apache.groovy.ginq.provider.collection.runtime

import groovy.transform.PackageScope

/**
 * Represents null object for GINQ
 *
 * @since 4.0.0
 */
@PackageScope
class Null {
    /** Shared null-object instance. */
    public static final Null INSTANCE = new Null()

    private Null() {}

    /**
     * Ignores missing instance methods and returns {@code null}.
     *
     * @param name the missing method name
     * @param args the method arguments
     * @return {@code null}
     */
    def methodMissing(String name, def args) {
        return null
    }

    /**
     * Ignores missing instance properties and returns {@code null}.
     *
     * @param name the missing property name
     * @return {@code null}
     */
    def propertyMissing(String name) {
        return null
    }

    /**
     * Ignores writes to missing instance properties.
     *
     * @param name the missing property name
     * @param value the ignored value
     * @return {@code null}
     */
    def propertyMissing(String name, def value) {
        return null
    }

    /**
     * Ignores missing static methods and returns {@code null}.
     *
     * @param name the missing method name
     * @param args the method arguments
     * @return {@code null}
     */
    static $static_methodMissing(String name, def args) {
        return null
    }

    /**
     * Ignores missing static properties and returns {@code null}.
     *
     * @param name the missing property name
     * @return {@code null}
     */
    static $static_propertyMissing(String name) {
        return null
    }

    /**
     * Ignores writes to missing static properties.
     *
     * @param name the missing property name
     * @param value the ignored value
     * @return {@code null}
     */
    static $static_propertyMissing(String name, def value) {
        return null
    }

    /**
     * Evaluates this null object as {@code false}.
     *
     * @return {@code false}
     */
    boolean asBoolean() {
        return false
    }

    /**
     * Returns this null object for any coercion target.
     *
     * @param c the requested target type
     * @return this instance
     */
    def asType(Class c) {
        return this
    }

    /**
     * Checks identity against another object.
     *
     * @param other the object to compare with
     * @return {@code true} if both references are identical
     */
    boolean is(Object other) {
        return other === this
    }

    /**
     * Returns the stable hash code for the null object.
     *
     * @return {@code 0}
     */
    @Override
    int hashCode() {
        return 0
    }

    /**
     * Tests whether the other object is the same null-object instance.
     *
     * @param obj the other object
     * @return {@code true} if it is this instance
     */
    @Override
    boolean equals(Object obj) {
        return obj === this
    }

    /**
     * Returns this singleton instance.
     *
     * @return this instance
     */
    @Override
    Object clone() {
        return this
    }
}
