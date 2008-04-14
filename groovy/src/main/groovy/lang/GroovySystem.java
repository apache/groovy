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

public final class GroovySystem {
    //
    //  TODO: make this initialisation able to set useReflection true
    //  TODO: have some way of specifying another MetaClass Registry implementation
    //
    static {
        USE_REFLECTION = true;
        META_CLASS_REGISTRY = new MetaClassRegistryImpl();
    }
    
    /**
     * The MetaClass for java.lang.Object
     */
    private static MetaClass objectMetaClass;
    /**
     * If true then the MetaClass will only use reflection for method dispatch, property acess, etc.
     */
    private static final boolean USE_REFLECTION;
    /**
     * Reference to the MetaClass Registry to be used by the Groovy run time system to map classes to MetaClasses
     */
    private static final MetaClassRegistry META_CLASS_REGISTRY;

    private static boolean keepJavaMetaClasses=false;
    
    private GroovySystem() {
        // Do not allow this class to be instantiated
    }

    public static boolean isUseReflection() {
        return USE_REFLECTION;
    }

    public static MetaClassRegistry getMetaClassRegistry() {
        return META_CLASS_REGISTRY;
    }
    
    public static void setKeepJavaMetaClasses(boolean keepJavaMetaClasses) {
        GroovySystem.keepJavaMetaClasses = keepJavaMetaClasses;
    }
    
    public static boolean isKeepJavaMetaClasses() {
        return keepJavaMetaClasses;
    }
    
}
