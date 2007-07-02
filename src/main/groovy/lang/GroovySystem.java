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
package groovy.lang;

import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;

public class GroovySystem {
    private GroovySystem() {
        // Do not allow this class to be instantiated
    }
    
    /**
     * If true then the MetaClass will only use reflection for method dispatch, property acess, etc.
     */
    private final static boolean useReflection;
    
    /**
     * Reference to the MetaClass Registry to be used by the Groovy run time system to map classes to MetaClasses
     */
    private final static MetaClassRegistry metaClassRegistry;
    
    /**
     * The MetaClass for java.lang.Object
     */
    private  static MetaClass objectMetaClass;


    public static boolean isUseReflection() {
        return useReflection;
    }


    public static MetaClassRegistry getMetaClassRegistry() {
        return metaClassRegistry;
    }
    
    //
    //  TODO: make this initialisation able to set useReflection true
    //  TODO: have some way of specifying another MetaClass Registry implementation
    //
    static {
        useReflection = true;
        metaClassRegistry = new MetaClassRegistryImpl();
    }
}
