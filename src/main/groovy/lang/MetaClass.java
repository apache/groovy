/*
 * Copyright 2005 John G. Wilson
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
 *
 */

package groovy.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.runtime.MetaClassHelper;

/**
 * @author John Wilson
 *
 */

public abstract class MetaClass {
    protected static final Logger log = Logger.getLogger(MetaClass.class.getName());
    protected static boolean useReflection = false;
    
    public static boolean isUseReflection() {
        return MetaClass.useReflection;
    }

    /**
     * Allows reflection to be enabled in situations where bytecode generation
     * of method invocations causes issues.
     *
     * @param useReflection
     */
    public static void setUseReflection(boolean useReflection) {
        MetaClass.useReflection = useReflection;
    }

    protected final Class theClass;
    
    protected MetaClass(final Class theClass) {
        this.theClass = theClass;
    }
    
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        if (arguments == null) {
            return invokeMethod(object, methodName, MetaClassHelper.EMPTY_ARRAY);
        }
        if (arguments instanceof Tuple) {
            Tuple tuple = (Tuple) arguments;
            return invokeMethod(object, methodName, tuple.toArray());
        }
        if (arguments instanceof Object[]) {
            return invokeMethod(object, methodName, (Object[])arguments);
        }
        else {
            return invokeMethod(object, methodName, new Object[]{arguments});
        }
    }
    
    public abstract Object invokeConstructor(Object[] arguments);
    public abstract Object invokeMethod(Object object, String methodName, Object[] arguments);
    public abstract Object getProperty(Object object, String property);
    public abstract void setProperty(Object object, String property, Object newValue);
    public abstract Object getAttribute(Object object, String attribute);
    public abstract void setAttribute(Object object, String attribute, Object newValue);
    
    // Possibly Temp methods
    public abstract List getMethods();
    protected abstract MetaMethod pickMethod(String methodName, Class[] arguments);
    protected abstract MetaMethod pickMethod(Object object, String methodName, Object[] arguments);
    public abstract MetaMethod retrieveMethod(Object owner, String methodName, Object[] arguments);
    public abstract MetaMethod retrieveMethod(String methodName, Class[] arguments);
    public abstract MetaMethod retrieveStaticMethod(String methodName, Class[] arguments);
    public abstract Constructor retrieveConstructor(Class[] arguments);
    protected abstract void addNewInstanceMethod(Method method);
    protected abstract void addNewStaticMethod(Method method);
    protected abstract void checkInitialised();
    public abstract List getProperties();
    public abstract void setProperties(Object bean, Map map);
    public abstract ClassNode getClassNode();
    public abstract List getMetaMethods();
    public abstract Object invokeStaticMethod(Object object, String methodName, Object[] arguments);
    public abstract Object invokeConstructorAt(Class at, Object[] arguments);

    // Possibly Temp fields
    protected List newGroovyMethodsList = new LinkedList();

}
