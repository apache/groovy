package groovy.lang;


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

/**
 * @author John Wilson
 *
 */

public abstract class NewMetaClass implements MOP {
    public static final Object NOT_CONSTRUCTED = new Object();
    
    protected static final Object[] NO_PARAMETERS = new Object[0];
    protected static final Class[] NO_PARAMETER_TYPES = new Class[0];
    
    //
    // The methods forming the Meta Object Protocol
    
    //
    /* (non-Javadoc)
     * @see groovy.lang.MOP#invokeMethod(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object[], java.lang.Class[])
     */
    public abstract Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object[] arguments, final Class[] argumentTypes);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#getProperty(java.lang.Object, java.lang.Class, java.lang.String)
     */
    public abstract Object getProperty(final Object object, final Class objectType, final String property);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#setProperty(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object, java.lang.Class)
     */
    public abstract void setProperty(final Object object, final Class objectType, final String property, final Object newValue, final Class newValueType);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#getAttribute(java.lang.Object, java.lang.Class, java.lang.String)
     */
    public abstract Object getAttribute(final Object object, final Class objectType, final String attribute);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#setAttribute(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object, java.lang.Class)
     */
    public abstract void setAttribute(final Object object, final Class objectType, final String attribute, final Object newValue, final Class newValueType);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#invokeThisMethod(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object[], java.lang.Class[])
     */
    public abstract Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object[] arguments, final Class[] argumentTypes);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#getThisProperty(java.lang.Object, java.lang.Class, java.lang.String)
     */
    public abstract Object getThisProperty(final Object object, final Class thisType, final String property);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#setThisProperty(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object, java.lang.Class)
     */
    public abstract boolean setThisProperty(final Object object, final Class thisType, final String property, final Object newValue, final Class newValueType);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#getThisAttribute(java.lang.Object, java.lang.Class, java.lang.String)
     */
    public abstract Object getThisAttribute(final Object object, final Class thisType, final String attribute);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#setThisAttribute(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object, java.lang.Class)
     */
    public abstract boolean setThisAttribute(final Object object, final Class thisType, final String attribute, final Object newValue, final Class newValueType);

    /* (non-Javadoc)
     * @see groovy.lang.MOP#invokeSuperMethod(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object[], java.lang.Class[])
     */
    public abstract Object invokeSuperMethod(final Object object, final Class superclassType, final String methodName, final Object[] arguments, final Class[] argumentTypes);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#getSuperProperty(java.lang.Object, java.lang.Class, java.lang.String)
     */
    public abstract Object getSuperProperty(final Object object, final Class superclassType, final String property);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#setSuperProperty(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object, java.lang.Class)
     */
    public abstract boolean setSuperProperty(final Object object, final Class superclassType, final String property, final Object newValue, final Class newValueType);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#getSuperAttribute(java.lang.Object, java.lang.Class, java.lang.String)
     */
    public abstract Object getSuperAttribute(final Object object, final Class superclassType, final String attribute);
    
    /* (non-Javadoc)
     * @see groovy.lang.MOP#setSuperAttribute(java.lang.Object, java.lang.Class, java.lang.String, java.lang.Object, java.lang.Class)
     */
    public abstract boolean setSuperAttribute(final Object object, final Class superclassType, final String attribute, final Object newValue, final Class newValueType);

    //
    // Methods which may migrate to the MOP at some time in the future
    //
    
    /**
     * Construct an instance of the class represented by this instance of the MetaClass.
     * Only public constructors may be called. There is currently no way of dynamically adding constructors but
     * if one is introduced then this method will use them.
     * If no suitable constuctor is found then a groovy.lang.MissingConstructorException is thrown
     * @param arguments
     * The arguments to the constructor call. The values in this array are undefined after the call is made.
     * The MetaClass may or may not change these vaues. DO NOT use these vaules after the call.
     * @param argumentTypes
     * This array must be the same length as the arguments array.
     * It contains the type of each parameter if known or null if unknown.
     * The if there are overloaded constructors then this information contributes to the selection of the correct constructor to call.
     * The MetaClass may or may not change these vaues. DO NOT use these values after the call.
     * @return
     * A new instance of the class represented by this instance of MetaClass.
     */
    public abstract Object invokeConstructor(final Object[] arguments, final Class[] argumentTypes);
    
    /**
     * Construct an instance of the class represented by this instance of the MetaClass.
     * Only public constructors will be called directly. There is currently no way of dynamically adding constructors but
     * if one is introduced then this method will use them.
     * If a protected or private constructor is the best match for the parameters then NOT_CONSTRUCTED is returned
     * otherwise a groovy.lang.MissingConstructorException is thrown
     * @param arguments
     * The arguments to the constructor.
     * If NOT_CONSTUCTED is returned and there is a private or protected constuctor which
     * should be called then this array will contain the parameters to that constructor. The MetaClass will have done any conversions
     * (e.g. GString to String) needed to allow the constuctor call to suceed.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CONSTUCTED is returned.
     * @param argumentTypes
     * This array must be the same length as the arguments arguments array.
     * It contains the type of each parameter if known or null if unknown.
     * The if there are multiple constructors with the same name and number of parameters then this information
     * contributes to the selection of the correct method to call.
     * If NOT_CONSTUCTED is returned and there is a private or protected constuctor which
     * should be called then this array will contain the classes which represent the types of the parameters to that constructor.
     * If any other value is returned then the values in this array are undefined. In this case the MetaClass may or may not
     * change these values. DO NOT use these values unless NOT_CALLED is returned.
     * @return
     * Either a new instance of the class represented by this instance of MetaClass or NOT_CONSTRUCTED.
     * NOT_CONSTRUCTED is returned if a suitable constructor exists but the MetaClass did not call it because it is not public.
     * In this case the caller is free to try to invoke the constuctor itself (e.g. by executing generated bytecode). 
     * If there are multiple constuctors with the same name and number of parameters then the values on the array passed
     * as argumentTypes should be used to choose the correct one to call.
     */
    public abstract Object invokeThisConstructor(final Object[] arguments, final Class[] argumentTypes);
    
    //
    // Additional methods for use when some or all of the type information is not available
    // We don't bother doing this with super calls properety and attribute acess as they are far less common
    //
    
    public Object invokeConstructor(final Object[] arguments) {
        return invokeConstructor(arguments, new Class[arguments.length]);
    }
    
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object[] arguments) {
        return invokeMethod(object, objectType, methodName, arguments, new Class[arguments.length]);
    }
    
    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments, final Class[] argumentTypes) {
        return invokeMethod(object, (Class)null, methodName, arguments, argumentTypes);
    }
    
    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        return invokeMethod(object, (Class)null, methodName, arguments, new Class[arguments.length]);
    }
    
    public Object getProperty(final Object object, final String property) {
        return getProperty(object, (Class)null, property);
    }
    
    public void setProperty(final Object object, final Class objectType, final String property, final Object newValue) {
        setProperty(object, objectType, property, newValue, (Class)null);
    }
    
    public void setProperty(final Object object, final String property, final Object newValue, final Class newValueType) {
        setProperty(object, (Class)null, property, newValue, newValueType);
    }
    
    public void setProperty(final Object object, final String property, final Object newValue) {
        setProperty(object, (Class)null, property, newValue, (Class)null);
    }
    
    public Object getAttribute(final Object object, final String attribute) {
        return getAttribute(object, (Class)null, attribute);
    }
    
    public void setAttribute(final Object object, final Class objectType, final String attribute, final Object newValue) {
        setAttribute(object, objectType, attribute, newValue, (Class)null);
    }
    
    public void setAttribute(final Object object, final String attribute, final Object newValue, final Class newValueType) {
        setAttribute(object, (Class)null, attribute, newValue, newValueType);
    }
    
    public void setAttribute(final Object object, final String attribute, final Object newValue) {
        setAttribute(object, (Class)null, attribute, newValue, (Class)null);
    }
    
    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object[] arguments) {
        return invokeThisMethod(object, thisType, methodName, arguments, new Class[arguments.length]);
    }
    
    public boolean setThisProperty(final Object object, final Class thisType, final String property, final Object newValue) {
        return setThisProperty(object, thisType, property, newValue, (Class)null);
    }
    
    public boolean setThisProperty(final Object object, final String property, final Object newValue) {
        return setThisProperty(object, (Class)null, property, newValue, (Class)null);
    }
    
    //
    // Methods used to optimise method calling when there are 0, 1, 2, 3 or 4 parameters
    // We don't bother doing this with super calls as they are far less common
    //
       
    public Object invokeMethod(final Object object, final String methodName) {
        return invokeMethod(object, (Class)null, methodName, NO_PARAMETERS, NO_PARAMETER_TYPES);
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName) {
        return invokeMethod(object, objectType, methodName, NO_PARAMETERS, NO_PARAMETER_TYPES);
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1, final Class c1) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1}, new Class[] {c1});
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1, final Class c1) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1}, new Class[] {c1});
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1}, new Class[1]);
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1}, new Class[1]);
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1, final Object p2, final Class c1, final Class c2) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1, p2}, new Class[] {c1, c2});
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1, final Object p2, final Class c1, final Class c2) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1, p2}, new Class[] {c1, c2});
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1, final Object p2) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1, p2}, new Class[2]);
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1, final Object p2) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1, p2}, new Class[2]);
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1, final Object p2, final Object p3, final Class c1, final Class c2, final Class c3) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1, p2, p3}, new Class[] {c1, c2, c3});
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1, final Object p2, final Object p3, final Class c1, final Class c2, final Class c3) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1, p2, p3}, new Class[] {c1, c2, c3});
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1, final Object p2, final Object p3) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1, p2, p3}, new Class[3]);
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1, final Object p2, final Object p3) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1, p2, p3}, new Class[3]);
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1, final Object p2, final Object p3, final Object p4, final Class c1, final Class c2, final Class c3, final Class c4) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1, p2, p3, p4}, new Class[] {c1, c2, c3, c4});
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1, final Object p2, final Object p3, final Object p4, final Class c1, final Class c2, final Class c3, final Class c4) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1, p2, p3, p4}, new Class[] {c1, c2, c3, c4});
    }
       
    public Object invokeMethod(final Object object, final String methodName, final Object p1, final Object p2, final Object p3, final Object p4) {
        return invokeMethod(object, (Class)null, methodName, new Object[] {p1, p2, p3, p4}, new Class[4]);
    }
       
    public Object invokeMethod(final Object object, final Class objectType, final String methodName, final Object p1, final Object p2, final Object p3, final Object p4) {
        return invokeMethod(object, objectType, methodName, new Object[] {p1, p2, p3, p4}, new Class[4]);
    }
       
    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName) {
        return invokeThisMethod(object, thisType, methodName, NO_PARAMETERS, NO_PARAMETER_TYPES);
    }
       
    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1, final Class c1) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1}, new Class[] {c1});
    }
       
    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1}, new Class[1]);
    }

    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1, final Object p2, final Class c1, final Class c2) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1, p2}, new Class[] {c1, c2});
    }

    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1, final Object p2) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1, p2}, new Class[2]);
    }

    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1, final Object p2, final Object p3, final Class c1, final Class c2, final Class c3) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1, p2, p3}, new Class[] {c1, c2, c3});
    }

    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1, final Object p2, final Object p3) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1, p2, p3}, new Class[3]);
    }

    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1, final Object p2, final Object p3, final Object p4, final Class c1, final Class c2, final Class c3, final Class c4) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1, p2, p3, p4}, new Class[] {c1, c2, c3, c4});
    }

    public Object invokeThisMethod(final Object object, final Class thisType, final String methodName, final Object p1, final Object p2, final Object p3, final Object p4) {
        return invokeThisMethod(object, thisType, methodName, new Object[] {p1, p2, p3, p4}, new Class[4]);
    }
}
