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
    
    // if original boolean meta class
    private static boolean origBoolean = true;
    // if origBoolean and withoutCustomHandle
    private static boolean origBooleanRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original boolean meta class is used.
     */
    public static boolean isOrigBool() {
        return origBooleanRes;
    }
    
    /**
     * sets if the original boolean meta class is used
     */
    public static void setOrigBool(boolean v) {
        origBoolean = v;
        origBooleanRes = withoutCustomHandle && origBoolean;
    }
    
    //---------------------------------------------
    //                  byte
    //---------------------------------------------
    
    // if original Byte meta class
    private static boolean origByte = true;
    // if origByte and withoutCustomHandle
    private static boolean origByteRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original byte meta class is used.
     */
    public static boolean isOrigByte() {
        return origByteRes;
    }
    
    /**
     * sets if the original byte meta class is used
     */
    public static void setOrigByte(boolean v) {
        origByte = v;
        origByteRes = withoutCustomHandle && origByte;
    }
    
    //---------------------------------------------
    //                  char
    //---------------------------------------------
    
    // if original char meta class
    private static boolean origChar = true;
    // if origChar and withoutCustomHandle
    private static boolean origCharRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original char meta class is used.
     */
    public static boolean isOrigChar() {
        return origCharRes;
    }
    
    /**
     * sets if the original char meta class is used
     */
    public static void setOrigChar(boolean v) {
        origChar = v;
        origCharRes = withoutCustomHandle && origChar;
    }
    
    //---------------------------------------------
    //                  short
    //---------------------------------------------
    
    // if original short meta class
    private static boolean origShort = true;
    // if origShort and withoutCustomHandle
    private static boolean origShortRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original short meta class is used.
     */
    public static boolean isOrigShort() {
        return origShortRes;
    }
    
    /**
     * sets if the original short meta class is used
     */
    public static void setOrigShort(boolean v) {
        origShort = v;
        origShortRes = withoutCustomHandle && origShort;
    }
    
    //---------------------------------------------
    //                  int
    //---------------------------------------------
    
    // if original Integer meta class
    private static boolean origInt = true;
    // if origInt and withoutCustomHandle
    private static boolean origIntRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original integer meta class is used.
     */
    public static boolean isOrigInt() {
        return origIntRes;
    }
    
    /**
     * sets if the original int meta class is used
     */
    public static void setOrigInt(boolean v) {
        origInt = v;
        origIntRes = withoutCustomHandle && origInt;
    }

    //---------------------------------------------
    //                  int[]
    //---------------------------------------------

    // if original Integer[] meta class
    private static boolean origIntArray = true;
    // if origInt and withoutCustomHandle
    private static boolean origIntArrayWCH = true;

    /**
     * @return  true if no meta class creation handle is set and if
     *          the original integer array meta class is used.
     */
    public static boolean isOrigIntArray() {
        return origIntArrayWCH;
    }
    
    /**
     * sets if the original int array meta class is used
     */
    public static void setOrigIntArray(boolean v) {
        origIntArray = v;
        origIntArrayWCH = withoutCustomHandle && origIntArray;
    }
    
    //---------------------------------------------
    //                  long
    //---------------------------------------------
    
    // if original long meta class
    private static boolean origLong = true;
    // if origLong and withoutCustomHandle
    private static boolean origLongRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original long meta class is used.
     */
    public static boolean isOrigLong() {
        return origLongRes;
    }
    
    /**
     * sets if the original long meta class is used
     */
    public static void setOrigLong(boolean v) {
        origLong = v;
        origLongRes = withoutCustomHandle && origLong;
    }
    
    //---------------------------------------------
    //                  float
    //---------------------------------------------
    
    // if original float meta class
    private static boolean origFloat = true;
    // if origFloat and withoutCustomHandle
    private static boolean origFloatRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original float meta class is used.
     */
    public static boolean isOrigFloat() {
        return origFloatRes;
    }
    
    /**
     * sets if the original float meta class is used
     */
    public static void setOrigFloat(boolean v) {
        origFloat = v;
        origFloatRes = withoutCustomHandle && origFloat;
    }
    
    //---------------------------------------------
    //                  double
    //---------------------------------------------
    
    // if original double meta class
    private static boolean origDouble = true;
    // if origFloat and withoutCustomHandle
    private static boolean origDoubleRes = true;
    
    /**
     * @return  true if no meta class creation handle is set and if
     *          the original double meta class is used.
     */
    public static boolean isOrigDouble() {
        return origDoubleRes;
    }
    
    /**
     * sets if the original double meta class is used
     */
    public static void setOrigDouble(boolean v) {
        origDouble = v;
        origDoubleRes = withoutCustomHandle && origDouble;
    }
    
    //---------------------------------------------
    //     custom meta class creation handle
    //---------------------------------------------

    // if a custom meta class creation handle is set
    private static boolean withoutCustomHandle = true;

    /**
     * sets if the system uses a custom meta class creation handle
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
    
    public static void setCategoryUsed(boolean b) {
        categoryUsed = b;
        disabledStandardMC = b || !withoutCustomHandle;
    }
    
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
    public static class ConstantMetaClassVersioning {
        private boolean valid = true; 
        public boolean isValid(){return valid;}
    }
    private static ConstantMetaClassVersioning constantMetaClassVersioning = new ConstantMetaClassVersioning();
    private static final Object constantMetaClassVersioningLock = new Object();
    
    public static ConstantMetaClassVersioning getCurrentConstantMetaClassVersioning() {
        return constantMetaClassVersioning;
    }
    
    public static ConstantMetaClassVersioning getNewConstantMetaClassVersioning() {
        synchronized (constantMetaClassVersioningLock) {
            constantMetaClassVersioning.valid = false;
            constantMetaClassVersioning = new ConstantMetaClassVersioning();
            return constantMetaClassVersioning;
        }
    }
    
}
