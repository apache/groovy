/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package groovy.lang;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.classgen.CompilerFacade;
import org.codehaus.groovy.runtime.ClosureListener;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.MethodHelper;
import org.objectweb.asm.ClassWriter;

/**
 * Allows methods to be dynamically added to existing classes at runtime
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MetaClass {

    protected static final Object[] EMPTY_ARRAY = {
    };
    protected static final Object[] ARRAY_WITH_NULL = { null };
    protected static Class[] EMPTY_TYPE_ARRAY = {
    };

    private MetaClassRegistry registry;
    private Class theClass;
    private ClassNode classNode;
    private Map methodIndex = new HashMap();
    private Map staticMethodIndex = new HashMap();
    private Map newStaticInstanceMethodIndex = new HashMap();
    private Map propertyDescriptors = Collections.synchronizedMap(new HashMap());
    private Map listeners = new HashMap();
    private Method genericGetMethod;
    private Method genericSetMethod;
    private List constructors;

    public MetaClass(MetaClassRegistry registry, Class theClass) throws IntrospectionException {
        this.registry = registry;
        this.theClass = theClass;

        constructors = Arrays.asList(theClass.getDeclaredConstructors());
        addMethods(theClass);

        // introspect
        BeanInfo info = Introspector.getBeanInfo(theClass);
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            propertyDescriptors.put(descriptor.getName(), descriptor);
        }
        EventSetDescriptor[] eventDescriptors = info.getEventSetDescriptors();
        for (int i = 0; i < eventDescriptors.length; i++) {
            EventSetDescriptor descriptor = eventDescriptors[i];
            Method[] listenerMethods = descriptor.getListenerMethods();
            for (int j = 0; j < listenerMethods.length; j++) {
                Method listenerMethod = listenerMethods[j];
                listeners.put(listenerMethod.getName(), descriptor.getAddListenerMethod());
            }
        }

        // now lets see if there are any methods on one of my interfaces
        Class[] interfaces = theClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            addNewStaticMethodsFrom(interfaces[i]);
        }

        if (theClass.isArray() && !theClass.equals(Object[].class)) {
            addNewStaticMethodsFrom(Object[].class);
        }

        // lets add all the base class methods
        Class c = theClass;
        while (true) {
            c = c.getSuperclass();
            if (c == null) {
                break;
            }
            addNewStaticMethodsFrom(c);
            addMethods(c);
        }

    }

    /**
	 * @return all the normal instance methods avaiable on this class for the
	 *         given name
	 */
    public List getMethods(String name) {
        List answer = (List) methodIndex.get(name);
        if (answer == null) {
            return Collections.EMPTY_LIST;
        }
        return answer;
    }

    /**
	 * @return all the normal static methods avaiable on this class for the
	 *         given name
	 */
    public List getStaticMethods(String name) {
        List answer = (List) staticMethodIndex.get(name);
        if (answer == null) {
            return Collections.EMPTY_LIST;
        }
        return answer;
    }

    /**
	 * Allows static method definitions to be added to a meta class as if it
	 * was an instance method
	 * 
	 * @param method
	 */
    protected void addNewStaticInstanceMethod(Method method) {
        String name = method.getName();
        List list = (List) newStaticInstanceMethodIndex.get(name);
        if (list == null) {
            list = new ArrayList();
            newStaticInstanceMethodIndex.put(name, list);
        }
        list.add(method);
    }

    public Object invokeMethod(Object object, String methodName, Object arguments) {
        /*
		 * System.out.println( "MetaClass: Invoking method on object: " +
		 * object + " method: " + methodName + " arguments: " + arguments);
		 */
        return invokeMethod(object, methodName, asArray(arguments));
    }

    /**
	 * Invokes the given method on the object.
	 *  
	 */
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        /*
		 * Class type = arguments == null ? null : arguments.getClass(); System
		 * .out .println( "MetaClass(Object[]) Invoking method on object: " +
		 * object + " method: " + methodName + " argument type: " + type + "
		 * arguments: " + InvokerHelper.toString(arguments));
		 * 
		 * //System.out.println("Type of first arg: " + arguments[0] + " type: " +
		 * arguments[0].getClass());
		 */

        if (object == null) {
            throw new NullPointerException("Cannot invoke method: " + methodName + " on null object");
        }

        List methods = getMethods(methodName);
        if (!methods.isEmpty()) {
            Method method = (Method) chooseMethod(methodName, methods, arguments);
            if (method != null) {
                return doMethodInvoke(object, method, arguments);
            }
        }

        // lets see if there's a new static method we've added in groovy-land
        // to this class
        List newStaticInstanceMethods = getNewStaticInstanceMethods(methodName);
        int size = (arguments != null) ? arguments.length : 0;
        Object[] staticArguments = new Object[size + 1];
        staticArguments[0] = object;
        if (size > 0) {
            System.arraycopy(arguments, 0, staticArguments, 1, size);
        }

        Method method = null;
        if (!newStaticInstanceMethods.isEmpty()) {
            method = (Method) chooseMethod(methodName, newStaticInstanceMethods, staticArguments);
        }
        if (method == null) {
            method = findNewStaticInstanceMethod(methodName, staticArguments);
        }
        if (method != null) {
            return doMethodInvoke(null, method, staticArguments);
        }

        // lets try a static method then
        try {
            return invokeStaticMethod(object, methodName, arguments);
        }
        catch (MissingMethodException e) {
            if (size == 1) {
                Object firstArgument = arguments[0];
                if (firstArgument instanceof List) {
                    // lets coerce the list arguments into an array of
                    // arguments
                    // e.g. calling JFrame.setLocation( [100, 100] )

                    List list = (List) firstArgument;
                    return invokeMethod(object, methodName, list.toArray());
                }
            }
            throw e;
        }
    }

    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        /*
		 * System.out.println("Calling static method: " + methodName + " on
		 * args: " + InvokerHelper.toString(arguments)); Class type = arguments ==
		 * null ? null : arguments.getClass(); System.out.println("Argument
		 * type: " + type); System.out.println("Type of first arg: " +
		 * arguments[0] + " type: " + arguments[0].getClass());
		 */

        List methods = getStaticMethods(methodName);

        if (!methods.isEmpty()) {
            Method method = (Method) chooseMethod(methodName, methods, arguments);
            if (method != null) {
                return doMethodInvoke(theClass, method, arguments);
            }
        }

        if (theClass != Class.class) {
            try {
                return registry.getMetaClass(Class.class).invokeMethod(object, methodName, arguments);
            }
            catch (GroovyRuntimeException e) {
                // throw our own exception
            }
        }
        throw new MissingMethodException(methodName, theClass);
    }

    public Object invokeConstructor(Object[] arguments) {
        Constructor constructor = (Constructor) chooseMethod("<init>", constructors, arguments);
        if (constructor != null) {
            return doConstructorInvoke(constructor, arguments);
        }
        if (arguments.length == 1 && arguments[0] instanceof Map) {
            constructor = (Constructor) chooseMethod("<init>", constructors, EMPTY_ARRAY);
            if (constructor != null) {
                Object bean = doConstructorInvoke(constructor, EMPTY_ARRAY);
                setProperties(bean, ((Map) arguments[0]));
                return bean;
            }
        }
        throw new GroovyRuntimeException("Could not find matching constructor for class: " + theClass.getName());
    }

    /**
	 * Sets a number of bean properties from the given Map where the keys are
	 * the String names of properties and the values are the values of the
	 * properties to set
	 */
    public void setProperties(Object bean, Map map) {
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            setProperty(bean, key, value);
        }
    }

    /**
	 * @return the currently registered static methods against this class
	 */
    public List getNewStaticInstanceMethods(String methodName) {
        List newStaticInstanceMethods = (List) newStaticInstanceMethodIndex.get(methodName);
        if (newStaticInstanceMethods == null) {
            return Collections.EMPTY_LIST;
        }
        return newStaticInstanceMethods;
    }

    /**
	 * @return the given property's value on the object
	 */
    public Object getProperty(final Object object, final String property) {
        PropertyDescriptor descriptor = (PropertyDescriptor) propertyDescriptors.get(property);
        if (descriptor != null) {
            Method method = descriptor.getReadMethod();
            if (method == null) {
                throw new GroovyRuntimeException("Cannot read property: " + property);
            }
            return doMethodInvoke(object, method, EMPTY_ARRAY);
        }

        if (genericGetMethod != null) {
            Object[] arguments = { property };
            Object answer = doMethodInvoke(object, genericGetMethod, arguments);
            if (answer != null) {
                return answer;
            }
        }

        // is the property the name of a method - in which case return a
        // closure
        List methods = getMethods(property);
        if (!methods.isEmpty()) {
            return new MethodClosure(object, property);
        }

        // lets try invoke a static getter method
        Exception lastException = null;
        try {
            Method method = findGetter(object, "get" + capitalize(property));
            if (method != null) {
                return doMethodInvoke(object, method, EMPTY_ARRAY);
            }
        }
        catch (GroovyRuntimeException e) {
            lastException = e;
        }

        /** @todo or are we an extensible groovy class? */
        if (genericGetMethod != null) {
            return null;
        }
        else {
            /** @todo these special cases should be special MetaClasses maybe */
            if (object instanceof Class) {
                // lets try a static field
                return getStaticProperty((Class) object, property);
            }
            if (object instanceof Collection) {
                return DefaultGroovyMethods.get((Collection) object, property);
            }
            if (object instanceof Object[]) {
                return DefaultGroovyMethods.get(Arrays.asList((Object[]) object), property);
            }

            // lets try the getter method
            if (lastException == null) {
                throw new MissingPropertyException(property, theClass);
            }
            else {
                throw new MissingPropertyException(property, theClass, lastException);
            }
        }
    }

    /**
	 * Sets the property value on an object
	 */
    public void setProperty(Object object, String property, Object newValue) {
        PropertyDescriptor descriptor = (PropertyDescriptor) propertyDescriptors.get(property);

        if (descriptor != null) {
            Method method = descriptor.getWriteMethod();
            if (method == null) {
                throw new GroovyRuntimeException("Cannot set read-only property: " + property);
            }
            Object[] arguments = { newValue };
            try {
                doMethodInvoke(object, method, arguments);
            }
            catch (GroovyRuntimeException e) {
                // if the value is a List see if we can construct the value
                // from a constructor
                if (newValue instanceof List) {
                    List list = (List) newValue;
                    int params = list.size();
                    Constructor[] constructors = descriptor.getPropertyType().getConstructors();
                    for (int i = 0; i < constructors.length; i++) {
                        Constructor constructor = constructors[i];
                        if (constructor.getParameterTypes().length == params) {
                            //System.out.println("Found constructor: " +
                            // constructor);
                            Object value = doConstructorInvoke(constructor, list.toArray());
                            doMethodInvoke(object, method, new Object[] { value });
                            return;
                        }
                    }
                }
                throw e;
            }
            return;
        }

        Method addListenerMethod = (Method) listeners.get(property);
        if (addListenerMethod != null && newValue instanceof Closure) {
            // lets create a dynamic proxy
            Object proxy = createListenerProxy(addListenerMethod.getParameterTypes()[0], property, (Closure) newValue);
            doMethodInvoke(object, addListenerMethod, new Object[] { proxy });
            return;
        }

        if (genericSetMethod != null) {
            Object[] arguments = { property, newValue };
            doMethodInvoke(object, genericSetMethod, arguments);
            return;
        }

        /** @todo or are we an extensible class? */

        // lets try invoke the set method
        String method = "set" + capitalize(property);
        invokeMethod(object, method, new Object[] { newValue });
    }

    public ClassNode getClassNode() {
        if (classNode == null && GroovyObject.class.isAssignableFrom(theClass)) {
            // lets try load it from the classpath
            String className = theClass.getName();
            String groovyFile = className;
            int idx = groovyFile.indexOf('$');
            if (idx > 0) {
                groovyFile = groovyFile.substring(0, idx);
            }
            groovyFile = groovyFile.replace('.', '/') + ".groovy";

            //System.out.println("Attempting to load: " + groovyFile);
            URL url = theClass.getClassLoader().getResource(groovyFile);
            if (url == null) {
                url = Thread.currentThread().getContextClassLoader().getResource(groovyFile);
            }
            if (url != null) {
                try {
                    InputStream in = url.openStream();

                    /**
					 * @todo there is no CompileUnit in scope so class name
					 * checking won't work but that mostly affects the bytecode
					 * generation rather than viewing the AST
					 */
                    CompilerFacade compiler = new CompilerFacade(theClass.getClassLoader(), new CompileUnit()) {
                        protected void onClass(ClassWriter classWriter, ClassNode classNode) {
                            if (classNode.getName().equals(theClass.getName())) {
                                //System.out.println("Found: " +
                                // classNode.getName());
                                MetaClass.this.classNode = classNode;
                            }
                        }
                    };
                    compiler.parseClass(in, groovyFile);
                }
                catch (Exception e) {
                    throw new GroovyRuntimeException("Exception thrown parsing: " + groovyFile + ". Reason: " + e, e);
                }
            }

        }
        return classNode;
    }

    public String toString() {
        return super.toString() + "[" + theClass + "]";
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
	 * Converts the given object into an array; if its an array then just cast
	 * otherwise wrap it in an array
	 */
    protected Object[] asArray(Object arguments) {
        if (arguments == null) {
            return EMPTY_ARRAY;
        }
        if (arguments instanceof Tuple) {
            Tuple tuple = (Tuple) arguments;
            return tuple.toArray();
        }
        if (arguments instanceof Object[]) {
            return (Object[]) arguments;
        }
        else {
            return new Object[] { arguments };
        }
    }
    /**
	 * @param listenerType
	 *            the interface of the listener to proxy
	 * @param listenerMethodName
	 *            the name of the method in the listener API to call the
	 *            closure on
	 * @param closure
	 *            the closure to invoke on the listenerMethodName method
	 *            invocation
	 * @return a dynamic proxy which calls the given closure on the given
	 *         method name
	 */
    protected Object createListenerProxy(Class listenerType, final String listenerMethodName, final Closure closure) {
        InvocationHandler handler = new ClosureListener(listenerMethodName, closure);
        return Proxy.newProxyInstance(listenerType.getClassLoader(), new Class[] { listenerType }, handler);
    }

    /**
	 * Adds all the methods declared in the given class to the metaclass
	 * ignoring any matching methods already defined by a derived class
	 * 
	 * @param theClass
	 */
    protected void addMethods(Class theClass) {
        Method[] methodArray = theClass.getDeclaredMethods();
        for (int i = 0; i < methodArray.length; i++) {
            Method method = methodArray[i];

            String name = method.getName();
            if (isGenericGetMethod(method) && genericGetMethod == null) {
                genericGetMethod = method;
            }
            else if (isGenericSetMethod(method) && genericSetMethod == null) {
                genericSetMethod = method;
            }
            if (MethodHelper.isStatic(method)) {
                List list = (List) staticMethodIndex.get(name);
                if (list == null) {
                    list = new ArrayList();
                    staticMethodIndex.put(name, list);
                    list.add(method);
                }
                else {
                    if (!containsMatchingMethod(list, method)) {
                        list.add(method);
                    }
                }
            }
            else {
                List list = (List) methodIndex.get(name);
                if (list == null) {
                    list = new ArrayList();
                    methodIndex.put(name, list);
                    list.add(method);
                }
                else {
                    if (!containsMatchingMethod(list, method)) {
                        list.add(method);
                    }
                }
            }
        }
    }

    /**
	 * @return true if a method of the same matching prototype was found in the
	 *         list
	 */
    protected boolean containsMatchingMethod(List list, Method method) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Method aMethod = (Method) iter.next();
            Class[] params1 = aMethod.getParameterTypes();
            Class[] params2 = method.getParameterTypes();
            if (params1.length == params2.length) {
                boolean matches = true;
                for (int i = 0; i < params1.length; i++) {
                    if (params1[i] != params2[i]) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
	 * Adds all of the newly defined methods from the given class to this
	 * metaclass
	 * 
	 * @param theClass
	 */
    protected void addNewStaticMethodsFrom(Class theClass) {
        MetaClass interfaceMetaClass = registry.getMetaClass(theClass);
        Iterator iter = interfaceMetaClass.newStaticInstanceMethodIndex.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            List values = (List) entry.getValue();

            if (values != null) {
                // lets add these methods to me
                List list = (List) newStaticInstanceMethodIndex.get(name);
                if (list == null) {
                    list = new ArrayList();
                    newStaticInstanceMethodIndex.put(name, list);
                }
                list.addAll(values);
            }
        }
    }

    /**
	 * @return the value of the static property of the given class
	 */
    protected Object getStaticProperty(Class aClass, String property) {
        //System.out.println("Invoking property: " + property + " on class: "
        // + aClass);

        Exception lastException = null;
        try {
            Field field = aClass.getField(property);
            if (field != null) {
                if ((field.getModifiers() & Modifier.STATIC) != 0) {
                    return field.get(null);
                }
            }
        }
        catch (Exception e) {
            lastException = e;
        }

        // lets try invoke a static getter method
        try {
            Method method = findStaticGetter(aClass, "get" + capitalize(property));
            if (method != null) {
                return doMethodInvoke(aClass, method, EMPTY_ARRAY);
            }
        }
        catch (GroovyRuntimeException e) {
            throw new MissingPropertyException(property, aClass, e);
        }

        if (lastException == null) {
            throw new MissingPropertyException(property, aClass);
        }
        else {
            throw new MissingPropertyException(property, aClass, lastException);
        }
    }

    /**
	 * @return the getter method for the given object
	 */
    protected Method findGetter(Object object, String name) {
        try {
            return object.getClass().getMethod(name, EMPTY_TYPE_ARRAY);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
	 * @return the Method of the given name with no parameters or null
	 */
    protected Method findStaticGetter(Class type, String name) {
        try {
            Method method = type.getMethod(name, EMPTY_TYPE_ARRAY);
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                return method;
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
	 * Lets walk the base class & interfaces list to see if we can find the
	 * method
	 */
    protected Method findNewStaticInstanceMethod(String methodName, Object[] staticArguments) {
        if (theClass.equals(Object.class)) {
            return null;
        }
        MetaClass superClass = registry.getMetaClass(theClass.getSuperclass());
        List list = superClass.getNewStaticInstanceMethods(methodName);
        if (!list.isEmpty()) {
            Method method = (Method) chooseMethod(methodName, list, staticArguments);
            if (method != null) {
                // lets cache it for next invocation
                addNewStaticInstanceMethod(method);
            }

            return method;
        }
        return superClass.findNewStaticInstanceMethod(methodName, staticArguments);
    }

    protected Object doMethodInvoke(Object object, Method method, Object[] argumentArray) {
        //System.out.println("Evaluating method: " + method);
        //System.out.println("on object: " + object + " with arguments: " +
        // InvokerHelper.toString(argumentArray));
        //System.out.println(this.theClass);

        try {
            if (registry.useAccessible()) {
                method.setAccessible(true);
            }
            if (argumentArray == null) {
                argumentArray = EMPTY_ARRAY;
            }
            else if (method.getParameterTypes().length == 1 && argumentArray.length == 0) {
                argumentArray = ARRAY_WITH_NULL;
            }
            return method.invoke(object, argumentArray);
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Error) {
                Error error = (Error) t;
                throw error;
            }
            if (t instanceof RuntimeException) {
                RuntimeException runtimeEx = (RuntimeException) t;
                throw runtimeEx;
            }
            throw new InvokerInvocationException(e);
        }
        catch (IllegalAccessException e) {
            throw new GroovyRuntimeException(
                "could not access method: "
                    + method
                    + " on: "
                    + object
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                e);
        }
        catch (IllegalArgumentException e) {
            if (argumentArray.length == 1 && argumentArray[0] instanceof GString) {
                argumentArray[0] = argumentArray[0].toString();
                return doMethodInvoke(object, method, argumentArray);
            }
            else {
                throw new GroovyRuntimeException(
                    "failed to invoke method: "
                        + method
                        + " on: "
                        + object
                        + " with arguments: "
                        + InvokerHelper.toString(argumentArray)
                        + " reason: "
                        + e,
                    e);
            }
        }
        catch (Exception e) {
            throw new GroovyRuntimeException(
                "failed to invoke method: "
                    + method
                    + " on: "
                    + object
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                e);
        }
    }

    protected Object doConstructorInvoke(Constructor constructor, Object[] argumentArray) {
        //System.out.println("Evaluating constructor: " + constructor + " with
        // arguments: " + InvokerHelper.toString(argumentArray));
        //System.out.println(this.theClass);

        try {
            return constructor.newInstance(argumentArray);
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Error) {
                Error error = (Error) t;
                throw error;
            }
            if (t instanceof RuntimeException) {
                RuntimeException runtimeEx = (RuntimeException) t;
                throw runtimeEx;
            }
            throw new InvokerInvocationException(e);
        }
        catch (IllegalAccessException e) {
            throw new GroovyRuntimeException(
                "could not access constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                e);
        }
        catch (Exception e) {
            throw new GroovyRuntimeException(
                "failed to invoke constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                e);
        }
    }

    /**
	 * Chooses the correct method to use from a list of methods which match by
	 * name.
	 * 
	 * @param methods
	 *            the possible methods to choose from
	 * @param arguments
	 *            the original argument to the method
	 * @return
	 */
    protected Object chooseMethod(String methodName, List methods, Object[] arguments) {
        int methodCount = methods.size();
        if (methodCount <= 0) {
            return null;
        }
        else if (methodCount == 1) {
            Object method = methods.get(0);
            if (isValidMethod(method, arguments, true)) {
                return method;
            }
            return null;
        }
        Object answer = null;
        if (arguments.length == 1 && arguments[0] == null) {
            answer = chooseMostGeneralMethodWith1Param(methods);
        }
        else if (arguments.length == 0) {
            answer = chooseEmptyMethodParams(methods);
        }
        else {
            List matchingMethods = new ArrayList();

            for (Iterator iter = methods.iterator(); iter.hasNext();) {
                Object method = iter.next();
                Class[] paramTypes;
                if (isValidMethod(method, arguments, false)) {
                    matchingMethods.add(method);
                }
            }
            if (matchingMethods.isEmpty()) {
                return null;
            }
            else if (matchingMethods.size() == 1) {
                return matchingMethods.get(0);
            }
            return chooseMostSpecificParams(methodName, matchingMethods, arguments);

        }
        if (answer != null) {
            return answer;
        }
        throw new GroovyRuntimeException(
            "Could not find which method to invoke from this list: "
                + methods
                + " for arguments: "
                + InvokerHelper.toString(arguments));
    }

    protected boolean isValidMethod(Object method, Object[] arguments, boolean includeCoerce) {
        if (arguments == null) {
            return true;
        }
        int size = arguments.length;
        Class[] paramTypes = getParameterTypes(method);
        boolean validMethod = false;
        if (paramTypes.length == size) {
            // lets check the parameter types match
            validMethod = true;
            for (int i = 0; i < size; i++) {
                Object value = arguments[i];
                if (!isCompatibleInstance(paramTypes[i], value, includeCoerce)) {
                    validMethod = false;
                }
            }
        }
        else {
            if (paramTypes.length == 1 && size == 0) {
                return true;
            }
        }
        return validMethod;
    }

    protected Object chooseMostSpecificParams(String name, List matchingMethods, Object[] arguments) {
        Object answer = null;
        int size = arguments.length;
        Class[] mostSpecificTypes = null;
        for (Iterator iter = matchingMethods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            Class[] paramTypes = getParameterTypes(method);
            if (answer == null) {
                answer = method;
                mostSpecificTypes = paramTypes;
            }
            else {
                boolean useThisMethod = false;
                for (int i = 0; i < size; i++) {
                    Class mostSpecificType = mostSpecificTypes[i];
                    Class type = paramTypes[i];

                    if (!isAssignableFrom(mostSpecificType, type)) {

                        useThisMethod = true;
                        break;
                    }

                }
                if (useThisMethod) {

                    if (size > 1) {
                        checkForInvalidOverloading(name, mostSpecificTypes, paramTypes);
                    }

                    answer = method;
                    mostSpecificTypes = paramTypes;
                }
            }
        }
        return answer;
    }

    /**
	 * Checks that one of the parameter types is a superset of the other and
	 * that the two lists of types don't conflict. e.g. foo(String, Object) and
	 * foo(Object, String) would conflict if called with foo("a", "b").
	 * 
	 * Note that this method is only called with 2 possible signatures. i.e.
	 * possible invalid combinations will already have been filtered out. So if
	 * there were methods foo(String, Object) and foo(Object, String) then one
	 * of these would be already filtered out if foo was called as foo(12, "a")
	 */
    protected void checkForInvalidOverloading(String name, Class[] baseTypes, Class[] derivedTypes) {
        for (int i = 0, size = baseTypes.length; i < size; i++) {
            Class baseType = baseTypes[i];
            Class derivedType = derivedTypes[i];
            if (!isAssignableFrom(derivedType, baseType)) {
                throw new GroovyRuntimeException(
                    "Ambiguous method overloading for method: "
                        + name
                        + ". Cannot resolve which method to invoke due to overlapping prototypes between: "
                        + InvokerHelper.toString(baseTypes)
                        + " and: "
                        + InvokerHelper.toString(derivedTypes));
            }
        }
    }

    protected Class[] getParameterTypes(Object methodOrConstructor) {
        if (methodOrConstructor instanceof Method) {
            Method method = (Method) methodOrConstructor;
            return method.getParameterTypes();
        }
        if (methodOrConstructor instanceof Constructor) {
            Constructor constructor = (Constructor) methodOrConstructor;
            return constructor.getParameterTypes();
        }
        throw new IllegalArgumentException("Must be a Method or Constructor");
    }

    /**
	 * @return the method with 1 parameter which takes the most general type of
	 *         object (e.g. Object)
	 */
    protected Object chooseMostGeneralMethodWith1Param(List methods) {
        // lets look for methods with 1 argument which matches the type of the
        // arguments
        Class closestClass = null;
        Object answer = null;

        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            Class[] paramTypes = getParameterTypes(method);
            int paramLength = paramTypes.length;
            if (paramLength == 1) {
                Class theType = paramTypes[0];
                if (closestClass == null || isAssignableFrom(closestClass, theType)) {
                    closestClass = theType;
                    answer = method;
                }
            }
        }
        return answer;
    }

    /**
	 * @return the method with 1 parameter which takes the most general type of
	 *         object (e.g. Object)
	 */
    protected Object chooseEmptyMethodParams(List methods) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            Class[] paramTypes = getParameterTypes(method);
            int paramLength = paramTypes.length;
            if (paramLength == 0) {
                return method;
            }
        }
        return null;
    }

    protected boolean isCompatibleInstance(Class type, Object value, boolean includeCoerce) {
        boolean answer = value == null || type.isInstance(value);
        if (!answer) {
            if (type.isPrimitive()) {
                if (type == int.class) {
                    return value instanceof Integer;
                }
                else if (type == double.class) {
                    return value instanceof Double;
                }
                else if (type == boolean.class) {
                    return value instanceof Boolean;
                }
                else if (type == long.class) {
                    return value instanceof Long;
                }
                else if (type == float.class) {
                    return value instanceof Float;
                }
                else if (type == char.class) {
                    return value instanceof Character;
                }
                else if (type == byte.class) {
                    return value instanceof Byte;
                }
                else if (type == short.class) {
                    return value instanceof Short;
                }
            }
            else if (includeCoerce && type == String.class && value instanceof GString) {
                return true;
            }
        }
        return answer;
    }

    protected boolean isAssignableFrom(Class mostSpecificType, Class type) {
        boolean answer = type.isAssignableFrom(mostSpecificType);
        if (!answer) {
            return autoboxType(type).isAssignableFrom(autoboxType(mostSpecificType));
        }
        return answer;
    }

    private Class autoboxType(Class type) {
        if (type.isPrimitive()) {
            if (type == int.class) {
                return Integer.class;
            }
            else if (type == double.class) {
                return Double.class;
            }
            else if (type == long.class) {
                return Long.class;
            }
            else if (type == boolean.class) {
                return Boolean.class;
            }
            else if (type == float.class) {
                return Float.class;
            }
            else if (type == char.class) {
                return Character.class;
            }
            else if (type == byte.class) {
                return Byte.class;
            }
            else if (type == short.class) {
                return Short.class;
            }
        }
        return type;
    }

    protected boolean isGenericSetMethod(Method method) {
        return (method.getName().equals("set") || method.getName().equals("setAttribute"))
            && method.getParameterTypes().length == 2;
    }

    protected boolean isGenericGetMethod(Method method) {
        if (method.getName().equals("get") || method.getName().equals("getAttribute")) {
            Class[] parameterTypes = method.getParameterTypes();
            return parameterTypes.length == 1 && parameterTypes[0] == String.class;
        }
        return false;
    }

    protected void registerStaticMethods() {
        Method[] methods = theClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (MethodHelper.isStatic(method)) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length > 0) {
                    Class owner = paramTypes[0];
                    registry.getMetaClass(owner).addNewStaticInstanceMethod(method);
                }
            }
        }
    }

    protected String capitalize(String property) {
        return property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
    }
}
