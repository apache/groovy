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
        II = MethodType.methodType(Void.TYPE, int.class, int.class),
        LL = MethodType.methodType(Void.TYPE, long.class, long.class), 
        DD = MethodType.methodType(Void.TYPE, double.class, double.class),
        GG = MethodType.methodType(Void.TYPE, BigDecimal.class, BigDecimal.class),
        OO = MethodType.methodType(Void.TYPE, Object.class, Object.class);

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    
    private static Map<String, Map<MethodType,MethodHandle>> methods = new HashMap();
    static {
        try {
            
            Map<MethodType,MethodHandle> xMap = new HashMap();
            methods.put("minus", xMap);
            
            xMap.put(II, LOOKUP.findStatic(IndyMath.class, "minus", II.changeReturnType(int.class)));
            
            xMap = new HashMap();
            methods.put("plus", xMap);
            
            xMap.put(II, LOOKUP.findStatic(IndyMath.class, "plus", II.changeReturnType(int.class)));
            
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }
    
    public static boolean chooseMathMethod(CallInfo info, MetaMethod metaMethod) {
        Map<MethodType,MethodHandle> xmap = methods.get(info.name);
        if (xmap==null) return false;

        MethodType type = replaceWithMoreSpecificType(info.args, info.targetType); 
        type = widenOperators(type); 
        
        MethodHandle handle = xmap.get(type);
        if (handle==null) return false;
        
        info.handle = handle;
        return true;
    }
    
    private static MethodType widenOperators(MethodType mt) {
        Class leftType = mt.parameterType(0);
        Class rightType = mt.parameterType(1);
        
        if (isIntCategory(leftType) && isIntCategory(rightType)) return II;
        if (isLongCategory(leftType) && isLongCategory(rightType)) return LL;
        if (isBigDecCategory(leftType) && isBigDecCategory(rightType)) return GG;
        if (isDoubleCategory(leftType) && isDoubleCategory(rightType)) return DD;
        
        return OO;
    }
    
    public static int minus(int a, int b) {return a-b;}

    public static int plus(int a, int b) {return a+b;}
}
