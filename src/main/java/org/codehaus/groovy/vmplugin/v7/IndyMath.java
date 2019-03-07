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
package org.codehaus.groovy.vmplugin.v7;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.GroovyBugError;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.codehaus.groovy.vmplugin.v7.IndyInterface.LOOKUP;
import static org.codehaus.groovy.vmplugin.v7.TypeHelper.isBigDecCategory;
import static org.codehaus.groovy.vmplugin.v7.TypeHelper.isDoubleCategory;
import static org.codehaus.groovy.vmplugin.v7.TypeHelper.isIntCategory;
import static org.codehaus.groovy.vmplugin.v7.TypeHelper.isLongCategory;
import static org.codehaus.groovy.vmplugin.v7.TypeHelper.replaceWithMoreSpecificType;

/**
 * This class contains math operations used by indy instead of the normal
 * meta method and call site caching system. The goal is to avoid boxing, thus
 * use primitive types for parameters and return types where possible. 
 * WARNING: This class is for internal use only. Do not use it outside of the 
 * org.codehaus.groovy.vmplugin.v7 package of groovy-core.
 */
public class IndyMath {
    
    private static final MethodType 
        IV   = MethodType.methodType(Void.TYPE, int.class),
        II   = MethodType.methodType(int.class, int.class),
        IIV  = MethodType.methodType(Void.TYPE, int.class, int.class),
        III  = MethodType.methodType(int.class, int.class, int.class),
        LV   = MethodType.methodType(Void.TYPE, long.class),
        LL   = MethodType.methodType(long.class, long.class),
        LLV  = MethodType.methodType(Void.TYPE, long.class, long.class),
        LLL  = MethodType.methodType(long.class, long.class, long.class),
        DV   = MethodType.methodType(Void.TYPE, double.class),
        DD   = MethodType.methodType(double.class, double.class),
        DDV  = MethodType.methodType(Void.TYPE, double.class, double.class),
        DDD  = MethodType.methodType(double.class, double.class, double.class),
        GV  = MethodType.methodType(Void.TYPE, BigDecimal.class),
        GGV  = MethodType.methodType(Void.TYPE, BigDecimal.class, BigDecimal.class),
        OOV  = MethodType.methodType(Void.TYPE, Object.class, Object.class);

    private static void makeMapEntry(String method, MethodType[] keys, MethodType[] values) throws NoSuchMethodException, IllegalAccessException {
        Map<MethodType,MethodHandle> xMap = new HashMap();
        methods.put(method, xMap);
        for (int i=0; i<keys.length; i++) {
            xMap.put(keys[i], LOOKUP.findStatic(IndyMath.class, method, values[i]));
        }
    }
    
    private static Map<String, Map<MethodType,MethodHandle>> methods = new HashMap();
    static {
        try {
            
            MethodType[] keys = new MethodType[]{IIV,LLV,DDV};
            MethodType[] values = new MethodType[]{III,LLL,DDD};
            makeMapEntry("minus",keys,values);
            makeMapEntry("plus",keys,values);
            makeMapEntry("multiply",keys,values);
            
            keys = new MethodType[]{DDV};
            values = new MethodType[]{DDD};
            makeMapEntry("div",keys,values);
            
            keys = new MethodType[]{IV,LV,DV};
            values = new MethodType[]{II,LL,DD};
            makeMapEntry("next",keys,values);
            makeMapEntry("previous",keys,values);
            
            keys = new MethodType[]{IIV,LLV};
            values = new MethodType[]{III,LLL};
            makeMapEntry("mod",keys,values);
            makeMapEntry("or",keys,values);
            makeMapEntry("xor",keys,values);
            makeMapEntry("and",keys,values);
            makeMapEntry("leftShift",keys,values);
            makeMapEntry("rightShift",keys,values);
            
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }
    
    /**
     * Choose a method to replace the originally chosen metaMethod to have a
     * more efficient call path. 
     */
    public static boolean chooseMathMethod(Selector info, MetaMethod metaMethod) {
        Map<MethodType,MethodHandle> xmap = methods.get(info.name);
        if (xmap==null) return false;

        MethodType type = replaceWithMoreSpecificType(info.args, info.targetType); 
        type = widenOperators(type); 
        
        MethodHandle handle = xmap.get(type);
        if (handle==null) return false;
        
        info.handle = handle;
        return true;
    }
    
    /**
     * Widens the operators. For math operations like a+b we generally
     * execute them using a conversion to certain types. If a for example
     * is an int and b a byte, we do the operation using integer math. This 
     * method gives a simplified MethodType that contains the two operators
     * with this widening according to Groovy rules applied. That means both
     * parameters in the MethodType will have the same type. 
     */
    private static MethodType widenOperators(MethodType mt) {
        if (mt.parameterCount()==2) {
            Class leftType = mt.parameterType(0);
            Class rightType = mt.parameterType(1);
            
            if (isIntCategory(leftType) && isIntCategory(rightType)) return IIV;
            if (isLongCategory(leftType) && isLongCategory(rightType)) return LLV;
            if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) return GGV;
            if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) return DDV;
            
            return OOV;
        } else if (mt.parameterCount()==1) {
            Class leftType = mt.parameterType(0);
            if (isIntCategory(leftType)) return IV;
            if (isLongCategory(leftType)) return LV;
            if (isBigDecCategory(leftType)) return GV;
            if (isDoubleCategory(leftType)) return DV;
        }
        return mt;
    }
    
    // math methods used by indy
    
    // int x int
    public static int plus(int a, int b) {return a+b;}
    public static int minus(int a, int b) {return a-b;}
    public static int multiply(int a, int b) {return a*b;}
    public static int mod(int a, int b) {return a%b;}
    public static int or(int a, int b) {return a|b;}
    public static int xor(int a, int b) {return a^b;}
    public static int and(int a, int b) {return a&b;}
    public static int leftShift(int a, int b) {return a<<b;}
    public static int rightShift(int a, int b) {return a>>b;}
    
    // long x long
    public static long plus(long a, long b) {return a+b;}
    public static long minus(long a, long b) {return a-b;}
    public static long multiply(long a, long b) {return a*b;}
    public static long mod(long a, long b) {return a%b;}
    public static long or(long a, long b) {return a|b;}
    public static long xor(long a, long b) {return a^b;}
    public static long and(long a, long b) {return a&b;}
    public static long leftShift(long a, long b) {return a<<b;}
    public static long rightShift(long a, long b) {return a>>b;}
    
    // double x double
    public static double plus(double a, double b) {return a+b;}
    public static double minus(double a, double b) {return a-b;}
    public static double multiply(double a, double b) {return a*b;}
    public static double div(double a, double b){return a/b;}
    
    // next & previous
    public static int next(int i) {return i+1;}
    public static long next(long l) {return l+1;}
    public static double next(double d) {return d+1;}
    public static int previous(int i) {return i-1;}
    public static long previous(long l) {return l-1;}
    public static double previous(double d) {return d-1;}

    /*
     further operations to be handled here maybe:
    a / b a.div(b) (if one is double, return double, otherwise BD)
    a[b]    a.getAt(b)
    a[b] = c    a.putAt(b, c)
    */
}
