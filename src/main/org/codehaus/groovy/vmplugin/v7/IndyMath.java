/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.vmplugin.v7;

import java.lang.invoke.*;
import java.math.BigDecimal;
import java.util.*;

import org.codehaus.groovy.GroovyBugError;

import groovy.lang.MetaMethod;

import static org.codehaus.groovy.vmplugin.v7.IndyInterface.*;
import static org.codehaus.groovy.vmplugin.v7.TypeHelper.*;

/**
 * This class contains math operations used by indy instead of the normal
 * meta method and call site caching system. The goal is to avoid boxing, thus
 * use primitive types for parameters and return types where possible. 
 * WARNING: This class is for internal use only. Do not use it outside of the 
 * org.codehaus.groovy.vmplugin.v7 package of groovy-core.
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class IndyMath {
    
    private static final MethodType 
        II  = MethodType.methodType(Void.TYPE, int.class, int.class),
        III = MethodType.methodType(int.class, int.class, int.class),
        LL  = MethodType.methodType(Void.TYPE, long.class, long.class),
        LLL = MethodType.methodType(long.class, long.class, long.class),
        DD  = MethodType.methodType(Void.TYPE, double.class, double.class),
        DDD = MethodType.methodType(double.class, double.class, double.class),
        GG  = MethodType.methodType(Void.TYPE, BigDecimal.class, BigDecimal.class),
        OO  = MethodType.methodType(Void.TYPE, Object.class, Object.class);

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
            
            MethodType[] keys = new MethodType[]{II,LL,DD};
            MethodType[] values = new MethodType[]{III,LLL,DDD};
            makeMapEntry("minus",keys,values);
            makeMapEntry("plus",keys,values);
            makeMapEntry("multiply",keys,values);

            keys = new MethodType[]{II,LL};
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
        Class leftType = mt.parameterType(0);
        Class rightType = mt.parameterType(1);
        
        if (isIntCategory(leftType) && isIntCategory(rightType)) return II;
        if (isLongCategory(leftType) && isLongCategory(rightType)) return LL;
        if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) return GG;
        if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) return DD;
        
        return OO;
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
    
    
    /*
     further operations to be handled here maybe:
    a / b a.div(b) (if one is double, return double, otherwise BD)
    a[b]    a.getAt(b)
    a[b] = c    a.putAt(b, c)
    */
}
