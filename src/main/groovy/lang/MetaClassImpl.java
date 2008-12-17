/*
 * Copyright 2003-2007 the original author or authors.
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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.util.FastArray;
import org.codehaus.groovy.util.SingleKeyHashMap;
import org.codehaus.groovy.util.ComplexKeyHashMap;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.reflection.*;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.callsite.*;
import org.codehaus.groovy.runtime.metaclass.*;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.NumberMathModificationInfo;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.objectweb.asm.ClassVisitor;

import java.beans.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

/**
 * Allows methods to be dynamically added to existing classes at runtime
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @author Jochen Theodorou
 * @author Graeme Rocher
 * @author Alex Tkachman
 * @version $Revision$
 * @see groovy.lang.MetaClass
 */
public class MetaClassImpl implements MetaClass, MutableMetaClass {

    private static final String CLOSURE_CALL_METHOD = "call";
    private static final String CLOSURE_DO_CALL_METHOD = "doCall";
    private static final String CLOSURE_CURRY_METHOD = "curry";
    protected static final String STATIC_METHOD_MISSING = "$static_methodMissing";
    protected static final String STATIC_PROPERTY_MISSING = "$static_propertyMissing";
    protected static final String METHOD_MISSING = "methodMissing";
    protected static final String PROPERTY_MISSING = "propertyMissing";
    private static final String GET_PROPERTY_METHOD = "getProperty";
    private static final String SET_PROPERTY_METHOD = "setProperty";
    protected static final String INVOKE_METHOD_METHOD = "invokeMethod";

    private static final Class[] METHOD_MISSING_ARGS = new Class[]{String.class, Object.class};
    private static final Class[] GETTER_MISSING_ARGS = new Class[]{String.class};
    private static final Class[] SETTER_MISSING_ARGS = METHOD_MISSING_ARGS;

    protected final Class theClass;
    protected final CachedClass theCachedClass;
    private static final MetaMethod[] EMPTY = new MetaMethod[0];
    protected MetaMethod getPropertyMethod;
    protected MetaMethod invokeMethodMethod;
    protected MetaMethod setPropertyMethod;

    public final CachedClass getTheCachedClass() {
        return theCachedClass;
    }

    protected MetaClassRegistry registry;
    protected final boolean isGroovyObject;
    protected final boolean isMap;
    private ClassNode classNode;

    private final Index classPropertyIndex = new MethodIndex();
    private Index classPropertyIndexForSuper = new MethodIndex();
    private final SingleKeyHashMap staticPropertyIndex = new SingleKeyHashMap();

    private final Map listeners = new HashMap();
    private FastArray constructors;
    private final List allMethods = new ArrayList();
    private List interfaceMethods;
    private boolean initialized;
    // we only need one of these that can be reused over and over.
    private final MetaProperty arrayLengthProperty = new MetaArrayLengthProperty();
    private static final MetaMethod AMBIGUOUS_LISTENER_METHOD = new DummyMetaMethod();
    private static final Object[] EMPTY_ARGUMENTS = {};
    private final Set newGroovyMethodsSet = new HashSet();

    private MetaMethod genericGetMethod;
    private MetaMethod genericSetMethod;
    private MetaMethod propertyMissingGet;
    private MetaMethod propertyMissingSet;
    private static final MetaMethod NULL_METHOD = new DummyMetaMethod();
    private MetaMethod methodMissing;
    private MetaMethodIndex.Header mainClassMethodHeader;
    protected final MetaMethodIndex metaMethodIndex;

    private final MetaMethod [] myNewMetaMethods;
    private final MetaMethod [] additionalMetaMethods;

    public MetaClassImpl(final Class theClass, MetaMethod [] add) {
        this.theClass = theClass;
        theCachedClass = ReflectionCache.getCachedClass(theClass);
        this.isGroovyObject = GroovyObject.class.isAssignableFrom(theClass);
        this.isMap = Map.class.isAssignableFrom(theClass);
        this.registry = GroovySystem.getMetaClassRegistry();
        metaMethodIndex = new MetaMethodIndex(theCachedClass);
        final MetaMethod[] metaMethods = theCachedClass.getNewMetaMethods();
        if (add != null && !(add.length == 0)) {
            ArrayList arr = new ArrayList();
            arr.addAll(Arrays.asList(metaMethods));
            arr.addAll(Arrays.asList(add));
            myNewMetaMethods = (MetaMethod[])arr.toArray(new MetaMethod[arr.size()]);
            additionalMetaMethods = metaMethods;
        }
        else {
            myNewMetaMethods = metaMethods;
            additionalMetaMethods = EMPTY;
        }
    }

    public MetaClassImpl(final Class theClass) {
        this(theClass, null);
    }

    public MetaClassImpl(MetaClassRegistry registry, final Class theClass, MetaMethod add []) {
        this(theClass, add);
        this.registry = registry;
        this.constructors = new FastArray(theCachedClass.getConstructors());
    }

    public MetaClassImpl(MetaClassRegistry registry, final Class theClass) {
        this(registry, theClass, null);
    }

    /**
     * @see MetaObjectProtocol#respondsTo(Object,String, Object[])
     */
    public List respondsTo(Object obj, String name, Object[] argTypes) {
        Class[] classes = MetaClassHelper.castArgumentsToClassArray(argTypes);
        MetaMethod m = getMetaMethod(name, classes);
        List methods = new ArrayList();
        if (m != null) {
            methods.add(m);
        }
        return methods;
    }

    /**
     * @see MetaObjectProtocol#respondsTo(Object,String, Object[])
     */
    public List respondsTo(final Object obj, final String name) {
        final Object o = getMethods(getTheClass(), name, false);
        if (o instanceof FastArray)
          return ((FastArray)o).toList();
        else
          return Collections.singletonList(o);
    }

    /**
     * @see MetaObjectProtocol#hasProperty(Object,String)
     */
    public MetaProperty hasProperty(Object obj, String name) {
        return getMetaProperty(name);
    }

    /**
     * @see MetaObjectProtocol#getMetaProperty(String)
     */
    public MetaProperty getMetaProperty(String name) {
        SingleKeyHashMap propertyMap = classPropertyIndex.getNotNull(theCachedClass);
        if (propertyMap.containsKey(name)) {
            return (MetaProperty) propertyMap.get(name);
        } else if (staticPropertyIndex.containsKey(name)) {
            return (MetaProperty) staticPropertyIndex.get(name);
        } else {
            propertyMap = classPropertyIndexForSuper.getNotNull(theCachedClass);
            if (propertyMap.containsKey(name))
              return (MetaProperty) propertyMap.get(name);
            else {
                CachedClass superClass = theCachedClass;
                while(superClass != null && superClass != ReflectionCache.OBJECT_CLASS) {
                    final MetaBeanProperty property = findPropertyInClassHierarchy(name, superClass);
                    if(property != null) {
                        onSuperPropertyFoundInHierarchy(property);
                        return property;
                    }
                    superClass = superClass.getCachedSuperClass();
                }
                return null;
            }
        }
    }

    /**
     * @see MetaObjectProtocol#getStaticMetaMethod(String, Object[])
     */
    public MetaMethod getStaticMetaMethod(String name, Object[] argTypes) {
        Class[] classes = MetaClassHelper.castArgumentsToClassArray(argTypes);
        return pickStaticMethod(name, classes);
    }


    /**
     * @see MetaObjectProtocol#getMetaMethod(String, Object[])
     */
    public MetaMethod getMetaMethod(String name, Object[] argTypes) {
        Class[] classes = MetaClassHelper.castArgumentsToClassArray(argTypes);
        return pickMethod(name, classes);
    }

    public Class getTheClass() {
        return this.theClass;
    }

    public boolean isGroovyObject() {
        return isGroovyObject;
    }

    private void fillMethodIndex() {
        mainClassMethodHeader = metaMethodIndex.getHeader(theClass);
        LinkedList superClasses = getSuperClasses();
        CachedClass firstGroovySuper = calcFirstGroovySuperClass(superClasses);

        Set interfaces = theCachedClass.getInterfaces();
        addInterfaceMethods(interfaces);

        populateMethods(superClasses, firstGroovySuper);

        inheritInterfaceNewMetaMethods(interfaces);
        if (isGroovyObject) {
          metaMethodIndex.copyMethodsToSuper();

          connectMultimethods(superClasses, firstGroovySuper);
          removeMultimethodsOverloadedWithPrivateMethods();

          replaceWithMOPCalls(theCachedClass.mopMethods);
        }
    }

    private void populateMethods(LinkedList superClasses, CachedClass firstGroovySuper) {
        Iterator iter = superClasses.iterator();

        MetaMethodIndex.Header header = metaMethodIndex.getHeader(firstGroovySuper.getTheClass());
        CachedClass c;
        for (; iter.hasNext();) {
            c = (CachedClass) iter.next();

            CachedMethod[] cachedMethods = c.getMethods();
            for (int i = 0; i < cachedMethods.length; i++) {
                MetaMethod metaMethod = cachedMethods[i];
                addToAllMethodsIfPublic(metaMethod);
                if (!metaMethod.isPrivate() || c == firstGroovySuper)
                  addMetaMethodToIndex(metaMethod, header);
            }

            MetaMethod[] cachedMethods1 = getNewMetaMethods(c);
            for (int i = 0; i < cachedMethods1.length; i++) {
                final MetaMethod method = cachedMethods1[i];

                if (!newGroovyMethodsSet.contains(method)) {
                    newGroovyMethodsSet.add(method);
                    addMetaMethodToIndex(method, header);
                }
            }

            if (c == firstGroovySuper)
              break;
        }

        MetaMethodIndex.Header last = header;
        for (;iter.hasNext();) {
            c = (CachedClass) iter.next();
            header = metaMethodIndex.getHeader(c.getTheClass());

            if (last != null) {
                metaMethodIndex.copyNonPrivateMethods(last, header);
            }
            last = header;

            CachedMethod[] cachedMethods = c.getMethods();
            for (int i = 0; i < cachedMethods.length; i++) {
                MetaMethod metaMethod = cachedMethods[i];
                addToAllMethodsIfPublic(metaMethod);
                addMetaMethodToIndex(metaMethod, header);
            }

            MetaMethod[] cachedMethods1 = getNewMetaMethods(c);
            for (int i = 0; i < cachedMethods1.length; i++) {
                final MetaMethod method = cachedMethods1[i];

                if (!newGroovyMethodsSet.contains(method)) {
                    newGroovyMethodsSet.add(method);
                    addMetaMethodToIndex(method, header);
                }
            }
        }
    }

    private MetaMethod[] getNewMetaMethods(CachedClass c) {
        if (theCachedClass != c)
          return c.getNewMetaMethods();

        return myNewMetaMethods;
    }

    private void addInterfaceMethods(Set interfaces) {
        MetaMethodIndex.Header header = metaMethodIndex.getHeader(theClass);
        for (Iterator iter = interfaces.iterator(); iter.hasNext();) {
            CachedClass c = (CachedClass) iter.next();
            final CachedMethod[] m = c.getMethods();
            for (int i=0; i != m.length; ++i) {
                MetaMethod method = m[i];
                addMetaMethodToIndex (method, header);
            }
        }
    }

    protected LinkedList<CachedClass> getSuperClasses() {
        LinkedList<CachedClass> superClasses = new LinkedList<CachedClass>();

        if (theClass.isInterface()) {
            superClasses.addFirst(ReflectionCache.OBJECT_CLASS);
        } else {
            for (CachedClass c = theCachedClass; c != null; c = c.getCachedSuperClass()) {
                superClasses.addFirst(c);
            }
            if (theCachedClass.isArray && theClass != Object[].class && !theClass.getComponentType().isPrimitive()) {
                superClasses.addFirst(ReflectionCache.OBJECT_ARRAY_CLASS);
            }
        }
        return superClasses;
    }

    private void removeMultimethodsOverloadedWithPrivateMethods() {
        MethodIndexAction mia = new MethodIndexAction() {
            public boolean skipClass(Class clazz) {
                return clazz == theClass;
            }

            public void methodNameAction(Class clazz, MetaMethodIndex.Entry e) {
                if (e.methods == null)
                  return;

                boolean hasPrivate = false;
                if (e.methods instanceof FastArray) {
                    FastArray methods = (FastArray) e.methods;
                    final int len = methods.size();
                    final Object[] data = methods.getArray();
                    for (int i = 0; i != len; ++i) {
                        MetaMethod method = (MetaMethod) data[i];
                        if (method.isPrivate() && clazz == method.getDeclaringClass().getTheClass()) {
                            hasPrivate = true;
                            break;
                        }
                    }
                }
                else {
                    MetaMethod method = (MetaMethod) e.methods;
                    if (method.isPrivate() && clazz == method.getDeclaringClass().getTheClass()) {
                       hasPrivate = true;
                    }
                }

                if (!hasPrivate) return;

                // We have private methods for that name, so remove the
                // multimethods. That is the same as in our index for
                // super, so just copy the list from there. It is not
                // possible to use a pointer here, because the methods
                // in the index for super are replaced later by MOP
                // methods like super$5$foo
                final Object o = e.methodsForSuper;
                if (o instanceof FastArray)
                  e.methods = ((FastArray) o).copy();
                else
                  e.methods = o;
            }
        };
        mia.iterate();
    }


