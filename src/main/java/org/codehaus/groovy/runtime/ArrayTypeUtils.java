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

package org.codehaus.groovy.runtime;

/**
 * Utilities for handling array types
 */
public class ArrayTypeUtils {

    /**
     * Calculate the dimension of array
     *
     * @param clazz the type of array
     * @return the dimension of array
     */
    public static int dimension(Class clazz) {
        checkArrayType(clazz);

        int result = 0;
        while (clazz.isArray()) {
            result++;
            clazz = clazz.getComponentType();
        }

        return result;
    }

    /**
     * Get the type of array elements
     *
     * @param clazz the type of array
     * @return the type of elements
     */
    public static Class elementType(Class clazz) {
        checkArrayType(clazz);

        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }

        return clazz;
    }

    /**
     * Get the type of array elements by the dimension
     *
     * @param clazz the type of array
     * @param dim the target dimension
     * @return the result array
     */
    public static Class elementType(Class clazz, int dim) {
        checkArrayType(clazz);

        if (dim < 0) {
            throw new IllegalArgumentException("The target dimension should not be less than zero: " + dim);
        }

        while (clazz.isArray() && dimension(clazz) > dim) {
            clazz = clazz.getComponentType();
        }

        return clazz;
    }

    /**
     * Check whether the type passed in is array type.
     * If the type is not array type, throw IllegalArgumentException.
     */
    private static void checkArrayType(Class clazz) {
        if (null == clazz) {
            throw new IllegalArgumentException("clazz can not be null");
        }

        if (!clazz.isArray()) {
            throw new IllegalArgumentException(clazz.getCanonicalName() + " is not array type");
        }
    }
}
