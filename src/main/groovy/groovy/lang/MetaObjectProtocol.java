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

import java.util.List;

/**
 * <p>An interface that defines the API usable by clients of Groovy's Meta Object Protocol (MOP). These methods are
 * implemented by the reference implementation of the {@link groovy.lang.MetaClass} interface.
 *
 * @see MetaClassImpl
 */
public interface MetaObjectProtocol {

    /**
     * Obtain a list of all meta properties available on this meta class
     *
     * @see groovy.lang.MetaProperty
     * @return A list of MetaProperty instances
     */
    List<MetaProperty> getProperties();
    /**
     * Obtain a list of all the meta methods available on this meta class
     *
     * @see groovy.lang.MetaMethod
     * @return A list of MetaMethod instances
     */
    List<MetaMethod> getMethods();
    
    /**
     * <p>Returns an object satisfying Groovy truth if the implementing MetaClass responds to
     * a method with the given name and arguments types.
     *
     * <p>Note that this method's return value is based on realised methods and does not take into account
     * objects or classes that implement invokeMethod or methodMissing
     *
     * <p>This method is "safe" in that it will always return a value and never throw an exception
     *
     * @param obj The object to inspect
     * @param name The name of the method of interest
     * @param argTypes The argument types to match against
     * @return A List of MetaMethods matching the argument types which will be empty if no matching methods exist
     */
    List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes);

    /**
     * <p>Returns an object satisfying Groovy truth if the implementing MetaClass responds to
     * a method with the given name regardless of arguments. In other words this method will
     * return for foo() and foo(String).
     *
     * <p>Note that this method's return value is based on realised methods and does not take into account
     * objects or classes that implement invokeMethod or methodMissing
     *
     * <p>This method is "safe" in that it will always return a value and never throw an exception
     *
     * @param obj The object to inspect
     * @param name The name of the method of interest
     * @return A List of MetaMethods which will be empty if no methods with the given name exist
     */
    List<MetaMethod> respondsTo(Object obj, String name);

    /**
     * <p>Returns true of the implementing MetaClass has a property of the given name
     *
     * <p>Note that this method will only return true for realised properties and does not take into
     * account implementation of getProperty or propertyMissing
     *
     * @param obj The object to inspect
     * @param name The name of the property
     * @return The MetaProperty or null if it doesn't exist
     */
    MetaProperty hasProperty(Object obj, String name);

    /**
     * Returns a MetaProperty for the given name or null if it doesn't exist
     *
     * @param name The name of the MetaProperty
     * @return A MetaProperty or null
     */
    MetaProperty getMetaProperty(String name);

    /**
     * Retrieves a static MetaMethod for the given name and argument values, using the types of the arguments
     * to establish the chosen MetaMethod
     *
     * @param name The name of the MetaMethod
     * @param args The argument types
     * @return A MetaMethod or null if it doesn't exist
     */
    MetaMethod getStaticMetaMethod(String name, Object[] args);


    /**
     * Retrieves an instance MetaMethod for the given name and argument values, using the types of the
     * argument values to establish the chosen MetaMethod
     *
     * @param name The name of the MetaMethod
     * @param args Array containing - 1) the argument values (using which their types are then inferred), or 2) the corresponding argument types  
     * @return A MetaMethod or null if it doesn't exist
     */
    MetaMethod getMetaMethod(String name, Object[] args);

    /**
     * Retrieves that Java Class that the attached Meta behaviours apply to
     *
     * @return The java.lang.Class instance
     */
    Class getTheClass();

    /**
     * Invokes a constructor for the given arguments. The MetaClass will attempt to pick the best argument which
     * matches the types of the objects passed within the arguments array
     *
     * @param arguments The arguments to the constructor
     * @return An instance of the java.lang.Class that this MetaObjectProtocol object applies to
     */
    Object invokeConstructor(Object[] arguments);

    /**
     * Invokes a method on the given Object with the given name and arguments. The MetaClass will attempt to pick
     * the best method for the given name and arguments. If a method cannot be invoked a MissingMethodException will be
     * thrown.
     *
     * @see groovy.lang.MissingMethodException
     * @param object The instance which the method is invoked on
     * @param methodName The name of the method
     * @param arguments The arguments to the method
     * @return The return value of the method which is null if the return type is void
     */
    Object invokeMethod(Object object, String methodName, Object[] arguments);

    /**
     * Invokes a method on the given object, with the given name and single argument.
     *
     * @see #invokeMethod(Object, String, Object[])
     * @param object The Object to invoke the method on
     * @param methodName The name of the method
     * @param arguments The argument to the method
     * @return The return value of the method which is null if the return type is void
     */
     Object invokeMethod(Object object, String methodName, Object arguments);

    /**
     * Invokes a static method on the given Object with the given name and arguments.
     * <p>
     * The Object can either be an instance of the class that this
     * MetaObjectProtocol instance applies to or the java.lang.Class instance itself. If a method cannot be invoked
     * a MissingMethodException is will be thrown
     *
     * @see groovy.lang.MissingMethodException
     * @param object An instance of the class returned by the getTheClass() method or the class itself
     * @param methodName The name of the method
     * @param arguments The arguments to the method
     * @return The return value of the method which is null if the return type is void
     */
    Object invokeStaticMethod(Object object, String methodName, Object[] arguments);

    /**
     * Retrieves a property of an instance of the class returned by the getTheClass() method.
     * <p>
     * What this means is largely down to the MetaClass implementation, however the default case would result
     * in an attempt to invoke a JavaBean getter, or if no such getter exists a public field of the instance.
     *
     * @see MetaClassImpl
     * @param object An instance of the class returned by the getTheClass() method
     * @param property The name of the property to retrieve the value for
     * @return The properties value
     */
    Object getProperty(Object object, String property);

    /**
     * Sets a property of an instance of the class returned by the getTheClass() method.
     * <p>
     * What this means is largely down to the MetaClass implementation, however the default case would result
     * in an attempt to invoke a JavaBean setter, or if no such setter exists to set a public field of the instance.
     *
     * @see MetaClassImpl
     * @param object An instance of the class returned by the getTheClass() method
     * @param property The name of the property to set
     * @param newValue The new value of the property
     */
    void setProperty(Object object, String property, Object newValue);

    /**
     * Retrieves an attribute of an instance of the class returned by the getTheClass() method.
     * <p>
     * What this means is largely down to the MetaClass implementation, however the default case would result
     * in attempt to read a field of the instance.
     *
     * @see MetaClassImpl
     * @param object An instance of the class returned by the getTheClass() method
     * @param attribute The name of the attribute to retrieve the value for
     * @return The attribute value
     */
    Object getAttribute(Object object, String attribute);

    /**
     * Sets an attribute of an instance of the class returned by the getTheClass() method.
     * <p>
     * What this means is largely down to the MetaClass implementation, however the default case would result
     * in an attempt to set a field of the instance.
     *
     * @see MetaClassImpl
     * @param object An instance of the class returned by the getTheClass() method
     * @param attribute The name of the attribute to set
     * @param newValue The new value of the attribute
     */
    void setAttribute(Object object, String attribute, Object newValue);
}