    private void replaceWithMOPCalls(final CachedMethod[] mopMethods) {
        // no MOP methods if not a child of GroovyObject
        if (!isGroovyObject) return;

        class MOPIter extends MethodIndexAction {
            boolean useThis;
            public boolean skipClass(CachedClass clazz) {
                return !useThis && clazz == theCachedClass;
            }

            public void methodNameAction(Class clazz, MetaMethodIndex.Entry e) {
                if (useThis) {
                    if (e.methods == null)
                      return;

                    if (e.methods instanceof FastArray) {
                        FastArray methods = (FastArray) e.methods;
                        processFastArray(methods);
                    }
                    else {
                        MetaMethod method = (MetaMethod) e.methods;
                        if (method instanceof NewMetaMethod)
                          return;
                        if (useThis ^ (method.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0)
                          return;
                        String mopName = method.getMopName();
                        int index = Arrays.binarySearch(mopMethods, mopName, CachedClass.CachedMethodComparatorWithString.INSTANCE);
                        if (index >= 0) {
                            int from = index;
                            while (from > 0 && mopMethods[from-1].getName().equals(mopName))
                              from--;
                            int to = index;
                            while (to < mopMethods.length-1 && mopMethods[to+1].getName().equals(mopName))
                              to++;

                            int matchingMethod = findMatchingMethod(mopMethods, from, to, method);
                            if (matchingMethod != -1) {
                                e.methods = mopMethods[matchingMethod];
                            }
                        }
                    }
                }
                else {
                    if (e.methodsForSuper == null)
                      return;

                    if (e.methodsForSuper instanceof FastArray) {
                        FastArray methods = (FastArray) e.methodsForSuper;
                        processFastArray(methods);
                    }
                    else {
                        MetaMethod method = (MetaMethod) e.methodsForSuper;
                        if (method instanceof NewMetaMethod)
                          return;
                        if (useThis ^ (method.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0)
                          return;
                        String mopName = method.getMopName();
                        int index = Arrays.binarySearch(mopMethods, mopName, CachedClass.CachedMethodComparatorWithString.INSTANCE);
                        if (index >= 0) {
                            int from = index;
                            while (from > 0 && mopMethods[from-1].getName().equals(mopName))
                              from--;
                            int to = index;
                            while (to < mopMethods.length-1 && mopMethods[to+1].getName().equals(mopName))
                              to++;

                            int matchingMethod = findMatchingMethod(mopMethods, from, to, method);
                            if (matchingMethod != -1) {
                                e.methodsForSuper = mopMethods[matchingMethod];
                            }
                        }
                    }
                }
            }

            private void processFastArray(FastArray methods) {
                final int len = methods.size();
                final Object[] data = methods.getArray();
                for (int i = 0; i != len; ++i) {
                    MetaMethod method = (MetaMethod) data[i];
                    if (method instanceof NewMetaMethod)
                      continue;
                    if (useThis ^ (method.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) continue;
                    String mopName = method.getMopName();
                    int index = Arrays.binarySearch(mopMethods, mopName, CachedClass.CachedMethodComparatorWithString.INSTANCE);
                    if (index >= 0) {
                        int from = index;
                        while (from > 0 && mopMethods[from-1].getName().equals(mopName))
                          from--;
                        int to = index;
                        while (to < mopMethods.length-1 && mopMethods[to+1].getName().equals(mopName))
                          to++;

                        int matchingMethod = findMatchingMethod(mopMethods, from, to, method);
                        if (matchingMethod != -1) {
                            methods.set(i, mopMethods[matchingMethod]);
                        }
                    }
                }
            }
        }
        MOPIter iter = new MOPIter();

        // replace all calls for super with the correct MOP method
        iter.useThis = false;
        iter.iterate();
        // replace all calls for this with the correct MOP method
        iter.useThis = true;
        iter.iterate();
    }

    private void inheritInterfaceNewMetaMethods(Set interfaces) {
        // add methods declared by DGM for interfaces
        for (Iterator it = interfaces.iterator(); it.hasNext(); ) {
            CachedClass cls = (CachedClass) it.next();
            MetaMethod methods [] = getNewMetaMethods(cls);
            for (int i = 0; i < methods.length; i++) {
                MetaMethod method = methods[i];
                if (!newGroovyMethodsSet.contains(method)) {
                    newGroovyMethodsSet.add(method);
                }
                addMetaMethodToIndex (method, mainClassMethodHeader);
            }
        }
    }

    private void connectMultimethods(List superClasses, CachedClass firstGroovyClass) {
        superClasses = DefaultGroovyMethods.reverse(superClasses);
        MetaMethodIndex.Header last = null;
        for (Iterator iter = superClasses.iterator(); iter.hasNext();) {
            CachedClass c = (CachedClass) iter.next();
            MetaMethodIndex.Header methodIndex = metaMethodIndex.getHeader(c.getTheClass());
            // We don't copy DGM methods to superclasses' indexes
            // The reason we can do that is particular set of DGM methods in use,
            // if at some point we will define DGM method for some Groovy class or
            // for a class derived from such, we will need to revise this condition.
            // It saves us a lot of space and some noticable time
            if (last != null) metaMethodIndex.copyNonPrivateNonNewMetaMethods(last, methodIndex);
            last = methodIndex;

            if (c == firstGroovyClass)
              break;
        }
    }

    private CachedClass calcFirstGroovySuperClass(Collection superClasses) {
        if (theCachedClass.isInterface)
          return ReflectionCache.OBJECT_CLASS;

        CachedClass firstGroovy = null;
        Iterator iter = superClasses.iterator();
        for (; iter.hasNext();) {
            CachedClass c = (CachedClass) iter.next();
            if (GroovyObject.class.isAssignableFrom(c.getTheClass())) {
              firstGroovy = c;
              break;
            }
        }

        if (firstGroovy == null)
          firstGroovy = theCachedClass;
        else {
            if (firstGroovy.getTheClass() == GroovyObjectSupport.class && iter.hasNext()) {
                firstGroovy = (CachedClass) iter.next();
                if (firstGroovy.getTheClass() == Closure.class && iter.hasNext()) {
                    firstGroovy = (CachedClass) iter.next();
                }
            }
        }

        return GroovyObject.class.isAssignableFrom(firstGroovy.getTheClass()) ? firstGroovy.getCachedSuperClass() : firstGroovy;
    }

    /**
     * @return all the normal instance methods avaiable on this class for the
     *         given name
     */
    private Object getMethods(Class sender, String name, boolean isCallToSuper) {
        Object answer;

        final MetaMethodIndex.Entry entry = metaMethodIndex.getMethods(sender, name);
        if (entry == null)
            answer = FastArray.EMPTY_LIST;
        else
            if (isCallToSuper) {
                answer = entry.methodsForSuper;
            } else {
                answer = entry.methods;
            }

        if (answer == null) answer = FastArray.EMPTY_LIST;

        if (!isCallToSuper) {
            List used = GroovyCategorySupport.getCategoryMethods(name);
            if (used != null) {
                FastArray arr;
                if (answer instanceof MetaMethod) {
                    arr = new FastArray();
                    arr.add(answer);
                }
                else
                  arr = ((FastArray) answer).copy();

                for (Iterator iter = used.iterator(); iter.hasNext();) {
                    MetaMethod element = (MetaMethod) iter.next();
                    if (!element.getDeclaringClass().getTheClass().isAssignableFrom(sender))
                      continue;
                    filterMatchingMethodForCategory(arr, element);
                }
                answer = arr;
            }
        }
        return answer;
    }

    /**
     * @return all the normal static methods avaiable on this class for the
     *         given name
     */
    private Object getStaticMethods(Class sender, String name) {
        final MetaMethodIndex.Entry entry = metaMethodIndex.getMethods(sender, name);
        if (entry == null)
            return FastArray.EMPTY_LIST;
        Object answer = entry.staticMethods;
        if (answer == null)
            return FastArray.EMPTY_LIST;
        return answer;
    }

    public boolean isModified() {
        return false;  // MetaClassImpl not designed for modification, just return false
    }

    public void addNewInstanceMethod(Method method) {
        final CachedMethod cachedMethod = CachedMethod.find(method);
        NewInstanceMetaMethod newMethod = new NewInstanceMetaMethod(cachedMethod);
        final CachedClass declaringClass = newMethod.getDeclaringClass();
        addNewInstanceMethodToIndex(newMethod, metaMethodIndex.getHeader(declaringClass.getTheClass()));
    }

    private void addNewInstanceMethodToIndex(MetaMethod newMethod, MetaMethodIndex.Header header) {
        if (!newGroovyMethodsSet.contains(newMethod)) {
            newGroovyMethodsSet.add(newMethod);
            addMetaMethodToIndex(newMethod, header);
        }
    }

    public void addNewStaticMethod(Method method) {
        final CachedMethod cachedMethod = CachedMethod.find(method);
        NewStaticMetaMethod newMethod = new NewStaticMetaMethod(cachedMethod);
        final CachedClass declaringClass = newMethod.getDeclaringClass();
        addNewStaticMethodToIndex(newMethod, metaMethodIndex.getHeader(declaringClass.getTheClass()));
    }

    private void addNewStaticMethodToIndex(MetaMethod newMethod, MetaMethodIndex.Header header) {
        if (!newGroovyMethodsSet.contains(newMethod)) {
            newGroovyMethodsSet.add(newMethod);
            addMetaMethodToIndex(newMethod, header);
        }
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
            return invokeMethod(object, methodName, (Object[]) arguments);
        } else {
            return invokeMethod(object, methodName, new Object[]{arguments});
        }
    }

    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        return invokeMissingMethod(instance, methodName, arguments, null, false);
    }

    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        Class theClass = instance instanceof Class ? (Class)instance : instance.getClass();
        CachedClass superClass = theCachedClass;
        while(superClass != null && superClass != ReflectionCache.OBJECT_CLASS) {
            final MetaBeanProperty property = findPropertyInClassHierarchy(propertyName, superClass);
            if(property != null) {
                onSuperPropertyFoundInHierarchy(property);
                if(!isGetter) {
                    property.setProperty(instance, optionalValue);
                    return null;
                }
                else {
                    return property.getProperty(instance);
                }
            }
            superClass = superClass.getCachedSuperClass();
        }
        // got here to property not found, look for getProperty or setProperty overrides
        if(isGetter) {
            final Class[] getPropertyArgs = {String.class};
            final MetaMethod method = findMethodInClassHeirarchy(instance.getClass(), GET_PROPERTY_METHOD, getPropertyArgs, this);
            if(method != null && method instanceof ClosureMetaMethod) {
                onGetPropertyFoundInHierarchy(method);
                return method.invoke(instance,new Object[]{propertyName});
            }
        }
        else {
            final Class[] setPropertyArgs = {String.class, Object.class};
            final MetaMethod method = findMethodInClassHeirarchy(instance.getClass(), SET_PROPERTY_METHOD, setPropertyArgs, this);
            if(method != null && method instanceof ClosureMetaMethod) {
                onSetPropertyFoundInHierarchy(method);
                return method.invoke(instance, new Object[]{propertyName, optionalValue});
            }
        }

        try {
            if (!(instance instanceof Class)) {
                if (isGetter && propertyMissingGet != null) {
                    return propertyMissingGet.invoke(instance, new Object[]{propertyName});
                } else {
                    if (propertyMissingSet != null)
                        return propertyMissingSet.invoke(instance, new Object[]{propertyName, optionalValue});
                }
            }
        } catch (InvokerInvocationException iie) {
            boolean shouldHandle = isGetter && propertyMissingGet != null;
            if (!shouldHandle) shouldHandle = !isGetter && propertyMissingSet != null;
            if (shouldHandle &&  iie.getCause() instanceof MissingPropertyException) {
                throw (MissingPropertyException) iie.getCause();
            }
            throw iie;
        }

        if (instance instanceof Class && theClass != Class.class) {
           final MetaProperty metaProperty = InvokerHelper.getMetaClass(Class.class).hasProperty(instance, propertyName);
           if (metaProperty != null)
             if (isGetter)
               return metaProperty.getProperty(instance);
             else {
               metaProperty.setProperty(instance, optionalValue);
               return null;
             }
        }
        throw new MissingPropertyExceptionNoStack(propertyName, theClass);
    }

    private Object invokeMissingMethod(Object instance, String methodName, Object[] arguments, RuntimeException original, boolean isCallToSuper) {
        if (!isCallToSuper) {
            Class instanceKlazz = instance.getClass();
            if (theClass != instanceKlazz && theClass.isAssignableFrom(instanceKlazz))
              instanceKlazz = theClass;
            
            Class[] argClasses = MetaClassHelper.castArgumentsToClassArray(arguments);

            MetaMethod method = findMixinMethod(methodName, argClasses);
            if(method != null) {
                onMixinMethodFound(method);
                return method.invoke(instance, arguments);
            }

            method = findMethodInClassHeirarchy(instanceKlazz, methodName, argClasses, this);
            if(method != null) {
                onSuperMethodFoundInHierarchy(method);
                return method.invoke(instance, arguments);
            }

            // still not method here, so see if there is an invokeMethod method up the heirarchy
            final Class[] invokeMethodArgs = {String.class, Object[].class};
            method = findMethodInClassHeirarchy(instanceKlazz, INVOKE_METHOD_METHOD, invokeMethodArgs, this );
            if(method != null && method instanceof ClosureMetaMethod) {
                onInvokeMethodFoundInHierarchy(method);
                return method.invoke(instance, invokeMethodArgs);
            }
        }

        if (methodMissing != null) {
            try {
                return methodMissing.invoke(instance, new Object[]{methodName, arguments});
            } catch (InvokerInvocationException iie) {
                if (methodMissing instanceof ClosureMetaMethod && iie.getCause() instanceof MissingMethodException) {
                    MissingMethodException mme =  (MissingMethodException) iie.getCause();
                    throw new MissingMethodExecutionFailed (mme.getMethod(), mme.getClass(),
                                                            mme.getArguments(),mme.isStatic(),mme);
                }
                throw iie;
            }
        } else if (original != null) throw original;
        else throw new MissingMethodExceptionNoStack(methodName, theClass, arguments, false);
    }

    protected void onSuperPropertyFoundInHierarchy(MetaBeanProperty property) {
    }

    protected void onMixinMethodFound(MetaMethod method) {
    }

    protected void onSuperMethodFoundInHierarchy(MetaMethod method) {
    }

    protected void onInvokeMethodFoundInHierarchy(MetaMethod method) {
    }

    protected void onSetPropertyFoundInHierarchy(MetaMethod method) {
    }

    protected void onGetPropertyFoundInHierarchy(MetaMethod method) {
    }


