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
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.runtime.MetaClassHelper;

/**
 * Base class for meta class implementations. 
 * The meta class is used to invoke methods or to get 
 * fields/properties. For proper initialization of this class 
 * it is not enough to only call the constructor, the
 * initialise() must be called too. The invoke methods should
 * check that initialise() was called. Adding methods is 
 * valid unless initilise method was called. Therefore 
 * addNewStaticMethod and addNewInstanceMethod should check that
 * that initilise awas not called before.
 * 
 * 
 * @author John Wilson
 *
 */

public abstract class MetaClass {
    protected static final Logger log = Logger.getLogger(MetaClass.class.getName());
    protected static boolean useReflection = false;
    public static final Object NO_METHOD_FOUND = new Object();
    protected final Class theClass;
    private boolean isGroovyObject;
    
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
    
    protected MetaClass(final Class theClass) {
        this.theClass = theClass;
        isGroovyObject = GroovyObject.class.isAssignableFrom(theClass);
    }
    
    public boolean isGroovyObject(){
        return isGroovyObject;
    }
    
    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        GroovyObject pogo = (GroovyObject) instance;
        return pogo.invokeMethod(methodName,arguments);
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
    
    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper){
        return invokeMethod(receiver,methodName,arguments);
    }
    
    public abstract Object invokeConstructor(Object[] arguments);
    public abstract Object invokeMethod(Object object, String methodName, Object[] arguments);
    public abstract Object invokeStaticMethod(Object object, String methodName, Object[] arguments);
    public abstract Object getProperty(Object object, String property);
    public abstract void setProperty(Object object, String property, Object newValue);
    public abstract Object getAttribute(Object object, String attribute);
    public abstract void setAttribute(Object object, String attribute, Object newValue);
    /**
     * adds a new instance method to this meta class. Instance
     * methods are able to overwrite the original methods of the
     * class. Calling this method should not be done after 
     * initlise was called.
     * @param method the method to be added
     */
    public abstract void addNewInstanceMethod(Method method);
    /**
     * adds a new static method to this meta class. This is only
     * possible as long as initilise was not called.
     * @param method the method to be added
     */
    public abstract void addNewStaticMethod(Method method);
    /**
     * complete the initlialisation process. After this method
     * is called no methods whould be added to the meta class.
     * Invocation of methods or access to fields/proeprties is
     * forbidden unless this method is called. This method 
     * should contain any initialisation code, taking a longer
     * time to complete. An example is the creation of the 
     * Reflector. It is suggested to synchronize this 
     * method.
     */
    public abstract void initialise();
    
    // Possibly Temp methods
    public abstract List getMethods();
    public abstract MetaMethod pickMethod(String methodName, Class[] arguments);
    public abstract MetaMethod pickMethod(Object object, String methodName, Object[] arguments);
    public abstract MetaMethod retrieveMethod(Object owner, String methodName, Object[] arguments);
    public abstract MetaMethod retrieveMethod(String methodName, Class[] arguments);
    public abstract MetaMethod retrieveStaticMethod(String methodName, Class[] arguments);
    public abstract Constructor retrieveConstructor(Class[] arguments);
    public abstract List getProperties();
    public abstract ClassNode getClassNode();
    public abstract List getMetaMethods();
    public abstract Object invokeConstructorAt(Class at, Object[] arguments);

}
