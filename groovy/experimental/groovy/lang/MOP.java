/*
 * Copyright 2003-2008 the original author or authors.
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

/**
 * @author John Wilson
 *
 */

/**
 * This inteface defines thew Meta Object Protocol implemented by the MetaClass
 * Note that this inteface is purely for documantation purposes
 * An object implementing this interface is not neccesarily a vaild MetaClass
 * A MetaClass needs to implement may more methods
 *
 */
public interface MOP {
    static final Object NOT_CALLED = new Object();
    static final Object NO_PROPERTY = new Object();
    static final Object NO_ATTRIBUTE = new Object();

    /**
     * Invoke a public method on an object.
     * Public methods are those declared public in the class and those added via mechanisms like DefaultGroovyMethods.
     * If no suitable method is found a groovy.lang.MissingMethodException is thrown.
     * @param object
     * The object on which the call is to be made
     * @param objectType
     * If objectType is not null then method to be called is selected as though the object was cast to this type before making the call.
     * @param methodName
     * The name of the method to be called
     * @param arguments
     * The arguments to the call.
     * If NOT_CALLED is returned and there is a private or protected method which
     * should be called then this array will contain the parameters to that method. The MetaClass will have done any conversions
     * (e.g. GString to String) needed to allow the call to suceed.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CALLED is returned.
     * @param argumentTypes
     * This array must be the same length as the arguments arguments array.
     * It contains the type of each parameter if known or null if unknown.
     * The if there are multiple methods with the same name and number of parameters then this information
     * contributes to the selection of the correct method to call.
     * If NOT_CALLED is returned and there is a private or protected method which
     * should be called then this array will contain the classes which represent the types of the parameters to that method.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CALLED is returned.
     * @return
     * The result of calling the method (null is returned if the method returns void).
     */
    Object invokeMethod( Object object,  Class objectType,  String methodName,  Object[] arguments,  Class[] argumentTypes);
    
    Object getProperty( Object object,  Class objectType,  String property);
    
    void setProperty( Object object,  Class objectType,  String property,  Object newValue,  Class newValueType);
    
    Object getAttribute( Object object,  Class objectType,  String attribute);
    
    void setAttribute( Object object,  Class objectType,  String attribute,  Object newValue,  Class newValueType);
    
    /**
     * Invoke a public method on an object.
     * These public methods are those declared public in the class and those added via mechanisms like DefaultGroovyMethods.
     * If no public method can be found but there exists protected or private methods which would match the name, number and type
     * of the parameters then the value NOT_CALLED is returned.
     * Oherwise a groovy.lang.MissingMethodException is thrown.
     * @param object
     * The object on which the call is to be made
     * @param thisType
     * The type of the class in which this call was made. This must never be null.
     * Note this type value may not be the same as object.getClass() as it my be a superclass of that class.
     * @param methodName
     * The name of the method to be called
     * @param arguments
     * The arguments to the call.
     * If NOT_CALLED is returned and there is a private or protected method which
     * should be called then this array will contain the parameters to that method. The MetaClass will have done any conversions
     * (e.g. GString to String) needed to allow the call to succeed.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CALLED is returned.
     * @param argumentTypes
     * This array must be the same length as the arguments arguments array.
     * It contains the type of each parameter if known or null if unknown.
     * The if there are multiple methods with the same name and number of parameters then this information
     * contributes to the selection of the correct method to call.
     * If NOT_CALLED is returned and there is a private or protected method which
     * should be called then this array will contain the classes which represent the types of the parameters to that method.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CALLED is returned.
     * @return
     * Either the result of calling the method (null is returned if the method returns void) or the value NOT_CALLED.
     * NOT_CALLED is returned if a suitable method exists but the MetaClass did not call it because it is not public.
     * In this case the caller is free to try to invoke the method itself (e.g. by executing generated bytecode).
     * If there are multiple methods with the same name and number of parameters then the values on the array passed
     * as argumentTypes should be used to choose the correct one to call.
     */
    Object invokeThisMethod( Object object,  Class thisType,  String methodName,  Object[] arguments,  Class[] argumentTypes);
    
    Object getThisProperty( Object object,  Class thisType,  String property);
    
    boolean setThisProperty( Object object,  Class thisType,  String property,  Object newValue,  Class newValueType);
    
    Object getThisAttribute( Object object,  Class thisType,  String attribute);
    
    boolean setThisAttribute( Object object,  Class thisType,  String attribute,  Object newValue,  Class newValueType);
    
    /**
     * Invoke an added method on an object. Methods are added via mechanisms like DefaultGroovyMethods.
     * If no added method can be found but there exists public, protected or private methods on the specified superclass
     * which would match the name, number and type of the parameters then the value NOT_CALLED is returned.
     * Oherwise a groovy.lang.MissingMethodException is thrown.
     * @param object
     * The object on which the call is to be made
     * @param superclassType
     * The type of the superclass. This must never be null.
     * @param methodName
     * The name of the method to be called
     * @param arguments
     * The arguments to the call.
     * If NOT_CALLED is returned and there is a private or protected method which
     * should be called then this array will contain the parameters to that method. The MetaClass will have done any conversions
     * (e.g. GString to String) needed to allow the call to succeed.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CALLED is returned.
     * @param argumentTypes
     * This array must be the same length as the arguments arguments array.
     * It contains the type of each parameter if known or null if unknown.
     * The if there are multiple methods with the same name and number of parameters then this information
     * contributes to the selection of the correct method to call.
     * If NOT_CALLED is returned and there is a method which
     * should be called then this array will contain the classes which represent the types of the parameters to that method.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CALLED is returned.
     * @return
     * Either the result of calling the method (null is returned if the method returns void) or the value NOT_CALLED.
     * NOT_CALLED is returned if a suitable method exists but the MetaClass did not call it because it is not added.
     * In this case the caller is free to try to invoke the method itself (e.g. by executing generated bytecode).
     * If there are multiple methods with the same name and number of parameters then the values on the array passed
     * as argumentTypes should be used to choose the correct one to call.
     */  
    Object invokeSuperMethod( Object object,  Class superclassType,  String methodName,  Object[] arguments,  Class[] argumentTypes);
    
    Object getSuperProperty( Object object,  Class superclassType,  String property);
    
    boolean setSuperProperty( Object object,  Class superclassType,  String property,  Object newValue,  Class newValueType);
    
    Object getSuperAttribute( Object object,  Class superclassType,  String attribute);
    
    boolean setSuperAttribute( Object object,  Class superclassType,  String attribute,  Object newValue,  Class newValueType);

}