    /**
     * Hook to deal with the case of MissingProperty for static properties. The method will look attempt to look up
     * "propertyMissing" handlers and invoke them otherwise thrown a MissingPropertyException
     *
     * @param instance      The instance
     * @param propertyName  The name of the property
     * @param optionalValue The value in the case of a setter
     * @param isGetter      True if its a getter
     * @return The value in the case of a getter or a MissingPropertyException
     */
    protected Object invokeStaticMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        MetaClass mc = instance instanceof Class ? registry.getMetaClass((Class) instance) : this;
        if (isGetter) {
            MetaMethod propertyMissing = mc.getMetaMethod(STATIC_PROPERTY_MISSING, GETTER_MISSING_ARGS);
            if (propertyMissing != null) {
                return propertyMissing.invoke(instance, new Object[]{propertyName});
            }
        } else {
            MetaMethod propertyMissing = mc.getMetaMethod(STATIC_PROPERTY_MISSING, SETTER_MISSING_ARGS);
            if (propertyMissing != null) {
                return propertyMissing.invoke(instance, new Object[]{propertyName, optionalValue});
            }
        }

        if (instance instanceof Class) {
            throw new MissingPropertyException(propertyName, (Class) instance);
        }
        throw new MissingPropertyException(propertyName, theClass);
    }

    /**
     * Invokes the given method on the object.
     * TODO: should this be depreciated? If so, we have to propogate to many places.
     */
    public Object invokeMethod(Object object, String methodName, Object[] originalArguments) {
        return invokeMethod(theClass, object, methodName, originalArguments, false, false);
    }


    /**
     * Invokes the given method on the object.
     */
    public Object invokeMethod(Class sender, Object object, String methodName, Object[] originalArguments, boolean isCallToSuper, boolean fromInsideClass) {
        checkInitalised();
        if (object == null) {
            throw new NullPointerException("Cannot invoke method: " + methodName + " on null object");
        }

        final Object[] arguments = originalArguments == null ? EMPTY_ARGUMENTS : originalArguments;
//        final Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);
//
//        unwrap(arguments);

        MetaMethod method = getMethodWithCaching(sender, methodName, arguments, isCallToSuper);
        MetaClassHelper.unwrap(arguments);

        if (method == null)
            method = tryListParamMetaMethod(sender, methodName, isCallToSuper, arguments);

        final boolean isClosure = object instanceof Closure;
        if (isClosure) {
            final Closure closure = (Closure) object;

            final Object owner = closure.getOwner();

            if (CLOSURE_CALL_METHOD.equals(methodName) || CLOSURE_DO_CALL_METHOD.equals(methodName)) {
                final Class objectClass = object.getClass();
                if (objectClass == MethodClosure.class) {
                    final MethodClosure mc = (MethodClosure) object;
                    methodName = mc.getMethod();
                    final Class ownerClass = owner instanceof Class ? (Class) owner : owner.getClass();
                    final MetaClass ownerMetaClass = registry.getMetaClass(ownerClass);
                    return ownerMetaClass.invokeMethod(ownerClass, owner, methodName, arguments, false, false);
                } else if (objectClass == CurriedClosure.class) {
                    final CurriedClosure cc = (CurriedClosure) object;
                    // change the arguments for an uncurried call
                    final Object[] curriedArguments = cc.getUncurriedArguments(arguments);
                    final Class ownerClass = owner instanceof Class ? (Class) owner : owner.getClass();
                    final MetaClass ownerMetaClass = registry.getMetaClass(ownerClass);
                    return ownerMetaClass.invokeMethod(owner, methodName, curriedArguments);
                }
                if (method==null) invokeMissingMethod(object,methodName,arguments);
            } else if (CLOSURE_CURRY_METHOD.equals(methodName)) {
                return closure.curry(arguments);
            }

            final Object delegate = closure.getDelegate();
            final boolean isClosureNotOwner = owner != closure;
            final int resolveStrategy = closure.getResolveStrategy();

            final Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);

            switch (resolveStrategy) {
                case Closure.TO_SELF:
                    method = closure.getMetaClass().pickMethod(methodName, argClasses);
                    if (method != null) return method.invoke(closure, arguments);
                    break;
                case Closure.DELEGATE_ONLY:
                    if (method == null && delegate != closure && delegate != null) {
                        MetaClass delegateMetaClass = lookupObjectMetaClass(delegate);
                        method = delegateMetaClass.pickMethod(methodName, argClasses);
                        if (method != null)
                            return delegateMetaClass.invokeMethod(delegate, methodName, originalArguments);
                        else if (delegate != closure && (delegate instanceof GroovyObject)) {
                            return invokeMethodOnGroovyObject(methodName, originalArguments, delegate);
                        }
                    }
                    break;
                case Closure.OWNER_ONLY:
                    if (method == null && owner != closure) {
                        MetaClass ownerMetaClass = lookupObjectMetaClass(owner);
                        return ownerMetaClass.invokeMethod(owner, methodName, originalArguments);
                    }
                    break;
                case Closure.DELEGATE_FIRST:
                    if (method == null && delegate != closure && delegate != null) {
                        MetaClass delegateMetaClass = lookupObjectMetaClass(delegate);
                        method = delegateMetaClass.pickMethod(methodName, argClasses);
                        if (method != null)
                            return delegateMetaClass.invokeMethod(delegate, methodName, originalArguments);
                    }
                    if (method == null && owner != closure) {
                        MetaClass ownerMetaClass = lookupObjectMetaClass(owner);
                        method = ownerMetaClass.pickMethod(methodName, argClasses);
                        if (method != null) return ownerMetaClass.invokeMethod(owner, methodName, originalArguments);
                    }
                    if (method == null && resolveStrategy != Closure.TO_SELF) {
                        // still no methods found, test if delegate or owner are GroovyObjects
                        // and invoke the method on them if so.
                        MissingMethodException last = null;
                        if (delegate != closure && (delegate instanceof GroovyObject)) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, delegate);
                            } catch (MissingMethodException mme) {
                                if (last == null) last = mme;
                            }
                        }
                        if (isClosureNotOwner && (owner instanceof GroovyObject)) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, owner);
                            } catch (MissingMethodException mme) {
                                last = mme;
                            }
                        }
                        if (last != null) return invokeMissingMethod(object, methodName, originalArguments, last, isCallToSuper);
                    }

                    break;
                default:
                    if (method == null && owner != closure) {
                        MetaClass ownerMetaClass = lookupObjectMetaClass(owner);
                        method = ownerMetaClass.pickMethod(methodName, argClasses);
                        if (method != null) return ownerMetaClass.invokeMethod(owner, methodName, originalArguments);
                    }
                    if (method == null && delegate != closure && delegate != null) {
                        MetaClass delegateMetaClass = lookupObjectMetaClass(delegate);
                        method = delegateMetaClass.pickMethod(methodName, argClasses);
                        if (method != null)
                            return delegateMetaClass.invokeMethod(delegate, methodName, originalArguments);
                    }
                    if (method == null && resolveStrategy != Closure.TO_SELF) {
                        // still no methods found, test if delegate or owner are GroovyObjects
                        // and invoke the method on them if so.
                        MissingMethodException last = null;
                        if (isClosureNotOwner && (owner instanceof GroovyObject)) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, owner);
                            } catch (MissingMethodException mme) {
                                if (last == null) last = mme;
                            }
                            catch (InvokerInvocationException iie) {
                                if (iie.getCause() instanceof MissingMethodException) {
                                    MissingMethodException mme = (MissingMethodException) iie.getCause();
                                    if (last == null) last = mme;
                                }
                                else
                                  throw iie;
                            }
                        }
                        if (delegate != closure && (delegate instanceof GroovyObject)) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, delegate);
                            } catch (MissingMethodException mme) {
                                last = mme;
                            }
                            catch (InvokerInvocationException iie) {
                                if (iie.getCause() instanceof MissingMethodException) {
                                    last = (MissingMethodException) iie.getCause();
                                }
                                else
                                  throw iie;
                            }
                        }
                        if (last != null) return invokeMissingMethod(object, methodName, originalArguments, last, isCallToSuper);
                    }
            }
        }

        if (method != null) {
            return method.doMethodInvoke(object, arguments);
        } else {
            return invokePropertyOrMissing(object, methodName, originalArguments, fromInsideClass, isCallToSuper);
        }
    }

    private MetaMethod tryListParamMetaMethod(Class sender, String methodName, boolean isCallToSuper, Object[] arguments) {
        MetaMethod method = null;
        if (arguments.length == 1 && arguments[0] instanceof List) {
            Object[] newArguments = ((List) arguments[0]).toArray();
            method = getMethodWithCaching(sender, methodName, newArguments, isCallToSuper);
            if (method != null) {
                method = new TransformMetaMethod(method) {
                    public Object invoke(Object object, Object[] arguments) {
                        Object firstArgument = arguments[0];
                        List list = (List) firstArgument;
                        arguments = list.toArray();
                        return super.invoke(object, arguments);
                    }
                };
            }
        }
        return method;
    }

    private Object invokePropertyOrMissing(Object object, String methodName, Object[] originalArguments, boolean fromInsideClass, boolean isCallToSuper) {
        // if no method was found, try to find a closure defined as a field of the class and run it
        Object value = null;
        final MetaProperty metaProperty = this.getMetaProperty(methodName, false);
        if (metaProperty != null)
          value = metaProperty.getProperty(object);
        else {
            if (object instanceof Map)
              value = ((Map)object).get(methodName);
        }

        if (value instanceof Closure) {  // This test ensures that value != this If you ever change this ensure that value != this
            Closure closure = (Closure) value;
            MetaClass delegateMetaClass = closure.getMetaClass();
            return delegateMetaClass.invokeMethod(closure.getClass(), closure, CLOSURE_DO_CALL_METHOD, originalArguments, false, fromInsideClass);
        }

        return invokeMissingMethod(object, methodName, originalArguments, null, isCallToSuper);
    }

    private MetaClass lookupObjectMetaClass(Object object) {
        if (object instanceof GroovyObject) {
            GroovyObject go = (GroovyObject) object;
            return go.getMetaClass();
        }
        Class ownerClass = object.getClass();
        if (ownerClass == Class.class) ownerClass = (Class) object;
        MetaClass metaClass = registry.getMetaClass(ownerClass);
        return metaClass;
    }

    private Object invokeMethodOnGroovyObject(String methodName, Object[] originalArguments, Object owner) {
        GroovyObject go = (GroovyObject) owner;
        return go.invokeMethod(methodName, originalArguments);
    }

    public MetaMethod getMethodWithCaching(Class sender, String methodName, Object[] arguments, boolean isCallToSuper) {
        // lets try use the cache to find the method
        if (!isCallToSuper && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            return getMethodWithoutCaching(sender, methodName, MetaClassHelper.convertToTypeArray(arguments), isCallToSuper);
        } else {
            final MetaMethodIndex.Entry e = metaMethodIndex.getMethods(sender, methodName);
            if (e == null)
              return null;

            return isCallToSuper ? getSuperMethodWithCaching(arguments, e) : getNormalMethodWithCaching(arguments, e);
        }
    }

    private static boolean sameClasses(Class[] params, Class[] arguments, boolean weakNullCheck) {
        // we do here a null check because the params field might not have been
  	 	// set yet
  	 	if (params==null) return false;
        
        if (params.length != arguments.length)
          return false;

        for (int i = params.length-1; i >= 0; i--) {
            Object arg = arguments[i];
            if (arg != null) {
               if (params[i] != arguments[i])
                  return false;
            }
            else
              if (!weakNullCheck)
                return false;
        }

        return true;
    }

    // This method should be called by CallSite only
    private MetaMethod getMethodWithCachingInternal (Class sender, CallSite site, Class [] params) {
        if (site.getUsage ().get() != 0 && GroovyCategorySupport.hasCategoryInCurrentThread())
            return getMethodWithoutCaching(sender, site.getName (), params, false);

        final MetaMethodIndex.Entry e = metaMethodIndex.getMethods(sender, site.getName());
        if (e == null) {
            return null;
        }

        MetaMethodIndex.CacheEntry cacheEntry;
        final Object methods = e.methods;
        if (methods == null)
          return null;

        cacheEntry = e.cachedMethod;
        if (cacheEntry != null
           && (sameClasses(cacheEntry.params, params, methods instanceof MetaMethod))) {
             return cacheEntry.method;
        }

        cacheEntry = new MetaMethodIndex.CacheEntry ();
        cacheEntry.params = params;
        cacheEntry.method = (MetaMethod) chooseMethod(e.name, methods, params);
        e.cachedMethod = cacheEntry;
        return cacheEntry.method;
    }

    private MetaMethod getSuperMethodWithCaching(Object[] arguments, MetaMethodIndex.Entry e) {
        MetaMethodIndex.CacheEntry cacheEntry;
        if (e.methodsForSuper == null)
          return null;

        cacheEntry = e.cachedMethodForSuper;
        
        if (cacheEntry != null &&
            MetaClassHelper.sameClasses(cacheEntry.params, arguments, e.methodsForSuper instanceof MetaMethod))
        {
            MetaMethod method = cacheEntry.method;
            if (method!=null) return method;
        }

        cacheEntry = new MetaMethodIndex.CacheEntry ();
        final Class[] classes = MetaClassHelper.convertToTypeArray(arguments);
        cacheEntry.params = classes;
        cacheEntry.method = (MetaMethod) chooseMethod(e.name, e.methodsForSuper, classes);
        if (cacheEntry.method.isAbstract()) cacheEntry.method = null;

        e.cachedMethodForSuper = cacheEntry;

        return cacheEntry.method;
    }

    private MetaMethod getNormalMethodWithCaching(Object[] arguments, MetaMethodIndex.Entry e) {
        MetaMethodIndex.CacheEntry cacheEntry;
        final Object methods = e.methods;
        if (methods == null)
          return null;

        cacheEntry = e.cachedMethod;

        if (cacheEntry != null &&
            MetaClassHelper.sameClasses(cacheEntry.params, arguments, methods instanceof MetaMethod))
        {
            MetaMethod method = cacheEntry.method;
            if (method!=null) return method;
        }

        cacheEntry = new MetaMethodIndex.CacheEntry ();
        final Class[] classes = MetaClassHelper.convertToTypeArray(arguments);
        cacheEntry.params = classes;
        cacheEntry.method = (MetaMethod) chooseMethod(e.name, methods, classes);
        
        e.cachedMethod = cacheEntry;
        
        return cacheEntry.method;
    }

    public Constructor retrieveConstructor(Class[] arguments) {
        CachedConstructor constructor = (CachedConstructor) chooseMethod("<init>", constructors, arguments);
        if (constructor != null) {
            return constructor.cachedConstructor;
        }
        constructor = (CachedConstructor) chooseMethod("<init>", constructors, arguments);
        if (constructor != null) {
            return constructor.cachedConstructor;
        }
        return null;
    }

    public MetaMethod retrieveStaticMethod(String methodName, Object[] arguments) {
        final MetaMethodIndex.Entry e = metaMethodIndex.getMethods(theClass, methodName);
        MetaMethodIndex.CacheEntry cacheEntry;
        if (e != null) {
            cacheEntry = e.cachedStaticMethod;

            if (cacheEntry != null &&
                MetaClassHelper.sameClasses(cacheEntry.params, arguments, e.staticMethods instanceof MetaMethod))
            {
                 return cacheEntry.method;
            }

            cacheEntry = new MetaMethodIndex.CacheEntry ();
            final Class[] classes = MetaClassHelper.convertToTypeArray(arguments);
            cacheEntry.params = classes;
            cacheEntry.method = pickStaticMethod(methodName, classes);
            
            e.cachedStaticMethod = cacheEntry;
            
            return cacheEntry.method;
        }
        else
          return pickStaticMethod(methodName, MetaClassHelper.convertToTypeArray(arguments));
    }

    public MetaMethod getMethodWithoutCaching(Class sender, String methodName, Class[] arguments, boolean isCallToSuper) {
        MetaMethod method = null;
        Object methods = getMethods(sender, methodName, isCallToSuper);
        if (methods != null) {
            method = (MetaMethod) chooseMethod(methodName, methods, arguments);
        }
        return method;
    }

    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        checkInitalised();

        final Class sender = object instanceof Class ? (Class) object : object.getClass();
        if (sender != theClass) {
            MetaClass mc = registry.getMetaClass(sender);
            return mc.invokeStaticMethod(sender, methodName, arguments);
        }
        if (sender == Class.class) {
            return invokeMethod(object, methodName, arguments);
        }

        if (arguments == null) arguments = EMPTY_ARGUMENTS;
