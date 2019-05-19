/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.lang;

import org.codehaus.groovy.ast.ClassNode;

import java.util.List;

/**
 * A MetaClass within Groovy defines the behaviour of any given Groovy or Java class. The MetaClass
 * interface defines two parts. The client API, which is defined via the extend MetaObjectProtocol interface
 * and the contract with the Groovy runtime system.
 *
 * In general the compiler and Groovy runtime engine interact with methods on this class whilst MetaClass
 * clients interact with the method defined by the MetaObjectProtocol interface
 *
 * @see MetaClassImpl
 * @see groovy.lang.MetaObjectProtocol
 */
public interface MetaClass extends MetaObjectProtocol {

    /**
     * <p>Invokes a method on the given receiver for the specified arguments. The sender is the class that invoked the method on the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * <p>The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
     * to the super class if necessary
     *
     * @param sender The java.lang.Class instance that invoked the method
     * @param receiver The object which the method was invoked on
     * @param methodName The name of the method
     * @param arguments The arguments to the method
     * @param isCallToSuper Whether the method is a call to a super class method
     * @param fromInsideClass Whether the call was invoked from the inside or the outside of the class
     *
     * @return The return value of the method
     */
     Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass);

    /**
     * <p>Retrieves a property on the given receiver for the specified arguments. The sender is the class that is requesting the property from the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * <p>The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
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
     * <p>Sets a property on the given receiver for the specified arguments. The sender is the class that is setting the property from the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * <p>The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
     * to the super class if necessary
     *
     * @param sender The java.lang.Class instance that is mutating the property
     * @param receiver The Object which the property is being set on
     * @param property The name of the property
     * @param value The new value of the property to set
     * @param isCallToSuper Whether the call is to a super class property
     * @param fromInsideClass Whether the call was invoked from the inside or the outside of the class
     */
     void setProperty(Class sender, Object receiver, String property, Object value, boolean isCallToSuper, boolean fromInsideClass);

    /**
     *
     * <p>Attempts to invoke the methodMissing method otherwise throws a MissingMethodException
     *
     * @see groovy.lang.MissingMethodException
     *
     * @param instance The instance to invoke methodMissing on
     * @param methodName The name of the method
     * @param arguments The arguments to the method
     * @return The results of methodMissing or throws MissingMethodException
     */
     Object invokeMissingMethod(Object instance, String methodName, Object[] arguments);

    /**
     * Invokes the propertyMissing method otherwise throws a MissingPropertyException
     *
     * @param instance The instance of the class
     * @param propertyName The name of the property
     * @param optionalValue The value of the property which could be null in the case of a getter
     * @param isGetter Whether the missing property event was the result of a getter or a setter
     * 
     * @return The result of the propertyMissing method or throws MissingPropertyException
     */
     Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter);

    /**
     * Retrieves the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
     *
     * @param sender The class of the object that requested the attribute
     * @param receiver The instance
     * @param messageName The name of the attribute
     * @param useSuper Whether to look-up on the super class or not
     * @return The attribute value
     */
     Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper);

    /**
     * Sets the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
     *
     * @param sender The class of the object that requested the attribute
     * @param receiver The instance
     * @param messageName The name of the attribute
     * @param messageValue The value of the attribute
     * @param useSuper Whether to look-up on the super class or not
     * @param fromInsideClass Whether the call happened from the inside or the outside of a class
     */
     void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass);
    
    /**
     * Complete the initialisation process. After this method
     * is called no methods should be added to the meta class.
     * Invocation of methods or access to fields/properties is
     * forbidden unless this method is called. This method 
     * should contain any initialisation code, taking a longer
     * time to complete. An example is the creation of the 
     * Reflector. It is suggested to synchronize this 
     * method.
     */
     void initialize();

    /**
     * Retrieves a list of MetaProperty instances that the MetaClass has
     *
     * @see MetaProperty
     *
     * @return A list of MetaProperty instances
     */
     List<MetaProperty> getProperties();

    /**
     * Retrieves a list of MetaMethods held by the class. This list does not include MetaMethods added by groovy.lang.ExpandoMetaClass.
     *
     * @return A list of MetaMethods
     */
     List<MetaMethod> getMethods();
     
     /**
      * Obtains a reference to the original AST for the MetaClass if it is available at runtime
      *
      * @return The original AST or null if it cannot be returned
      */
     ClassNode getClassNode();

     /**
      * Retrieves a list of MetaMethods held by this class. This list includes MetaMethods added by groovy.lang.ExpandoMetaClass.
      *
      * @return A list of MetaMethods
      */
     List<MetaMethod> getMetaMethods();
    
     /**
      *
      * Internal method to support Groovy runtime. Not for client usage.
      *
      * @param numberOfConstructors The number of constructors
      * @param arguments The arguments
      *
      * @return selected index
      */
     int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments);

    /**
     * Selects a method by name and argument classes. This method
     * does not search for an exact match, it searches for a compatible
     * method. For this the method selection mechanism is used as provided
     * by the implementation of this MetaClass. pickMethod may or may
     * not be used during the method selection process when invoking a method.
     * There is no warranty for that.
     *
     * @return a matching MetaMethod or null
     * @throws GroovyRuntimeException if there is more than one matching method
     * @param methodName the name of the method to pick
     * @param arguments the method arguments
     */
     MetaMethod pickMethod(String methodName, Class[] arguments);
}
