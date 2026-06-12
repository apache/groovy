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
package org.codehaus.groovy.runtime.metaclass;

/**
 * WARNING: This class is for internal use only!
 * We use this class to store information about if a default MetaClass is
 * used for certain primitives.
 */
public class DefaultMetaClassInfo {

    //---------------------------------------------
    //                  boolean
    //---------------------------------------------

    // if original boolean metaclass
    private static boolean origBoolean = true;
    // if origBoolean and withoutCustomHandle
    private static boolean origBooleanRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original boolean metaclass is used.
     */
    public static boolean isOrigBool() {
        return origBooleanRes;
    }

    /**
     * Sets if the original boolean metaclass is used.
     */
    public static void setOrigBool(boolean v) {
        origBoolean = v;
        origBooleanRes = withoutCustomHandle && origBoolean;
    }

    //---------------------------------------------
    //                  byte
    //---------------------------------------------

    // if original Byte metaclass
    private static boolean origByte = true;
    // if origByte and withoutCustomHandle
    private static boolean origByteRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original byte metaclass is used.
     */
    public static boolean isOrigByte() {
        return origByteRes;
    }

    /**
     * Sets if the original byte metaclass is used.
     */
    public static void setOrigByte(boolean v) {
        origByte = v;
        origByteRes = withoutCustomHandle && origByte;
    }

    //---------------------------------------------
    //                  char
    //---------------------------------------------

    // if original char metaclass
    private static boolean origChar = true;
    // if origChar and withoutCustomHandle
    private static boolean origCharRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original char metaclass is used.
     */
    public static boolean isOrigChar() {
        return origCharRes;
    }

    /**
     * Sets if the original char metaclass is used.
     */
    public static void setOrigChar(boolean v) {
        origChar = v;
        origCharRes = withoutCustomHandle && origChar;
    }

    //---------------------------------------------
    //                  short
    //---------------------------------------------

    // if original short metaclass
    private static boolean origShort = true;
    // if origShort and withoutCustomHandle
    private static boolean origShortRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original short metaclass is used.
     */
    public static boolean isOrigShort() {
        return origShortRes;
    }

    /**
     * Sets if the original short metaclass is used.
     */
    public static void setOrigShort(boolean v) {
        origShort = v;
        origShortRes = withoutCustomHandle && origShort;
    }

    //---------------------------------------------
    //                  int
    //---------------------------------------------

    // if original Integer metaclass
    private static boolean origInt = true;
    // if origInt and withoutCustomHandle
    private static boolean origIntRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original integer metaclass is used.
     */
    public static boolean isOrigInt() {
        return origIntRes;
    }

    /**
     * Sets if the original int metaclass is used.
     */
    public static void setOrigInt(boolean v) {
        origInt = v;
        origIntRes = withoutCustomHandle && origInt;
    }

    //---------------------------------------------
    //                  int[]
    //---------------------------------------------

    // if original Integer[] metaclass
    private static boolean origIntArray = true;
    // if origInt and withoutCustomHandle
    private static boolean origIntArrayWCH = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original integer array metaclass is used.
     */
    public static boolean isOrigIntArray() {
        return origIntArrayWCH;
    }

    /**
     * Sets if the original int array metaclass is used.
     */
    public static void setOrigIntArray(boolean v) {
        origIntArray = v;
        origIntArrayWCH = withoutCustomHandle && origIntArray;
    }

    //---------------------------------------------
    //                  long
    //---------------------------------------------

    // if original long metaclass
    private static boolean origLong = true;
    // if origLong and withoutCustomHandle
    private static boolean origLongRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original long metaclass is used.
     */
    public static boolean isOrigLong() {
        return origLongRes;
    }

    /**
     * Sets if the original long metaclass is used.
     */
    public static void setOrigLong(boolean v) {
        origLong = v;
        origLongRes = withoutCustomHandle && origLong;
    }

    //---------------------------------------------
    //                  float
    //---------------------------------------------

    // if original float metaclass
    private static boolean origFloat = true;
    // if origFloat and withoutCustomHandle
    private static boolean origFloatRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original float metaclass is used.
     */
    public static boolean isOrigFloat() {
        return origFloatRes;
    }

    /**
     * Sets if the original float metaclass is used.
     */
    public static void setOrigFloat(boolean v) {
        origFloat = v;
        origFloatRes = withoutCustomHandle && origFloat;
    }

    //---------------------------------------------
    //                  double
    //---------------------------------------------

    // if original double metaclass
    private static boolean origDouble = true;
    // if origFloat and withoutCustomHandle
    private static boolean origDoubleRes = true;

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original double metaclass is used.
     */
    public static boolean isOrigDouble() {
        return origDoubleRes;
    }

