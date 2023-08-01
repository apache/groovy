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

import org.codehaus.groovy.runtime.metaclass.DefaultMetaClassInfo;

/**
 * This class contains methods special to optimizations used directly from bytecode in Groovy 1.8
 */
public class BytecodeInterface8 {

    public static boolean disabledStandardMetaClass() {
        return DefaultMetaClassInfo.disabledStandardMetaClass();
    }

    // int ---------------------------------------------------------------------

    /**
     * @return true if integer has its default MetaClass
     */
    public static boolean isOrigInt() {
       return DefaultMetaClassInfo.isOrigInt();
    }

    /**
     * @return true if integer array has its default MetaClass
     */
    public static boolean isOrigIntArray() {
       return DefaultMetaClassInfo.isOrigIntArray();
    }

    /**
     * Gets value from int[] using normalized index.
     */
    public static int intArrayGet(int[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into int[] using normalized index.
     */
    public static void intArraySet(int[] a, int i, int v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // byte --------------------------------------------------------------------

    /**
     * @return true if byte has its default MetaClass
     */
    public static boolean isOrigB() {
       return DefaultMetaClassInfo.isOrigByte();
    }

    /**
     * @return true if byte array has its default MetaClass
     */
    public static boolean isOrigBArray() {
       return false;
    }

    /**
     * Gets value from byte[] using normalized index.
     */
    public static byte bArrayGet(byte[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into byte[] using normalized index.
     */
    public static void bArraySet(byte[] a, int i, byte v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // short -------------------------------------------------------------------

    /**
     * @return true if short has its default MetaClass
     */
    public static boolean isOrigS() {
       return DefaultMetaClassInfo.isOrigShort();
    }

    /**
     * @return true if short array has its default MetaClass
     */
    public static boolean isOrigSArray() {
       return false;
    }

    /**
     * Gets value from short[] using normalized index.
     */
    public static short sArrayGet(short[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into short[] using normalized index.
     */
    public static void sArraySet(short[] a, int i, short v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // char --------------------------------------------------------------------

    /**
     * @return true if char has its default MetaClass
     */
    public static boolean isOrigC() {
       return DefaultMetaClassInfo.isOrigChar();
    }

    /**
     * @return true if char array has its default MetaClass
     */
    public static boolean isOrigCArray() {
       return false;
    }

    /**
     * Gets value from char[] using normalized index.
     */
    public static char cArrayGet(char[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into char[] using normalized index.
     */
    public static void cArraySet(char[] a, int i, char v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // long --------------------------------------------------------------------

    /**
     * @return true if long has its default MetaClass
     */
    public static boolean isOrigL() {
       return DefaultMetaClassInfo.isOrigLong();
    }

    /**
     * @return true if long array has its default MetaClass
     */
    public static boolean isOrigLArray() {
       return false;
    }

    /**
     * Gets value from long[] using normalized index.
     */
    public static long lArrayGet(long[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into long[] using normalized index.
     */
    public static void lArraySet(long[] a, int i, long v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // boolean -----------------------------------------------------------------

    /**
     * @return true if boolean has its default MetaClass
     */
    public static boolean isOrigZ() {
       return DefaultMetaClassInfo.isOrigBool();
    }

    /**
     * @return true if boolean array has its default MetaClass
     */
    public static boolean isOrigZArray() {
       return false;
    }

    /**
     * Gets value from boolean[] using normalized index.
     */
    public static boolean zArrayGet(boolean[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into boolean[] using normalized index.
     */
    public static void zArraySet(boolean[] a, int i, boolean v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // float -------------------------------------------------------------------

    /**
     * @return true if float has its default MetaClass
     */
    public static boolean isOrigF() {
       return DefaultMetaClassInfo.isOrigFloat();
    }

    /**
     * @return true if float array has its default MetaClass
     */
    public static boolean isOrigFArray() {
       return false;
    }

    /**
     * Gets value from float[] using normalized index.
     */
    public static float fArrayGet(float[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into float[] using normalized index.
     */
    public static void fArraySet(float[] a, int i, float v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // double ------------------------------------------------------------------

    /**
     * @return true if double has its default MetaClass
     */
    public static boolean isOrigD() {
       return DefaultMetaClassInfo.isOrigDouble();
    }

    /**
     * @return true if double array has its default MetaClass
     */
    public static boolean isOrigDArray() {
       return false;
    }

    /**
     * Gets value from double[] using normalized index.
     */
    public static double dArrayGet(double[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into double[] using normalized index.
     */
    public static void dArraySet(double[] a, int i, double v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }

    // Object ------------------------------------------------------------------

    /**
     * Gets value from Object[] using normalized index.
     */
    public static Object objectArrayGet(Object[] a, int i) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        return a[i];
    }

    /**
     * Sets value into Object[] using normalized index.
     */
    public static void objectArraySet(Object[] a, int i, Object v) {
        if (i < 0) i = DefaultGroovyMethodsSupport.normaliseIndex(i, a.length);
        a[i] = v;
    }
}
