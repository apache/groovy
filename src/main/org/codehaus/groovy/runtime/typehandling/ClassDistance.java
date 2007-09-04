/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.runtime.typehandling;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.WeakHashMap;


/*
 * Class for calculating "distances" between classes. Such a distance
 * is not a real distance to something but should be seen as the order
 * classes and interfaces are choosen for method selection. The class
 * will keep a weak cache and recalculate the distances on demand.
 */
public class ClassDistance {
    private static final WeakHashMap CLASS_DISTANCES;
    
    private static class Entry {
        
    }
    
    private static class LinearEntry  extends Entry{
        Class[] entries;
        void concat(Class[] c,LinearEntry le){
            entries = new Class[c.length+le.entries.length];
            System.arraycopy(c,0,entries,0,c.length);
            System.arraycopy(le.entries,0,entries,c.length,le.entries.length);
        }
        void concat(Class c,LinearEntry le){
            entries = new Class[1+le.entries.length];
            entries[0] = c;
            System.arraycopy(le.entries,0,entries,1,le.entries.length);
        }
    }
    
    static {
        CLASS_DISTANCES = new WeakHashMap();
        initialPopulate();
    }
    
    private static void initialPopulate() {
        // int, double, byte, float, BigInteger, BigDecimal, long, short
        // GString, char
        
        
        LinearEntry object = new LinearEntry();
        object.entries = new Class[]{Object.class};
        CLASS_DISTANCES.put(Object.class,object);
        
        LinearEntry number = new LinearEntry();
        number.concat(new Class[]{Number.class,Serializable.class},object);
        CLASS_DISTANCES.put(Number.class,number);

        LinearEntry compareableNumber = new LinearEntry();
        compareableNumber.concat(Comparable.class,number);
        
        LinearEntry binteger = new LinearEntry();
        binteger.concat(new Class[]{BigInteger.class, BigDecimal.class}, compareableNumber);
        CLASS_DISTANCES.put(BigInteger.class,object);
        
        LinearEntry bdec = new LinearEntry();
        binteger.concat(new Class[]{BigDecimal.class, BigInteger.class}, compareableNumber);
        CLASS_DISTANCES.put(BigDecimal.class,object);
        
        
        
        // byte:
        LinearEntry start = new LinearEntry();
        start.entries =  new Class[]{
                byte.class, Byte.class, short.class, Short.class,
                int.class, Integer.class, long.class, Long.class,
                BigInteger.class,
                float.class, Float.class,  double.class, Double.class, 
                BigDecimal.class,
                Number.class,Object.class};
        CLASS_DISTANCES.put(byte.class,start);
        
        // short:
        start = new LinearEntry();
        start.entries =  new Class[]{
                short.class, Short.class,
                int.class, Integer.class, long.class, Long.class,
                BigInteger.class,
                float.class, Float.class,  double.class, Double.class, 
                BigDecimal.class,
                Number.class,Object.class};
        CLASS_DISTANCES.put(short.class,start);
        
        // int:
        start = new LinearEntry();
        start.entries =  new Class[]{
                int.class, Integer.class, long.class, Long.class,
                BigInteger.class,
                float.class, Float.class,  double.class, Double.class, 
                BigDecimal.class,
                Number.class,Object.class};
        CLASS_DISTANCES.put(int.class,start);
        
        // long:
        start = new LinearEntry();
        start.entries =  new Class[]{
                long.class, Long.class,
                BigInteger.class,
                float.class, Float.class,  double.class, Double.class, 
                BigDecimal.class,
                Number.class,Object.class};
        CLASS_DISTANCES.put(long.class,start);
        
        // Biginteger:
        start = new LinearEntry();
        start.entries =  new Class[]{
                BigInteger.class,
                float.class, Float.class,  double.class, Double.class, 
                BigDecimal.class,
                Number.class,Object.class};
        CLASS_DISTANCES.put(long.class,start);
        
        // float:
        start = new LinearEntry();
        start.entries =  new Class[]{ 
                byte.class, Byte.class, short.class, Short.class,
                int.class, Integer.class, long.class, Long.class,
                BigInteger.class,
                float.class, Float.class,  double.class, Double.class, 
                BigDecimal.class,
                Number.class,Object.class};
        CLASS_DISTANCES.put(float.class,start);
        
        // double:
        start = new LinearEntry();
        start.entries =  new Class[]{ 
                double.class,
                Double.class, BigDecimal.class,
                Number.class,Object.class};
        CLASS_DISTANCES.put(double.class,start);

    }
    
    private static synchronized void popultate(Class clazz) {
        if (CLASS_DISTANCES.get(clazz) != null) return;
        
    }
    
}
