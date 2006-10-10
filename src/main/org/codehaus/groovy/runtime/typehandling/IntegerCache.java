package org.codehaus.groovy.runtime.typehandling;

public class IntegerCache {
    private IntegerCache(){}
    
    static final Integer cache[] = new Integer[-(-128) + 127 + 1];
    
    static {
        for(int i = 0; i < cache.length; i++)
            cache[i] = new Integer(i - 128);
    }
    
    public static Integer integerValue(int i) {
        final int offset = 128;
        if (i >= -128 && i <= 127) { // must cache 
            return cache[i + offset];
        }
        return new Integer(i);
    }
}