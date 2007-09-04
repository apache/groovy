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

public class IntegerCache {
    private IntegerCache(){}
    
    static final Integer CACHE[] = new Integer[-(-128) + 127 + 1];
    
    static {
        for(int i = 0; i < CACHE.length; i++)
            CACHE[i] = new Integer(i - 128);
    }
    
    public static Integer integerValue(int i) {
        final int offset = 128;
        if (i >= -128 && i <= 127) { // must cache 
            return CACHE[i + offset];
        }
        return new Integer(i);
    }
}