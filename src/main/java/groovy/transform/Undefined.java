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
package groovy.transform;

import org.codehaus.groovy.ast.ClassNode;

import java.io.Serial;

/**
 * Java doesn't allow you to have null as an attribute value. It wants you to indicate what you really
 * mean by null, so that is what we do here - as ugly as it is.
 */
public final class Undefined {
    private Undefined() {}

    /**
     * Sentinel string value used when an annotation member has no explicit string value.
     */
    public static final String STRING = "<DummyUndefinedMarkerString-DoNotUse>";

    /**
     * Sentinel type used when an annotation member has no explicit class value.
     */
    public static final class CLASS {}

    /**
     * Sentinel exception type used when an annotation member has no explicit exception value.
     */
    public static final class EXCEPTION extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -3960500360386581172L;
    }

    /**
     * Tests whether the supplied string is the undefined sentinel.
     *
     * @param other the value to test
     * @return {@code true} if the value is undefined
     */
    public static boolean isUndefined(String other) { return STRING.equals(other); }

    /**
     * Tests whether the supplied class node is the undefined class sentinel.
     *
     * @param other the class node to test
     * @return {@code true} if the value is undefined
     */
    public static boolean isUndefined(ClassNode other) { return CLASS.class.getName().equals(other.getName()); }

    /**
     * Tests whether the supplied class node is the undefined exception sentinel.
     *
     * @param other the class node to test
     * @return {@code true} if the value is undefined
     */
    public static boolean isUndefinedException(ClassNode other) { return EXCEPTION.class.getName().equals(other.getName()); }
}
