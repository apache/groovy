/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.lang;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.ReflectorGenerator;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.runtime.ClosureListener;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.MethodHelper;
import org.codehaus.groovy.runtime.MethodKey;
import org.codehaus.groovy.runtime.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.NewStaticMetaMethod;
import org.codehaus.groovy.runtime.ReflectionMetaMethod;
import org.codehaus.groovy.runtime.Reflector;
import org.codehaus.groovy.runtime.TemporaryMethodKey;
import org.codehaus.groovy.runtime.TransformMetaMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Allows methods to be dynamically added to existing classes at runtime
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MetaClass {

    private static final Logger log = Logger.getLogger(MetaClass.class.getName());

    public static final Object[] EMPTY_ARRAY = {
    };
    public static Class[] EMPTY_TYPE_ARRAY = {
    };
    protected static final Object[] ARRAY_WITH_NULL = { null };

    private static boolean useReflection = false;

    private MetaClassRegistry registry;
    private Class theClass;
    private ClassNode classNode;
    private Map methodIndex = new HashMap();
    private Map staticMethodIndex = new HashMap();
    private List newGroovyMethodsList = new ArrayList();
    private Map propertyDescriptors = Collections.synchronizedMap(new HashMap());
    private Map listeners = new HashMap();
    private Map methodCache = Collections.synchronizedMap(new HashMap());
    private Map staticMethodCache = Collections.synchronizedMap(new HashMap());
    private MetaMethod genericGetMethod;
    private MetaMethod genericSetMethod;
    private List constructors;
    private List allMethods = new ArrayList();
    private List interfaceMethods;
    private Reflector reflector;
    private boolean initialised;

    public MetaClass(MetaClassRegistry registry, final Class theClass) throws IntrospectionException {
        this.registry = registry;
        this.theClass = theClass;

        constructors = Arrays.asList(theClass.getDeclaredConstructors());
        addMethods(theClass);

        // introspect
        BeanInfo info = null;
        try {
        	info =(BeanInfo) AccessController.doPrivileged(new PrivilegedExceptionAction() {
        		public Object run() throws IntrospectionException {
        			return Introspector.getBeanInfo(theClass);
        		}
        	});
        } catch (PrivilegedActionException pae) {
        	if (pae.getException() instanceof IntrospectionException) {
        		throw (IntrospectionException) pae.getException();
        	} else {
        		throw new RuntimeException(pae.getException());
        	}
        }

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
                MetaMethod metaMethod = createMetaMethod(descriptor.getAddListenerMethod());
                listeners.put(listenerMethod.getName(), metaMethod);
            }
        }
    }

    public static boolean isUseReflection() {
        return useReflection;
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

    private void addInheritedMethods(Class theClass) {
        // lets add all the base class methods
        Class c = theClass;
        if (c != Object.class) {
            while (true) {
                c = c.getSuperclass();
                if (c == Object.class || c == null) {
                    break;
                }
                addMethods(c);
                addNewStaticMethodsFrom(c);

            }
        }

        // now lets see if there are any methods on one of my interfaces
        Class[] interfaces = theClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            addNewStaticMethodsFrom(interfaces[i]);
        }

        // lets add Object methods after interfaces, as all interfaces derive from Object. 
        // this ensures List and Collection methods come before Object etc
        if (theClass != Object.class) {
            addMethods(Object.class);
            addNewStaticMethodsFrom(Object.class);
        }

        if (theClass.isArray() && !theClass.equals(Object[].class)) {
            addNewStaticMethodsFrom(Object[].class);
        }
    }

    /**
     * @return all the normal instance methods avaiable on this class for the
     *         given name
     */
    public List getMethods(String name) {
        List answer = (List) methodIndex.get(name);
        List used = GroovyCategorySupport.getCategoryMethods(theClass, name);
        if (used != null) {
            if (answer != null) {
                answer.addAll(used);
            } else{
                answer = used;
            }
        }
        if (answer == null) {
            answer = Collections.EMPTY_LIST;
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
    protected void addNewInstanceMethod(Method method) {
        if (initialised) {
            throw new RuntimeException("Already initialized, cannot add new method: " + method);
        }
        else {
            NewInstanceMetaMethod newMethod = new NewInstanceMetaMethod(createMetaMethod(method));
            addMethod(newMethod);
            addNewInstanceMethod(newMethod);
        }
    }

    protected void addNewInstanceMethod(MetaMethod method) {
        newGroovyMethodsList.add(method);
    }

    protected void addNewStaticMethod(Method method) {
        if (initialised) {
            throw new RuntimeException("Already initialized, cannot add new method: " + method);
        }
        else {
            NewStaticMetaMethod newMethod = new NewStaticMetaMethod(createMetaMethod(method));
            addMethod(newMethod);
            addNewStaticMethod(newMethod);
        }
    }

    protected void addNewStaticMethod(MetaMethod method) {
        newGroovyMethodsList.add(method);
    }

    public Object invokeMethod(Object object, String methodName, Object arguments) {
        return invokeMethod(object, methodName, asArray(arguments));
    }

    /**
     * Invokes the given method on the object.
     *  
     */
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        if (object == null) {
            throw new NullPointerException("Cannot invoke method: " + methodName + " on null object");
        }

        // lets try use the cache to find the method
        MethodKey methodKey = new TemporaryMethodKey(methodName, arguments);
        MetaMethod method = (MetaMethod) methodCache.get(methodKey);
        if (method == null) {
            method = pickMethod(object, methodName, arguments);
            if (method != null && method.isCacheable()) {
                methodCache.put(methodKey.createCopy(), method);
            }
        }

        if (method != null) {
            return doMethodInvoke(object, method, arguments);
        }

        throw new MissingMethodException(methodName, theClass, arguments);
    }

    /**
     * Picks which method to invoke for the given object, method name and arguments
     */
    protected MetaMethod pickMethod(Object object, String methodName, Object[] arguments) {
        MetaMethod method = null;
        List methods = getMethods(methodName);
        if (!methods.isEmpty()) {
            method = (MetaMethod) chooseMethod(methodName, methods, arguments, false);
            if (method == null) {
                method = (MetaMethod) chooseMethod(methodName, methods, arguments, true);
                if (method == null) {
                    int size = (arguments != null) ? arguments.length : 0;
                    if (size == 1) {
                        Object firstArgument = arguments[0];
                        if (firstArgument instanceof List) {
                            // lets coerce the list arguments into an array of
                            // arguments
                            // e.g. calling JFrame.setLocation( [100, 100] )

                            List list = (List) firstArgument;
                            arguments = list.toArray();
                            method = (MetaMethod) chooseMethod(methodName, methods, arguments, true);
                            return new TransformMetaMethod(method) {
                                public Object invoke(Object object, Object[] arguments) throws Exception {
                                    Object firstArgument = arguments[0];
                                    List list = (List) firstArgument;
                                    arguments = list.toArray();
                                    return super.invoke(object, arguments);
                                }
                            };
                        }
                    }
                }
            }
        }
        return method;
    }

    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        //        System.out.println("Calling static method: " + methodName + " on args: " + InvokerHelper.toString(arguments));
        //        Class type = arguments == null ? null : arguments.getClass();
        //        System.out.println("Argument  type: " + type);
        //        System.out.println("Type of first arg: " + arguments[0] + " type: " + arguments[0].getClass());

        // lets try use the cache to find the method
        MethodKey methodKey = new TemporaryMethodKey(methodName, arguments);
        MetaMethod method = (MetaMethod) staticMethodCache.get(methodKey);
        if (method == null) {
            method = pickStaticMethod(object, methodName, arguments);
            if (method != null) {
                staticMethodCache.put(methodKey.createCopy(), method);
            }
        }

        if (method != null) {
            return doMethodInvoke(object, method, arguments);
        }
        /*
        List methods = getStaticMethods(methodName);
        
        if (!methods.isEmpty()) {
            MetaMethod method = (MetaMethod) chooseMethod(methodName, methods, arguments, false);
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
        */
        throw new MissingMethodException(methodName, theClass, arguments);
    }

    protected MetaMethod pickStaticMethod(Object object, String methodName, Object[] arguments) {
        MetaMethod method = null;
        List methods = getStaticMethods(methodName);

        if (!methods.isEmpty()) {
            method = (MetaMethod) chooseMethod(methodName, methods, arguments, false);
        }

        if (method == null && theClass != Class.class) {
            MetaClass classMetaClass = registry.getMetaClass(Class.class);
            method = classMetaClass.pickMethod(object, methodName, arguments);
        }
        return method;
    }

    public Object invokeConstructor(Object[] arguments) {
        Constructor constructor = (Constructor) chooseMethod("<init>", constructors, arguments, false);
        if (constructor != null) {
            return doConstructorInvoke(constructor, arguments);
        }
        else {
            constructor = (Constructor) chooseMethod("<init>", constructors, arguments, true);
            if (constructor != null) {
                return doConstructorInvoke(constructor, arguments);
            }
        }

        if (arguments.length == 1) {
            Object firstArgument = arguments[0];
            if (firstArgument instanceof Map) {
                constructor = (Constructor) chooseMethod("<init>", constructors, EMPTY_ARRAY, false);
                if (constructor != null) {
                    Object bean = doConstructorInvoke(constructor, EMPTY_ARRAY);
                    setProperties(bean, ((Map) firstArgument));
                    return bean;
                }
            }
        }
        throw new GroovyRuntimeException(
        			"Could not find matching constructor for: "
        				+ theClass.getName()
						+ "("+InvokerHelper.toTypeString(arguments)+")");
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
            try {
                setProperty(bean, key, value);
            }
            catch (GroovyRuntimeException e) {
                // lets ignore missing properties
                /** @todo should replace this code with a getMetaProperty(key) != null check
                 i.e. don't try and set a non-existent property
                 */
            }
        }
    }

    /**
     * @return the given property's value on the object
     */
    public Object getProperty(final Object object, final String property) {
        MetaMethod metaMethod = null;
        PropertyDescriptor descriptor = (PropertyDescriptor) propertyDescriptors.get(property);
        if (descriptor != null) {
            Method method = descriptor.getReadMethod();
            if (method == null) {
                throw new GroovyRuntimeException("Cannot read property: " + property);
            }
            metaMethod = findMethod(method);
            if (metaMethod == null) {
                // lets try invoke a static getter method
                metaMethod = findGetter(object, "get" + capitalize(property));
            }
            if (metaMethod != null) {
                return doMethodInvoke(object, metaMethod, EMPTY_ARRAY);
            }
        }

        if (genericGetMethod == null) {
            // Make sure there isn't a generic method in the "use" cases
            List possibleGenericMethods = getMethods("get");
            if (possibleGenericMethods != null) {
                for (Iterator i = possibleGenericMethods.iterator(); i.hasNext(); ) {
                    MetaMethod mmethod = (MetaMethod) i.next();
                    Class[] paramTypes = mmethod.getParameterTypes();
                    if (paramTypes.length == 1 && paramTypes[0] == String.class) {
                        Object[] arguments = {property};
                        Object answer = doMethodInvoke(object, mmethod, arguments);
                        return answer;
                    }
                }
            }
        } else {
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
            MetaMethod method = findGetter(object, "get" + capitalize(property));
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
                return DefaultGroovyMethods.getAt((Collection) object, property);
            }
            if (object instanceof Object[]) {
                return DefaultGroovyMethods.getAt(Arrays.asList((Object[]) object), property);
            }
            if (object instanceof Object) {
                try {
                    // lets try a public field
                    Field field = object.getClass().getDeclaredField(property);
                    return field.get(object);
                }
                catch (Exception e1) {
                    // fall through
                }
            }
            
            MetaMethod addListenerMethod = (MetaMethod) listeners.get(property);
            if (addListenerMethod != null) {
                /* @todo one day we could try return the previously registered Closure listener for easy removal */
                return null;
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
                throw new ReadOnlyPropertyException(property, theClass);
            }
            MetaMethod metaMethod = findMethod(method);
            Object[] arguments = { newValue };
            try {
                doMethodInvoke(object, metaMethod, arguments);
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
                            Object value = doConstructorInvoke(constructor, list.toArray());
                            doMethodInvoke(object, metaMethod, new Object[] { value });
                            return;
                        }
                    }
                    
                    // if value is an array  
                    Class parameterType = method.getParameterTypes()[0];
                    if (parameterType.isArray()) {
                        Object objArray = asPrimitiveArray(list, parameterType);
                        doMethodInvoke(object, metaMethod, new Object[]{
                            objArray
                        });
                        return;
                    }
                }
                
                // if value is an multidimensional array  
                if (newValue.getClass().isArray()) {
                    List list = Arrays.asList((Object[])newValue);
                    
                    Class parameterType = method.getParameterTypes()[0];
                    Class arrayType = parameterType.getComponentType();
                    Object objArray = Array.newInstance(arrayType, list.size());
                    
                    for (int i = 0; i < list.size(); i++) {
                        List list2 =Arrays.asList((Object[]) list.get(i));
                        Object objArray2 = asPrimitiveArray(list2, arrayType);
                        Array.set(objArray, i, objArray2);
                    }

                    doMethodInvoke(object, metaMethod, new Object[]{
                        objArray
                    });
                    return;
                }
                
                throw new MissingPropertyException(property, theClass, e);
            }
            return;
        }

        try {
            MetaMethod addListenerMethod = (MetaMethod) listeners.get(property);
            if (addListenerMethod != null && newValue instanceof Closure) {
                // lets create a dynamic proxy
                Object proxy =
                    createListenerProxy(addListenerMethod.getParameterTypes()[0], property, (Closure) newValue);
                doMethodInvoke(object, addListenerMethod, new Object[] { proxy });
                return;
            }

            if (genericGetMethod == null) {
                // Make sure there isn't a generic method in the "use" cases
                List possibleGenericMethods = getMethods("set");
                if (possibleGenericMethods != null) {
                    for (Iterator i = possibleGenericMethods.iterator(); i.hasNext(); ) {
                        MetaMethod mmethod = (MetaMethod) i.next();
                        Class[] paramTypes = mmethod.getParameterTypes();
                        if (paramTypes.length == 2 && paramTypes[0] == String.class) {
                            Object[] arguments = {property, newValue};
                            Object answer = doMethodInvoke(object, mmethod, arguments);
                            return;
                        }
                    }
                }
            } else{
                Object[] arguments = { property, newValue };
                doMethodInvoke(object, genericSetMethod, arguments);
                return;
            }

            /** @todo or are we an extensible class? */

            // lets try invoke the set method

            String method = "set" + capitalize(property);
            try {
                invokeMethod(object, method, new Object[] { newValue });
            }
            catch (MissingMethodException e1) {
                try {
                    Field field = object.getClass().getDeclaredField(property);
                    field.set(object, newValue);
                }
                catch (Exception e2) {
                    throw new MissingPropertyException(property, theClass, e2);
                }
            }
            
        }
        catch (GroovyRuntimeException e) {
            throw new MissingPropertyException(property, theClass, e);
        }
    }

    /**
     * @param list
     * @param params
     * @param parameterType
     * @return
     */
    private Object asPrimitiveArray(List list, Class parameterType) {
        Class arrayType = parameterType.getComponentType();
        Object objArray = Array.newInstance(arrayType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (arrayType.isPrimitive()) {
                if (obj instanceof Integer) {
                    Array.setInt(objArray, i, ((Integer) obj).intValue());
                }
                else if (obj instanceof Double) {
                    Array.setDouble(objArray, i, ((Double) obj).doubleValue());
                }
                else if (obj instanceof Boolean) {
                    Array.setBoolean(objArray, i, ((Boolean) obj).booleanValue());
                }
                else if (obj instanceof Long) {
                    Array.setLong(objArray, i, ((Long) obj).longValue());
                }
                else if (obj instanceof Float) {
                    Array.setFloat(objArray, i, ((Float) obj).floatValue());
                }
                else if (obj instanceof Character) {
                    Array.setChar(objArray, i, ((Character) obj).charValue());
                }
                else if (obj instanceof Byte) {
                    Array.setByte(objArray, i, ((Byte) obj).byteValue());
                }
                else if (obj instanceof Short) {
                    Array.setShort(objArray, i, ((Short) obj).shortValue());
                }
            }
            else {
                Array.set(objArray, i, obj);
            }
        }
        return objArray;
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

                    /**
                     * @todo there is no CompileUnit in scope so class name
                     * checking won't work but that mostly affects the bytecode
                     * generation rather than viewing the AST
                     */

                    CompilationUnit.ClassgenCallback search = new CompilationUnit.ClassgenCallback() {
                        public void call( ClassVisitor writer, ClassNode node ) {
                            if( node.getName().equals(theClass.getName()) ) {
                                MetaClass.this.classNode = node;
                            }
                        }
                    };
                    
                    
                    CompilationUnit unit = new CompilationUnit( getClass().getClassLoader() );
                    unit.setClassgenCallback( search );
                    unit.addSource( url );
                    unit.compile( Phases.CLASS_GENERATION );
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
        	Method reflectionMethod = methodArray[i];
            if ( reflectionMethod.getName().indexOf('+') >= 0 ) {
        		continue;
        	}
            MetaMethod method = createMetaMethod(reflectionMethod);
            addMethod(method);
        }
    }

    protected void addMethod(MetaMethod method) {
        String name = method.getName();

        //System.out.println(theClass.getName() + " == " + name + Arrays.asList(method.getParameterTypes()));

        if (isGenericGetMethod(method) && genericGetMethod == null) {
            genericGetMethod = method;
        }
        else if (isGenericSetMethod(method) && genericSetMethod == null) {
            genericSetMethod = method;
        }
        if (method.isStatic()) {
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

    /**
     * @return true if a method of the same matching prototype was found in the
     *         list
     */
    protected boolean containsMatchingMethod(List list, MetaMethod method) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MetaMethod aMethod = (MetaMethod) iter.next();
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
        Iterator iter = interfaceMetaClass.newGroovyMethodsList.iterator();
        while (iter.hasNext()) {
            MetaMethod method = (MetaMethod) iter.next();
            addMethod(method);
            newGroovyMethodsList.add(method);
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
            MetaMethod method = findStaticGetter(aClass, "get" + capitalize(property));
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
     * @return the matching method which should be found
     */
    protected MetaMethod findMethod(Method aMethod) {
        List methods = getMethods(aMethod.getName());
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MetaMethod method = (MetaMethod) iter.next();
            if (method.isMethod(aMethod)) {
                return method;
            }
        }
        //log.warning("Creating reflection based dispatcher for: " + aMethod);
        return new ReflectionMetaMethod(aMethod);
    }

    /**
     * @return the getter method for the given object
     */
    protected MetaMethod findGetter(Object object, String name) {
        List methods = getMethods(name);
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MetaMethod method = (MetaMethod) iter.next();
            if (method.getParameterTypes().length == 0) {
                return method;
            }
        }
        return null;
    }

    /**
     * @return the Method of the given name with no parameters or null
     */
    protected MetaMethod findStaticGetter(Class type, String name) {
        List methods = getStaticMethods(name);
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MetaMethod method = (MetaMethod) iter.next();
            if (method.getParameterTypes().length == 0) {
                return method;
            }
        }

        /** @todo dirty hack - don't understand why this code is necessary - all methods should be in the allMethods list! */
        try {
            Method method = type.getMethod(name, EMPTY_TYPE_ARRAY);
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                return findMethod(method);
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    protected Object doMethodInvoke(Object object, MetaMethod method, Object[] argumentArray) {
        //System.out.println("Evaluating method: " + method);
        //System.out.println("on object: " + object + " with arguments: " +
        // InvokerHelper.toString(argumentArray));
        //System.out.println(this.theClass);

        try {
            if (argumentArray == null) {
                argumentArray = EMPTY_ARRAY;
            }
            else if (method.getParameterTypes().length == 1 && argumentArray.length == 0) {
                argumentArray = ARRAY_WITH_NULL;
            }
            return method.invoke(object, argumentArray);
        }
        catch (ClassCastException e) {
            if (coerceGStrings(argumentArray)) {
                try {
                    return doMethodInvoke(object, method, argumentArray);
                }
                catch (Exception e2) {
                    // allow fall through
                }
            }
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
            if (coerceGStrings(argumentArray)) {
                try {
                    return doMethodInvoke(object, method, argumentArray);
                }
                catch (Exception e2) {
                    // allow fall through
                }
            }
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
        catch (RuntimeException e) {
            throw e;
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
        catch (IllegalArgumentException e) {
            if (coerceGStrings(argumentArray)) {
                try {
                    return constructor.newInstance(argumentArray);
                }
                catch (Exception e2) {
                    // allow fall through
                }
            }
            throw new GroovyRuntimeException(
                "failed to invoke constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                e);
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
    protected Object chooseMethod(String methodName, List methods, Object[] arguments, boolean coerce) {
        int methodCount = methods.size();
        if (methodCount <= 0) {
            return null;
        }
        else if (methodCount == 1) {
            Object method = methods.get(0);
            if (isValidMethod(method, arguments, coerce)) {
                return method;
            }
            return null;
        }
        Object answer = null;
        if (arguments == null || arguments.length == 0) {
            answer = chooseEmptyMethodParams(methods);
        }
        else if (arguments.length == 1 && arguments[0] == null) {
            answer = chooseMostGeneralMethodWith1Param(methods);
        }
        else {
            List matchingMethods = new ArrayList();

            for (Iterator iter = methods.iterator(); iter.hasNext();) {
                Object method = iter.next();
                Class[] paramTypes;

                // making this false helps find matches
                if (isValidMethod(method, arguments, coerce)) {
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
        Class[] paramTypes = getParameterTypes(method);
        return isValidMethod(paramTypes, arguments, includeCoerce);
    }

    protected static boolean isValidMethod(Class[] paramTypes, Object[] arguments, boolean includeCoerce) {
        if (arguments == null) {
            return true;
        }
        int size = arguments.length;
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
        if (methodOrConstructor instanceof MetaMethod) {
            MetaMethod method = (MetaMethod) methodOrConstructor;
            return method.getParameterTypes();
        }
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

    protected static boolean isCompatibleInstance(Class type, Object value, boolean includeCoerce) {
        boolean answer = value == null || type.isInstance(value);
        if (!answer) {
            if (type.isPrimitive()) {
                if (type == int.class) {
                    return value instanceof Integer;
                }
                else if (type == double.class) {
                    return value instanceof Double || value instanceof Float || value instanceof Integer || value instanceof BigDecimal;
                }
                else if (type == boolean.class) {
                    return value instanceof Boolean;
                }
                else if (type == long.class) {
                    return value instanceof Long || value instanceof Integer;
                }
                else if (type == float.class) {
                    return value instanceof Float || value instanceof Integer;
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
            else if (includeCoerce) {
                if (type == String.class && value instanceof GString) {
                    return true;
                }
                else if (value instanceof Number) {
                    // lets allow numbers to be coerced downwards?
                    return Number.class.isAssignableFrom(type);
                }
            }
        }
        return answer;
    }

    protected boolean isAssignableFrom(Class mostSpecificType, Class type) {
        boolean answer = type.isAssignableFrom(mostSpecificType);
        if (!answer) {
            answer = autoboxType(type).isAssignableFrom(autoboxType(mostSpecificType));
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

    /**
     * Coerces any GString instances into Strings
     * 
     * @return true if some coercion was done. 
     */
    protected boolean coerceGStrings(Object[] arguments) {
        boolean coerced = false;
        for (int i = 0, size = arguments.length; i < size; i++) {
            Object argument = arguments[i];
            if (argument instanceof GString) {
                arguments[i] = argument.toString();
                coerced = true;
            }
        }
        return coerced;
    }

    protected boolean isGenericSetMethod(MetaMethod method) {
        return (method.getName().equals("set"))
            && method.getParameterTypes().length == 2;
    }

    protected boolean isGenericGetMethod(MetaMethod method) {
        if (method.getName().equals("get")) {
            Class[] parameterTypes = method.getParameterTypes();
            return parameterTypes.length == 1 && parameterTypes[0] == String.class;
        }
        return false;
    }

    private void registerMethods(boolean instanceMethods) {
        Method[] methods = theClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (MethodHelper.isStatic(method)) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length > 0) {
                    Class owner = paramTypes[0];
                    if (instanceMethods) {
                        registry.lookup(owner).addNewInstanceMethod(method);
                    } else {
                        registry.lookup(owner).addNewStaticMethod(method);
                    }
                }
            }
        }
    }

    protected void registerStaticMethods() {
        registerMethods(false);
    }

    protected void registerInstanceMethods() {
        registerMethods(true);
    }

    protected String capitalize(String property) {
        return property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
    }

    /**
     * Call this method when any mutation method is called, such as adding a new
     * method to this MetaClass so that any caching or bytecode generation can be
     * regenerated.
     */
    protected synchronized void onMethodChange() {
        reflector = null;
    }
    
    protected synchronized void checkInitialised() {
        if (!initialised) {
            initialised = true;
            addInheritedMethods(theClass);
        }
        if (reflector == null) {
            generateReflector();
        }
    }

    protected MetaMethod createMetaMethod(final Method method) {
	    if (registry.useAccessible()) {
	    	AccessController.doPrivileged(new PrivilegedAction() {
	    		public Object run() {
	                method.setAccessible(true);
	                return null;
	    		}
	    	});
	    }
	    if (useReflection) {
	        //log.warning("Creating reflection based dispatcher for: " + method);
	        return new ReflectionMetaMethod(method);
	    }
	    MetaMethod answer = new MetaMethod(method);
	    if (isValidReflectorMethod(answer)) {
	        allMethods.add(answer);
	        answer.setMethodIndex(allMethods.size());
	    }
	    else {
	        //log.warning("Creating reflection based dispatcher for: " + method);
	        answer = new ReflectionMetaMethod(method);
	    }
	    return answer;
	}

    protected boolean isValidReflectorMethod(MetaMethod method) {
        // We cannot use a reflector if the method is private, protected, or package accessible only.
        if (!method.isPublic()) {
            return false;
        }
        Class declaringClass = method.getDeclaringClass();
        if (!Modifier.isPublic(declaringClass.getModifiers())) {
            // lets see if this method is implemented on an interface
            List list = getInterfaceMethods();
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                MetaMethod aMethod = (MetaMethod) iter.next();
                if (method.isSame(aMethod)) {
                    method.setInterfaceClass(aMethod.getDeclaringClass());
                    return true;
                }
            }
            /** @todo */
            //log.warning("Cannot invoke method on protected/private class which isn't visible on an interface so must use reflection instead: " + method);
            return false;
        }
        return true;
    }

    protected void generateReflector() {
        reflector = loadReflector(allMethods);
        if (reflector == null) {
            throw new RuntimeException("Should have a reflector!");
        }
        // lets set the reflector on all the methods
        for (Iterator iter = allMethods.iterator(); iter.hasNext();) {
            MetaMethod metaMethod = (MetaMethod) iter.next();
            //System.out.println("Setting reflector for method: " + metaMethod + " with index: " + metaMethod.getMethodIndex());
            metaMethod.setReflector(reflector);
        }
    }

    protected Reflector loadReflector(List methods) {
        ReflectorGenerator generator = new ReflectorGenerator(methods);
        String className = theClass.getName();
        String packagePrefix = "gjdk.";
        /*
        if (className.startsWith("java.")) {
            packagePrefix = "gjdk.";
        }
        */
        String name = packagePrefix + className + "_GroovyReflector";
        if (theClass.isArray()) {
            String componentName = theClass.getComponentType().getName();
            /*
            if (componentName.startsWith("java.")) {
                packagePrefix = "gjdk.";
            }
            */
            name = packagePrefix + componentName + "_GroovyReflectorArray";
        }
        // lets see if its already loaded
        try {
            Class type = loadReflectorClass(name);
            return (Reflector) type.newInstance();
        }
        catch (AccessControlException ace) {
        	//Don't ignore this exception type
        	throw ace;
        } 
        catch (Exception e) {
            // lets ignore, lets generate it && load it
        }

        ClassWriter cw = new ClassWriter(true);
        generator.generate(cw, name);

        byte[] bytecode = cw.toByteArray();

        try {
            Class type = loadReflectorClass(name, bytecode);
            return (Reflector) type.newInstance();
        }
        catch (Exception e) {
            throw new GroovyRuntimeException("Could not load the reflector for class: " + name + ". Reason: " + e, e);
        }
    }

    protected Class loadReflectorClass(final String name, final byte[] bytecode) throws ClassNotFoundException {
        ClassLoader loader = theClass.getClassLoader();
        if (loader instanceof GroovyClassLoader) {
            final GroovyClassLoader gloader = (GroovyClassLoader) loader;
        	return (Class) AccessController.doPrivileged(new PrivilegedAction() {
        		public Object run() {
        			return gloader.defineClass(name, bytecode, getClass().getProtectionDomain());
        		}
        	});
        }
        return registry.loadClass(name, bytecode);
    }

    protected Class loadReflectorClass(String name) throws ClassNotFoundException {
        ClassLoader loader = theClass.getClassLoader();
        if (loader instanceof GroovyClassLoader) {
            GroovyClassLoader gloader = (GroovyClassLoader) loader;
            return gloader.loadClass(name);
        }
        return registry.loadClass(name);
    }

    public List getMethods() {
        return allMethods;
    }

    protected synchronized List getInterfaceMethods() {
        if (interfaceMethods == null) {
            interfaceMethods = new ArrayList();
            Class type = theClass;
            while (type != null) {
                Class[] interfaces = type.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    Class iface = interfaces[i];
                    Method[] methods = iface.getMethods();
                    addInterfaceMethods(interfaceMethods, methods);
                }
                type = type.getSuperclass();
            }
        }
        return interfaceMethods;
    }

    private void addInterfaceMethods(List list, Method[] methods) {
        for (int i = 0; i < methods.length; i++) {
            list.add(createMetaMethod(methods[i]));
        }
    }
}
