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

import java.lang.reflect.Method;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;


public abstract interface MetaClass extends MOP {
    /*
     * This should be set from some kind of environment variable in the future
     */
    static boolean useReflection = false;
    
    /*
     * Called on the MetaClass of the superclass of the class to allow that MetaClass to control
     * the behaviour of its subclass if it wants to.
     */
     MetaClass createMetaClass(Class theClass, MetaClassRegistry registry);
    
     /*
      * 
      * Do we need this?
      * 
      */
     boolean isGroovyObject();
    
    /*
     * 
     * Do we need this method? 
     * 
     */
     Object invokeMissingMethod(Object instance, String methodName, Object[] arguments);
    
    /*
     * 
     * Do we really want this method?
     * 
     */
     Object invokeMethod(Object object, String methodName, Object arguments);
    

     Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass);
    
     Object getProperty(Class sender, Object receiver, String messageName, boolean useSuper, boolean fromInsideClass);
    
     void setProperty(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass);
    
    /*
     *  Why does this take only one boolean parameter?
     * 
     */
     Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper);
    
     void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass);
    
    /**
     * adds a new instance method to this meta class. Instance
     * methods are able to overwrite the original methods of the
     * class. Calling this method should not be done after 
     * initlise was called.
     * @param method the method to be added
     */
     void addNewInstanceMethod(Method method);
     
    /**
     * adds a new static method to this meta class. This is only
     * possible as long as initilise was not called.
     * @param method the method to be added
     */
     void addNewStaticMethod(Method method);
     
     /*
      * 
      * do we need this?
      * 
      * surely the MetaClass can do lazy initialisation on the first call of 
      * one of its MetaClass methods?
      */
    /**
     * complete the initlialisation process. After this method
     * is called no methods should be added to the meta class.
     * Invocation of methods or access to fields/proeprties is
     * forbidden unless this method is called. This method 
     * should contain any initialisation code, taking a longer
     * time to complete. An example is the creation of the 
     * Reflector. It is suggested to synchronize this 
     * method.
     */
     void initialize();
    
     List getProperties();
     
     List getMethods();
     
     /*
      * 
      * This is the problematic method used by SQL
      * We really need to find a way to either do this properly or to remove it
      */
     ClassNode getClassNode();
     
     
     /*
      * 
      * Why have this and getMethods() what's the difference?
      */
     List getMetaMethods();
    
    /**
     * Warning, this method will be removed
     * @deprecated use invokeConstructor instead
     */
     Object invokeConstructorAt(Class at, Object[] arguments);
     
     /*
      * 
      * What is this for?
      * 
      */
     int selectConstructorAndTransformArguments(int numberOfCosntructors, Object[] arguments);
     
     /*
      * Do we need this?
      */
    /**
     * Selects a method by name and argument classes. This method
     * does not search for an exact match, it searches for a compatible
     * method. For this the method selection mechanism is used as provided
     * bye the implementation of this MetaClass. pickMethod may or may
     * not used during the method selection process when invoking a method
     * thereis no warranty for that.
     * 
     * @returns a matching MetaMethod or null
     * @throws GroovyRuntimeException if there is more than one matching method
     */
     MetaMethod pickMethod(String methodName, Class[] arguments);
}
