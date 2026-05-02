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
package org.codehaus.groovy.util;

import java.util.Arrays;

/**
 * A utility class to help calculate hashcode values
 * using an algorithm similar to that outlined in
 * "Effective Java, Joshua Bloch, 2nd Edition".
 */
public class HashCodeHelper {
    private static final int SEED = 127;
    private static final int MULT = 59;

    /**
     * Returns the initial seed for a hash-code calculation sequence.
     *
     * @return the starting hash value
     */
    public static int initHash() {
        return SEED;
    }

    /**
     * Mixes a boolean value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, boolean var) {
        return shift(current) + (var ? 79 : 97);
    }

    /**
     * Mixes a character value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, char var) {
        return shift(current) + (int) var;
    }

    /**
     * Mixes a boxed character value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, Character var) {
        return updateHash(current, var == null ? 0 : var);
    }

    /**
     * Mixes an integer value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, int var) {
        return shift(current) + var;
    }

    /**
     * Mixes a boxed integer value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, Integer var) {
        return updateHash(current, var == null ? 0 : var);
    }

    /**
     * Mixes a long value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, long var) {
        return shift(current) + (int) (var ^ (var >>> 32));
    }

    /**
     * Mixes a boxed long value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, Long var) {
        return updateHash(current, var == null ? 0L : var);
    }

    /**
     * Mixes a float value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, float var) {
        return updateHash(current, Float.floatToIntBits(var));
    }

    /**
     * Mixes a boxed float value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, Float var) {
        return updateHash(current, var == null ? 0f : var);
    }

    /**
     * Mixes a double value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, double var) {
        return updateHash(current, Double.doubleToLongBits(var));
    }

    /**
     * Mixes a boxed double value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, Double var) {
        return updateHash(current, var == null ? 0d : var);
    }

    /**
     * Mixes an object or object-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, Object var) {
        if (var == null) return updateHash(current, 0);
        if (var.getClass().isArray())
            return shift(current) + Arrays.hashCode((Object[]) var);
        return updateHash(current, var.hashCode());
    }

    /**
     * Mixes a boolean-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, boolean[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    /**
     * Mixes a char-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, char[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    /**
     * Mixes a byte-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, byte[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    /**
     * Mixes a short-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, short[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    /**
     * Mixes an int-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, int[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    /**
     * Mixes a long-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, long[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    /**
     * Mixes a float-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, float[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    /**
     * Mixes a double-array value into an in-progress hash code.
     *
     * @param current the current hash value
     * @param var the value to mix in
     * @return the updated hash value
     */
    public static int updateHash(int current, double[] var) {
        if (var == null) return updateHash(current, 0);
        return shift(current) + Arrays.hashCode(var);
    }

    private static int shift(int current) {
        return MULT * current;
    }

}