//        Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);

        MetaMethod method = retrieveStaticMethod(methodName, arguments);
        // lets try use the cache to find the method

        if (method != null) {
            MetaClassHelper.unwrap(arguments);
            return method.doMethodInvoke(object, arguments);
        }
        Object prop = null;
        try {
            prop = getProperty(theClass, theClass, methodName, false, false);
        } catch (MissingPropertyException mpe) {
            // ignore
        }

        if (prop instanceof Closure) {
            return invokeStaticClosureProperty(arguments, prop);
        }

        Object[] originalArguments = (Object[]) arguments.clone();
        MetaClassHelper.unwrap(arguments);

        Class superClass = sender.getSuperclass();
        Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);
        while (superClass != Object.class && superClass != null) {
            MetaClass mc = registry.getMetaClass(superClass);
            method = mc.getStaticMetaMethod(methodName, argClasses);
            if (method != null) return method.doMethodInvoke(object, arguments);

            try {
                prop = mc.getProperty(superClass, superClass, methodName, false, false);
            } catch (MissingPropertyException mpe) {
                // ignore
            }

            if (prop instanceof Closure) {
                return invokeStaticClosureProperty(originalArguments, prop);
            }

            superClass = superClass.getSuperclass();
        }

        return invokeStaticMissingMethod(sender, methodName, arguments);
    }

    private Object invokeStaticClosureProperty(Object[] originalArguments, Object prop) {
        Closure closure = (Closure) prop;
        MetaClass delegateMetaClass = closure.getMetaClass();
        return delegateMetaClass.invokeMethod(closure.getClass(), closure, CLOSURE_DO_CALL_METHOD, originalArguments, false, false);
    }

    private Object invokeStaticMissingMethod(Class sender, String methodName, Object[] arguments) {
        MetaMethod metaMethod = getStaticMetaMethod(STATIC_METHOD_MISSING, METHOD_MISSING_ARGS);
        if (metaMethod != null) {
            return metaMethod.invoke(sender, new Object[]{methodName, arguments});
        }
        throw new MissingMethodException(methodName, sender, arguments, true);
    }

    private MetaMethod pickStaticMethod(String methodName, Class[] arguments) {
        MetaMethod method = null;
        Object methods = getStaticMethods(theClass, methodName);

        if (!(methods instanceof FastArray) || !((FastArray)methods).isEmpty()) {
            method = (MetaMethod) chooseMethod(methodName, methods, arguments);
        }
        if (method == null && theClass != Class.class) {
            MetaClass classMetaClass = registry.getMetaClass(Class.class);
            method = classMetaClass.pickMethod(methodName, arguments);
        }
        if (method == null) {
            method = (MetaMethod) chooseMethod(methodName, methods, MetaClassHelper.convertToTypeArray(arguments));
        }
        return method;
    }

    /**
     * Warning, this method will be removed
     *
     * @deprecated use invokeConstructor instead
     */
    public Object invokeConstructorAt(Class at, Object[] arguments) {
        return invokeConstructor(arguments);
    }

    public Object invokeConstructor(Object[] arguments) {
        return invokeConstructor(theClass, arguments);
    }

    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        //TODO: that is just a quick prototype, not the real thing!
        if (numberOfConstructors != constructors.size()) {
            throw new IncompatibleClassChangeError("the number of constructors during runtime and compile time for " +
                    this.theClass.getName() + " do not match. Expected " + numberOfConstructors + " but got " + constructors.size());
        }

        if (arguments == null) arguments = EMPTY_ARGUMENTS;
        Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);
        MetaClassHelper.unwrap(arguments);
        CachedConstructor constructor = (CachedConstructor) chooseMethod("<init>", constructors, argClasses);
        if (constructor == null) {
            constructor = (CachedConstructor) chooseMethod("<init>", constructors, argClasses);
        }
        if (constructor == null) {
            throw new GroovyRuntimeException(
                    "Could not find matching constructor for: "
                            + theClass.getName()
                            + "(" + InvokerHelper.toTypeString(arguments) + ")");
        }
        List l = new ArrayList(constructors.toList());
        Comparator comp = new Comparator() {
            public int compare(Object arg0, Object arg1) {
                CachedConstructor c0 = (CachedConstructor) arg0;
                CachedConstructor c1 = (CachedConstructor) arg1;
                String descriptor0 = BytecodeHelper.getMethodDescriptor(Void.TYPE, c0.getNativeParameterTypes());
                String descriptor1 = BytecodeHelper.getMethodDescriptor(Void.TYPE, c1.getNativeParameterTypes());
                return descriptor0.compareTo(descriptor1);
            }
        };
        Collections.sort(l, comp);
        int found = -1;
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) != constructor) continue;
            found = i;
            break;
        }
        // NOTE: must be changed to "1 |" if constructor was vargs
        return 0 | (found << 8);
    }

    /**
     * checks if the initialisation of the class id complete.
     * This method should be called as a form of assert, it is no
     * way to test if there is still initialisation work to be done.
     * Such logic must be implemented in a different way.
     *
     * @throws IllegalStateException if the initialisation is incomplete yet
     */
    protected void checkInitalised() {
        if (!isInitialized())
            throw new IllegalStateException(
                    "initialize must be called for meta " +
                            "class of " + theClass +
                            "(" + this.getClass() + ") " +
                            "to complete initialisation process " +
                            "before any invocation or field/property " +
                            "access can be done");
    }

    private Object invokeConstructor(Class at, Object[] arguments) {
        checkInitalised();
        if (arguments == null) arguments = EMPTY_ARGUMENTS;
        Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);
        MetaClassHelper.unwrap(arguments);
        CachedConstructor constructor = (CachedConstructor) chooseMethod("<init>", constructors, argClasses);
        if (constructor != null) {
            return constructor.doConstructorInvoke(arguments);
        }

        if (arguments.length == 1) {
            Object firstArgument = arguments[0];
            if (firstArgument instanceof Map) {
                constructor = (CachedConstructor) chooseMethod("<init>", constructors, MetaClassHelper.EMPTY_TYPE_ARRAY);
                if (constructor != null) {
                    Object bean = constructor.doConstructorInvoke(MetaClassHelper.EMPTY_ARRAY);
                    setProperties(bean, ((Map) firstArgument));
                    return bean;
                }
            }
        }
        throw new GroovyRuntimeException(
                "Could not find matching constructor for: "
                        + theClass.getName()
                        + "(" + InvokerHelper.toTypeString(arguments) + ")");
    }

    /**
     * Sets a number of bean properties from the given Map where the keys are
     * the String names of properties and the values are the values of the
     * properties to set
     */
    public void setProperties(Object bean, Map map) {
        checkInitalised();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString();

            Object value = entry.getValue();
            setProperty(bean, key, value);
        }
    }

    /**
     * @return the given property's value on the object
     */
    public Object getProperty(Class sender, Object object, String name, boolean useSuper, boolean fromInsideClass) {

        //----------------------------------------------------------------------
        // handling of static
        //----------------------------------------------------------------------
        boolean isStatic = theClass != Class.class && object instanceof Class;
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class) object);
            return mc.getProperty(sender, object, name, useSuper, false);
        }

        checkInitalised();

        //----------------------------------------------------------------------
        // turn getProperty on a Map to get on the Map itself
        //----------------------------------------------------------------------
        if (!isStatic && this.isMap) {
            return ((Map) object).get(name);
        }

        MetaMethod method = null;
        Object[] arguments = EMPTY_ARGUMENTS;

        //----------------------------------------------------------------------
        // getter
        //----------------------------------------------------------------------
        MetaProperty mp = getMetaProperty(sender, name, useSuper, isStatic);
        if (mp != null) {
            if (mp instanceof MetaBeanProperty) {
                MetaBeanProperty mbp = (MetaBeanProperty) mp;
                method = mbp.getGetter();
                mp = mbp.getField();
            }
        }

        // check for a category method named like a getter
        if (!useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            String getterName = "get" + MetaClassHelper.capitalize(name);
            MetaMethod categoryMethod = getCategoryMethodGetter(sender, getterName, false);
            if (categoryMethod != null) method = categoryMethod;
        }

        //----------------------------------------------------------------------
        // field
        //----------------------------------------------------------------------
        if (method == null && mp != null) {
            try {
                return mp.getProperty(object);
            } catch (IllegalArgumentException e) {
                // can't access the field directly but there may be a getter
                mp = null;
            }
        }

        //----------------------------------------------------------------------
        // generic get method
        //----------------------------------------------------------------------
        // check for a generic get method provided through a category
        if (method == null && !useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            method = getCategoryMethodGetter(sender, "get", true);
            if (method != null) arguments = new Object[]{name};
        }

        // the generic method is valid, if available (!=null), if static or
        // if it is not static and we do no static access
        if (method == null && genericGetMethod != null && !(!genericGetMethod.isStatic() && isStatic)) {
            arguments = new Object[]{name};
            method = genericGetMethod;
        }

        //----------------------------------------------------------------------
        // special cases
        //----------------------------------------------------------------------
        if (method == null) {
            /** todo these special cases should be special MetaClasses maybe */
            if (theClass != Class.class && object instanceof Class) {
                MetaClass mc = registry.getMetaClass(Class.class);
                return mc.getProperty(Class.class, object, name, useSuper, false);
            } else if (object instanceof Collection) {
                return DefaultGroovyMethods.getAt((Collection) object, name);
            } else if (object instanceof Object[]) {
                return DefaultGroovyMethods.getAt(Arrays.asList((Object[]) object), name);
            } else {
                MetaMethod addListenerMethod = (MetaMethod) listeners.get(name);
                if (addListenerMethod != null) {
                    //TODO: one day we could try return the previously registered Closure listener for easy removal
                    return null;
                }
            }
        } else {

            //----------------------------------------------------------------------
            // executing the getter method
            //----------------------------------------------------------------------
            return method.doMethodInvoke(object, arguments);
        }

        //----------------------------------------------------------------------
        // error due to missing method/field
        //----------------------------------------------------------------------
        if (isStatic || object instanceof Class)
            return invokeStaticMissingProperty(object, name, null, true);
        else
            return invokeMissingProperty(object, name, null, true);
    }

    public MetaProperty getEffectiveGetMetaProperty(final Class sender, final Object object, String name, final boolean useSuper) {

        //----------------------------------------------------------------------
        // handling of static
        //----------------------------------------------------------------------
        boolean isStatic = theClass != Class.class && object instanceof Class;
        if (isStatic && object != theClass) {
            return new MetaProperty(name, Object.class) {
                final MetaClass mc = registry.getMetaClass((Class) object);

                public Object getProperty(Object object) {
                    return mc.getProperty(sender, object, name, useSuper,false);
                }

                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        checkInitalised();

        //----------------------------------------------------------------------
        // turn getProperty on a Map to get on the Map itself
        //----------------------------------------------------------------------
        if (!isStatic && this.isMap) {
            return new MetaProperty(name, Object.class) {
                public Object getProperty(Object object) {
                    return ((Map) object).get(name);
                }

                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        MetaMethod method = null;

        //----------------------------------------------------------------------
        // getter
        //----------------------------------------------------------------------
        MetaProperty mp = getMetaProperty(sender, name, useSuper, isStatic);
        if (mp != null) {
            if (mp instanceof MetaBeanProperty) {
                MetaBeanProperty mbp = (MetaBeanProperty) mp;
                method = mbp.getGetter();
                mp = mbp.getField();
            }
        }

        // check for a category method named like a getter
        if (!useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            String getterName = "get" + MetaClassHelper.capitalize(name);
            MetaMethod categoryMethod = getCategoryMethodGetter(sender, getterName, false);
            if (categoryMethod != null)
              method = categoryMethod;
        }

        //----------------------------------------------------------------------
        // field
        //----------------------------------------------------------------------
        if (method != null)
            return new GetBeanMethodMetaProperty(name, method);

        if (mp != null) {
            return mp;
//            try {
//                return mp.getProperty(object);
//            } catch (IllegalArgumentException e) {
//                // can't access the field directly but there may be a getter
//                mp = null;
//            }
        }

        //----------------------------------------------------------------------
        // generic get method
        //----------------------------------------------------------------------
        // check for a generic get method provided through a category
        if (!useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            method = getCategoryMethodGetter(sender, "get", true);
            if (method != null)
                return new GetMethodMetaProperty(name, method);
        }

        // the generic method is valid, if available (!=null), if static or
        // if it is not static and we do no static access
        if (genericGetMethod != null && !(!genericGetMethod.isStatic() && isStatic)) {
            method = genericGetMethod;
            if (method != null)
                return new GetMethodMetaProperty(name, method);
        }

        //----------------------------------------------------------------------
        // special cases
        //----------------------------------------------------------------------
        /** todo these special cases should be special MetaClasses maybe */
        if (theClass != Class.class && object instanceof Class) {
            return new MetaProperty(name, Object.class) {
                public Object getProperty(Object object) {
                    MetaClass mc = registry.getMetaClass(Class.class);
                    return mc.getProperty(Class.class, object, name, useSuper, false);
                }

                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        } else if (object instanceof Collection) {
            return new MetaProperty(name, Object.class) {
                public Object getProperty(Object object) {
                    return DefaultGroovyMethods.getAt((Collection) object, name);
                }

                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        } else if (object instanceof Object[]) {
            return new MetaProperty(name, Object.class) {
                public Object getProperty(Object object) {
                    return DefaultGroovyMethods.getAt(Arrays.asList((Object[]) object), name);
                }

                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            MetaMethod addListenerMethod = (MetaMethod) listeners.get(name);
            if (addListenerMethod != null) {
                //TODO: one day we could try return the previously registered Closure listener for easy removal
                return new MetaProperty(name, Object.class) {
                    public Object getProperty(Object object) {
                        return null;
                    }

                    public void setProperty(Object object, Object newValue) {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }

        //----------------------------------------------------------------------
        // error due to missing method/field
        //----------------------------------------------------------------------
        if (isStatic || object instanceof Class)
            return new MetaProperty(name, Object.class) {
                public Object getProperty(Object object) {
                    return invokeStaticMissingProperty(object, name, null, true);
                }

                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        else
            return new MetaProperty(name, Object.class) {
                public Object getProperty(Object object) {
                    return invokeMissingProperty(object, name, null, true);
                }

                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
    }



    private MetaMethod getCategoryMethodGetter(Class sender, String name, boolean useLongVersion) {
        List possibleGenericMethods = GroovyCategorySupport.getCategoryMethods(name);
        if (possibleGenericMethods != null) {
            for (Iterator iter = possibleGenericMethods.iterator(); iter.hasNext();) {
                MetaMethod mmethod = (MetaMethod) iter.next();
                if (!mmethod.getDeclaringClass().getTheClass().isAssignableFrom(sender))
                  continue;

                CachedClass[] paramTypes = mmethod.getParameterTypes();
                if (useLongVersion) {
                    if (paramTypes.length == 1 && paramTypes[0].getTheClass() == String.class) {
                        return mmethod;
                    }
                } else {
                    if (paramTypes.length == 0) return mmethod;
                }
            }
        }
        return null;
    }

    private MetaMethod getCategoryMethodSetter(Class sender, String name, boolean useLongVersion) {
        List possibleGenericMethods = GroovyCategorySupport.getCategoryMethods(name);
        if (possibleGenericMethods != null) {
            for (Iterator iter = possibleGenericMethods.iterator(); iter.hasNext();) {
                MetaMethod mmethod = (MetaMethod) iter.next();
                if (!mmethod.getDeclaringClass().getTheClass().isAssignableFrom(sender))
                  continue;

                CachedClass[] paramTypes = mmethod.getParameterTypes();
                if (useLongVersion) {
                    if (paramTypes.length == 2 && paramTypes[0].getTheClass() == String.class) {
                        return mmethod;
                    }
                } else {
                    if (paramTypes.length == 1) return mmethod;
                }
            }
        }
        return null;
    }

    /**
     * Get all the properties defined for this type
     *
     * @return a list of MetaProperty objects
     */
    public List getProperties() {
        checkInitalised();
        SingleKeyHashMap propertyMap = classPropertyIndex.getNullable(theCachedClass);
        // simply return the values of the metaproperty map as a List
        List ret = new ArrayList(propertyMap.size());
        for (ComplexKeyHashMap.EntryIterator iter = propertyMap.getEntrySetIterator(); iter.hasNext();) {
            MetaProperty element = (MetaProperty) ((SingleKeyHashMap.Entry) iter.next()).value;
            if (element instanceof CachedField) continue;
            // filter out DGM beans
            if (element instanceof MetaBeanProperty) {
                MetaBeanProperty mp = (MetaBeanProperty) element;
                boolean setter = true;
                boolean getter = true;
                if (mp.getGetter() == null || mp.getGetter() instanceof GeneratedMetaMethod || mp.getGetter() instanceof NewInstanceMetaMethod) {
                    getter = false;
                }
                if (mp.getSetter() == null || mp.getSetter() instanceof GeneratedMetaMethod || mp.getSetter() instanceof NewInstanceMetaMethod) {
                    setter = false;
                }
                if (!setter && !getter) continue;
//  TODO: I (ait) don't know why these strange tricks needed and comment following as it effects some Grails tests             
//                if (!setter && mp.getSetter() != null) {
//                    element = new MetaBeanProperty(mp.getName(), mp.getType(), mp.getGetter(), null);
//                }
//                if (!getter && mp.getGetter() != null) {
//                    element = new MetaBeanProperty(mp.getName(), mp.getType(), null, mp.getSetter());
//                }
            }
            ret.add(element);
        }
        return ret;
    }

    private MetaMethod findPropertyMethod(Object methodOrList, boolean isGetter) {
        if (methodOrList == null)
          return null;

        Object ret = null;
        if (methodOrList instanceof MetaMethod) {
            MetaMethod element = (MetaMethod)methodOrList;
            if (!isGetter &&
                    //(element.getReturnType() == Void.class || element.getReturnType() == Void.TYPE) &&
                    element.getParameterTypes().length == 1) {
                ret = addElementToList(ret, element);
            }
            if (isGetter &&
                    !(element.getReturnType() == Void.class || element.getReturnType() == Void.TYPE) &&
                    element.getParameterTypes().length == 0) {
                ret = addElementToList(ret, element);
            }

        }
        else {
            FastArray methods = (FastArray) methodOrList;
            final int len = methods.size();
            final Object[] data = methods.getArray();
            for (int i = 0; i != len; ++i) {
                MetaMethod element = (MetaMethod) data[i];
                if (!isGetter &&
                        //(element.getReturnType() == Void.class || element.getReturnType() == Void.TYPE) &&
                        element.getParameterTypes().length == 1) {
                    ret = addElementToList(ret, element);
                }
                if (isGetter &&
                        !(element.getReturnType() == Void.class || element.getReturnType() == Void.TYPE) &&
                        element.getParameterTypes().length == 0) {
                    ret = addElementToList(ret, element);
                }
            }
        }

        if (ret == null) return null;
        if (ret instanceof MetaMethod) return (MetaMethod) ret;

        // we found multiple matching methods
        // this is a problem, because we can use only one
        // if it is a getter, then use the most general return
        // type to decide which method to use. If it is a setter
        // we use the type of the first parameter
        MetaMethod method = null;
        int distance = -1;
        for (Iterator iter = ((List) ret).iterator(); iter.hasNext();) {
            MetaMethod element = (MetaMethod) iter.next();
            Class c;
            if (isGetter) {
                c = element.getReturnType();
            } else {
                c = element.getParameterTypes()[0].getTheClass();
            }
            int localDistance = distanceToObject(c);
            //TODO: maybe implement the case localDistance==distance
            if (distance == -1 || distance > localDistance) {
                distance = localDistance;
                method = element;
            }
        }
        return method;
    }

    private Object addElementToList(Object ret, MetaMethod element) {
        if (ret == null)
            ret = element;
        else if (ret instanceof List)
            ((List) ret).add(element);
        else {
            List list = new LinkedList();
            list.add(ret);
            list.add(element);
            ret = list;
        }
        return ret;
    }

    private static int distanceToObject(Class c) {
        int count;
        for (count = 0; c != null; count++) {
            c = c.getSuperclass();
        }
        return count;
    }


    /**
     * This will build up the property map (Map of MetaProperty objects, keyed on
     * property name).
     */
    private void setupProperties(PropertyDescriptor[] propertyDescriptors) {
        if (theCachedClass.isInterface) {
            LinkedList superClasses = new LinkedList();
            superClasses.add(ReflectionCache.OBJECT_CLASS);
            Set interfaces = theCachedClass.getInterfaces();

            classPropertyIndexForSuper = classPropertyIndex;
            final SingleKeyHashMap cPI = classPropertyIndex.getNotNull(theCachedClass);
            for (Iterator interfaceIter = interfaces.iterator(); interfaceIter.hasNext();) {
                CachedClass iclass = (CachedClass) interfaceIter.next();
                SingleKeyHashMap iPropertyIndex = cPI;
                addFields(iclass, iPropertyIndex);
                classPropertyIndex.put(iclass, iPropertyIndex);
            }
            classPropertyIndex.put(ReflectionCache.OBJECT_CLASS, cPI);

            applyPropertyDescriptors(propertyDescriptors);
            applyStrayPropertyMethods(superClasses, classPropertyIndex, true);

            makeStaticPropertyIndex();
        } else {
            LinkedList superClasses = getSuperClasses();
            Set interfaces = theCachedClass.getInterfaces();

            // if this an Array, then add the special read-only "length" property
            if (theCachedClass.isArray) {
                SingleKeyHashMap map = new SingleKeyHashMap();
                map.put("length", arrayLengthProperty);
                classPropertyIndex.put(theCachedClass, map);
            }

            inheritStaticInterfaceFields(superClasses, interfaces);
            inheritFields(superClasses);

            applyPropertyDescriptors(propertyDescriptors);

            applyStrayPropertyMethods(superClasses, classPropertyIndex, true);
            applyStrayPropertyMethods(superClasses, classPropertyIndexForSuper, false);

            copyClassPropertyIndexForSuper(classPropertyIndexForSuper);
            makeStaticPropertyIndex();
        }
    }

    private void makeStaticPropertyIndex() {
        SingleKeyHashMap propertyMap = classPropertyIndex.getNotNull(theCachedClass);
        for (ComplexKeyHashMap.EntryIterator iter = propertyMap.getEntrySetIterator(); iter.hasNext();) {
            SingleKeyHashMap.Entry entry = ((SingleKeyHashMap.Entry) iter.next());

            MetaProperty mp = (MetaProperty) entry.getValue();
            if (mp instanceof CachedField) {
                CachedField mfp = (CachedField) mp;
                if (!mfp.isStatic()) continue;
            } else if (mp instanceof MetaBeanProperty) {
                MetaProperty result = establishStaticMetaProperty(mp);
                if (result == null) continue;
                else {
                    mp = result;
                }
            } else {
                continue; // ignore all other types
            }
            staticPropertyIndex.put(entry.getKey(), mp);
        }

    }

    private MetaProperty establishStaticMetaProperty(MetaProperty mp) {
        MetaBeanProperty mbp = (MetaBeanProperty) mp;
        MetaProperty result = null;
        final MetaMethod getterMethod = mbp.getGetter();
        final MetaMethod setterMethod = mbp.getSetter();
        final CachedField metaField = mbp.getField();

        boolean getter = getterMethod == null || getterMethod.isStatic();
        boolean setter = setterMethod == null || setterMethod.isStatic();
        boolean field = metaField == null || metaField.isStatic();

        if (!getter && !setter && !field) {
            return result;
        } else {
            final String propertyName = mbp.getName();
            final Class propertyType = mbp.getType();

            if (setter && getter) {
                if (field) {
                    result = mbp; // nothing to do
                } else {
                    result = new MetaBeanProperty(propertyName, propertyType, getterMethod, setterMethod);
                }
            } else if (getter && !setter) {
                if (getterMethod == null) {
                    result = metaField;
                } else {
                    MetaBeanProperty newmp = new MetaBeanProperty(propertyName, propertyType, getterMethod, null);
                    if (field) newmp.setField(metaField);
                    result = newmp;
                }
            } else if (setter && !getter) {
                if (setterMethod == null) {
                    result = metaField;
                } else {
                    MetaBeanProperty newmp = new MetaBeanProperty(propertyName, propertyType, null, setterMethod);
                    if (field) newmp.setField(metaField);
                    result = newmp;
                }
            } else
                result = metaField;
        }
        return result;
    }

    private void copyClassPropertyIndexForSuper(Index dest) {
        for (ComplexKeyHashMap.EntryIterator iter = classPropertyIndex.getEntrySetIterator(); iter.hasNext();) {
            SingleKeyHashMap.Entry entry = (SingleKeyHashMap.Entry) iter.next();
            SingleKeyHashMap newVal = new SingleKeyHashMap();
            dest.put((CachedClass) entry.getKey(), newVal);
        }
    }

    private void inheritStaticInterfaceFields(LinkedList superClasses, Set interfaces) {
        for (Iterator interfaceIter = interfaces.iterator(); interfaceIter.hasNext();) {
            CachedClass iclass = (CachedClass) interfaceIter.next();
            SingleKeyHashMap iPropertyIndex = classPropertyIndex.getNotNull(iclass);
            addFields(iclass, iPropertyIndex);
            for (Iterator classIter = superClasses.iterator(); classIter.hasNext();) {
                CachedClass sclass = (CachedClass) classIter.next();
                if (!iclass.getTheClass().isAssignableFrom(sclass.getTheClass())) continue;
                SingleKeyHashMap sPropertyIndex = classPropertyIndex.getNotNull(sclass);
                copyNonPrivateFields(iPropertyIndex, sPropertyIndex);
            }
        }
    }

    private void inheritFields(LinkedList superClasses) {
        SingleKeyHashMap last = null;
        for (Iterator iter = superClasses.iterator(); iter.hasNext();) {
            CachedClass klass = (CachedClass) iter.next();
            SingleKeyHashMap propertyIndex = classPropertyIndex.getNotNull(klass);
            if (last != null) {
                copyNonPrivateFields(last, propertyIndex);
            }
            last = propertyIndex;
            addFields(klass, propertyIndex);
        }
    }

    private void addFields(final CachedClass klass, SingleKeyHashMap propertyIndex) {
        CachedField[] fields = klass.getFields();
        for (int i = 0; i < fields.length; i++) {
            propertyIndex.put(fields[i].getName(), fields[i]);
        }
    }

    private void copyNonPrivateFields(SingleKeyHashMap from, SingleKeyHashMap to) {
        for (ComplexKeyHashMap.EntryIterator iter = from.getEntrySetIterator(); iter.hasNext();) {
            SingleKeyHashMap.Entry entry = (SingleKeyHashMap.Entry) iter.next();
            CachedField mfp = (CachedField) entry.getValue();
            if (!Modifier.isPublic(mfp.getModifiers()) && !Modifier.isProtected(mfp.getModifiers())) continue;
            to.put(entry.getKey(), mfp);
        }
    }

    private void applyStrayPropertyMethods(LinkedList superClasses, Index classPropertyIndex, boolean isThis) {
        // now look for any stray getters that may be used to define a property
        for (Iterator iter = superClasses.iterator(); iter.hasNext();) {
            CachedClass klass = (CachedClass) iter.next();
            MetaMethodIndex.Header header = metaMethodIndex.getHeader(klass.getTheClass());
            SingleKeyHashMap propertyIndex = classPropertyIndex.getNotNull(klass);
            for (MetaMethodIndex.Entry e = header.head; e != null; e = e.nextClassEntry)
            {
                String methodName = e.name;
                // name too short?
                if (methodName.length() < 4) continue;
                // possible getter/setter?
                boolean isGetter = methodName.startsWith("get");
                boolean isSetter = methodName.startsWith("set");
                if (!isGetter && !isSetter) continue;

                MetaMethod propertyMethod = findPropertyMethod(isThis ? e.methods : e.methodsForSuper, isGetter);
                if (propertyMethod == null) continue;

                String propName = getPropName(methodName);
                createMetaBeanProperty(propertyIndex, propName, isGetter, propertyMethod);
            }
        }
    }

    private static final HashMap propNames = new HashMap(1024);

    private String getPropName(String methodName) {
        String name = (String) propNames.get(methodName);
        if (name != null)
            return name;

        synchronized (propNames) {
            String propName = java.beans.Introspector.decapitalize(methodName.substring(3));
            propNames.put(methodName, propName);
            return propName;
        }
    }

    private void createMetaBeanProperty(SingleKeyHashMap propertyIndex, String propName, boolean isGetter, MetaMethod propertyMethod) {
        // is this property already accounted for?
        MetaProperty mp = (MetaProperty) propertyIndex.get(propName);
        if (mp == null) {
            if (isGetter) {
                mp = new MetaBeanProperty(propName,
                        propertyMethod.getReturnType(),
                        propertyMethod, null);
            } else {
                //isSetter
                mp = new MetaBeanProperty(propName,
                        propertyMethod.getParameterTypes()[0].getTheClass(),
                        null, propertyMethod);
            }
        } else {
            MetaBeanProperty mbp;
            CachedField mfp;
            if (mp instanceof MetaBeanProperty) {
                mbp = (MetaBeanProperty) mp;
                mfp = mbp.getField();
            } else if (mp instanceof CachedField) {
                mfp = (CachedField) mp;
                mbp = new MetaBeanProperty(propName,
                        mfp.getType(),
                        null, null);
            } else {
                throw new GroovyBugError("unknown MetaProperty class used. Class is " + mp.getClass());
            }
            // we may have already found one for this name
            if (isGetter && mbp.getGetter() == null) {
                mbp.setGetter(propertyMethod);
            } else if (!isGetter && mbp.getSetter() == null) {
                mbp.setSetter(propertyMethod);
            }
            mbp.setField(mfp);
            mp = mbp;
        }
        propertyIndex.put(propName, mp);
    }

    private void applyPropertyDescriptors(PropertyDescriptor[] propertyDescriptors) {
        // now iterate over the map of property descriptors and generate
        // MetaBeanProperty objects
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor pd = propertyDescriptors[i];

            // skip if the property type is unknown (this seems to be the case if the
            // property descriptor is based on a setX() method that has two parameters,
            // which is not a valid property)
            if (pd.getPropertyType() == null)
                continue;

            // get the getter method
            Method method = pd.getReadMethod();
            MetaMethod getter;
            if (method != null)
                getter = findMethod(CachedMethod.find(method));
            else
                getter = null;

            // get the setter method
            MetaMethod setter;
            method = pd.getWriteMethod();
            if (method != null)
                setter = findMethod(CachedMethod.find(method));
            else
                setter = null;

            // now create the MetaProperty object
            MetaBeanProperty mp = new MetaBeanProperty(pd.getName(), pd.getPropertyType(), getter, setter);
            addMetaBeanProperty(mp);
        }
    }

    /**
     * Adds a new MetaBeanProperty to this MetaClass
     *
     * @param mp The MetaBeanProperty
     */
    public void addMetaBeanProperty(MetaBeanProperty mp) {

        MetaProperty staticProperty = establishStaticMetaProperty(mp);
        if (staticProperty != null) {
            staticPropertyIndex.put(mp.getName(), mp);
        } else {

            SingleKeyHashMap propertyMap = classPropertyIndex.getNotNull(theCachedClass);
            //keep field
            CachedField field;
            MetaProperty old = (MetaProperty) propertyMap.get(mp.getName());
            if (old != null) {
                if (old instanceof MetaBeanProperty) {
                    field = ((MetaBeanProperty) old).getField();
                } else {
                    field = (CachedField) old;
                }
                mp.setField(field);
            }

            // put it in the list
            // this will overwrite a possible field property
            propertyMap.put(mp.getName(), mp);
        }

    }

    /**
     * Sets the property value on an object
     */
    public void setProperty(Class sender, Object object, String name, Object newValue, boolean useSuper, boolean fromInsideClass) {
        checkInitalised();

        //----------------------------------------------------------------------
        // handling of static
        //----------------------------------------------------------------------
        boolean isStatic = theClass != Class.class && object instanceof Class;
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class) object);
            mc.getProperty(sender, object, name, useSuper, fromInsideClass);
            return;
        }

        //----------------------------------------------------------------------
        // Unwrap wrapped values fo now - the new MOP will handle them properly
        //----------------------------------------------------------------------
        if (newValue instanceof Wrapper) newValue = ((Wrapper) newValue).unwrap();

        //----------------------------------------------------------------------
        // turn setProperty on a Map to put on the Map itself
        //----------------------------------------------------------------------
        if (!isStatic && this.isMap) {
            ((Map) object).put(name, newValue);
            return;
        }


        MetaMethod method = null;
        Object[] arguments = null;

        //----------------------------------------------------------------------
        // setter
        //----------------------------------------------------------------------
        MetaProperty mp = getMetaProperty(sender, name, useSuper, isStatic);
        MetaProperty field = null;
        if (mp != null) {
            if (mp instanceof MetaBeanProperty) {
                MetaBeanProperty mbp = (MetaBeanProperty) mp;
                method = mbp.getSetter();
                MetaProperty f = mbp.getField();
                if (method != null || (f != null && !Modifier.isFinal(f.getModifiers()))) {
                    arguments = new Object[]{newValue};
                    field = f;
                }
            } else {
                field = mp;
            }
        }

        // check for a category method named like a setter
        if (!useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            String getterName = "set" + MetaClassHelper.capitalize(name);
            MetaMethod categoryMethod = getCategoryMethodSetter(sender, getterName, false);
            if (categoryMethod != null) {
                method = categoryMethod;
                arguments = new Object[]{newValue};
            }
        }

        //----------------------------------------------------------------------
        // listener method
        //----------------------------------------------------------------------
        boolean ambiguousListener = false;
        if (method == null) {
            method = (MetaMethod) listeners.get(name);
            ambiguousListener = method == AMBIGUOUS_LISTENER_METHOD;
            if (method != null &&
                    !ambiguousListener &&
                    newValue instanceof Closure) {
                // lets create a dynamic proxy
                Object proxy = Proxy.newProxyInstance(
                        theClass.getClassLoader(),
                        new Class[]{method.getParameterTypes()[0].getTheClass()},
                        new ConvertedClosure((Closure) newValue, name));
                arguments = new Object[]{proxy};
                newValue = proxy;
            } else {
                method = null;
            }
        }

        //----------------------------------------------------------------------
        // field
        //----------------------------------------------------------------------
        if (method == null && field != null) {
            if (Modifier.isFinal(field.getModifiers())) {
                throw new ReadOnlyPropertyException(name, theClass);
            }
            field.setProperty(object, newValue);
            return;
        }

        //----------------------------------------------------------------------
        // generic set method
        //----------------------------------------------------------------------
        // check for a generic get method provided through a category
        if (method == null && !useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            method = getCategoryMethodSetter(sender, "set", true);
            if (method != null) arguments = new Object[]{name, newValue};
        }

        // the generic method is valid, if available (!=null), if static or
        // if it is not static and we do no static access
        if (method == null && genericSetMethod != null && !(!genericSetMethod.isStatic() && isStatic)) {
            arguments = new Object[]{name, newValue};
            method = genericSetMethod;
        }

        //----------------------------------------------------------------------
        // executing the getter method
        //----------------------------------------------------------------------
        if (method != null) {
            if (arguments.length == 1) {
                newValue = DefaultTypeTransformation.castToType(
                        newValue,
                        method.getParameterTypes()[0].getTheClass());
                arguments[0] = newValue;
            } else {
                newValue = DefaultTypeTransformation.castToType(
                        newValue,
                        method.getParameterTypes()[1].getTheClass());
                arguments[1] = newValue;
            }
            method.doMethodInvoke(object, arguments);
            return;
        }

        //----------------------------------------------------------------------
        // error due to missing method/field
        //----------------------------------------------------------------------
        if (ambiguousListener) {
            throw new GroovyRuntimeException("There are multiple listeners for the property " + name + ". Please do not use the bean short form to access this listener.");
        }
        if (mp != null) {
            throw new ReadOnlyPropertyException(name, theClass);
        }

        invokeMissingProperty(object, name, newValue, false);
    }

    private MetaProperty getMetaProperty(Class _clazz, String name, boolean useSuper, boolean useStatic) {
        if (_clazz == theClass)
          return getMetaProperty(name, useStatic);

        CachedClass clazz = ReflectionCache.getCachedClass(_clazz);
        while (true) {
            SingleKeyHashMap propertyMap;
            if (useStatic) {
                propertyMap = staticPropertyIndex;
            } else if (useSuper) {
                propertyMap = classPropertyIndexForSuper.getNullable(clazz);
            } else {
                propertyMap = classPropertyIndex.getNullable(clazz);
            }
            if (propertyMap == null) {
                if (clazz != theCachedClass) {
                    clazz = theCachedClass;
                    continue;
                } else {
                    return null;
                }
            }
            return (MetaProperty) propertyMap.get(name);
        }
    }


    private MetaProperty getMetaProperty(String name, boolean useStatic) {
        CachedClass clazz = theCachedClass;
        SingleKeyHashMap propertyMap;
        if (useStatic) {
            propertyMap = staticPropertyIndex;
        } else {
            propertyMap = classPropertyIndex.getNullable(clazz);
        }
        if (propertyMap == null) {
            return null;
        }
        return (MetaProperty) propertyMap.get(name);
    }


    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        return getAttribute(receiver, messageName);
    }

    /**
     * Looks up the given attribute (field) on the given object
     */
    public Object getAttribute(Class sender, Object object, String attribute, boolean useSuper, boolean fromInsideClass) {
        checkInitalised();

        boolean isStatic = theClass != Class.class && object instanceof Class;
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class) object);
            return mc.getAttribute(sender, object, attribute, useSuper);
        }

        MetaProperty mp = getMetaProperty(sender, attribute, useSuper, isStatic);

        if (mp != null) {
            if (mp instanceof MetaBeanProperty) {
                MetaBeanProperty mbp = (MetaBeanProperty) mp;
                mp = mbp.getField();
            }
            try {
                // delegate the get operation to the metaproperty
                if (mp != null) return mp.getProperty(object);
            } catch (Exception e) {
                throw new GroovyRuntimeException("Cannot read field: " + attribute, e);
            }
        }

        throw new MissingFieldException(attribute, theClass);
    }

    /**
     * Sets the given attribute (field) on the given object
     */
    public void setAttribute(Class sender, Object object, String attribute, Object newValue, boolean useSuper, boolean fromInsideClass) {
        checkInitalised();

        boolean isStatic = theClass != Class.class && object instanceof Class;
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class) object);
            mc.setAttribute(sender, object, attribute, newValue, useSuper, fromInsideClass);
            return;
        }

        MetaProperty mp = getMetaProperty(sender, attribute, useSuper, isStatic);

        if (mp != null) {
            if (mp instanceof MetaBeanProperty) {
                MetaBeanProperty mbp = (MetaBeanProperty) mp;
                mp = mbp.getField();
            }
            if (mp != null) {
                mp.setProperty(object, newValue);
                return;
            }
        }

        throw new MissingFieldException(attribute, theClass);
    }

    public ClassNode getClassNode() {
        if (classNode == null && GroovyObject.class.isAssignableFrom(theClass)) {
            // lets try load it from the classpath
            String groovyFile = theClass.getName();
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
                     * todo there is no CompileUnit in scope so class name
                     * checking won't work but that mostly affects the bytecode
                     * generation rather than viewing the AST
                     */
                    CompilationUnit.ClassgenCallback search = new CompilationUnit.ClassgenCallback() {
                        public void call(ClassVisitor writer, ClassNode node) {
                            if (node.getName().equals(theClass.getName())) {
                                MetaClassImpl.this.classNode = node;
                            }
                        }
                    };

                    final ClassLoader parent = theClass.getClassLoader();
                    GroovyClassLoader gcl = (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return new GroovyClassLoader(parent);
                        }
                    });
                    CompilationUnit unit = new CompilationUnit();
                    unit.setClassgenCallback(search);
                    unit.addSource(url);
                    unit.compile(Phases.CLASS_GENERATION);
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
     * adds a MetaMethod to this class. WARNING: this method will not
     * do the neccessary steps for multimethod logic and using this
     * method doesn't mean, that a method added here is replacing another
     * method from a parent class completely. These steps are usually done
     * by initalize, which means if you need these steps, you have to add
     * the method before running initialize the first time.
     *
     * @param method the MetaMethod
     * @see #initialize()
     */
    public void addMetaMethod(MetaMethod method) {
        if (isInitialized()) {
            throw new RuntimeException("Already initialized, cannot add new method: " + method);
        }

        final CachedClass declaringClass = method.getDeclaringClass();
        addMetaMethodToIndex(method, metaMethodIndex.getHeader(declaringClass.getTheClass()));
    }

    protected void addMetaMethodToIndex(MetaMethod method, MetaMethodIndex.Header header) {
        checkIfStdMethod(method);

        String name = method.getName();
        MetaMethodIndex.Entry e = metaMethodIndex.getOrPutMethods(name, header);
        if (method.isStatic()) {
            e.staticMethods = metaMethodIndex.addMethodToList(e.staticMethods, method);
        }
        e.methods = metaMethodIndex.addMethodToList(e.methods, method);
    }

    /**
     * Checks if the metaMethod is a method from the GroovyObject interface such as setProperty, getProperty and invokeMethod
     *
     * @param metaMethod The metaMethod instance
     * @see GroovyObject
     */
    protected final void checkIfGroovyObjectMethod(MetaMethod metaMethod) {
        if (metaMethod instanceof ClosureMetaMethod || metaMethod instanceof MixinInstanceMetaMethod) {
            if(isGetPropertyMethod(metaMethod)) {
                getPropertyMethod = metaMethod;
            }
            else if(isInvokeMethod(metaMethod)) {
                invokeMethodMethod = metaMethod;
            }
            else if(isSetPropertyMethod(metaMethod)) {
                setPropertyMethod = metaMethod;
            }
        }
    }

    private boolean isSetPropertyMethod(MetaMethod metaMethod) {
        return SET_PROPERTY_METHOD.equals(metaMethod.getName())  && metaMethod.getParameterTypes().length == 2;
    }

    private boolean isGetPropertyMethod(MetaMethod metaMethod) {
        return GET_PROPERTY_METHOD.equals(metaMethod.getName());
    }

    private boolean isInvokeMethod(MetaMethod metaMethod) {
        return INVOKE_METHOD_METHOD.equals(metaMethod.getName()) && metaMethod.getParameterTypes().length == 2;
    }

    private void checkIfStdMethod(MetaMethod method) {
        checkIfGroovyObjectMethod(method);

        if (isGenericGetMethod(method) && genericGetMethod == null) {
            genericGetMethod = method;
        } else if (MetaClassHelper.isGenericSetMethod(method) && genericSetMethod == null) {
            genericSetMethod = method;
        }
        if (propertyMissingGet == null && method.getName().equals(PROPERTY_MISSING)) {
            CachedClass[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1) {
                propertyMissingGet = method;
            }
        }
        if (propertyMissingSet == null && method.getName().equals(PROPERTY_MISSING)) {
            CachedClass[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 2) {
                propertyMissingSet = method;
            }
        }
        if (method.getName().equals(METHOD_MISSING)) {
            CachedClass[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 2
                    && parameterTypes[0].getTheClass() == String.class
                    && parameterTypes[1].getTheClass() == Object.class) {
                methodMissing = method;
            }
        }

        if (theCachedClass.isNumber) {
            NumberMathModificationInfo.instance.checkIfStdMethod (method);
        }
    }

    protected boolean isInitialized() {
        return initialized;
    }

    /**
     * return false: add method
     *        null:  ignore method
     *        true:  replace
     */
    private Boolean getMatchKindForCategory(MetaMethod aMethod, MetaMethod categoryMethod) {
        CachedClass[] params1 = aMethod.getParameterTypes();
        CachedClass[] params2 = categoryMethod.getParameterTypes();
        if (params1.length != params2.length) return Boolean.FALSE;

        for (int i = 0; i < params1.length; i++) {
            if (params1[i] != params2[i]) return Boolean.FALSE;
        }

        Class aMethodClass = aMethod.getDeclaringClass().getTheClass();
        Class categoryMethodClass = categoryMethod.getDeclaringClass().getTheClass();

        if (aMethodClass==categoryMethodClass) return Boolean.TRUE;
        boolean match = aMethodClass.isAssignableFrom(categoryMethodClass);
        if (match) return Boolean.TRUE;
        return null;
    }

    private void filterMatchingMethodForCategory(FastArray list, MetaMethod method) {
        int len = list.size();
        if (len==0) {
            list.add(method);
            return;
        }

        Object data[] = list.getArray();
        for (int j = 0; j != len; ++j) {
            MetaMethod aMethod = (MetaMethod) data[j];
            Boolean match = getMatchKindForCategory(aMethod, method);
            // true == replace
            if (match==Boolean.TRUE) {
                list.set(j, method);
                return;
            // null == ignore (we have a better method already)
            } else if (match==null) {
                return;
            }
        }
        // the casese true and null for a match are through, the
        // remaining case is false and that means adding the method
        // to our list
        list.add(method);
    }

    private int findMatchingMethod(CachedMethod[] data, int from, int to, MetaMethod method) {
        for (int j = from; j <= to; ++j) {
            CachedMethod aMethod = data[j];
            CachedClass[] params1 = aMethod.getParameterTypes();
            CachedClass[] params2 = method.getParameterTypes();
            if (params1.length == params2.length) {
                boolean matches = true;
                for (int i = 0; i < params1.length; i++) {
                    if (params1[i] != params2[i]) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return j;
                }
            }
        }
        return -1;
    }

    /**
     * @return the matching method which should be found
     */
    private MetaMethod findMethod(CachedMethod aMethod) {
        Object methods = getMethods(theClass, aMethod.getName(), false);
        if (methods instanceof FastArray) {
            FastArray m  = (FastArray) methods;
            final int len = m.size;
            final Object data[] = m.getArray();
            for (int i = 0; i != len; ++i) {
                MetaMethod method = (MetaMethod) data[i];
                if (method.isMethod(aMethod)) {
                    return method;
                }
            }
        }
        else {
            MetaMethod method = (MetaMethod) methods;
            if (method.getName().equals(aMethod.getName())
//                    TODO: shoulld be better check for case when only diff in modifiers can be SYNTETIC flag
//                    && method.getModifiers() == aMethod.getModifiers()
                    && method.getReturnType().equals(aMethod.getReturnType())
                    && MetaMethod.equal(method.getParameterTypes(), aMethod.getParameterTypes())) {
                return method;
            }
        }
        //log.warning("Creating reflection based dispatcher for: " + aMethod);
        synchronized (aMethod.cachedClass) {
            return aMethod;
        }
    }


    /**
     * Chooses the correct method to use from a list of methods which match by
     * name.
     *
     * @param methodOrList   the possible methods to choose from
     * @param arguments
     */
    protected Object chooseMethod(String methodName, Object methodOrList, Class[] arguments) {
        if (methodOrList instanceof MetaMethod) {
            if (((ParameterTypes) methodOrList).isValidMethod(arguments)) {
                return methodOrList;
            }
            return null;
        }

        FastArray methods = (FastArray) methodOrList;
        if (methods==null) return null;
        int methodCount = methods.size();
        if (methodCount <= 0) {
            return null;
        } else if (methodCount == 1) {
            Object method = methods.get(0);
            if (((ParameterTypes) method).isValidMethod(arguments)) {
                return method;
            }
            return null;
        }
        Object answer;
        if (arguments == null || arguments.length == 0) {
            answer = MetaClassHelper.chooseEmptyMethodParams(methods);
        } else if (arguments.length == 1 && arguments[0] == null) {
            answer = MetaClassHelper.chooseMostGeneralMethodWith1NullParam(methods);
        } else {
            Object matchingMethods = null;

            final int len = methods.size;
            Object data[] = methods.getArray();
            for (int i = 0; i != len; ++i) {
                Object method = data[i];

                // making this false helps find matches
                if (((ParameterTypes) method).isValidMethod(arguments)) {
                    if (matchingMethods == null)
                      matchingMethods = method;
                    else
                        if (matchingMethods instanceof ArrayList)
                          ((ArrayList)matchingMethods).add(method);
                        else {
                            ArrayList arr = new ArrayList(4);
                            arr.add(matchingMethods);
                            arr.add(method);
                            matchingMethods = arr;
                        }
                }
            }
            if (matchingMethods == null) {
                return null;
            } else if (!(matchingMethods instanceof ArrayList)) {
                return matchingMethods;
            }
            return chooseMostSpecificParams(methodName, (List) matchingMethods, arguments);

        }
        if (answer != null) {
            return answer;
        }
        throw new MethodSelectionException(methodName, methods, arguments);
    }

    private Object chooseMostSpecificParams(String name, List matchingMethods, Class[] arguments) {

        long matchesDistance = -1;
        LinkedList matches = new LinkedList();
        for (Iterator iter = matchingMethods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            ParameterTypes paramTypes = (ParameterTypes) method;
            long dist = MetaClassHelper.calculateParameterDistance(arguments, paramTypes);
            if (dist == 0) return method;
            if (matches.size() == 0) {
                matches.add(method);
                matchesDistance = dist;
            } else if (dist < matchesDistance) {
                matchesDistance = dist;
                matches.clear();
                matches.add(method);
            } else if (dist == matchesDistance) {
                matches.add(method);
            }

        }
        if (matches.size() == 1) {
            return matches.getFirst();
        }
        if (matches.size() == 0) {
            return null;
        }

        //more than one matching method found --> ambiguous!
        String msg = "Ambiguous method overloading for method ";
        msg += theClass.getName() + "#" + name;
        msg += ".\nCannot resolve which method to invoke for ";
        msg += InvokerHelper.toString(arguments);
        msg += " due to overlapping prototypes between:";
        for (Iterator iter = matches.iterator(); iter.hasNext();) {
            Class[] types = ((ParameterTypes) iter.next()).getNativeParameterTypes();
            msg += "\n\t" + InvokerHelper.toString(types);
        }
        throw new GroovyRuntimeException(msg);
    }

    private boolean isGenericGetMethod(MetaMethod method) {
        if (method.getName().equals("get")) {
            CachedClass[] parameterTypes = method.getParameterTypes();
            return parameterTypes.length == 1 && parameterTypes[0].getTheClass() == String.class;
        }
        return false;
    }


    public synchronized void initialize() {
        if (!isInitialized()) {
            fillMethodIndex();
            addProperties();
            initialized = true;
        }
    }

    private void addProperties() {
        BeanInfo info;
        final Class stopClass;
        //     introspect
        try {
            if (isBeanDerivative(theClass)) {
                info = (BeanInfo) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IntrospectionException {
                        return Introspector.getBeanInfo(theClass, Introspector.IGNORE_ALL_BEANINFO);
                    }
                });
            } else {
                info = (BeanInfo) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IntrospectionException {
                        return Introspector.getBeanInfo(theClass);
                    }
                });
            }
        } catch (PrivilegedActionException pae) {
            throw new GroovyRuntimeException("exception during bean introspection", pae.getException());
        }
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        // build up the metaproperties based on the public fields, property descriptors,
        // and the getters and setters
        setupProperties(descriptors);

        EventSetDescriptor[] eventDescriptors = info.getEventSetDescriptors();
        for (int i = 0; i < eventDescriptors.length; i++) {
            EventSetDescriptor descriptor = eventDescriptors[i];
            Method[] listenerMethods = descriptor.getListenerMethods();
            for (int j = 0; j < listenerMethods.length; j++) {
                Method listenerMethod = listenerMethods[j];
                final MetaMethod metaMethod = CachedMethod.find(descriptor.getAddListenerMethod());
                addToAllMethodsIfPublic(metaMethod);
                String name = listenerMethod.getName();
                if (listeners.containsKey(name)) {
                    listeners.put(name, AMBIGUOUS_LISTENER_METHOD);
                } else {
                    listeners.put(name, metaMethod);
                }
            }
        }
    }

    private boolean isBeanDerivative(Class theClass) {
        Class next = theClass;
        while (next != null) {
            if (Arrays.asList(next.getInterfaces()).contains(BeanInfo.class)) return true;
            next = next.getSuperclass();
        }
        return false;
    }

    private void addToAllMethodsIfPublic(MetaMethod metaMethod) {
        if (Modifier.isPublic(metaMethod.getModifiers()))
            allMethods.add(metaMethod);
    }

    public List getMethods() {
        return allMethods;
    }

    public List getMetaMethods() {
        return new ArrayList(newGroovyMethodsSet);
    }

    protected void dropStaticMethodCache(String name) {
        metaMethodIndex.clearCaches(name);
    }

    protected void dropMethodCache(String name) {
        metaMethodIndex.clearCaches(name);
    }

    public CallSite createPojoCallSite(CallSite site, Object receiver, Object[] args) {
        if (!(this instanceof AdaptingMetaClass)) {
            Class [] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod metaMethod = getMethodWithCachingInternal(getTheClass(), site, params);
            if (metaMethod != null)
               return PojoMetaMethodSite.createPojoMetaMethodSite(site, this, metaMethod, params, receiver, args);
        }
        return new PojoMetaClassSite(site, this);
    }


    public CallSite createStaticSite(CallSite site, Object[] args) {
        if (!(this instanceof AdaptingMetaClass)) {
            Class [] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod metaMethod = retrieveStaticMethod(site.getName(), args);
            if (metaMethod != null)
               return StaticMetaMethodSite.createStaticMetaMethodSite(site, this, metaMethod, params, args);
        }
        return new StaticMetaClassSite(site, this);
    }

    public CallSite createPogoCallSite(CallSite site, Object[] args) {
        if (site.getUsage().get() == 0 && !(this instanceof AdaptingMetaClass)) {
            Class [] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod metaMethod = getMethodWithCachingInternal(theClass, site, params);
            if (metaMethod != null)
               return PogoMetaMethodSite.createPogoMetaMethodSite(site, this, metaMethod, params, args);
        }
        return new PogoMetaClassSite(site, this);
    }

    public CallSite createPogoCallCurrentSite(CallSite site, Class sender, Object[] args) {
        if (site.getUsage().get() == 0 && !(this instanceof AdaptingMetaClass)) {
          Class [] params = MetaClassHelper.convertToTypeArray(args);
          MetaMethod metaMethod = getMethodWithCachingInternal(sender, site, params);
          if (metaMethod != null)
            return PogoMetaMethodSite.createPogoMetaMethodSite(site, this, metaMethod, params, args);
        }
        return new PogoMetaClassSite(site, this);
    }

    public CallSite createConstructorSite(CallSite site, Object[] args) {
        if (!(this instanceof AdaptingMetaClass)) {
            Class[] params = MetaClassHelper.convertToTypeArray(args);
            CachedConstructor constructor = (CachedConstructor) chooseMethod("<init>", constructors, params);
            if (constructor != null) {
                return ConstructorSite.createConstructorSite(site, this,constructor,params, args);
            }
            else {
                if (args.length == 1 && args[0] instanceof Map) {
                    constructor = (CachedConstructor) chooseMethod("<init>", constructors, MetaClassHelper.EMPTY_TYPE_ARRAY);
                    if (constructor != null) {
                        return new ConstructorSite.NoParamSite(site, this,constructor,params);
                    }
                }
            }
        }
        return new MetaClassConstructorSite(site, this);
    }

    public ClassInfo getClassInfo() {
        return theCachedClass.classInfo;
    }

    public int getVersion() {
        return theCachedClass.classInfo.getVersion();
    }

    public MetaMethod[] getAdditionalMetaMethods() {
        return additionalMetaMethods;
    }

    protected MetaBeanProperty findPropertyInClassHierarchy(String propertyName, CachedClass theClass) {
        MetaBeanProperty property= null;
        if (theClass == null)
            return null;

        final CachedClass superClass = theClass.getCachedSuperClass();
        if (superClass == null)
          return null;

        MetaClass metaClass = this.registry.getMetaClass(superClass.getTheClass());
        if(metaClass instanceof MutableMetaClass) {
            property = getMetaPropertyFromMutableMetaClass(propertyName,metaClass);
            if(property == null) {
                if(superClass != ReflectionCache.OBJECT_CLASS) {
                    property = findPropertyInClassHierarchy(propertyName, superClass);
                }
                if(property == null) {
                    final Class[] interfaces = theClass.getTheClass().getInterfaces();
                    property = searchInterfacesForMetaProperty(propertyName, interfaces);
                }
            }
        }
        return property;

    }

    private MetaBeanProperty searchInterfacesForMetaProperty(String propertyName, Class[] interfaces) {
        MetaBeanProperty property = null;
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            MetaClass metaClass = this.registry.getMetaClass(anInterface);
            if(metaClass instanceof MutableMetaClass) {
                property = getMetaPropertyFromMutableMetaClass(propertyName,metaClass);
                if(property != null) break;
            }
            Class[] superInterfaces = anInterface.getInterfaces();
            if(superInterfaces.length > 0) {
                property = searchInterfacesForMetaProperty(propertyName, superInterfaces);
                if(property!=null) break;
            }

        }
        return property;
    }

    private MetaBeanProperty getMetaPropertyFromMutableMetaClass(String propertyName, MetaClass metaClass) {
        final boolean isModified = ((MutableMetaClass) metaClass).isModified();
        final MetaProperty metaProperty = metaClass.getMetaProperty(propertyName);
        if(metaProperty instanceof MetaBeanProperty)
            return isModified ? (MetaBeanProperty)metaProperty : null;
        else
            return null;

    }

    protected MetaMethod findMixinMethod(String methodName, Class[] arguments) {
        return null;
    }

    protected static MetaMethod findMethodInClassHeirarchy(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass) {
        MetaMethod method = null;

        Class superClass;
        if (metaClass.getTheClass().isArray() && !metaClass.getTheClass().getComponentType().isPrimitive() && metaClass.getTheClass().getComponentType() != Object.class) {
            superClass = Object[].class;
        }
        else {
            superClass = metaClass.getTheClass().getSuperclass();
        }

        if (superClass != null) {
          MetaClass superMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(superClass);
          method = findMethodInClassHeirarchy(instanceKlazz, methodName, arguments, superMetaClass);
        }
        else {
            if (metaClass.getTheClass().isInterface()) {
                MetaClass superMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(Object.class);
                method = findMethodInClassHeirarchy(instanceKlazz, methodName, arguments, superMetaClass);
            }
        }

        method = findSubClassMethod(instanceKlazz, methodName, arguments, metaClass, method);

        MetaMethod infMethod = searchInterfacesForMetaMethod(instanceKlazz, methodName, arguments, metaClass);
        if (infMethod != null) {
            if (method == null)
              method = infMethod;
            else
              method = mostSpecific(method, infMethod, instanceKlazz);
        }

        method = findOwnMethod(instanceKlazz, methodName, arguments, metaClass, method);

        return method;
    }

    private static MetaMethod findSubClassMethod(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass, MetaMethod method) {
        if (metaClass instanceof MetaClassImpl) {
            Object list = ((MetaClassImpl) metaClass).getSubclassMetaMethods(methodName);
            if (list != null) {
                if (list instanceof MetaMethod) {
                    MetaMethod m = (MetaMethod) list;
                    if (m.getDeclaringClass().getTheClass().isAssignableFrom(instanceKlazz)) {
                        if (m.isValidExactMethod(arguments)) {
                            if (method == null)
                              method = m;
                            else {
                              method = mostSpecific (method, m, instanceKlazz);
                            }
                        }
                    }
                }
                else {
                    FastArray arr = (FastArray) list;
                    for (int i = 0; i != arr.size(); ++i) {
                        MetaMethod m = (MetaMethod) arr.get(i);
                        if (m.getDeclaringClass().getTheClass().isAssignableFrom(instanceKlazz)) {
                            if (m.isValidExactMethod(arguments)) {
                                if (method == null)
                                  method = m;
                                else {
                                  method = mostSpecific (method, m, instanceKlazz);
                                }
                            }
                        }
                    }
                }
            }
        }
        return method;
    }

    private static MetaMethod mostSpecific(MetaMethod method, MetaMethod newMethod, Class instanceKlazz) {
        Class newMethodC = newMethod.getDeclaringClass().getTheClass();
        Class methodC = method.getDeclaringClass().getTheClass();

        if (!newMethodC.isAssignableFrom(instanceKlazz))
          return method;

        if (newMethodC == methodC)
          return newMethod;

        if (newMethodC.isAssignableFrom(methodC)) {
            return method;
        }

        if (methodC.isAssignableFrom(newMethodC)) {
            return newMethod;
        }

        return newMethod;
    }

    private static MetaMethod searchInterfacesForMetaMethod(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass) {
        Class[] interfaces = metaClass.getTheClass().getInterfaces();

        MetaMethod method = null;
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            MetaClass infMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(anInterface);
            MetaMethod infMethod = searchInterfacesForMetaMethod(instanceKlazz, methodName,arguments, infMetaClass);
            if (infMethod != null) {
                if (method == null)
                  method = infMethod;
                else
                  method = mostSpecific(method, infMethod, instanceKlazz);
            }
        }

        method = findSubClassMethod(instanceKlazz, methodName, arguments, metaClass, method);

        method = findOwnMethod(instanceKlazz, methodName, arguments, metaClass, method);

        return method;
    }

    protected static MetaMethod findOwnMethod(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass, MetaMethod method) {
        // we trick ourselves here
        if (instanceKlazz == metaClass.getTheClass())
          return method;

        MetaMethod ownMethod = metaClass.pickMethod(methodName, arguments);
        if (ownMethod != null) {
            if (method == null)
              method = ownMethod;
            else
              method = mostSpecific(method, ownMethod, instanceKlazz);
        }
        return method;
    }

    protected Object getSubclassMetaMethods(String methodName) {
        return null;
    }

    private abstract class MethodIndexAction {
        public void iterate() {
            final ComplexKeyHashMap.Entry[] table = metaMethodIndex.methodHeaders.getTable();
            int len = table.length;
            for (int i = 0; i != len; ++i) {
                for (SingleKeyHashMap.Entry classEntry = (SingleKeyHashMap.Entry) table[i];
                     classEntry != null;
                     classEntry = (SingleKeyHashMap.Entry) classEntry.next) {

                    Class clazz = (Class) classEntry.getKey();

                    if (skipClass(clazz)) continue;

                    MetaMethodIndex.Header header = (MetaMethodIndex.Header) classEntry.getValue();
                    for (MetaMethodIndex.Entry nameEntry = header.head; nameEntry != null; nameEntry = nameEntry.nextClassEntry) {
                        methodNameAction(clazz, nameEntry);
                    }
                }
            }
        }

        public abstract void methodNameAction(Class clazz, MetaMethodIndex.Entry methods);

        public boolean skipClass(Class clazz) {
            return false;
        }
    }

    public Object getProperty(Object object, String property) {
        return getProperty(theClass, object, property, false, false);
    }

    public void setProperty(Object object, String property, Object newValue) {
        setProperty(theClass, object, property, newValue, false, false);
    }

    public Object getAttribute(Object object, String attribute) {
        return getAttribute(theClass, object, attribute, false, false);
    }

    public void setAttribute(Object object, String attribute, Object newValue) {
        setAttribute(theClass, object, attribute, newValue, false, false);
    }

    public MetaMethod pickMethod(String methodName, Class[] arguments) {
        return getMethodWithoutCaching(theClass, methodName, arguments, false);
    }

    /**
     * @deprecated use pickMethod instead
     */
    protected MetaMethod retrieveMethod(String methodName, Class[] arguments) {
        return pickMethod(methodName, arguments);
    }

    /**
     * remove all method call cache entries. This should be done if a
     * method is added during runtime, but not by using a category.
     */
    protected void clearInvocationCaches() {
        metaMethodIndex.clearCaches ();
    }

    private static final SingleKeyHashMap.Copier NAME_INDEX_COPIER = new SingleKeyHashMap.Copier() {
        public Object copy(Object value) {
            if (value instanceof FastArray)
              return ((FastArray) value).copy();
            else
              return value;
        }
    };

    private static final SingleKeyHashMap.Copier METHOD_INDEX_COPIER = new SingleKeyHashMap.Copier() {
        public Object copy(Object value) {
            return SingleKeyHashMap.copy(new SingleKeyHashMap(false), (SingleKeyHashMap) value, NAME_INDEX_COPIER);
        }
    };

    class MethodIndex extends Index {
        public MethodIndex(boolean b) {
            super(false);
        }

        public MethodIndex(int size) {
            super(size);
        }

        public MethodIndex() {
            super();
        }

        MethodIndex copy() {
            return (MethodIndex) SingleKeyHashMap.copy(new MethodIndex(false), this, METHOD_INDEX_COPIER);
        }

        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public static class Index extends SingleKeyHashMap {

        public Index(int size) {
        }

        public Index() {
        }

        public Index(boolean size) {
            super(false);
        }

        public SingleKeyHashMap getNotNull(CachedClass key) {
            Entry res = getOrPut(key);
            if (res.value == null) {
                res.value = new SingleKeyHashMap();
            }
            return (SingleKeyHashMap) res.value;
        }

        public void put(CachedClass key, SingleKeyHashMap value) {
            ((Entry) getOrPut(key)).value = value;
        }

        public SingleKeyHashMap getNullable(CachedClass clazz) {
            return (SingleKeyHashMap) get(clazz);
        }

        public boolean checkEquals(ComplexKeyHashMap.Entry e, Object key) {
            return ((Entry) e).key.equals(key);
        }
    }

    private static class DummyMetaMethod extends MetaMethod {

        public int getModifiers() {
            return 0;
        }

        public String getName() {
            return null;
        }

        public Class getReturnType() {
            return null;
        }

        public CachedClass getDeclaringClass() {
            return null;
        }

        public ParameterTypes getParamTypes() {
            return null;
        }

        public Object invoke(Object object, Object[] arguments) {
            return null;
        }
    }

    private static class GetMethodMetaProperty extends MetaProperty {
        private final MetaMethod theMethod;

        public GetMethodMetaProperty(String name, MetaMethod theMethod) {
            super(name, Object.class);
            this.theMethod = theMethod;
        }

        public Object getProperty(Object object) {
            return theMethod.doMethodInvoke(object, new Object[]{name});
        }

        public void setProperty(Object object, Object newValue) {
            throw new UnsupportedOperationException();
        }
    }

    private static class GetBeanMethodMetaProperty extends MetaProperty {
        private final MetaMethod theMethod;

        public GetBeanMethodMetaProperty(String name, MetaMethod theMethod) {
            super(name, Object.class);
            this.theMethod = theMethod;
        }

        public Object getProperty(Object object) {
            return theMethod.doMethodInvoke(object, EMPTY_ARGUMENTS);
        }

        public void setProperty(Object object, Object newValue) {
            throw new UnsupportedOperationException();
        }
    }
}