    /**
     * Sets if the original double metaclass is used.
     */
    public static void setOrigDouble(boolean v) {
        origDouble = v;
        origDoubleRes = withoutCustomHandle && origDouble;
    }

    //---------------------------------------------
    //     custom metaclass creation handle
    //---------------------------------------------

    // if a custom metaclass creation handle is set
    private static boolean withoutCustomHandle = true;

    /**
     * Sets whether the system uses a custom metaclass creation handle.
     * When set to false, indicates that a custom handle is in use; when set to true,
     * indicates the system uses the standard metaclass creation handle.
     *
     * @param mch true if using standard metaclass creation handle, false if using custom
     */
    public static void setWithoutCustomMetaclassCreationHandle(boolean mch) {
        withoutCustomHandle = mch;
        changeFlags(mch);
    }

    //---------------------------------------------
    //              category handle
    //---------------------------------------------
    private static boolean categoryUsed = false;
    private static boolean disabledStandardMC = false;

    /**
     * Sets whether categories are currently in use and updates related flags accordingly.
     * When categories are used, standard metaclass functionality may be affected.
     *
     * @param b true if categories are in use, false otherwise
     */
    public static void setCategoryUsed(boolean b) {
        categoryUsed = b;
        disabledStandardMC = b || !withoutCustomHandle;
    }

    /**
     * Returns whether standard metaclass functionality has been disabled.
     * This occurs when either categories are in use or a custom metaclass creation handle is set.
     *
     * @return true if standard metaclass is disabled, false otherwise
     */
    public static boolean disabledStandardMetaClass() {
        return disabledStandardMC;
    }


    private static void changeFlags(boolean mch) {
        if (mch) {
            disabledStandardMC = true;
            origIntArrayWCH = false;
            origByteRes = origChar = origBoolean =false;
            origShortRes = origIntRes = origLong = false;
            origFloat = origDouble = false;
        } else {
            disabledStandardMC = categoryUsed;
            origByteRes = origByte; origCharRes = origChar;
            origBooleanRes = origBoolean; origShortRes = origShort;
            origIntRes = origInt; origLongRes = origLong;
            origFloatRes = origFloat; origDoubleRes = origDouble;
            origIntArrayWCH = origIntArray;
        }
    }

    /**
     * Sets whether the original metaclass is used for the given primitive class.
     * This method updates the appropriate flag based on the class type (Byte, Character, Short,
     * Integer, Long, Float, Double, or Integer array).
     *
     * @param c the primitive or primitive array class
     * @param orig true if using the original metaclass, false otherwise
     */
    public static void setPrimitiveMeta(Class c, boolean orig) {
        if (c==Byte.class) {
            setOrigByte(orig);
        } else if (c==Character.class) {
            setOrigChar(orig);
        } else if (c==Short.class) {
            setOrigShort(orig);
        } else if (c==Integer.class) {
            setOrigInt(orig);
        } else if (c.getComponentType()==Integer.class) {
            setOrigIntArray(orig);
        } else if (c==Long.class) {
            setOrigLong(orig);
        } else if (c==Float.class) {
            setOrigFloat(orig);
        } else if (c==Double.class) {
            setOrigDouble(orig);
        }

    }

    //---------------------------------------------
    //         GlobalMetaClassVersioning
    //---------------------------------------------
    /**
     * Represents a constant version of the metaclass configuration at a point in time.
     * Used to track when metaclass configurations have changed and invalidate caches
     * accordingly.
     */
    public static class ConstantMetaClassVersioning {
        private boolean valid = true;
        
        /**
         * Returns whether this versioning instance is still valid.
         * Returns false if a newer version has been created.
         *
         * @return true if this version is still current, false otherwise
         */
        public boolean isValid(){return valid;}
    }
    private static ConstantMetaClassVersioning constantMetaClassVersioning = new ConstantMetaClassVersioning();
    private static final Object constantMetaClassVersioningLock = new Object();

    /**
     * Retrieves the current constant metaclass versioning object.
     * All cached metaclass information is tagged with the current version.
     * When the version changes, all caches using the old version become invalid.
     *
     * @return the current metaclass versioning instance
     */
    public static ConstantMetaClassVersioning getCurrentConstantMetaClassVersioning() {
        return constantMetaClassVersioning;
    }

    /**
     * Creates a new constant metaclass versioning instance and invalidates the old one.
     * This should be called when global metaclass configuration changes occur.
     *
     * @return the new metaclass versioning instance
     */
    public static ConstantMetaClassVersioning getNewConstantMetaClassVersioning() {
        synchronized (constantMetaClassVersioningLock) {
            constantMetaClassVersioning.valid = false;
            constantMetaClassVersioning = new ConstantMetaClassVersioning();
            return constantMetaClassVersioning;
        }
    }

}
