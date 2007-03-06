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

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;

/**
 * A MetaClass within Groovy defines the behaviour of any given Groovy or Java class. The MetaClass
 * interface defines two parts. The client API, which is defined via the extend MetaObjectProtocol interface
 * and the contract with the Groovy runtime system.
 *
 * In general the compiler and Groovy runtime engine interact with methods on this class whilst MetaClass
 * clients interact with the method defined by the MetaObjectProtocol interface
 *
 *
 * @see org.codehaus.groovy.runtime.metaclass.MetaClassImpl
 * @see groovy.lang.MetaObjectProtocol
 * 
 * @author John Wilson
 * @author Graeme Rocher
 */
public interface MetaClass extends MetaObjectProtocol {
    /**
     * Called on the MetaClass of the superclass of the class to allow that MetaClass to control
     * the behaviour of its subclass if it wants to.
     *
     * @param theClass The java.lang.Class instance to a create a MetaClass for
     * @param registry The MetaClassRegistry instance
     *
     * @return A MetaClas for the specified java.lang.Class instance
     */
     MetaClass createMetaClass(Class theClass, MetaClassRegistry registry);


    /**
     * Invokes a method on the given receiver for the specified arguments. The sender is the class that invoked the method on the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
     * to the super class if necessary
     *
     * @param sender The java.lang.Class instance that invoked the method
     * @param receiver The object which the method was invoked on
     * @param methodName The name of the method
     * @param arguments The arguments to the method
     * @param isCallToSuper Whether the method is a call to a super class method
     * @param fromInsideClass ??
     *
     * @return The return value of the method
     */
     Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass);


    /**
     * Retrieves a property on the given receiver for the specified arguments. The sender is the class that is requesting the property from the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
     * to the super class if necessary
     *
     * @param sender The java.lang.Class instance that requested the property
     * @param receiver The Object which the property is being retrieved from
     * @param property The name of the property
     * @param isCallToSuper Whether the call is to a super class property
     * @param fromInsideClass ??
     *
     * @return The properties value
     */
     Object getProperty(Class sender, Object receiver, String property, boolean isCallToSuper, boolean fromInsideClass);

    /**
     * Retrieves a property on the given receiver for the specified arguments. The sender is the class that is requesting the property from the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
     * to the super class if necessary
     *
     * @param sender The java.lang.Class instance that is mutating the property
     * @param receiver The Object which the property is being set on
     * @param property The name of the property
     * @param value The new value of the property to set
     * @param isCallToSuper Whether the call is to a super class property
     * @param fromInsideClass ??
     *
     */
     void setProperty(Class sender, Object receiver, String property, Object value, boolean isCallToSuper, boolean fromInsideClass);

    /*
     *
     * Do we need this method?
     *
     */
     Object invokeMissingMethod(Object instance, String methodName, Object[] arguments);
    
    
    /*
     *  Why does this take only one boolean parameter?
     * 
     */
     Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper);
    
     void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass);
    

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
     * @return a matching MetaMethod or null
     * @throws GroovyRuntimeException if there is more than one matching method
     * @param methodName the name of the method to pick
     * @param arguments the method arguments
     */
     MetaMethod pickMethod(String methodName, Class[] arguments);
}
