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
    
    // ------------------ int ------------------
    
    /**
     * @return true if integer has its default MetaClass
     */
    public static boolean isOrigInt(){
       return DefaultMetaClassInfo.isOrigInt(); 
    }

    // ------------------ int[] ------------------
    
    /**
     * @return true if integer array has its default MetaClass
     */
    public static boolean isOrigIntArray(){
       return DefaultMetaClassInfo.isOrigIntArray(); 
    }
    
    
    /**
     * get value from int[] using normalized index
     */
    public static int intArrayGet(int[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from int[] using normalized index
     */
    public static void intArraySet(int[] a, int i, int v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ byte ------------------
    
    /**
     * @return true if byte has its default MetaClass
     */
    public static boolean isOrigB(){
       return DefaultMetaClassInfo.isOrigByte(); 
    }

    // ------------------ byte[] ------------------
    
    /**
     * @return true if byte array has its default MetaClass
     */
    public static boolean isOrigBArray(){
       return false; 
    }
    
    
    /**
     * get value from byte[] using normalized index
     */
    public static byte bArrayGet(byte[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from byte[] using normalized index
     */
    public static void bArraySet(byte[] a, int i, byte v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ short ------------------
    
    /**
     * @return true if short has its default MetaClass
     */
    public static boolean isOrigS(){
       return DefaultMetaClassInfo.isOrigShort(); 
    }

    // ------------------ short[] ------------------
    
    /**
     * @return true if short array has its default MetaClass
     */
    public static boolean isOrigSArray(){
       return false; 
    }
    
    
    /**
     * get value from short[] using normalized index
     */
    public static short sArrayGet(short[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from short[] using normalized index
     */
    public static void sArraySet(short[] a, int i, short v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ char ------------------
    
    /**
     * @return true if char has its default MetaClass
     */
    public static boolean isOrigC(){
       return DefaultMetaClassInfo.isOrigChar(); 
    }

    // ------------------ char[] ------------------
    
    /**
     * @return true if char array has its default MetaClass
     */
    public static boolean isOrigCArray(){
       return false; 
    }
    
    
    /**
     * get value from char[] using normalized index
     */
    public static char cArrayGet(char[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from char[] using normalized index
     */
    public static void cArraySet(char[] a, int i, char v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ long ------------------
    
    /**
     * @return true if long has its default MetaClass
     */
    public static boolean isOrigL(){
       return DefaultMetaClassInfo.isOrigLong(); 
    }

    // ------------------ long[] ------------------
    
    /**
     * @return true if long array has its default MetaClass
     */
    public static boolean isOrigLArray(){
       return false; 
    }
    
    
    /**
     * get value from long[] using normalized index
     */
    public static long lArrayGet(long[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from long[] using normalized index
     */
    public static void lArraySet(long[] a, int i, long v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ boolean ------------------
    
    /**
     * @return true if boolean has its default MetaClass
     */
    public static boolean isOrigZ(){
       return DefaultMetaClassInfo.isOrigBool(); 
    }

    // ------------------ boolean[] ------------------
    
    /**
     * @return true if boolean array has its default MetaClass
     */
    public static boolean isOrigZArray(){
       return false; 
    }
    
    /**
     * get value from boolean[] using normalized index
     */
    public static boolean zArrayGet(boolean[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from boolean[] using normalized index
     */
    public static void zArraySet(boolean[] a, int i, boolean v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ float ------------------
    
    /**
     * @return true if float has its default MetaClass
     */
    public static boolean isOrigF(){
       return DefaultMetaClassInfo.isOrigFloat(); 
    }

    // ------------------ float[] ------------------
    
    /**
     * @return true if float array has its default MetaClass
     */
    public static boolean isOrigFArray(){
       return false; 
    }
    
    /**
     * get value from float[] using normalized index
     */
    public static float fArrayGet(float[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from float[] using normalized index
     */
    public static void fArraySet(float[] a, int i, float v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ double ------------------
    
    /**
     * @return true if double has its default MetaClass
     */
    public static boolean isOrigD(){
       return DefaultMetaClassInfo.isOrigDouble(); 
    }

    // ------------------ double[] ------------------
    
    /**
     * @return true if double array has its default MetaClass
     */
    public static boolean isOrigDArray(){
       return false; 
    }
    
    /**
     * get value from double[] using normalized index
     */
    public static double dArrayGet(double[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from double[] using normalized index
     */
    public static void dArraySet(double[] a, int i, double v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
    
    // ------------------ Object[] ------------------
    public static Object objectArrayGet(Object[] a, int i) {
        try {
            return a[i];
        } catch (Throwable t) {
            return a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)];
        }
    }
    
    /**
     * set value from double[] using normalized index
     */
    public static void objectArraySet(Object[] a, int i, Object v) {
        try {
            a[i]=v;
        } catch (Throwable t) {
            a[DefaultGroovyMethodsSupport.normaliseIndex(i,a.length)]=v;
        }
    }
}
