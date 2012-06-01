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

/**
 * This class contains helper methods for converting and comparing types.
 * WARNING: This class is for internal use only. do not use it outside of its 
 * package and not outside groovy-core. 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class TypeHelper {
    /**
     * Get wrapper class for a given class. 
     * If the class is for a primitive number type, then the wrapper class
     * will be returned. If it is no primtive number type, we return the 
     * class itself.
     */
    protected static Class getWrapperClass(Class c) {
        if (c == Integer.TYPE) {
            c = Integer.class;
        } else if (c == Byte.TYPE) {
            c = Byte.class;
        } else if (c == Long.TYPE) {
            c = Long.class;
        } else if (c == Double.TYPE) {
            c = Double.class;
        } else if (c == Float.TYPE) {
            c = Float.class;
        }
        return c;
    }
    
    
    /**
     * Realizes an unsharp equal for the class. 
     * In general we return true if the provided arguments are the same. But
     * we will also return true if our argument class is a wrapper for
     * the parameter class. For example the parameter is an int and the
     * argument class is a wrapper.
     */
    protected static boolean argumentClassIsParameterClass(Class argumentClass, Class parameterClass) {
        if (argumentClass == parameterClass) return true;
        if (getWrapperClass(parameterClass) == argumentClass) return true;
        return false;
    }
}
