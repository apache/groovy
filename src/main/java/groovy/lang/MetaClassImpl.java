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

import org.apache.groovy.internal.util.UncheckedThrow;
import org.apache.groovy.util.BeanUtils;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedConstructor;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.android.AndroidSupport;
import org.codehaus.groovy.runtime.ArrayTypeUtils;
import org.codehaus.groovy.runtime.ConvertedClosure;
import org.codehaus.groovy.runtime.CurriedClosure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.FormatHelper;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.GroovyCategorySupport.CategoryMethod;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.callsite.AbstractCallSite;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.ConstructorSite;
import org.codehaus.groovy.runtime.callsite.MetaClassConstructorSite;
import org.codehaus.groovy.runtime.callsite.PogoMetaClassSite;
import org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaClassSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;
import org.codehaus.groovy.runtime.callsite.StaticMetaClassSite;
import org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MetaMethodIndex;
import org.codehaus.groovy.runtime.metaclass.MethodMetaProperty.GetBeanMethodMetaProperty;
import org.codehaus.groovy.runtime.metaclass.MethodMetaProperty.GetMethodMetaProperty;
import org.codehaus.groovy.runtime.metaclass.MethodSelectionException;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack;
import org.codehaus.groovy.runtime.metaclass.MixinInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.MultipleSetterProperty;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod;
import org.codehaus.groovy.runtime.metaclass.TransformMetaMethod;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.NumberMathModificationInfo;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.util.FastArray;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Opcodes;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import static groovy.lang.Tuple.tuple;
import static java.lang.Character.isUpperCase;
import static org.apache.groovy.util.Arrays.concat;
import static org.codehaus.groovy.ast.tools.GeneralUtils.inSamePackage;
import static org.codehaus.groovy.reflection.ReflectionUtils.checkAccessible;

/**
 * Allows methods to be dynamically added to existing classes at runtime
 *
 * @see groovy.lang.MetaClass
 */
public class MetaClassImpl implements MetaClass, MutableMetaClass {

    public static final Object[] EMPTY_ARGUMENTS = MetaClassHelper.EMPTY_ARRAY;

    protected static final String STATIC_METHOD_MISSING = "$static_methodMissing";
    protected static final String STATIC_PROPERTY_MISSING = "$static_propertyMissing";
    protected static final String METHOD_MISSING = "methodMissing";
    protected static final String PROPERTY_MISSING = "propertyMissing";
    protected static final String INVOKE_METHOD_METHOD = "invokeMethod";

    private static final String CALL_METHOD = "call";
    private static final String DO_CALL_METHOD = "doCall";
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String GET_PROPERTY_METHOD = "getProperty";
    private static final String SET_PROPERTY_METHOD = "setProperty";

    private static final Class[] METHOD_MISSING_ARGS = new Class[]{String.class, Object.class};
    private static final Class[] GETTER_MISSING_ARGS = new Class[]{String.class};
    private static final Class[] SETTER_MISSING_ARGS = METHOD_MISSING_ARGS;
    private static final MetaMethod AMBIGUOUS_LISTENER_METHOD = new DummyMetaMethod();
    private static final Comparator<CachedClass> CACHED_CLASS_NAME_COMPARATOR = Comparator.comparing(CachedClass::getName);
    private static final boolean PERMISSIVE_PROPERTY_ACCESS = SystemUtil.getBooleanSafe("groovy.permissive.property.access");
    private static final VMPlugin VM_PLUGIN = VMPluginFactory.getPlugin();

    protected final Class theClass;
    protected final CachedClass theCachedClass;
    protected final boolean isGroovyObject;
    protected final boolean isMap;
    protected final MetaMethodIndex metaMethodIndex;

    private final Map<CachedClass, LinkedHashMap<String, MetaProperty>> classPropertyIndex = new LinkedHashMap<>();
    private final Map<String, MetaProperty> staticPropertyIndex = new LinkedHashMap<>();
    private final Map<String, MetaMethod> listeners = new LinkedHashMap<>();
    private final List<MetaMethod> allMethods = new ArrayList<>();
    // we only need one of these that can be reused over and over.
    private final MetaProperty arrayLengthProperty = new MetaArrayLengthProperty();
    private final Map<CachedClass, LinkedHashMap<String, MetaProperty>> classPropertyIndexForSuper = new LinkedHashMap<>();
    private final Set<MetaMethod> newGroovyMethodsSet = new LinkedHashSet<>();
    private final MetaMethod[] myNewMetaMethods;
    private final MetaMethod[] additionalMetaMethods;

    protected MetaMethod getPropertyMethod;
    protected MetaMethod invokeMethodMethod;
    protected MetaMethod setPropertyMethod;
    protected MetaClassRegistry registry;
    private ClassNode classNode;
    private FastArray constructors;
    private volatile boolean initialized;
    private MetaMethod genericGetMethod;
    private MetaMethod genericSetMethod;
    private MetaMethod propertyMissingGet;
    private MetaMethod propertyMissingSet;
    private MetaMethod methodMissing;
    private Map<String, MetaMethodIndex.Cache> mainClassMethodHeader;
    private boolean permissivePropertyAccess = PERMISSIVE_PROPERTY_ACCESS;

    /**
     * Constructor
     *
     * @param theClass The class this is the metaclass for
     * @param add      The methods for this class
     */
    public MetaClassImpl(final Class theClass, final MetaMethod[] add) {
        this.theClass = theClass;
        theCachedClass = ReflectionCache.getCachedClass(theClass);
        this.isGroovyObject = GroovyObject.class.isAssignableFrom(theClass);
        this.isMap = Map.class.isAssignableFrom(theClass);
        this.registry = GroovySystem.getMetaClassRegistry();
        metaMethodIndex = new MetaMethodIndex(theCachedClass);
        final MetaMethod[] metaMethods = theCachedClass.getNewMetaMethods();
        if (add != null && add.length != 0) {
            myNewMetaMethods = concat(metaMethods, add);
            additionalMetaMethods = metaMethods;
        } else {
            myNewMetaMethods = metaMethods;
            additionalMetaMethods = MetaMethod.EMPTY_ARRAY;
        }
    }

    /**
     * Constructor that sets the methods to null
     *
     * @param theClass The class this is the metaclass for
     */
    public MetaClassImpl(final Class theClass) {
        this(theClass, null);
    }

    /**
     * Constructor with registry
     *
     * @param registry The metaclass registry for this MetaClass
     * @param theClass The class
     * @param add      The methods
     */
    public MetaClassImpl(final MetaClassRegistry registry, final Class theClass, final MetaMethod[] add) {
        this(theClass, add);
        this.registry = registry;
        this.constructors = new FastArray(theCachedClass.getConstructors());
    }

    /**
     * Constructor with registry setting methods to null
     *
     * @param registry The metaclass registry for this MetaClass
     * @param theClass The class
     */
    public MetaClassImpl(MetaClassRegistry registry, final Class theClass) {
        this(registry, theClass, null);
    }

    /**
     * Returns the cached class for this metaclass
     *
     * @return The cached class.
     */
    public final CachedClass getTheCachedClass() {
        return theCachedClass;
    }

    /**
     * Returns the registry for this metaclass
     *
     * @return The registry
     */
    public MetaClassRegistry getRegistry() {
        return registry;
    }

    /**
     * @see MetaObjectProtocol#respondsTo(Object, String, Object[])
     */
    @Override
    public List<MetaMethod> respondsTo(final Object obj, final String name, final Object[] argTypes) {
        MetaMethod m = getMetaMethod(name, MetaClassHelper.castArgumentsToClassArray(argTypes));
        return (m != null ? Collections.singletonList(m) : Collections.emptyList());
    }

    /**
     * @see MetaObjectProtocol#respondsTo(Object, String)
     */
    @Override
    public List<MetaMethod> respondsTo(final Object obj, final String name) {
        final Object o = getMethods(getTheClass(), name, false);
        if (o instanceof FastArray) {
            return ((FastArray) o).toList();
        }
        return Collections.<MetaMethod>singletonList((MetaMethod) o);
    }

    /**
     * @see MetaObjectProtocol#hasProperty(Object, String)
     */
    @Override
    public MetaProperty hasProperty(final Object obj, final String name) {
        return getMetaProperty(name);
    }

    /**
     * @see MetaObjectProtocol#getMetaProperty(String)
     */
    @Override
    public MetaProperty getMetaProperty(final String name) {
        MetaProperty metaProperty = null;

        LinkedHashMap<String, MetaProperty> propertyMap = classPropertyIndex.computeIfAbsent(theCachedClass, k -> new LinkedHashMap<>());
        metaProperty = propertyMap.get(name);
        if (metaProperty == null) {
            metaProperty = staticPropertyIndex.get(name);
            if (metaProperty == null) {
                propertyMap = classPropertyIndexForSuper.computeIfAbsent(theCachedClass, k -> new LinkedHashMap<>());
                metaProperty = propertyMap.get(name);
                if (metaProperty == null) {
                    MetaBeanProperty property = findPropertyInClassHierarchy(name, theCachedClass);
                    if (property != null) {
                        onSuperPropertyFoundInHierarchy(property);
                        metaProperty = property;
                    }
                }
            }
        }

        return metaProperty;
    }

    /**
     * @see MetaObjectProtocol#getStaticMetaMethod(String, Object[])
     */
    @Override
    public MetaMethod getStaticMetaMethod(final String name, final Object[] argTypes) {
        return pickStaticMethod(name, MetaClassHelper.castArgumentsToClassArray(argTypes));
    }

    /**
     * @see MetaObjectProtocol#getMetaMethod(String, Object[])
     */
    @Override
    public MetaMethod getMetaMethod(final String name, final Object[] argTypes) {
        return pickMethod(name, MetaClassHelper.castArgumentsToClassArray(argTypes));
    }

    /**
     * Returns the class this object this is the metaclass of.
     *
     * @return The class contained by this metaclass
     */
    @Override
    public Class getTheClass() {
        return this.theClass;
    }

    /**
     * Return whether the class represented by this metaclass instance is an instance of the GroovyObject class
     *
     * @return true if this is a groovy class, false otherwise.
     */
    public boolean isGroovyObject() {
        return isGroovyObject;
    }

    private void fillMethodIndex() {
        mainClassMethodHeader = metaMethodIndex.getHeader(theClass);

        Set<CachedClass> interfaces    = theCachedClass.getInterfaces();
        List<CachedClass> superClasses = getSuperClasses(); // in reverse order
        CachedClass firstGroovySuper   = calcFirstGroovySuperClass(superClasses);

        for (CachedClass c : interfaces) {
            for (CachedMethod m : c.getMethods()) {
                if (c == theCachedClass || (m.isPublic() && !m.isStatic())) { // GROOVY-8164
                    addMetaMethodToIndex(m, mainClassMethodHeader);
                }
            }
        }

        populateMethods(superClasses, firstGroovySuper);

        inheritInterfaceNewMetaMethods(interfaces);

        if (isGroovyObject) {
            metaMethodIndex.copyMethodsToSuper(); // methods --> methodsForSuper
            connectMultimethods(superClasses, firstGroovySuper);
            removeMultimethodsOverloadedWithPrivateMethods();
            replaceWithMOPCalls(theCachedClass.mopMethods);
        }
    }

    private void populateMethods(final List<CachedClass> superClasses, final CachedClass firstGroovySuper) {
        var header = metaMethodIndex.getHeader(firstGroovySuper.getTheClass());
        CachedClass c;
        Iterator<CachedClass> iter = superClasses.iterator();
        while (iter.hasNext()) {
            c = iter.next();

            for (var metaMethod : c.getMethods()) {
                addToAllMethodsIfPublic(metaMethod);
                if (c == firstGroovySuper || (metaMethod.isPublic() || metaMethod.isProtected() ||
                        (!metaMethod.isPrivate() && inSamePackage(metaMethod.getDeclaringClass().getTheClass(), theClass)))) { // GROOVY-11357
                    addMetaMethodToIndex(metaMethod, header);
                }
            }

            for (var metaMethod : getNewMetaMethods(c)) {
                if (newGroovyMethodsSet.add(metaMethod)) {
                    addMetaMethodToIndex(metaMethod, header);
                }
            }

            if (c == firstGroovySuper)
                break;
        }

        var last = header;
        while (iter.hasNext()) {
            c = iter.next();
            header = metaMethodIndex.getHeader(c.getTheClass());

            if (last != null) {
                metaMethodIndex.copyNonPrivateMethods(last, header);
            }
            last = header;

            for (var metaMethod : c.getMethods()) {
                addToAllMethodsIfPublic(metaMethod);
                addMetaMethodToIndex(metaMethod, header);
            }

            for (var metaMethod : getNewMetaMethods(c)) {
                if (metaMethod.getName().equals(CONSTRUCTOR_NAME)
                        && !metaMethod.getDeclaringClass().equals(theCachedClass)) continue;
                if (newGroovyMethodsSet.add(metaMethod)) {
                    addMetaMethodToIndex(metaMethod, header);
                }
            }
        }
    }

    private MetaMethod[] getNewMetaMethods(final CachedClass c) {
        if (theCachedClass != c)
            return c.getNewMetaMethods();

        return myNewMetaMethods;
    }

    protected LinkedList<CachedClass> getSuperClasses() {
        LinkedList<CachedClass> superClasses = new LinkedList<>();

        if (theClass.isInterface()) {
            superClasses.add(ReflectionCache.OBJECT_CLASS);
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
            @Override
            public boolean skipClass(final Class<?> clazz) {
                return clazz == theClass;
            }

            @Override
            public void methodNameAction(final Class<?> clazz, final MetaMethodIndex.Cache e) {
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
                } else {
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
                if (o instanceof FastArray) {
                    e.methods = ((FastArray) o).copy();
                } else {
                    e.methods = o;
                }
            }
        };
        mia.iterate();
    }

    private void replaceWithMOPCalls(final CachedMethod[] mopMethods) {
        class MOPIter extends MethodIndexAction {
            boolean useThis;

            @Override
            public void methodNameAction(final Class<?> c, final MetaMethodIndex.Cache e) {
                Object arrayOrMethod = (useThis ? e.methods : e.methodsForSuper);
                if (arrayOrMethod instanceof FastArray) {
                    FastArray methods = (FastArray) arrayOrMethod;
                    Object[] data = methods.getArray();
                    for (int i = 0; i < methods.size(); i += 1) {
                        MetaMethod method = (MetaMethod) data[i];
                        int matchedMethod = mopArrayIndex(method, c);
                        if (matchedMethod >= 0) {
                            methods.set(i, mopMethods[matchedMethod]);
                        } else if (!useThis && !isDGM(method) && (isBridge(method)
                                || c == method.getDeclaringClass().getTheClass())) {
                            methods.remove(i--); // not fit for super usage
                        }
                    }
                    if (!useThis) {
                        int n = methods.size();
                        if (n == 0) e.methodsForSuper = null;
                        else if (n == 1) e.methodsForSuper = data[0];
                    }
                } else if (arrayOrMethod != null) {
                    MetaMethod method = (MetaMethod) arrayOrMethod;
                    int matchedMethod = mopArrayIndex(method, c);
                    if (matchedMethod >= 0) {
                        if (useThis) e.methods = mopMethods[matchedMethod];
                        else e.methodsForSuper = mopMethods[matchedMethod];
                    } else if (!useThis && !isDGM(method) && (isBridge(method)
                            || c == method.getDeclaringClass().getTheClass())) {
                        e.methodsForSuper = null; // not fit for super usage
                    }
                }
            }

            private int mopArrayIndex(final MetaMethod method, final Class<?> c) {
                if (mopMethods == null || mopMethods.length == 0) return -1;
                if (isDGM(method) || (useThis ^ method.isPrivate())) return -1;
                if (useThis) return mopArrayIndex(method, method.getMopName());
                // GROOVY-4922: Due to a numbering scheme change, find the super$number$methodName with
                // the highest value. If we don't, no method may be found, leading to a stack overflow!
                int distance = ReflectionCache.getCachedClass(c).getSuperClassDistance() - 1;
                if (isBridge(method)) // GROOVY-6663
                    return mopArrayIndex(method, "super$" + distance + "$" + method.getName());
                while (distance > 0) {
                    int index = mopArrayIndex(method, "super$" + distance + "$" + method.getName());
                    if (index >= 0) return index;
                    distance -= 1;
                }
                return -1;
            }

            private int mopArrayIndex(final MetaMethod method, final String mopName) {
                int index = Arrays.binarySearch(mopMethods, mopName, CachedClass.CachedMethodComparatorWithString.INSTANCE);
                if (index >= 0) {
                    int from = index, to = index; // include overloads in search
                    while (from > 0 && mopMethods[from - 1].getName().equals(mopName)) from -= 1;
                    while (to < mopMethods.length - 1 && mopMethods[to + 1].getName().equals(mopName)) to += 1;

                    for (index = from; index <= to; index += 1) {
                        CachedClass[] params1 = mopMethods[index].getParameterTypes();
                        CachedClass[] params2 = method.getParameterTypes();
                        if (MetaMethod.equal(params1, params2)) {
                            return index;
                        }
                    }
                }
                return -1;
            }

            private boolean isBridge(final MetaMethod method) {
                return (method.getModifiers() & Opcodes.ACC_BRIDGE) != 0;
            }

            private boolean isDGM(final MetaMethod method) {
                return method instanceof GeneratedMetaMethod || method instanceof NewMetaMethod;
            }
        }
        MOPIter iter = new MOPIter();

        // replace all calls for super with the correct MOP method
        iter.useThis = false;
        iter.iterate();

        if (mopMethods == null || mopMethods.length == 0) return;

        // replace all calls for this with the correct MOP method
        iter.useThis = true;
        iter.iterate();
    }

    private void inheritInterfaceNewMetaMethods(final Set<CachedClass> interfaces) {
        Method[] theClassMethods = null;
        // add methods declared by DGM for interfaces
        for (CachedClass face : interfaces) {
            for (MetaMethod method : getNewMetaMethods(face)) {
                boolean skip = false;
                // skip DGM methods on an interface if the class already has the method
                // but don't skip for GroovyObject-related methods as it breaks things :-(
                if (method instanceof GeneratedMetaMethod && !GroovyObject.class.isAssignableFrom(method.getDeclaringClass().getTheClass())) {
                    final String generatedMethodName = method.getName();
                    final CachedClass[] generatedMethodParameterTypes = method.getParameterTypes();
                    for (Method m : (null == theClassMethods ? theClassMethods = theClass.getMethods() : theClassMethods)) {
                        if (generatedMethodName.equals(m.getName())
                                // below not true for DGM#push and also co-variant return scenarios
                                //&& method.getReturnType().equals(m.getReturnType())
                                && MetaMethod.equal(generatedMethodParameterTypes, m.getParameterTypes())) {
                            skip = true;
                            break;
                        }
                    }
                }
                if (!skip) {
                    newGroovyMethodsSet.add(method);
                    addMetaMethodToIndex(method, mainClassMethodHeader);
                }
            }
        }
    }

    private void connectMultimethods(final List<CachedClass> superClasses, final CachedClass firstGroovyClass) {
        Map<String, MetaMethodIndex.Cache> last = null;
        for (ListIterator<CachedClass> iter = superClasses.listIterator(superClasses.size()); iter.hasPrevious(); ) {
            CachedClass c = iter.previous();

            var methodIndex = metaMethodIndex.getHeader(c.getTheClass());
            // We don't copy DGM methods to superclasses' indexes
            // The reason we can do that is particular set of DGM methods in use,
            // if at some point we will define DGM method for some Groovy class or
            // for a class derived from such, we will need to revise this condition.
            // It saves us a lot of space and some noticeable time
            if (last != null) metaMethodIndex.copyNonPrivateNonNewMetaMethods(last, methodIndex);
            last = methodIndex;

            if (c == firstGroovyClass)
                break;
        }
    }

    private CachedClass calcFirstGroovySuperClass(final List<CachedClass> superClasses) {
        if (theCachedClass.isInterface)
            return ReflectionCache.OBJECT_CLASS;

        CachedClass firstGroovy = null;
        Iterator<CachedClass> iter = superClasses.iterator();
        while (iter.hasNext()) {
            CachedClass c = iter.next();
            if (GroovyObject.class.isAssignableFrom(c.getTheClass())) {
                firstGroovy = c;
                break;
            }
        }

        if (firstGroovy == null) {
            return isGroovyObject ? theCachedClass.getCachedSuperClass() : theCachedClass;
        }
        // Closure for closures and GroovyObjectSupport for extenders (including Closure)
        if (firstGroovy.getTheClass() == GroovyObjectSupport.class) {
            if (iter.hasNext()) { var nextGroovy = iter.next() ;
                if (nextGroovy.getTheClass() == Closure.class) {
                    if (iter.hasNext()) return nextGroovy;
                }
                return firstGroovy; // GroovyObjectSupport
            }
        }
        return firstGroovy.getCachedSuperClass();
    }

    /**
     * Gets all the normal object methods of this class for the given name.
     *
     * @return object methods available from this class for given name
     */
    private Object getMethods(final Class<?> sender, final String name, final boolean isCallToSuper) {
        Object answer;

        final MetaMethodIndex.Cache entry = metaMethodIndex.getMethods(sender, name);
        if (entry == null) {
            answer = FastArray.EMPTY_LIST;
        } else if (isCallToSuper) {
            answer = entry.methodsForSuper;
        } else {
            answer = entry.methods;
        }

        if (answer == null) answer = FastArray.EMPTY_LIST;

        if (!isCallToSuper) {
            List<CategoryMethod> used = GroovyCategorySupport.getCategoryMethods(name);
            if (used != null) {
                FastArray arr;
                if (answer instanceof MetaMethod) {
                    arr = new FastArray();
                    arr.add(answer);
                } else {
                    arr = ((FastArray) answer).copy();
                }
                for (CategoryMethod cm : used) {
                    if (!cm.getDeclaringClass().getTheClass().isAssignableFrom(sender))
                        continue;
                    filterMatchingMethodForCategory(arr, cm);
                }
                answer = arr;
            }
        }
        return answer;
    }

    /**
     * Gets all the normal static methods of this class for the given name.
     *
     * @return static methods available from this class for given name
     */
    private Object getStaticMethods(final Class<?> sender, final String name) {
        final MetaMethodIndex.Cache entry = metaMethodIndex.getMethods(sender, name);
        if (entry == null)
            return FastArray.EMPTY_LIST;
        Object answer = entry.staticMethods;
        if (answer == null)
            return FastArray.EMPTY_LIST;
        return answer;
    }

    /**
     * Returns whether this MetaClassImpl has been modified. Since MetaClassImpl
     * is not designed for modification this method always returns false
     *
     * @return false
     */
    @Override
    public boolean isModified() {
        return false;  // MetaClassImpl not designed for modification, just return false
    }

    /**
     * Adds an instance method to this metaclass.
     *
     * @param method The method to be added
     */
    @Override
    public void addNewInstanceMethod(final Method method) {
        CachedMethod cachedMethod = CachedMethod.find(method);
        NewInstanceMetaMethod newMethod = new NewInstanceMetaMethod(cachedMethod);
        addNewInstanceMethodToIndex(newMethod, metaMethodIndex.getHeader(newMethod.getDeclaringClass().getTheClass()));
    }

    private void addNewInstanceMethodToIndex(final MetaMethod newMethod, final Map<String, MetaMethodIndex.Cache> header) {
        if (!newGroovyMethodsSet.contains(newMethod)) {
            newGroovyMethodsSet.add(newMethod);
            addMetaMethodToIndex(newMethod, header);
        }
    }

    /**
     * Adds a static method to this metaclass.
     *
     * @param method The method to be added
     */
    @Override
    public void addNewStaticMethod(final Method method) {
        CachedMethod cachedMethod = CachedMethod.find(method);
        NewStaticMetaMethod newMethod = new NewStaticMetaMethod(cachedMethod);
        addNewStaticMethodToIndex(newMethod, metaMethodIndex.getHeader(newMethod.getDeclaringClass().getTheClass()));
    }

    private void addNewStaticMethodToIndex(final MetaMethod newMethod, final Map<String,MetaMethodIndex.Cache> header) {
        if (!newGroovyMethodsSet.contains(newMethod)) {
            newGroovyMethodsSet.add(newMethod);
            addMetaMethodToIndex(newMethod, header);
        }
    }

    /**
     * Invoke a method on the given object with the given arguments.
     *
     * @param object     The object the method should be invoked on.
     * @param methodName The name of the method to invoke.
     * @param arguments  The arguments to the invoked method as null, a Tuple, an array or a single argument of any type.
     * @return The result of the method invocation.
     */
    @Override
    public Object invokeMethod(final Object object, final String methodName, final Object arguments) {
        if (arguments == null) {
            return invokeMethod(object, methodName, EMPTY_ARGUMENTS);
        }
        if (arguments instanceof Tuple) {
            return invokeMethod(object, methodName, ((Tuple<?>) arguments).toArray());
        }
        if (arguments instanceof Object[]) {
            return invokeMethod(object, methodName, (Object[]) arguments);
        }
        return invokeMethod(object, methodName, new Object[]{arguments});
    }

    /**
     * Invoke a missing method on the given object with the given arguments.
     *
     * @param instance   The object the method should be invoked on.
     * @param methodName The name of the method to invoke.
     * @param arguments  The arguments to the invoked method.
     * @return The result of the method invocation.
     */
    @Override
    public Object invokeMissingMethod(final Object instance, final String methodName, final Object[] arguments) {
        return invokeMissingMethod(instance, methodName, arguments, null, false);
    }

    /**
     * Invoke a missing property on the given object with the given arguments.
     *
     * @param instance      The object the method should be invoked on.
     * @param propertyName  The name of the property to invoke.
     * @param optionalValue The (optional) new value for the property
     * @param isGetter      Whether the method is a getter
     * @return The result of the method invocation.
     */
    @Override
    public Object invokeMissingProperty(final Object instance, final String propertyName, final Object optionalValue, final boolean isGetter) {
        MetaBeanProperty property = findPropertyInClassHierarchy(propertyName, theCachedClass);
        if (property != null) {
            onSuperPropertyFoundInHierarchy(property);
            if (!isGetter) {
                property.setProperty(instance, optionalValue);
                return null;
            }
            return property.getProperty(instance);
        }

        // look for getProperty or setProperty overrides
        if (isGetter) {
            final Class<?>[] getPropertyArgs = {String.class};
            final MetaMethod method = findMethodInClassHierarchy(instance.getClass(), GET_PROPERTY_METHOD, getPropertyArgs, this);
            if (method instanceof ClosureMetaMethod) {
                onGetPropertyFoundInHierarchy(method);
                return method.invoke(instance, new Object[]{propertyName});
            }
        } else {
            final Class<?>[] setPropertyArgs = {String.class, Object.class};
            final MetaMethod method = findMethodInClassHierarchy(instance.getClass(), SET_PROPERTY_METHOD, setPropertyArgs, this);
            if (method instanceof ClosureMetaMethod) {
                onSetPropertyFoundInHierarchy(method);
                return method.invoke(instance, new Object[]{propertyName, optionalValue});
            }
        }

        try {
            if (!(instance instanceof Class)) {
                if (isGetter) {
                    if (propertyMissingGet != null) {
                        return propertyMissingGet.invoke(instance, new Object[]{propertyName});
                    }
                } else {
                    if (propertyMissingSet != null) {
                        return propertyMissingSet.invoke(instance, new Object[]{propertyName, optionalValue});
                    }
                }
            }
        } catch (InvokerInvocationException iie) {
            boolean shouldHandle = isGetter && propertyMissingGet != null;
            if (!shouldHandle) shouldHandle = !isGetter && propertyMissingSet != null;
            if (shouldHandle && iie.getCause() instanceof MissingPropertyException) {
                throw (MissingPropertyException) iie.getCause();
            }
            throw iie;
        }

        Class<?> theClass = instance instanceof Class ? (Class<?>) instance : instance.getClass();
        if (instance instanceof Class && theClass != Class.class) {
            final MetaProperty metaProperty = InvokerHelper.getMetaClass(Class.class).hasProperty(instance, propertyName);
            if (metaProperty != null) {
                if (isGetter) {
                    return metaProperty.getProperty(instance);
                }
                metaProperty.setProperty(instance, optionalValue);
                return null;
            }
        }

        throw new MissingPropertyExceptionNoStack(propertyName, theClass);
    }

    private Object invokeMissingMethod(final Object instance, final String methodName, final Object[] arguments, final RuntimeException original, final boolean isCallToSuper) {
        if (isCallToSuper) {
            MetaClass metaClass = InvokerHelper.getMetaClass(theClass.getSuperclass());
            return metaClass.invokeMissingMethod(instance, methodName, arguments);
        }

        Class<?> instanceKlazz = instance.getClass();
        if (theClass != instanceKlazz && theClass.isAssignableFrom(instanceKlazz))
            instanceKlazz = theClass;

        Class<?>[] argClasses = MetaClassHelper.convertToTypeArray(arguments);

        MetaMethod method = findMixinMethod(methodName, argClasses);
        if (method != null) {
            onMixinMethodFound(method);
            return method.invoke(instance, arguments);
        }

        method = findMethodInClassHierarchy(instanceKlazz, methodName, argClasses, this);
        if (method != null) {
            onSuperMethodFoundInHierarchy(method);
            return method.invoke(instance, arguments);
        }

        // still not method here, so see if there is an invokeMethod method up the hierarchy
        final Class<?>[] invokeMethodArgs = {String.class, Object[].class};
        method = findMethodInClassHierarchy(instanceKlazz, INVOKE_METHOD_METHOD, invokeMethodArgs, this);
        if (method instanceof ClosureMetaMethod) {
            onInvokeMethodFoundInHierarchy(method);
            return method.invoke(instance, invokeMethodArgs);
        }

        // last resort look in the category
        if (method == null && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            method = getCategoryMethodMissing(instanceKlazz);
            if (method != null) {
                return method.invoke(instance, new Object[]{methodName, arguments});
            }
        }

        if (methodMissing != null) {
            try {
                return methodMissing.invoke(instance, new Object[]{methodName, arguments});
            } catch (InvokerInvocationException iie) {
                if (methodMissing instanceof ClosureMetaMethod && iie.getCause() instanceof MissingMethodException) {
                    MissingMethodException mme = (MissingMethodException) iie.getCause();
                    throw new MissingMethodExecutionFailed(mme.getMethod(), mme.getClass(),
                            mme.getArguments(), mme.isStatic(), mme);
                }
                throw iie;
            } catch (MissingMethodException mme) {
                if (methodMissing instanceof ClosureMetaMethod) {
                    throw new MissingMethodExecutionFailed(mme.getMethod(), mme.getClass(),
                            mme.getArguments(), mme.isStatic(), mme);
                } else {
                    throw mme;
                }
            }
        } else if (original != null) {
            throw original;
        } else {
            throw new MissingMethodExceptionNoStack(methodName, theClass, arguments, false);
        }
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
     * @param isGetter      True if it's a getter
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
     * Invokes a method on the given receiver for the specified arguments.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * @param object     The object which the method was invoked on
     * @param methodName The name of the method
     * @param arguments  The arguments to the method
     * @return The return value of the method
     * @see MetaClass#invokeMethod(Class, Object, String, Object[], boolean, boolean)
     */
    @Override
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        return invokeMethod(theClass, object, methodName, arguments, false, false);
    }

    private Object invokeMethodClosure(final MethodClosure closure, final Object[] arguments) {
        var owner = closure.getOwner();
        var method = closure.getMethod();
        var ownerClass = closure.getOwnerClass();
        var ownerIsClass = (owner instanceof Class);
        var ownerMetaClass = registry.getMetaClass(ownerClass);
        try {
            return ownerMetaClass.invokeMethod(ownerClass, owner, method, arguments, false, false);
        } catch (GroovyRuntimeException e) { // GroovyRuntimeException(cause:IllegalArgumentException) thrown for final fields
                                             // InvokerInvocationException(cause:IllegalArgumentException) thrown for not this
            if (!ownerIsClass || !(e instanceof MissingMethodException || e.getCause() instanceof IllegalArgumentException)) {
                throw e;
            }
            if (MethodClosure.NEW.equals(method)) {
              // CONSTRUCTOR REFERENCE

                if (!ownerClass.isArray())
                    return ownerMetaClass.invokeConstructor(arguments);

                int nArguments = arguments.length;
                if (nArguments == 0) {
                    throw new GroovyRuntimeException("The arguments(specifying size) are required to create array[" + ownerClass.getCanonicalName() + "]");
                }
                int arrayDimension = ArrayTypeUtils.dimension(ownerClass);
                if (arrayDimension < nArguments) {
                    throw new GroovyRuntimeException("The length[" + nArguments + "] of arguments should not be greater than the dimensions[" + arrayDimension + "] of array[" + ownerClass.getCanonicalName() + "]");
                }
                var elementType = arrayDimension == nArguments
                        ? ArrayTypeUtils.elementType(ownerClass)
                        : ArrayTypeUtils.elementType(ownerClass, arrayDimension - nArguments);
                return Array.newInstance(elementType, Arrays.stream(arguments).mapToInt(argument ->
                    argument instanceof Integer ? (Integer) argument : Integer.parseInt(String.valueOf(argument))
                ).toArray());
            } else {
              // METHOD REFERENCE

                // if the owner is a class and the method closure can be related to some instance method(s)
                // try to invoke method with adjusted arguments -- first argument is instance of owner type
                if (arguments.length > 0 && ownerClass.isInstance(arguments[0])
                        && (Boolean) closure.getProperty(MethodClosure.ANY_INSTANCE_METHOD_EXISTS)) {
                    try {
                        Object newReceiver = arguments[0];
                        Object[] newArguments = Arrays.copyOfRange(arguments, 1, arguments.length);
                        return ownerMetaClass.invokeMethod(ownerClass, newReceiver, method, newArguments, false, false);
                    } catch (MissingMethodException ignore) {}
                }

                if (ownerClass != Class.class) { // maybe it's a reference to a Class method
                    try {
                        MetaClass cmc = registry.getMetaClass(Class.class);
                        return cmc.invokeMethod(Class.class, owner, method, arguments, false, false);
                    } catch (MissingMethodException ignore) {}
                }

                return invokeMissingMethod(closure, method, arguments);
            }
        }
    }

    /**
     * Invokes a method on the given receiver for the specified arguments. The sender is the class that invoked the method on the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     * <p>
     * The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
     * to the super class if necessary
     *
     * @param sender            The java.lang.Class instance that invoked the method
     * @param object            The object which the method was invoked on
     * @param methodName        The name of the method
     * @param originalArguments The arguments to the method
     * @param isCallToSuper     Whether the method is a call to a super class method
     * @param fromInsideClass   Whether the call was invoked from the inside or the outside of the class
     * @return The return value of the method.
     * @see MetaClass#invokeMethod(Class, Object, String, Object[], boolean, boolean)
     */
    @Override
    public Object invokeMethod(final Class sender, final Object object, final String methodName, final Object[] originalArguments, final boolean isCallToSuper, final boolean fromInsideClass) {
        checkInitalised();
        if (object == null) {
            throw new NullPointerException("Cannot invoke method: " + methodName + " on null object");
        }

        final Object[] arguments = originalArguments == null ? EMPTY_ARGUMENTS : originalArguments;

        MetaMethod method = getMetaMethod(sender, object, methodName, isCallToSuper, arguments);

        if (object instanceof Closure) {
            final Closure closure = (Closure)object;
            final Object owner = closure.getOwner();

            if (CALL_METHOD.equals(methodName) || DO_CALL_METHOD.equals(methodName)) {
                var closureClass = closure.getClass();
                if (closureClass == MethodClosure.class) {
                    return invokeMethodClosure((MethodClosure) closure, arguments);
                } else if (closureClass == CurriedClosure.class) {
                    MetaClass ownerMetaClass = registry.getMetaClass(owner instanceof Class ? (Class) owner : owner.getClass());
                    return ownerMetaClass.invokeMethod(owner, methodName, ((CurriedClosure) closure).getUncurriedArguments(arguments));
                }
                if (method == null) invokeMissingMethod(object, methodName, arguments);
            }

            final Object delegate = closure.getDelegate();
            final int resolveStrategy = closure.getResolveStrategy();

            final Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);

            switch (resolveStrategy) {
                case Closure.TO_SELF:
                    method = closure.getMetaClass().pickMethod(methodName, argClasses);
                    if (method != null) return method.invoke(closure, arguments);
                    break;

                case Closure.DELEGATE_ONLY:
                    if (method == null && delegate != null && delegate != closure) {
                        MetaClass delegateMetaClass = lookupObjectMetaClass(delegate);
                        method = delegateMetaClass.pickMethod(methodName, argClasses);
                        if (method != null) {
                            return delegateMetaClass.invokeMethod(delegate, methodName, originalArguments);
                        }
                        if (delegate instanceof GroovyObject) {
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
                    Tuple2<Object, MetaMethod> tuple = invokeMethod(method, delegate, closure, methodName, argClasses, originalArguments, owner);
                    Object result = tuple.getV1();
                    method = tuple.getV2();
                    if (InvokeMethodResult.NONE != result) {
                        return result;
                    }
                    if (method == null && resolveStrategy != Closure.TO_SELF) {
                        // still no methods found, test if delegate or owner are GroovyObjects
                        // and invoke the method on them if so.
                        MissingMethodException last = null;
                        if (delegate != closure && delegate instanceof GroovyObject) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, delegate);
                            } catch (MissingMethodException mme) {
                                if (last == null) last = mme;
                            }
                        }
                        if (owner != closure && owner instanceof GroovyObject) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, owner);
                            } catch (MissingMethodException mme) {
                                last = mme;
                            }
                        }
                        if (last != null)
                            return invokeMissingMethod(object, methodName, originalArguments, last, isCallToSuper);
                    }
                    break;

                default:
                    Tuple2<Object, MetaMethod> t = invokeMethod(method, delegate, closure, methodName, argClasses, originalArguments, owner);
                    Object r = t.getV1();
                    method = t.getV2();
                    if (InvokeMethodResult.NONE != r) {
                        return r;
                    }
                    if (method == null && resolveStrategy != Closure.TO_SELF) {
                        // still no methods found, if delegate or owner are GroovyObjects, invoke the method on them
                        MissingMethodException last = null;
                        if (owner != closure && owner instanceof GroovyObject) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, owner);
                            } catch (MissingMethodException mme) {
                                if (methodName.equals(mme.getMethod())) {
                                    if (last == null) last = mme;
                                } else {
                                    throw mme;
                                }
                            } catch (InvokerInvocationException iie) {
                                if (iie.getCause() instanceof MissingMethodException) {
                                    MissingMethodException mme = (MissingMethodException) iie.getCause();
                                    if (methodName.equals(mme.getMethod())) {
                                        if (last == null) last = mme;
                                    } else {
                                        throw iie;
                                    }
                                } else {
                                    throw iie;
                                }
                            }
                        }
                        if (delegate != closure && delegate instanceof GroovyObject) {
                            try {
                                return invokeMethodOnGroovyObject(methodName, originalArguments, delegate);
                            } catch (MissingMethodException mme) {
                                last = mme;
                            } catch (InvokerInvocationException iie) {
                                if (iie.getCause() instanceof MissingMethodException) {
                                    last = (MissingMethodException) iie.getCause();
                                } else {
                                    throw iie;
                                }
                            }
                        }
                        if (last != null)
                            return invokeMissingMethod(object, methodName, originalArguments, last, isCallToSuper);
                    }
            }
        }

        if (method != null) {
            if (arguments.length == 0 && "clone".equals(methodName) && method.getDeclaringClass() == ReflectionCache.OBJECT_CLASS) {
                throw method.processDoMethodInvokeException(new CloneNotSupportedException(), object, arguments);
            }
            MetaMethod transformedMetaMethod = VM_PLUGIN.transformMetaMethod(this, method);
            return transformedMetaMethod.doMethodInvoke(object, arguments);
        }

        return invokePropertyOrMissing(object, methodName, originalArguments, fromInsideClass, isCallToSuper);
    }

    private MetaMethod getMetaMethod(Class sender, Object object, String methodName, boolean isCallToSuper, Object... arguments) {
        MetaMethod method = null;
        if (CALL_METHOD.equals(methodName) && object instanceof GeneratedClosure) {
            method = getMethodWithCaching(sender, DO_CALL_METHOD, arguments, isCallToSuper);
        }
        if (method == null) {
            method = getMethodWithCaching(sender, methodName, arguments, isCallToSuper);
        }
        MetaClassHelper.unwrap(arguments);

        if (method == null)
            method = tryListParamMetaMethod(sender, methodName, isCallToSuper, arguments);
        return method;
    }

    private MetaMethod tryListParamMetaMethod(Class sender, String methodName, boolean isCallToSuper, Object[] arguments) {
        MetaMethod method = null;
        if (arguments.length == 1 && arguments[0] instanceof List) {
            Object[] newArguments = ((List) arguments[0]).toArray();
            method = createTransformMetaMethod(getMethodWithCaching(sender, methodName, newArguments, isCallToSuper));
        }
        return method;
    }

    protected MetaMethod createTransformMetaMethod(MetaMethod method) {
        if (method == null) {
            return null;
        }

        return new TransformMetaMethod(method) {
            @Override
            public Object invoke(Object object, Object[] arguments) {
                Object firstArgument = arguments[0];
                List list = (List) firstArgument;
                arguments = list.toArray();
                return super.invoke(object, arguments);
            }
        };
    }

    /**
     * Tries to find a callable property and make the call.
     */
    private Object invokePropertyOrMissing(final Object object, final String methodName, final Object[] originalArguments, final boolean fromInsideClass, final boolean isCallToSuper) {
        MetaProperty metaProperty = this.getMetaProperty(methodName, false);

        Object value = null;
        if (metaProperty != null) {
            value = metaProperty.getProperty(object);
        } else if (object instanceof Map) {
            value = ((Map<?, ?>) object).get(methodName);
        } else if (object instanceof Script) {
            value = ((Script) object).getBinding().getVariables().get(methodName);
        }

        if (value instanceof Closure) {
            Closure<?> closure = (Closure<?>) value;
            MetaClass metaClass = closure.getMetaClass();
            try {
                return metaClass.invokeMethod(closure.getClass(), closure, DO_CALL_METHOD, originalArguments, false, fromInsideClass);
            } catch (MissingMethodException mme) {
                // fall through -- "doCall" is not intrinsic to Closure
            }
        }

        if (value != null && !(value instanceof Map) && !methodName.equals(CALL_METHOD)) {
            try {
                MetaClass metaClass = ((MetaClassRegistryImpl) registry).getMetaClass(value);
                return metaClass.invokeMethod(value, CALL_METHOD, originalArguments); // delegate to call method of property value
            } catch (MissingMethodException mme) {
                // ignore
            }
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

    private static Object invokeMethodOnGroovyObject(String methodName, Object[] originalArguments, Object owner) {
        GroovyObject go = (GroovyObject) owner;
        return go.invokeMethod(methodName, originalArguments);
    }

    public MetaMethod getMethodWithCaching(final Class sender, final String methodName, final Object[] arguments, final boolean isCallToSuper) {
        if (!isCallToSuper && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            return getMethodWithoutCaching(sender, methodName, MetaClassHelper.convertToTypeArray(arguments), isCallToSuper);
        }
        var e = metaMethodIndex.getMethods(sender, methodName);
        if (e == null ? (sender == theClass && !sender.isEnum() && (sender.getModifiers() & Opcodes.ACC_ENUM) != 0) // GROOVY-9523: private
                      : (isCallToSuper && e.methodsForSuper == null)) { // allow "super.name()" to find DGM if class declares method "name"
            e = metaMethodIndex.getMethods(sender.getSuperclass(), methodName);
        }
        if (e == null) {
            return null;
        } else if (isCallToSuper) {
            return getSuperMethodWithCaching(arguments, e);
        } else {
            return getNormalMethodWithCaching(arguments, e);
        }
    }

    private static boolean sameClasses(final Class[] params, final Class[] arguments) {
        // we do here a null check because the params field might not have been set yet
        if (params == null || params.length != arguments.length)
            return false;

        for (int i = params.length - 1; i >= 0; i -= 1) {
            Object arg = arguments[i];
            if (arg != null) {
                if (params[i] != arguments[i]) return false;
            } else {
                return false;
            }
        }

        return true;
    }

    // This method should be called by CallSite only
    private MetaMethod getMethodWithCachingInternal(final Class sender, final CallSite site, final Class<?>[] params) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread())
            return getMethodWithoutCaching(sender, site.getName(), params, false);

        MetaMethodIndex.Cache e = metaMethodIndex.getMethods(sender, site.getName());
        if (e == null || e.methods == null)
            return null;

        MetaMethodIndex.MetaMethodCache cacheEntry = e.cachedMethod;
        if (cacheEntry != null && (sameClasses(cacheEntry.params, params))) {
            return cacheEntry.method;
        }

        cacheEntry = e.cachedMethod = new MetaMethodIndex.MetaMethodCache(params, (MetaMethod) chooseMethod(e.name, e.methods, params));

        return cacheEntry.method;
    }

    private MetaMethod getSuperMethodWithCaching(final Object[] arguments, final MetaMethodIndex.Cache e) {
        if (e.methodsForSuper == null)
            return null;

        MetaMethodIndex.MetaMethodCache cacheEntry = e.cachedMethodForSuper;

        if (cacheEntry != null && cacheEntry.method != null
                && MetaClassHelper.sameClasses(cacheEntry.params, arguments, e.methodsForSuper instanceof MetaMethod)) {
            return cacheEntry.method;
        }

        Class<?>[] types = MetaClassHelper.convertToTypeArray(arguments);
        MetaMethod method = (MetaMethod) chooseMethod(e.name, e.methodsForSuper, types);
        cacheEntry = e.cachedMethodForSuper = new MetaMethodIndex.MetaMethodCache(types, method.isAbstract() ? null : method);

        return cacheEntry.method;
    }

    private MetaMethod getNormalMethodWithCaching(final Object[] arguments, final MetaMethodIndex.Cache e) {
        if (e.methods == null)
            return null;

        MetaMethodIndex.MetaMethodCache cacheEntry = e.cachedMethod;

        if (cacheEntry != null && cacheEntry.method != null
                && MetaClassHelper.sameClasses(cacheEntry.params, arguments, e.methods instanceof MetaMethod)) {
            return cacheEntry.method;
        }

        Class<?>[] types = MetaClassHelper.convertToTypeArray(arguments);
        MetaMethod method = (MetaMethod) chooseMethod(e.name, e.methods, types);
        cacheEntry = e.cachedMethod = new MetaMethodIndex.MetaMethodCache(types, method);

        return cacheEntry.method;
    }

    public Constructor retrieveConstructor(Class[] arguments) {
        CachedConstructor constructor = (CachedConstructor) chooseMethod(CONSTRUCTOR_NAME, constructors, arguments);
        if (constructor != null) {
            return constructor.getCachedConstructor();
        }
        return null;
    }

    public MetaMethod retrieveStaticMethod(String methodName, Object[] arguments) {
        final MetaMethodIndex.Cache e = metaMethodIndex.getMethods(theClass, methodName);
        MetaMethodIndex.MetaMethodCache cacheEntry;
        if (e != null) {
            cacheEntry = e.cachedStaticMethod;

            if (cacheEntry != null &&
                    MetaClassHelper.sameClasses(cacheEntry.params, arguments, e.staticMethods instanceof MetaMethod)) {
                return cacheEntry.method;
            }

            final Class[] classes = MetaClassHelper.convertToTypeArray(arguments);
            cacheEntry = new MetaMethodIndex.MetaMethodCache(classes, pickStaticMethod(methodName, classes));

            e.cachedStaticMethod = cacheEntry;

            return cacheEntry.method;
        }
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

    @Override
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

        MetaMethod method = retrieveStaticMethod(methodName, arguments);
        // let's try to use the cache to find the method

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

        Object[] originalArguments = arguments.clone();
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

        if (prop != null) {
            MetaClass propMC = registry.getMetaClass(prop.getClass());
            return propMC.invokeMethod(prop, CALL_METHOD, arguments);
        }

        return invokeStaticMissingMethod(sender, methodName, arguments);
    }

    private static Object invokeStaticClosureProperty(Object[] originalArguments, Object prop) {
        Closure closure = (Closure) prop;
        MetaClass delegateMetaClass = closure.getMetaClass();
        return delegateMetaClass.invokeMethod(closure.getClass(), closure, DO_CALL_METHOD, originalArguments, false, false);
    }

    private Object invokeStaticMissingMethod(Class sender, String methodName, Object[] arguments) {
        MetaMethod metaMethod = getStaticMetaMethod(STATIC_METHOD_MISSING, METHOD_MISSING_ARGS);
        if (metaMethod != null) {
            return metaMethod.invoke(sender, new Object[]{methodName, arguments});
        }
        throw new MissingMethodException(methodName, sender, arguments, true);
    }

    private MetaMethod pickStaticMethod(String methodName, Class[] arguments) throws MethodSelectionException {
        MetaMethod method = null;
        MethodSelectionException mse = null;
        Object methods = getStaticMethods(theClass, methodName);

        if (!(methods instanceof FastArray) || !((FastArray) methods).isEmpty()) {
            try {
                method = (MetaMethod) chooseMethod(methodName, methods, arguments);
            } catch (MethodSelectionException msex) {
                mse = msex;
            }
        }
        if (method == null && theClass != Class.class) {
            MetaClass cmc = registry.getMetaClass(Class.class);
            method = cmc.pickMethod(methodName, arguments);
        }
        if (method == null) {
            method = (MetaMethod) chooseMethod(methodName, methods, arguments);
        }

        if (method == null && mse != null) {
            throw mse;
        } else {
            return method;
        }
    }

    @Override
    public Object invokeConstructor(Object[] arguments) {
        return invokeConstructor(theClass, arguments);
    }

    @Override
    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        if (numberOfConstructors == -1) {
            return selectConstructorAndTransformArguments1(arguments);
        }
        // falling back to pre 2.1.9 selection algorithm
        // in practice this branch will only be reached if the class calling this code is a Groovy class
        // compiled with an earlier version of the Groovy compiler
        return selectConstructorAndTransformArguments0(numberOfConstructors, arguments);
    }

    private int selectConstructorAndTransformArguments0(final int numberOfConstructors, Object[] arguments) {
        //TODO: that is just a quick prototype, not the real thing!
        if (numberOfConstructors != constructors.size()) {
            throw new IncompatibleClassChangeError("the number of constructors during runtime and compile time for " +
                    this.theClass.getName() + " do not match. Expected " + numberOfConstructors + " but got " + constructors.size());
        }

        CachedConstructor constructor = createCachedConstructor(arguments);
        List<CachedConstructor> list = new ArrayList(constructors.toList());
        list.sort((Comparator<CachedConstructor>) (cc0, cc1) -> {
            String descriptor0 = BytecodeHelper.getMethodDescriptor(Void.TYPE, cc0.getNativeParameterTypes());
            String descriptor1 = BytecodeHelper.getMethodDescriptor(Void.TYPE, cc1.getNativeParameterTypes());
            return descriptor0.compareTo(descriptor1);
        });
        int found = -1;
        for (int i = 0, n = list.size(); i < n; i++) {
            if (list.get(i) != constructor) continue;
            found = i;
            break;
        }
        // NOTE: must be changed to "1 |" if constructor was vargs
        return (found << 8);
    }

    private CachedConstructor createCachedConstructor(Object[] arguments) {
        if (arguments == null) arguments = EMPTY_ARGUMENTS;
        Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);
        MetaClassHelper.unwrap(arguments);
        CachedConstructor constructor = (CachedConstructor) chooseMethod(CONSTRUCTOR_NAME, constructors, argClasses);
        if (constructor != null) {
            return constructor;
        }
        throw new GroovyRuntimeException(
            "Could not find matching constructor for: "
                + theClass.getName()
                + "(" + FormatHelper.toTypeString(arguments) + ")");
    }

    /**
     * Constructor selection algorithm for Groovy 2.1.9+.
     * This selection algorithm was introduced as a workaround for GROOVY-6080. Instead of generating an index between
     * 0 and N where N is the number of super constructors at the time the class is compiled, this algorithm uses
     * a hash of the constructor descriptor instead.
     * <p>
     * This has the advantage of letting the super class add new constructors while being binary compatible. But there
     * are still problems with this approach:
     * <ul>
     *     <li>There's a risk of hash collision, even if it's very low (two constructors of the same class must have the same hash)</li>
     *     <li>If the super class adds a new constructor which takes as an argument a superclass of an existing constructor parameter and
     *     that this new constructor is selected at runtime, it would not find it.</li>
     * </ul>
     * <p>
     * Hopefully in the last case, the error message is much nicer now since it explains that it's a binary incompatible change.
     *
     * @param arguments the actual constructor call arguments
     * @return a hash used to identify the constructor to be called
     * @since 2.1.9
     */
    private int selectConstructorAndTransformArguments1(Object[] arguments) {
        CachedConstructor constructor = createCachedConstructor(arguments);
        final String methodDescriptor = BytecodeHelper.getMethodDescriptor(Void.TYPE, constructor.getNativeParameterTypes());
        // keeping 3 bits for additional information such as vargs
        return BytecodeHelper.hashCode(methodDescriptor);
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

    /**
     * This is a helper class which is used only by indy. It is for internal use.
     *
     * @since 2.1.0
     */
    @groovy.transform.Internal
    public static final class MetaConstructor extends MetaMethod {
        private final CachedConstructor cc;
        private final boolean beanConstructor;

        private MetaConstructor(CachedConstructor cc, boolean bean) {
            super(cc.getNativeParameterTypes());
            this.setParametersTypes(cc.getParameterTypes());
            this.cc = cc;
            this.beanConstructor = bean;
        }

        @Override
        public int getModifiers() {
            return cc.getModifiers();
        }

        @Override
        public String getName() {
            return CONSTRUCTOR_NAME;
        }

        @Override
        public Class getReturnType() {
            return cc.getCachedClass().getTheClass();
        }

        @Override
        public CachedClass getDeclaringClass() {
            return cc.getCachedClass();
        }

        @Override
        public Object invoke(Object object, Object[] arguments) {
            return cc.doConstructorInvoke(arguments);
        }

        public CachedConstructor getCachedConstrcutor() {
            return cc;
        }

        public boolean isBeanConstructor() {
            return beanConstructor;
        }
    }

    /**
     * This is a helper method which is used only by indy. It is for internal use.
     *
     * @since 2.1.0
     */
    @groovy.transform.Internal
    public MetaMethod retrieveConstructor(Object[] arguments) {
        checkInitalised();
        if (arguments == null) arguments = EMPTY_ARGUMENTS;
        Class[] argTypes = MetaClassHelper.convertToTypeArray(arguments);
        MetaClassHelper.unwrap(arguments);
        Object res = chooseMethod(CONSTRUCTOR_NAME, constructors, argTypes);
        if (res instanceof MetaMethod) return (MetaMethod) res;
        CachedConstructor constructor = (CachedConstructor) res;
        if (constructor != null) return new MetaConstructor(constructor, false);
        // handle named args on class or inner class (one level only for now)
        if ((arguments.length == 1 && arguments[0] instanceof Map) ||
                (arguments.length == 2 && arguments[1] instanceof Map &&
                        theClass.getEnclosingClass() != null &&
                        theClass.getEnclosingClass().isAssignableFrom(argTypes[0]))) {
            res = retrieveNamedArgCompatibleConstructor(argTypes, arguments);
        }
        if (res instanceof MetaMethod) return (MetaMethod) res;
        constructor = (CachedConstructor) res;
        if (constructor != null) return new MetaConstructor(constructor, true);

        return null;
    }

    private Object retrieveNamedArgCompatibleConstructor(Class[] origArgTypes, Object[] origArgs) {
        // if we get here Map variant already not found so allow for no-arg plus setters
        Class[] argTypes = Arrays.copyOf(origArgTypes, origArgTypes.length - 1);
        Object[] args = Arrays.copyOf(origArgs, origArgs.length - 1);
        Object res = chooseMethod(CONSTRUCTOR_NAME, constructors, argTypes);
        // chooseMethod allows fuzzy matching implicit null case but we don't want that here
        // code here handles inner class case but we currently don't do fuzzy matching for inner classes
        if (res instanceof ParameterTypes && ((ParameterTypes) res).getParameterTypes().length == origArgTypes.length) {
            String prettyOrigArgs = FormatHelper.toTypeString(origArgs);
            if (prettyOrigArgs.endsWith("LinkedHashMap")) {
                prettyOrigArgs = prettyOrigArgs.replaceFirst("LinkedHashMap$", "Map");
            }
            throw new GroovyRuntimeException(
                    "Could not find named-arg compatible constructor. Expecting one of:\n"
                            + theClass.getName() + "(" + prettyOrigArgs + ")\n"
                            + theClass.getName() + "(" + FormatHelper.toTypeString(args) + ")"
            );
        }
        return res;
    }

    private Object invokeConstructor(Class at, Object[] arguments) {
        checkInitalised();
        if (arguments == null) arguments = EMPTY_ARGUMENTS;
        Class[] argTypes = MetaClassHelper.convertToTypeArray(arguments);
        MetaClassHelper.unwrap(arguments);
        CachedConstructor constructor = (CachedConstructor) chooseMethod(CONSTRUCTOR_NAME, constructors, argTypes);
        if (constructor != null) {
            return constructor.doConstructorInvoke(arguments);
        }
        // handle named args on class or inner class (one level only for now)
        if ((arguments.length == 1 && arguments[0] instanceof Map) ||
                (arguments.length == 2 && arguments[1] instanceof Map &&
                        theClass.getEnclosingClass() != null &&
                        theClass.getEnclosingClass().isAssignableFrom(argTypes[0]))) {
            constructor = (CachedConstructor)  retrieveNamedArgCompatibleConstructor(argTypes, arguments);
            if (constructor != null) {
                Object[] args = Arrays.copyOf(arguments, arguments.length - 1);
                Object bean = constructor.doConstructorInvoke(args);
                setProperties(bean, ((Map) arguments[arguments.length - 1]));
                return bean;
            }
        }

        throw new GroovyRuntimeException(
                "Could not find matching constructor for: "
                        + theClass.getName()
                        + "(" + FormatHelper.toTypeString(arguments) + ")");
    }

    /**
     * Sets a number of bean properties from the given Map where the keys are
     * the String names of properties and the values are the values of the
     * properties to set
     */
    public void setProperties(Object bean, Map map) {
        checkInitalised();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = entry.getKey().toString();

            Object value = entry.getValue();
            setProperty(bean, key, value);
        }
    }

    /**
     * @return the given property's value on the object
     */
    @Override
    public Object getProperty(final Class sender, final Object object, final String name, final boolean useSuper, final boolean fromInsideClass) {

        //----------------------------------------------------------------------
        // handling of static
        //----------------------------------------------------------------------
        boolean isStatic = (theClass != Class.class && object instanceof Class);
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class<?>) object);
            return mc.getProperty(sender, object, name, useSuper, false);
        }

        checkInitalised();

        //----------------------------------------------------------------------
        // getter
        //----------------------------------------------------------------------
        Tuple2<MetaMethod, MetaProperty> methodAndProperty = createMetaMethodAndMetaProperty(sender, name, useSuper, isStatic);
        MetaMethod method = methodAndProperty.getV1();

        if (method == null || isSpecialProperty(name)) {
            //------------------------------------------------------------------
            // public field
            //------------------------------------------------------------------
            MetaProperty mp = methodAndProperty.getV2();
            if (mp != null && mp.isPublic()) {
                try {
                    return mp.getProperty(object);
                } catch (GroovyRuntimeException e) {
                    // can't access the field directly but there may be a getter
                    mp = null;
                }
            }

            //------------------------------------------------------------------
            // java.util.Map get method
            //------------------------------------------------------------------
            if (isMap && !isStatic) {
                return ((Map<?,?>) object).get(name);
            }

            //------------------------------------------------------------------
            // non-public field
            //------------------------------------------------------------------
            if (mp != null) {
                try {
                    return mp.getProperty(object);
                } catch (GroovyRuntimeException e) {
                }
            }
        }

        //----------------------------------------------------------------------
        // java.util.Map get method before non-public getter -- see GROOVY-11367
        //----------------------------------------------------------------------
        if (isMap && !isStatic && !method.isPublic()) {
            return ((Map<?,?>) object).get(name);
        }

        //----------------------------------------------------------------------
        // propertyMissing (via category) or generic get method
        //----------------------------------------------------------------------
        Object[] arguments = EMPTY_ARGUMENTS;
        if (method == null && !useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            // check for propertyMissing provided through a category; TODO:should this have lower precedence?
            method = getCategoryMethodGetter(theClass, PROPERTY_MISSING, true);
            if (method == null) {
                // check for a generic get method provided through a category
                method = getCategoryMethodGetter(theClass, "get", true);
            }
            if (method != null) arguments = new Object[]{name};
        }
        if (method == null && genericGetMethod != null && (genericGetMethod.isStatic() || !isStatic)) {
            arguments = new Object[]{name};
            method = genericGetMethod;
        }

        if (method != null) {
            //------------------------------------------------------------------
            // executing the method
            //------------------------------------------------------------------
            MetaMethod metaMethod = VM_PLUGIN.transformMetaMethod(this, method);
            return metaMethod.doMethodInvoke(object, arguments);
        } else {
            //------------------------------------------------------------------
            // special cases -- maybe these cases should be special MetaClasses!
            //------------------------------------------------------------------
            if (isStatic) {
                MetaClass cmc = registry.getMetaClass(Class.class);
                return cmc.getProperty(Class.class, object, name, false, false);
            }
            if (object instanceof Collection) {
                return DefaultGroovyMethods.getAt((Collection<?>) object, name);
            }
            if (object instanceof Object[]) {
                return DefaultGroovyMethods.getAt(Arrays.asList((Object[]) object), name);
            }
            MetaMethod addListenerMethod = listeners.get(name);
            if (addListenerMethod != null) {
                // TODO: one day we could try return the previously registered Closure listener for easy removal
                return null;
            }
        }

        //----------------------------------------------------------------------
        // missing property protocol
        //----------------------------------------------------------------------
        if (object instanceof Class) {
            return invokeStaticMissingProperty(object, name, null, true);
        }
        return invokeMissingProperty(object, name, null, true);
    }

    public MetaProperty getEffectiveGetMetaProperty(final Class sender, final Object object, String name, final boolean useSuper) {

        //----------------------------------------------------------------------
        // handling of static
        //----------------------------------------------------------------------
        boolean isStatic = (theClass != Class.class && object instanceof Class);
        if (isStatic && object != theClass) {
            return new MetaProperty(name, Object.class) {
                @Override
                public Object getProperty(Object object) {
                    MetaClass mc = registry.getMetaClass((Class<?>) object);
                    return mc.getProperty(sender, object, name, useSuper, false);
                }

                @Override
                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        checkInitalised();

        //----------------------------------------------------------------------
        // getter
        //----------------------------------------------------------------------
        Tuple2<MetaMethod, MetaProperty> methodAndProperty = createMetaMethodAndMetaProperty(sender, name, useSuper, isStatic);
        MetaMethod method = methodAndProperty.getV1();

        if (method == null || isSpecialProperty(name)) {
            //------------------------------------------------------------------
            // public field
            //------------------------------------------------------------------
            MetaProperty mp = methodAndProperty.getV2();
            if (mp != null && mp.isPublic()) {
                return mp;
            }

            //------------------------------------------------------------------
            // java.util.Map get method
            //------------------------------------------------------------------
            if (isMap && !isStatic) {
                return new MetaProperty(name, Object.class) {
                    @Override
                    public Object getProperty(Object object) {
                        return ((Map<?,?>) object).get(name);
                    }

                    @Override
                    public void setProperty(Object object, Object newValue) {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            //------------------------------------------------------------------
            // non-public field
            //------------------------------------------------------------------
            if (mp != null) {
                return mp;
            }
        }

        //----------------------------------------------------------------------
        // java.util.Map get method before non-public getter -- see GROOVY-11367
        //----------------------------------------------------------------------
        if (isMap && !isStatic && !method.isPublic()) {
            return new MetaProperty(name, Object.class) {
                @Override
                public Object getProperty(Object object) {
                    return ((Map<?,?>) object).get(name);
                }

                @Override
                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        if (method != null) {
            return new GetBeanMethodMetaProperty(name, VM_PLUGIN.transformMetaMethod(this, method));
        }

        //----------------------------------------------------------------------
        // generic get method
        //----------------------------------------------------------------------
        if (!useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            method = getCategoryMethodGetter(theClass, "get", true);
            if (null == method) {
                method = getCategoryMethodGetter(theClass, PROPERTY_MISSING, true);
            }
            if (method != null) {
                return new GetMethodMetaProperty(name, VM_PLUGIN.transformMetaMethod(this, method));
            }
        }
        if (genericGetMethod != null && (genericGetMethod.isStatic() || !isStatic)) {
            return new GetMethodMetaProperty(name, VM_PLUGIN.transformMetaMethod(this, genericGetMethod));
        }

        //----------------------------------------------------------------------
        // special cases
        //----------------------------------------------------------------------
        if (theClass != Class.class && object instanceof Class) {
            return new MetaProperty(name, Object.class) {
                @Override
                public Object getProperty(Object object) {
                    MetaClass cmc = registry.getMetaClass(Class.class);
                    return cmc.getProperty(Class.class, object, name, false, false);
                }

                @Override
                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        if (object instanceof Collection) {
            return new MetaProperty(name, Object.class) {
                @Override
                public Object getProperty(Object object) {
                    return DefaultGroovyMethods.getAt((Collection<?>) object, name);
                }

                @Override
                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        if (object instanceof Object[]) {
            return new MetaProperty(name, Object.class) {
                @Override
                public Object getProperty(Object object) {
                    return DefaultGroovyMethods.getAt(Arrays.asList((Object[]) object), name);
                }

                @Override
                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        MetaMethod addListenerMethod = listeners.get(name);
        if (addListenerMethod != null) {
            return new MetaProperty(name, Object.class) {
                @Override
                public Object getProperty(Object object) {
                    return null;
                }

                @Override
                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        //----------------------------------------------------------------------
        // error due to missing method/field
        //----------------------------------------------------------------------
        if (isStatic || object instanceof Class) {
            return new MetaProperty(name, Object.class) {
                @Override
                public Object getProperty(Object object) {
                    return invokeStaticMissingProperty(object, name, null, true);
                }

                @Override
                public void setProperty(Object object, Object newValue) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return new MetaProperty(name, Object.class) {
            @Override
            public Object getProperty(Object object) {
                return invokeMissingProperty(object, name, null, true);
            }

            @Override
            public void setProperty(Object object, Object newValue) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Object#getClass and Map#isEmpty
     */
    private boolean isSpecialProperty(final String name) {
        return "class".equals(name) || (isMap && ("empty".equals(name)));
    }

    private Tuple2<MetaMethod, MetaProperty> createMetaMethodAndMetaProperty(final Class sender, final String name, final boolean useSuper, final boolean isStatic) {
        MetaMethod method = null;
        MetaProperty mp = getMetaProperty(sender, name, useSuper, isStatic);
        if ((mp == null || mp instanceof CachedField) && !name.isEmpty() && isUpperCase(name.charAt(0)) && (name.length() < 2 || !isUpperCase(name.charAt(1))) && !"Class".equals(name) && !"MetaClass".equals(name)) {
            // GROOVY-9618 adjust because capitalised properties aren't stored as meta bean props
            MetaProperty saved = mp;
            mp = getMetaProperty(sender, BeanUtils.decapitalize(name), useSuper, isStatic);
            if (mp == null || (saved != null && mp instanceof CachedField)) {
                // restore if we didn't find something better
                mp = saved;
            }
        }

        if (mp instanceof MetaBeanProperty) {
            MetaBeanProperty mbp = (MetaBeanProperty) mp;
            method = mbp.getGetter();
            mp = mbp.getField();
        }

        // check for a category method named like a getter
        if (!useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            String getterName = GroovyCategorySupport.getPropertyCategoryGetterName(name);
            if (getterName != null) {
                MetaMethod categoryMethod = getCategoryMethodGetter(theClass, getterName, false);
                if (categoryMethod != null)
                    method = categoryMethod;
            }
        }

        return tuple(method, mp);
    }

    private static CategoryMethod getCategoryMethodMissing(final Class<?> sender) {
        return findCategoryMethod(METHOD_MISSING, sender, params ->
            params.length == 2 && params[0].getTheClass() == String.class
        );
    }

    private static CategoryMethod getCategoryMethodGetter(final Class<?> sender, final String name, final boolean useLongVersion) {
        return findCategoryMethod(name, sender, params ->
            useLongVersion ? params.length == 1 && params[0].getTheClass() == String.class : params.length == 0
        );
    }

    private static CategoryMethod getCategoryMethodSetter(final Class<?> sender, final String name, final boolean useLongVersion) {
        return findCategoryMethod(name, sender, params ->
            useLongVersion ? params.length == 2 && params[0].getTheClass() == String.class : params.length == 1
        );
    }

    private static CategoryMethod findCategoryMethod(final String name, final Class<?> sender, final java.util.function.Predicate<CachedClass[]> paramFilter) {
        List<CategoryMethod> categoryMethods = GroovyCategorySupport.getCategoryMethods(name);
        if (categoryMethods != null) {
            List<CategoryMethod> choices = new ArrayList<>();
            for (CategoryMethod categoryMethod : categoryMethods) {
                if (categoryMethod.getOwnerClass().isAssignableFrom(sender)
                        && paramFilter.test(categoryMethod.getParameterTypes())) {
                    choices.add(categoryMethod);
                }
            }
            if (!choices.isEmpty()) {
                if (choices.size() > 1) { // GROOVY-5453, GROOVY-10214: order by self-type distance
                    choices.sort(Comparator.comparingLong(m -> MetaClassHelper.calculateParameterDistance(
                            new Class[]{sender}, new ParameterTypes(new CachedClass[]{m.getOwnerClass()}))));
                }
                return choices.get(0);
            }
        }
        return null;
    }

    /**
     * Returns the available properties for this type.
     *
     * @return a list of {@code MetaProperty} objects
     */
    @Override
    public List<MetaProperty> getProperties() {
        checkInitalised();
        Map<String, MetaProperty> propertyMap = classPropertyIndex.get(theCachedClass);
        if (propertyMap == null) {
            // GROOVY-6903: May happen in some special environment, like Android, due to class-loading issues
            propertyMap = Collections.emptyMap();
        }
        // simply return the values of the metaproperty map as a List
        List<MetaProperty> ret = new ArrayList<>(propertyMap.size());
        for (MetaProperty mp : propertyMap.values()) {
            if (mp instanceof CachedField) {
                if (mp.isSynthetic()
                        // GROOVY-5169, GROOVY-9081, GROOVY-9103, GROOVY-10438, GROOVY-10555, et al.
                        || (!permissivePropertyAccess && !checkAccessible(getClass(), ((CachedField) mp).getDeclaringClass(), mp.getModifiers(), false))) {
                    continue;
                }
            } else if (mp instanceof MetaBeanProperty) {
                MetaBeanProperty mbp = (MetaBeanProperty) mp;
                // filter out extrinsic properties (DGM, ...)
                boolean getter = true, setter = true;
                MetaMethod getterMetaMethod = mbp.getGetter();
                if (getterMetaMethod == null || getterMetaMethod instanceof GeneratedMetaMethod || getterMetaMethod instanceof NewInstanceMetaMethod) {
                    getter = false;
                }
                MetaMethod setterMetaMethod = mbp.getSetter();
                if (setterMetaMethod == null || setterMetaMethod instanceof GeneratedMetaMethod || setterMetaMethod instanceof NewInstanceMetaMethod) {
                    setter = false;
                }
                if (!getter && !setter) continue;

                if (isMap && isSpecialProperty(mp.getName())) continue;

                if (!permissivePropertyAccess) { // GROOVY-5169, GROOVY-9081, GROOVY-9103
                    boolean getterAccessible = canAccessLegally(getterMetaMethod);
                    boolean setterAccessible = canAccessLegally(setterMetaMethod);
                    if (!(getterAccessible && setterAccessible)) continue;
                }
            }

            ret.add(mp);
        }
        return ret;
    }

    private static boolean canAccessLegally(final MetaMethod method) {
        return !(method instanceof CachedMethod)
            || ((CachedMethod) method).canAccessLegally(MetaClassImpl.class);
    }

    /**
     * return null if nothing valid has been found, a MetaMethod (for getter always the case if not null) or
     * a LinkedList&lt;MetaMethod&gt; if there are multiple setter
     */
    private static Object filterPropertyMethod(Object methodOrList, boolean isGetter, boolean booleanGetter) {
        // Method has been optimized to reach a target of 325 bytecode size, making it JIT'able
        Object ret = null;

        if (methodOrList instanceof MetaMethod) {
            MetaMethod method = (MetaMethod) methodOrList;
            int parameterCount = method.getParameterTypes().length;
            Class<?> returnType = method.getReturnType();
            if (!isGetter && parameterCount == 1
                    //&& returnType == Void.TYPE
            ) {
                ret = method;
            }
            if (isGetter && parameterCount == 0 && (booleanGetter ? returnType == Boolean.TYPE
                    : returnType != Void.TYPE && returnType != Void.class)) {
                ret = method;
            }
        } else if (methodOrList instanceof FastArray) {
            FastArray methods = (FastArray) methodOrList;
            final int n = methods.size();
            final Object[] data = methods.getArray();
            for (int i = 0; i < n; i += 1) {
                MetaMethod element = (MetaMethod) data[i];
                int parameterCount = element.getParameterTypes().length;
                Class<?> returnType = element.getReturnType();
                if (!isGetter && parameterCount == 1
                        //&& returnType == Void.TYPE
                ) {
                    ret = addElementToList(ret, element);
                }
                if (isGetter && parameterCount == 0 && (booleanGetter ? returnType == Boolean.TYPE
                        : returnType != Void.TYPE && returnType != Void.class)) {
                    ret = addElementToList(ret, element);
                }
            }
        }

        if (ret == null
                || (ret instanceof MetaMethod)
                || !isGetter) {
            return ret;
        }

        // we found multiple matching methods
        // this is a problem, because we can use only one
        // if it is a getter, then use the most general return
        // type to decide which method to use. If it is a setter
        // we use the type of the first parameter
        MetaMethod method = null;
        int distance = -1;
        for (final Object o : ((List) ret)) {
            MetaMethod element = (MetaMethod) o;
            int localDistance = distanceToObject(element.getReturnType());
            //TODO: maybe implement the case localDistance==distance
            if (distance == -1 || distance > localDistance) {
                distance = localDistance;
                method = element;
            }
        }
        return method;
    }

    private static Object addElementToList(Object ret, MetaMethod element) {
        if (ret == null) {
            ret = element;
        } else if (ret instanceof List) {
            ((List) ret).add(element);
        } else {
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
     *
     * @param propertyDescriptors the property descriptors
     */
    private void setUpProperties(final PropertyDescriptor[] propertyDescriptors) {
        if (theCachedClass.isInterface) {
            CachedClass superClass = ReflectionCache.OBJECT_CLASS;
            List<CachedClass> superInterfaces = new ArrayList<>(theCachedClass.getInterfaces());
            superInterfaces.remove(theCachedClass); // always includes interface theCachedClass
            // sort interfaces so that we may ensure a deterministic behaviour in case of
            // ambiguous fields -- class implementing two interfaces using the same field
            if (superInterfaces.size() > 1) {
                superInterfaces.sort(CACHED_CLASS_NAME_COMPARATOR);
            }

            Map<String, MetaProperty> iPropertyIndex = classPropertyIndex.computeIfAbsent(theCachedClass, x -> new LinkedHashMap<>());
            for (CachedClass sInterface : superInterfaces) {
                Map<String, MetaProperty> sPropertyIndex = classPropertyIndex.computeIfAbsent(sInterface, x -> new LinkedHashMap<>());
                copyNonPrivateFields(sPropertyIndex, iPropertyIndex, null);
                addFields(sInterface, iPropertyIndex);
            }
            addFields(theCachedClass, iPropertyIndex);

            applyPropertyDescriptors(propertyDescriptors);
            applyStrayPropertyMethods(superClass, classPropertyIndex.computeIfAbsent(superClass, x -> new LinkedHashMap<>()), true);
        } else {
            List<CachedClass> superClasses = getSuperClasses();
            List<CachedClass> superInterfaces = new ArrayList<>(theCachedClass.getInterfaces());
            // sort interfaces so that we may ensure a deterministic behaviour in case of
            // ambiguous fields -- class implementing two interfaces using the same field
            if (superInterfaces.size() > 1) {
                superInterfaces.sort(CACHED_CLASS_NAME_COMPARATOR);
            }

            if (theCachedClass.isArray) { // add the special read-only "length" property
                LinkedHashMap<String, MetaProperty> map = new LinkedHashMap<>();
                map.put("length", arrayLengthProperty);
                classPropertyIndex.put(theCachedClass, map);
            }

            inheritStaticInterfaceFields(superClasses, superInterfaces);
            inheritFields(superClasses);

            applyPropertyDescriptors(propertyDescriptors);

            applyStrayPropertyMethods(superClasses, classPropertyIndex, true);
            applyStrayPropertyMethods(superClasses, classPropertyIndexForSuper, false);
        }
        fillStaticPropertyIndex();
    }

    private void fillStaticPropertyIndex() {
        BiConsumer<String, MetaProperty> indexStaticProperty = (name, prop) -> {
            if (prop instanceof CachedField) {
                CachedField field = (CachedField) prop;
                if (!field.isStatic()) { prop = null; }
            } else if (prop instanceof MetaBeanProperty) {
                prop = establishStaticMetaProperty(prop);
            } else if (prop instanceof MultipleSetterProperty) {
                prop = ((MultipleSetterProperty) prop).createStaticVersion();
            } else {
                prop = null; // ignore all other types
            }

            if (prop != null) staticPropertyIndex.put(name, prop);
        };

        classPropertyIndex.computeIfAbsent(theCachedClass, x -> new LinkedHashMap<>()).forEach(indexStaticProperty);

        if (theCachedClass.isInterface) { // GROOVY-10592: static interface accessors
            Map<String, MetaProperty> strayProperties = new LinkedHashMap<>();
            applyStrayPropertyMethods(theCachedClass, strayProperties, true);
            strayProperties.forEach(indexStaticProperty);
        }
    }

    private MetaMethod findStaticAccessMethod(final MetaBeanProperty mbp) {
        MetaMethod getterMethod = mbp.getGetter();
        // GROOVY-10962: non-static isser shadows static getter
        if (getterMethod != null && !getterMethod.isStatic()) {
            String name = getterMethod.getName();
            if (name.startsWith("is")) {
                name = "get" + name.substring(2);
                Object getter = filterPropertyMethod(getStaticMethods(theClass, name), true, false);
                if (getter != null) { // keep original if no static getter
                    getterMethod = (MetaMethod) getter;
                }
            }
        }
        return getterMethod;
    }

    private MetaProperty establishStaticMetaProperty(final MetaProperty mp) {
        MetaBeanProperty mbp = (MetaBeanProperty) mp;
        final MetaMethod setterMethod = mbp.getSetter();
        final MetaMethod getterMethod = findStaticAccessMethod(mbp);
        final CachedField staticField = Optional.ofNullable(mbp.getField()).filter(f -> f.isStatic()).orElse(null);

        boolean getter = getterMethod == null || getterMethod.isStatic();
        boolean setter = setterMethod == null || setterMethod.isStatic();
        boolean field  = staticField  != null;

        MetaProperty staticProperty = staticField;
        if (getter || setter || field) {
            if (getter && setter) {
                boolean shadow = (getterMethod != mbp.getGetter());
                if (field && !shadow) {
                    staticProperty = mbp; // mbp has static field, null or static getter and null or static setter
                } else if (getterMethod != null || setterMethod != null) {
                    Class<?> type = mbp.getType();
                    if (type == Boolean.TYPE && shadow)
                        type = getterMethod.getReturnType();
                    staticProperty = new MetaBeanProperty(mbp.getName(), type, getterMethod, setterMethod);
                }
            } else if (getter) {
                if (getterMethod != null) {
                    Class<?> type = getterMethod.getReturnType();
                    MetaBeanProperty newmp = new MetaBeanProperty(mbp.getName(), type, getterMethod, null);
                    newmp.setField(staticField);
                    staticProperty = newmp;
                }
            } else if (setter) {
                if (setterMethod != null) {
                    MetaBeanProperty newmp = new MetaBeanProperty(mbp.getName(), mbp.getType(), null, setterMethod);
                    newmp.setField(staticField);
                    staticProperty = newmp;
                }
            }
        }
        return staticProperty;
    }

    private void inheritStaticInterfaceFields(List<CachedClass> superClasses, Iterable<CachedClass> interfaces) {
        for (CachedClass iclass : interfaces) {
            LinkedHashMap<String, MetaProperty> iPropertyIndex = classPropertyIndex.computeIfAbsent(iclass, k -> new LinkedHashMap<>());
            addFields(iclass, iPropertyIndex);
            for (CachedClass superClass : superClasses) {
                if (!iclass.getTheClass().isAssignableFrom(superClass.getTheClass())) continue;
                LinkedHashMap<String, MetaProperty> sPropertyIndex = classPropertyIndex.computeIfAbsent(superClass, k -> new LinkedHashMap<>());
                copyNonPrivateFields(iPropertyIndex, sPropertyIndex, null);
            }
        }
    }

    private void inheritFields(final Iterable<CachedClass> superClasses) {
        Map<String, MetaProperty> sci = null;
        for (CachedClass cc : superClasses) {
            Map<String, MetaProperty> cci = classPropertyIndex.computeIfAbsent(cc, x -> new LinkedHashMap<>());
            if (sci != null && !sci.isEmpty()) {
                copyNonPrivateFields(sci, cci, cc);
                // GROOVY-9608, GROOVY-9609: add public, protected, and package-private fields to index for super
                copyNonPrivateFields(sci, classPropertyIndexForSuper.computeIfAbsent(cc, x -> new LinkedHashMap<>()), cc);
            }
            sci = cci;
            addFields(cc, cci);
        }
    }

    private static void addFields(CachedClass klass, Map<String, MetaProperty> propertyIndex) {
        for (CachedField field : klass.getFields()) {
            propertyIndex.put(field.getName(), field);
        }
    }

    private static void copyNonPrivateFields(Map<String, MetaProperty> from, Map<String, MetaProperty> to, @javax.annotation.Nullable CachedClass klass) {
        for (Map.Entry<String, MetaProperty> entry : from.entrySet()) {
            CachedField field = (CachedField) entry.getValue();
            if (field.isPublic() || field.isProtected() || (!field.isPrivate()
                    && klass != null && inSamePackage(field.getDeclaringClass(), klass.getTheClass()))) {
                to.put(entry.getKey(), field);
            }
        }
    }

    private void applyStrayPropertyMethods(Iterable<CachedClass> classes, Map<CachedClass, LinkedHashMap<String, MetaProperty>> propertyIndex, boolean isThis) {
        for (CachedClass cc : classes) {
            applyStrayPropertyMethods(cc, propertyIndex.computeIfAbsent(cc, x -> new LinkedHashMap<>()), isThis);
        }
    }

    /**
     * Looks for any stray getters/setters that may be used to define a property.
     */
    private void applyStrayPropertyMethods(CachedClass source, Map<String, MetaProperty> target, boolean isThis) {
        var header = metaMethodIndex.getHeader(source.getTheClass());
        for (MetaMethodIndex.Cache e : header.values()) {
            String methodName = e.name;
            int methodNameLength = methodName.length();
            boolean isBooleanGetter = methodName.startsWith("is");
            if (methodNameLength < (isBooleanGetter ? 3 : 4)) continue;

            boolean isGetter = methodName.startsWith("get") || isBooleanGetter;
            boolean isSetter = methodName.startsWith("set");
            if (!isGetter && !isSetter) continue;

            Object propertyMethods = filterPropertyMethod(isThis ? e.methods : e.methodsForSuper, isGetter, isBooleanGetter);
            if (propertyMethods == null) continue;

            String propName = getPropName(methodName);
            if (propertyMethods instanceof MetaMethod) {
                createMetaBeanProperty(target, propName, isGetter, (MetaMethod) propertyMethods);
            } else {
                for (MetaMethod m : (Iterable<MetaMethod>) propertyMethods) {
                    createMetaBeanProperty(target, propName, isGetter, m);
                }
            }
        }
    }

    private static final ConcurrentMap<String, String> PROP_NAMES = new ConcurrentHashMap<>(1024);

    private static String getPropName(String methodName) {
        return PROP_NAMES.computeIfAbsent(methodName, k -> {
            // assume "is" or "[gs]et"
            return BeanUtils.decapitalize(methodName.startsWith("is")
                    ? methodName.substring(2) : methodName.substring(3));
        });
    }

    private static MetaProperty makeReplacementMetaProperty(MetaProperty mp, String propName, boolean isGetter, MetaMethod propertyMethod) {
        if (mp == null) {
            if (isGetter) {
                return new MetaBeanProperty(propName,
                        propertyMethod.getReturnType(),
                        propertyMethod, null);
            } else {
                //isSetter
                return new MetaBeanProperty(propName,
                        propertyMethod.getParameterTypes()[0].getTheClass(),
                        null, propertyMethod);
            }
        }

        if (mp instanceof CachedField) {
            CachedField mfp = (CachedField) mp;
            MetaBeanProperty mbp = new MetaBeanProperty(propName, mfp.getType(),
                    isGetter ? propertyMethod : null,
                    isGetter ? null : propertyMethod);
            mbp.setField(mfp);
            return mbp;
        } else if (mp instanceof MultipleSetterProperty) {
            MultipleSetterProperty msp = (MultipleSetterProperty) mp;
            if (isGetter) {
                // GROOVY-10133: do not replace "isPropName()" with "getPropName()" or ...
                if (msp.getGetter() == null || !msp.getGetter().getName().startsWith("is")) {
                    msp.setGetter(propertyMethod);
                }
            }
            return msp;
        } else if (mp instanceof MetaBeanProperty) {
            MetaBeanProperty mbp = (MetaBeanProperty) mp;
            if (isGetter) {
                // GROOVY-10133: do not replace "isPropName()" with "getPropName()" or ...
                if (mbp.getGetter() == null || !mbp.getGetter().getName().startsWith("is")) {
                    mbp.setGetter(propertyMethod);
                }
                return mbp;
            } else if (mbp.getSetter() == null || mbp.getSetter() == propertyMethod) {
                mbp.setSetter(propertyMethod);
                return mbp;
            } else {
                MultipleSetterProperty msp = new MultipleSetterProperty(propName);
                msp.setField(mbp.getField());
                msp.setGetter(mbp.getGetter());
                return msp;
            }
        } else {
            throw new GroovyBugError("unknown MetaProperty class used. Class is " + mp.getClass());
        }
    }

    private static void createMetaBeanProperty(Map<String, MetaProperty> propertyIndex, String propName, boolean isGetter, MetaMethod propertyMethod) {
        // is this property already accounted for?
        MetaProperty mp = propertyIndex.get(propName);
        MetaProperty newMp = makeReplacementMetaProperty(mp, propName, isGetter, propertyMethod);
        if (newMp != mp) {
            propertyIndex.put(propName, newMp);
        }
    }

    protected void applyPropertyDescriptors(PropertyDescriptor[] propertyDescriptors) {
        // now iterate over the map of property descriptors and generate
        // MetaBeanProperty objects
        for (PropertyDescriptor pd : propertyDescriptors) {
            // skip if the property type is unknown (this seems to be the case if the
            // property descriptor is based on a setX() method that has two parameters,
            // which is not a valid property)
            if (pd.getPropertyType() == null)
                continue;

            // get the getter method
            Method method = pd.getReadMethod();
            MetaMethod getter;

            if (method != null) {
                CachedMethod cachedGetter = CachedMethod.find(method);
                getter = cachedGetter == null ? null : findMethod(cachedGetter);
            } else {
                getter = null;
            }

            // get the setter method
            MetaMethod setter;
            method = pd.getWriteMethod();
            if (method != null) {
                CachedMethod cachedSetter = CachedMethod.find(method);
                setter = cachedSetter == null ? null : findMethod(cachedSetter);
            } else {
                setter = null;
            }

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
    @Override
    public void addMetaBeanProperty(MetaBeanProperty mp) {
        MetaProperty staticProperty = establishStaticMetaProperty(mp);
        if (staticProperty != null) {
            staticPropertyIndex.put(mp.getName(), mp);
        } else {
            Map<String, MetaProperty> propertyMap = classPropertyIndex.computeIfAbsent(theCachedClass, k -> new LinkedHashMap<>());
            // remember field
            CachedField field;
            MetaProperty old = propertyMap.get(mp.getName());
            if (old != null) {
                if (old instanceof MetaBeanProperty) {
                    field = ((MetaBeanProperty) old).getField();
                } else if (old instanceof MultipleSetterProperty) {
                    field = ((MultipleSetterProperty) old).getField();
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
     * Writes a property on the given receiver for the specified arguments. The
     * sender is the class that is requesting the property from the object. The
     * MetaClass will attempt to establish the method to invoke based on the name
     * and arguments provided.
     * <p>
     * The useSuper and fromInsideClass help the runtime perform optimisations
     * on the call to go directly to the super class if necessary
     *
     * @param sender          The java.lang.Class instance that is mutating the property
     * @param object          The Object which the property is being set on
     * @param name            The name of the property
     * @param newValue        The new value of the property to set
     * @param useSuper        Whether the call is to a super class property
     * @param fromInsideClass Whether the call was invoked from the inside or the outside of the class.
     */
    @Override
    public void setProperty(final Class sender, final Object object, final String name, Object newValue, final boolean useSuper, final boolean fromInsideClass) {

        //----------------------------------------------------------------------
        // handling of static
        //----------------------------------------------------------------------
        boolean isStatic = (theClass != Class.class && object instanceof Class);
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class<?>) object);
            mc.getProperty(sender, object, name, useSuper, fromInsideClass);
            return;
        }

        checkInitalised();

        //----------------------------------------------------------------------
        // Unwrap wrapped values for now - the new MOP will handle them properly
        //----------------------------------------------------------------------
        if (newValue instanceof Wrapper) newValue = ((Wrapper) newValue).unwrap();

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
                if (method != null || (f != null && !f.isFinal())) {
                    arguments = new Object[]{newValue};
                    field = f;
                }
            } else {
                field = mp;
            }
        }

        // check for a category method named like a setter
        if (!useSuper && !isStatic && !name.isEmpty() && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            var setterName = GroovyCategorySupport.getPropertyCategorySetterName(name);
            if (setterName != null) {
                MetaMethod categoryMethod = getCategoryMethodSetter(theClass, setterName, false);
                if (categoryMethod != null) {
                    method = categoryMethod;
                    arguments = new Object[]{newValue};
                }
            }
        }

        //----------------------------------------------------------------------
        // listener method
        //----------------------------------------------------------------------
        boolean ambiguousListener = false;
        if (method == null) {
            method = listeners.get(name);
            ambiguousListener = (method == AMBIGUOUS_LISTENER_METHOD);
            if (method != null && !ambiguousListener && newValue instanceof Closure) {
                // bean.name = { -> } is short for bean.addSomeListener({ -> });
                // where "name" derives from the SomeListener interface's method
                var listener = method.getParameterTypes()[0].getTheClass();
                Object proxy = Proxy.newProxyInstance(
                        listener.getClassLoader(),
                        new Class[]{listener},
                        new ConvertedClosure((Closure<?>) newValue, name));
                arguments = new Object[]{proxy};
                newValue = proxy;
            } else {
                method = null;
            }
        }

        //----------------------------------------------------------------------
        // field
        //----------------------------------------------------------------------
        if (method == null && field != null
                && (!isMap || isStatic // GROOVY-8065
                    || field.isPublic())) { // GROOVY-11367
            if (!field.isFinal()) {
                field.setProperty(object, newValue);
                return;
            } else {
                throw new ReadOnlyPropertyException(name, theClass); // GROOVY-5985
            }
        }

        //----------------------------------------------------------------------
        // generic set method
        //----------------------------------------------------------------------
        // check for a generic get method provided through a category
        if (method == null && !useSuper && !isStatic && GroovyCategorySupport.hasCategoryInCurrentThread()) {
            method = getCategoryMethodSetter(theClass, "set", true);
            if (method != null) arguments = new Object[]{name, newValue};
        }
        if (method == null && genericSetMethod != null && (genericSetMethod.isStatic() || !isStatic)) {
            arguments = new Object[]{name, newValue};
            method = genericSetMethod;
        }

        //----------------------------------------------------------------------
        // java.util.Map put method before non-public method -- see GROOVY-11367
        //----------------------------------------------------------------------
        if (isMap && !isStatic && !(method != null && method.isPublic())
                  && (mp == null || !mp.isPublic() || isSpecialProperty(name))) {
            ((Map) object).put(name, newValue);
            return;
        }

        //----------------------------------------------------------------------
        // executing the method
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

            VM_PLUGIN.transformMetaMethod(this, method).doMethodInvoke(object, arguments);
            return;
        }

        //----------------------------------------------------------------------
        // missing property protocol
        //----------------------------------------------------------------------
        if (ambiguousListener) {
            throw new GroovyRuntimeException("There are multiple listeners for the property " + name + ". Please do not use the bean short form to access this listener.");
        }
        if (mp != null) {
            throw new ReadOnlyPropertyException(name, theClass);
        }
        if (object instanceof Class && !"metaClass".equals(name)) {
            invokeStaticMissingProperty(object, name, newValue, false);
        } else {
            invokeMissingProperty(object, name, newValue, false);
        }
    }

    private MetaProperty getMetaProperty(final Class clazz, final String name, final boolean useSuper, final boolean useStatic) {
        if (clazz == theClass && !useSuper)
            return getMetaProperty(name, useStatic);

        CachedClass cachedClass = ReflectionCache.getCachedClass(clazz);
        while (true) {
            Map<String, MetaProperty> propertyMap;
            if (useStatic) {
                propertyMap = staticPropertyIndex;
            } else if (useSuper) {
                propertyMap = classPropertyIndexForSuper.get(cachedClass);
            } else {
                propertyMap = classPropertyIndex.get(cachedClass);
            }
            if (propertyMap == null) {
                if (cachedClass != theCachedClass) {
                    cachedClass = theCachedClass;
                    continue;
                } else {
                    return null;
                }
            }
            return propertyMap.get(name);
        }
    }

    private MetaProperty getMetaProperty(final String name, final boolean useStatic) {
        CachedClass clazz = theCachedClass;
        Map<String, MetaProperty> propertyMap;
        if (useStatic) {
            propertyMap = staticPropertyIndex;
        } else {
            propertyMap = classPropertyIndex.get(clazz);
        }
        if (propertyMap == null) {
            return null;
        }
        return propertyMap.get(name);
    }

    /**
     * Retrieves the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
     *
     * @param sender      The class of the object that requested the attribute
     * @param object      The instance
     * @param attribute   The name of the attribute
     * @param useSuper    Whether to look-up on the super class or not
     * @return The attribute value
     */
    @Override
    public Object getAttribute(final Class sender, final Object object, final String attribute, final boolean useSuper) {
        return getAttribute(sender, object, attribute, useSuper, false);
    }

    /**
     * Retrieves the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
     *
     * @param sender          The class of the object that requested the attribute
     * @param object          The instance the attribute is to be retrieved from
     * @param attribute       The name of the attribute
     * @param useSuper        Whether to look-up on the super class or not
     * @param fromInsideClass Whether the call was invoked from the inside or the outside of the class.
     * @return The attribute value
     */
    public Object getAttribute(final Class sender, final Object object, final String attribute, final boolean useSuper, final boolean fromInsideClass) {
        checkInitalised();

        boolean isStatic = (theClass != Class.class && object instanceof Class);
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class<?>) object);
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

        throw new MissingFieldException(attribute, !useSuper ? theClass : theClass.getSuperclass());
    }

    /**
     * <p>Sets an attribute on the given receiver for the specified arguments. The sender is the class that is setting the attribute from the object.
     * The MetaClass will attempt to establish the method to invoke based on the name and arguments provided.
     *
     * <p>The isCallToSuper and fromInsideClass help the Groovy runtime perform optimisations on the call to go directly
     * to the super class if necessary
     *
     * @param sender          The java.lang.Class instance that is mutating the property
     * @param object          The Object which the property is being set on
     * @param attribute       The name of the attribute,
     * @param newValue        The new value of the attribute to set
     * @param useSuper        Whether the call is to a super class property
     * @param fromInsideClass Whether the call was invoked from the inside or the outside of the class
     */
    @Override
    public void setAttribute(final Class sender, final Object object, final String attribute, final Object newValue, final boolean useSuper, final boolean fromInsideClass) {
        checkInitalised();

        boolean isStatic = (theClass != Class.class && object instanceof Class);
        if (isStatic && object != theClass) {
            MetaClass mc = registry.getMetaClass((Class<?>) object);
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

        throw new MissingFieldException(attribute, !useSuper ? theClass : theClass.getSuperclass());
    }

    /**
     * Obtains a reference to the original AST for the MetaClass if it is available at runtime
     *
     * @return The original AST or null if it cannot be returned
     */
    @Override
    public ClassNode getClassNode() {
        if (classNode == null && GroovyObject.class.isAssignableFrom(theClass)) {
            // let's try load it from the classpath
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

                    /*
                     * todo there is no CompileUnit in scope so class name
                     * checking won't work but that mostly affects the bytecode
                     * generation rather than viewing the AST
                     */
                    CompilationUnit.ClassgenCallback search = (writer, node) -> {
                        if (node.getName().equals(theClass.getName())) {
                            MetaClassImpl.this.classNode = node;
                        }
                    };

                    CompilationUnit unit = new CompilationUnit();
                    unit.setClassgenCallback(search);
                    unit.addSource(url);
                    unit.compile(Phases.CLASS_GENERATION);
                } catch (Exception e) {
                    throw new GroovyRuntimeException("Exception thrown parsing: " + groovyFile + ". Reason: " + e, e);
                }
            }

        }
        return classNode;
    }

    /**
     * Returns a string representation of this metaclass
     */
    @Override
    public String toString() {
        return super.toString() + "[" + theClass + "]";
    }

    // Implementation methods
    //--------------------------------------------------------------------------

    /**
     * adds a MetaMethod to this class. WARNING: this method will not
     * do the necessary steps for multimethod logic and using this
     * method doesn't mean, that a method added here is replacing another
     * method from a parent class completely. These steps are usually done
     * by initialize, which means if you need these steps, you have to add
     * the method before running initialize the first time.
     *
     * @param method the MetaMethod
     * @see #initialize()
     */
    @Override
    public void addMetaMethod(MetaMethod method) {
        if (isInitialized()) {
            throw new RuntimeException("Already initialized, cannot add new method: " + method);
        }

        final CachedClass declaringClass = method.getDeclaringClass();
        addMetaMethodToIndex(method, metaMethodIndex.getHeader(declaringClass.getTheClass()));
    }

    protected void addMetaMethodToIndex(MetaMethod method, Map<String, MetaMethodIndex.Cache> cacheIndex) {
        checkIfStdMethod(method);
        metaMethodIndex.addMetaMethod(method, cacheIndex);
    }

    /**
     * Checks if the metaMethod is a method from the GroovyObject interface such as setProperty, getProperty and invokeMethod
     *
     * @param metaMethod The metaMethod instance
     * @see GroovyObject
     */
    protected final void checkIfGroovyObjectMethod(MetaMethod metaMethod) {
        if (metaMethod instanceof ClosureMetaMethod
                || metaMethod instanceof NewInstanceMetaMethod
                || metaMethod instanceof MixinInstanceMetaMethod) {
            if (isGetPropertyMethod(metaMethod)) {
                getPropertyMethod = metaMethod;
            } else if (isInvokeMethod(metaMethod)) {
                invokeMethodMethod = metaMethod;
            } else if (isSetPropertyMethod(metaMethod)) {
                setPropertyMethod = metaMethod;
            }
        }
    }

    private static boolean isSetPropertyMethod(MetaMethod metaMethod) {
        return SET_PROPERTY_METHOD.equals(metaMethod.getName()) && metaMethod.getParameterTypes().length == 2;
    }

    private static boolean isGetPropertyMethod(MetaMethod metaMethod) {
        return GET_PROPERTY_METHOD.equals(metaMethod.getName());
    }

    private static boolean isInvokeMethod(MetaMethod metaMethod) {
        return INVOKE_METHOD_METHOD.equals(metaMethod.getName()) && metaMethod.getParameterTypes().length == 2;
    }

    private void checkIfStdMethod(MetaMethod method) {
        checkIfGroovyObjectMethod(method);

        if (isGenericGetMethod(method) && genericGetMethod == null) {
            genericGetMethod = method;
        } else if (MetaClassHelper.isGenericSetMethod(method) && genericSetMethod == null) {
            genericSetMethod = method;
        }
        if (method.getName().equals(PROPERTY_MISSING)) {
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
            NumberMathModificationInfo.instance.checkIfStdMethod(method);
        }
    }

    protected boolean isInitialized() {
        return initialized;
    }

    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * @return {@code false}: add method
     *         {@code null} : ignore method
     *         {@code true} : replace
     */
    private static Boolean getMatchKindForCategory(final MetaMethod aMethod, final MetaMethod categoryMethod) {
        CachedClass[] paramTypes1 = aMethod.getParameterTypes();
        CachedClass[] paramTypes2 = categoryMethod.getParameterTypes();
        int n = paramTypes1.length;
        if (n != paramTypes2.length) return Boolean.FALSE;
        for (int i = 0; i < n; i += 1) {
            if (paramTypes1[i] != paramTypes2[i]) return Boolean.FALSE;
        }

        Class selfType1 = aMethod.getDeclaringClass().getTheClass();
        Class selfType2 = categoryMethod.getDeclaringClass().getTheClass();
        // replace if self type is the same or the category self type is more specific
        if (selfType1 == selfType2 || selfType1.isAssignableFrom(selfType2)) return Boolean.TRUE;
        // GROOVY-6363: replace if the private method self type is more specific
        if (aMethod.isPrivate() && selfType2.isAssignableFrom(selfType1)) return Boolean.TRUE;

        return null;
    }

    private static void filterMatchingMethodForCategory(FastArray list, MetaMethod method) {
        int len = list.size();
        if (len == 0) {
            list.add(method);
            return;
        }

        Object[] data = list.getArray();
        for (int j = 0; j != len; ++j) {
            MetaMethod aMethod = (MetaMethod) data[j];
            Boolean match = getMatchKindForCategory(aMethod, method);
            // true == replace
            if (Boolean.TRUE.equals(match)) {
                list.set(j, method);
                return;
                // null == ignore (we have a better method already)
            } else if (match == null) {
                return;
            }
        }
        // the cases true and null for a match are through, the
        // remaining case is false and that means adding the method
        // to our list
        list.add(method);
    }

    /**
     * @return the matching method which should be found
     */
    private MetaMethod findMethod(CachedMethod aMethod) {
        Object methods = getMethods(theClass, aMethod.getName(), false);
        if (methods instanceof FastArray) {
            FastArray m = (FastArray) methods;
            final int len = m.size();
            final Object[] data = m.getArray();
            for (int i = 0; i != len; ++i) {
                MetaMethod method = (MetaMethod) data[i];
                if (method.isMethod(aMethod)) {
                    return method;
                }
            }
        } else {
            MetaMethod method = (MetaMethod) methods;
            if (method.getName().equals(aMethod.getName())
                    && method.getReturnType().equals(aMethod.getReturnType())
            // TODO && method.compatibleModifiers(method.getModifiers(), aMethod.getModifiers())
                    && MetaMethod.equal(method.getParameterTypes(), aMethod.getParameterTypes())) {
                return method;
            }
        }
        return aMethod;
    }

    /**
     * Chooses the correct method to use from a list of methods which match by
     * name.
     *
     * @param methodOrList the possible methods to choose from
     * @param arguments    the arguments
     */
    protected Object chooseMethod(String methodName, Object methodOrList, Class[] arguments) throws MethodSelectionException {
        Object method = chooseMethodInternal(methodName, methodOrList, arguments);
        if (method instanceof GeneratedMetaMethod.Proxy)
            return ((GeneratedMetaMethod.Proxy) method).proxy();
        return method;
    }

    private Object chooseMethodInternal(String methodName, Object methodOrList, Class[] arguments) throws MethodSelectionException {
        if (methodOrList instanceof ParameterTypes) {
            if (((ParameterTypes) methodOrList).isValidMethod(arguments)) {
                return methodOrList;
            }
            return null;
        }

        var methods = (FastArray) methodOrList;
        if (methods == null) return null;
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
        if (arguments == null || arguments.length == 0) {
            var method = MetaClassHelper.chooseEmptyMethodParams(methods);
            if (method != null) {
                return method;
            }
            throw new MethodSelectionException(methodName, methods, arguments);
        }

        List<MetaMember> matchingMethods = new ArrayList<>(methodCount);
        Object[] methodArray = methods.getArray();
        for (int i = 0; i < methodCount; i += 1) {
            var method = (ParameterTypes & MetaMember) methodArray[i];
            if (method.isValidMethod(arguments)) {
                matchingMethods.add(method);
            }
        }
        methodCount = matchingMethods.size();
        if (methodCount == 0) {
            return null;
        } else if (methodCount == 1) {
            return matchingMethods.get(0);
        }
        return doChooseMostSpecificParams(theClass.getName(), methodName, matchingMethods, arguments, false);
    }

    protected static Object doChooseMostSpecificParams(final String theClassName, final String name, final List matchingMethods, final Class[] arguments, final boolean checkParameterCompatibility) {
        var matchesDistance = -1L;
        var matches = new LinkedList<>();
        for (Object method : matchingMethods) {
            var parameterTypes = (ParameterTypes) method;
            if (!checkParameterCompatibility || MetaClassHelper.parametersAreCompatible(arguments, parameterTypes.getNativeParameterTypes())) {
                long dist = MetaClassHelper.calculateParameterDistance(arguments, parameterTypes);
                matchesDistance = handleMatches(matchesDistance, matches, method, dist);
            }
        }

        int size = matches.size();
        if (size > 1) { // GROOVY-11258
            for (var iter = matches.iterator(); iter.hasNext(); ) {
                Object outer = iter.next(); // if redundant, remove
                for (Object inner : matches) {
                    if (inner == outer) continue;
                    Class<?>[] innerTypes = ((ParameterTypes) inner).getNativeParameterTypes();
                    Class<?>[] outerTypes = ((ParameterTypes) outer).getNativeParameterTypes();
                    if (!Arrays.equals(innerTypes, outerTypes) && MetaClassHelper.parametersAreCompatible(innerTypes, outerTypes)) {
                        iter.remove(); // for the given argument type(s), inner can accept everything that outer can
                        size -= 1;
                        break;
                    }
                }
            }
        }

        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return matches.getFirst();
        }
        throw new GroovyRuntimeException(createErrorMessageForAmbiguity(theClassName, name, arguments, matches));
    }

    protected static String createErrorMessageForAmbiguity(String theClassName, String name, Class[] arguments, LinkedList matches) {
        StringBuilder msg = new StringBuilder("Ambiguous method overloading for method ");
        msg.append(theClassName).append("#").append(name)
                .append(".\nCannot resolve which method to invoke for ")
                .append(FormatHelper.toString(arguments))
                .append(" due to overlapping prototypes between:");
        for (final Object match : matches) {
            CachedClass[] types = ((ParameterTypes) match).getParameterTypes();
            msg.append("\n\t").append(FormatHelper.toString(types));
        }
        return msg.toString();
    }

    protected static long handleMatches(long matchesDistance, LinkedList matches, Object method, long dist) {
        if (matches.isEmpty()) {
            matches.add(method);
            matchesDistance = dist;
        } else if (dist < matchesDistance) {
            matchesDistance = dist;
            matches.clear();
            matches.add(method);
        } else if (dist == matchesDistance) {
            matches.add(method);
        }
        return matchesDistance;
    }

    private static boolean isGenericGetMethod(MetaMethod method) {
        if ("get".equals(method.getName())) {
            CachedClass[] parameterTypes = method.getParameterTypes();
            return parameterTypes.length == 1 && parameterTypes[0].getTheClass() == String.class;
        }
        return false;
    }

    /**
     * Complete the initialisation process. After this method
     * is called no methods should be added to the metaclass.
     * Invocation of methods or access to fields/properties is
     * forbidden unless this method is called. This method
     * should contain any initialisation code, taking a longer
     * time to complete. An example is the creation of the
     * Reflector. It is suggested to synchronize this
     * method.
     */
    @Override
    public synchronized void initialize() {
        if (!isInitialized()) {
            reinitialize();
        }
    }

    protected synchronized void reinitialize() {
        fillMethodIndex();
        try {
            addProperties();
        } catch (Throwable t) {
            if (!AndroidSupport.isRunningAndroid()) {
                UncheckedThrow.rethrow(t);
            }
            // Introspection failure...
            // May happen in Android
        }
        setInitialized(true);
    }

    private void addProperties() {
        BeanInfo info;
        //     introspect
        try {
            if (isBeanDerivative(theClass)) {
                info = (BeanInfo) doPrivileged((PrivilegedExceptionAction) () -> Introspector.getBeanInfo(theClass, Introspector.IGNORE_ALL_BEANINFO));
            } else {
                info = (BeanInfo) doPrivileged((PrivilegedExceptionAction) () -> Introspector.getBeanInfo(theClass));
            }
        } catch (PrivilegedActionException pae) {
            throw new GroovyRuntimeException("exception during bean introspection", pae.getException());
        }
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        // build up the metaproperties based on the public fields, property descriptors,
        // and the getters and setters
        setUpProperties(descriptors);
        addRecordProperties(info);

        EventSetDescriptor[] eventDescriptors = info.getEventSetDescriptors();
        for (EventSetDescriptor descriptor : eventDescriptors) {
            Method[] listenerMethods = descriptor.getListenerMethods();
            for (Method listenerMethod : listenerMethods) {
                final MetaMethod metaMethod = CachedMethod.find(descriptor.getAddListenerMethod());
                // GROOVY-5202
                // there might be a non-public listener of some kind
                // we skip that here
                if (metaMethod == null) continue;
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

    private void addRecordProperties(BeanInfo info) {
        VMPlugin plugin = VMPluginFactory.getPlugin();
        Set<String> componentNames = new HashSet<>(plugin.getRecordComponentNames(theClass));
        if (!componentNames.isEmpty()) {
            MethodDescriptor[] methodDescriptors = info.getMethodDescriptors();
            Map<String, MetaProperty> propIndex = classPropertyIndex.computeIfAbsent(theCachedClass, x -> new LinkedHashMap<>());
            for (MethodDescriptor md : methodDescriptors) {
                if (md.getMethod().getParameterCount() != 0) continue;
                String name = md.getName();
                if (componentNames.contains(name)) {
                    CachedMethod cachedMethod = CachedMethod.find(md.getMethod());
                    if (cachedMethod != null) {
                        MetaMethod accessor = findMethod(cachedMethod);
                        createMetaBeanProperty(propIndex, name, true, accessor);
                    }
                }
            }
        }
    }

    @SuppressWarnings("removal") // TODO a future Groovy version should perform the operation not as a privileged action
    private Object doPrivileged(PrivilegedExceptionAction action) throws PrivilegedActionException {
        return java.security.AccessController.doPrivileged(action);
    }

    private static boolean isBeanDerivative(Class theClass) {
        Class next = theClass;
        while (next != null) {
            if (Arrays.asList(next.getInterfaces()).contains(BeanInfo.class)) return true;
            next = next.getSuperclass();
        }
        return false;
    }

    private void addToAllMethodsIfPublic(final MetaMethod metaMethod) {
        if (metaMethod.isPublic())
            allMethods.add(metaMethod);
    }

    /**
     * Retrieves the list of MetaMethods held by the class. This list does not include MetaMethods added by groovy.lang.ExpandoMetaClass.
     *
     * @return A list of MetaMethods
     */
    @Override
    public List<MetaMethod> getMethods() {
        return allMethods;
    }

    /**
     * Retrieves the list of MetaMethods held by this class. This list includes MetaMethods added by groovy.lang.ExpandoMetaClass.
     *
     * @return A list of MetaMethods
     */
    @Override
    public List<MetaMethod> getMetaMethods() {
        return new ArrayList<>(newGroovyMethodsSet);
    }

    protected void dropStaticMethodCache(String name) {
        metaMethodIndex.clearCaches(name);
    }

    protected void dropMethodCache(String name) {
        metaMethodIndex.clearCaches(name);
    }

    /**
     * Create a CallSite
     */
    public CallSite createPojoCallSite(CallSite site, Object receiver, Object[] args) {
        if (!(this instanceof AdaptingMetaClass)) {
            Class[] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod metaMethod = getMethodWithCachingInternal(getTheClass(), site, params);
            if (metaMethod != null)
                return PojoMetaMethodSite.createPojoMetaMethodSite(site, this, metaMethod, params, receiver, args);
        }
        return new PojoMetaClassSite(site, this);
    }

    /**
     * Create a CallSite
     */
    public CallSite createStaticSite(CallSite site, Object[] args) {
        if (!(this instanceof AdaptingMetaClass)) {
            Class[] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod metaMethod = retrieveStaticMethod(site.getName(), args);
            if (metaMethod != null)
                return StaticMetaMethodSite.createStaticMetaMethodSite(site, this, metaMethod, params, args);
        }
        return new StaticMetaClassSite(site, this);
    }

    /**
     * Create a CallSite
     */
    public CallSite createPogoCallSite(CallSite site, Object[] args) {
        if (!GroovyCategorySupport.hasCategoryInCurrentThread() && !(this instanceof AdaptingMetaClass)) {
            Class[] params = MetaClassHelper.convertToTypeArray(args);
            CallSite tempSite = site;
            if (site.getName().equals(CALL_METHOD) && GeneratedClosure.class.isAssignableFrom(theClass)) {
                // here, we want to point to a method named "doCall" instead of "call"
                // but we don't want to replace the original call site name, otherwise
                // we lose the fact that the original method name was "call" so instead
                // we will point to a metamethod called "doCall"
                // see GROOVY-5806 for details
                tempSite = new AbstractCallSite(site.getArray(), site.getIndex(), DO_CALL_METHOD);
            }
            MetaMethod metaMethod = getMethodWithCachingInternal(theClass, tempSite, params);
            if (metaMethod != null)
                return PogoMetaMethodSite.createPogoMetaMethodSite(site, this, metaMethod, params, args);
        }
        return new PogoMetaClassSite(site, this);
    }

    /**
     * Create a CallSite
     */
    public CallSite createPogoCallCurrentSite(CallSite site, Class sender, Object[] args) {
        if (!GroovyCategorySupport.hasCategoryInCurrentThread() && !(this instanceof AdaptingMetaClass)) {
            Class[] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod metaMethod = getMethodWithCachingInternal(sender, site, params);
            if (metaMethod != null)
                return PogoMetaMethodSite.createPogoMetaMethodSite(site, this, metaMethod, params, args);
        }
        return new PogoMetaClassSite(site, this);
    }

    /**
     * Create a CallSite
     */
    public CallSite createConstructorSite(CallSite site, Object[] args) {
        if (!(this instanceof AdaptingMetaClass)) {
            Class[] argTypes = MetaClassHelper.convertToTypeArray(args);
            CachedConstructor constructor = (CachedConstructor) chooseMethod(CONSTRUCTOR_NAME, constructors, argTypes);
            if (constructor != null) {
                return ConstructorSite.createConstructorSite(site, this, constructor, argTypes, args);
            }
            if ((args.length == 1 && args[0] instanceof Map) ||
                    (args.length == 2 && args[1] instanceof Map &&
                            theClass.getEnclosingClass() != null &&
                            theClass.getEnclosingClass().isAssignableFrom(argTypes[0]))) {
                constructor = (CachedConstructor) retrieveNamedArgCompatibleConstructor(argTypes, args);
                if (constructor != null) {
                    return args.length == 1
                            ? new ConstructorSite.NoParamSite(site, this, constructor, argTypes)
                            : new ConstructorSite.NoParamSiteInnerClass(site, this, constructor, argTypes);
                }
            }
        }
        return new MetaClassConstructorSite(site, this);
    }

    /**
     * Returns the ClassInfo for the contained Class
     *
     * @return The ClassInfo for the contained class.
     */
    public ClassInfo getClassInfo() {
        return theCachedClass.classInfo;
    }

    /**
     * Returns version of the contained Class
     *
     * @return The version of the contained class.
     */
    public int getVersion() {
        return theCachedClass.classInfo.getVersion();
    }

    /**
     * Increments version of the contained Class
     */
    public void incVersion() {
        theCachedClass.classInfo.incVersion();
    }

    /**
     * Retrieves a list of additional MetaMethods held by this class
     *
     * @return A list of MetaMethods
     */
    public MetaMethod[] getAdditionalMetaMethods() {
        return additionalMetaMethods;
    }

    protected MetaBeanProperty findPropertyInClassHierarchy(String propertyName, CachedClass theClass) {
        if (theClass == null || theClass == ReflectionCache.OBJECT_CLASS)
            return null;

        final CachedClass superClass = theClass.getCachedSuperClass();
        if (superClass == null)
            return null;

        MetaBeanProperty property = null;

        MetaClass metaClass = this.registry.getMetaClass(superClass.getTheClass());
        if (metaClass instanceof MutableMetaClass) {
            property = getMetaPropertyFromMutableMetaClass(propertyName, metaClass);
            if (property == null) {
                if (superClass != ReflectionCache.OBJECT_CLASS) {
                    property = findPropertyInClassHierarchy(propertyName, superClass);
                }
                if (property == null) {
                    final Class[] interfaces = theClass.getTheClass().getInterfaces();
                    property = searchInterfacesForMetaProperty(propertyName, interfaces);
                }
            }
        }
        return property;
    }

    private MetaBeanProperty searchInterfacesForMetaProperty(String propertyName, Class[] interfaces) {
        MetaBeanProperty property = null;
        for (Class anInterface : interfaces) {
            MetaClass metaClass = registry.getMetaClass(anInterface);
            if (metaClass instanceof MutableMetaClass) {
                property = getMetaPropertyFromMutableMetaClass(propertyName, metaClass);
                if (property != null) break;
            }
            Class[] superInterfaces = anInterface.getInterfaces();
            if (superInterfaces.length > 0) {
                property = searchInterfacesForMetaProperty(propertyName, superInterfaces);
                if (property != null) break;
            }

        }
        return property;
    }

    private static MetaBeanProperty getMetaPropertyFromMutableMetaClass(String propertyName, MetaClass metaClass) {
        final boolean isModified = ((MutableMetaClass) metaClass).isModified();
        if (isModified) {
            final MetaProperty metaProperty = metaClass.getMetaProperty(propertyName);
            if (metaProperty instanceof MetaBeanProperty)
                return (MetaBeanProperty) metaProperty;
        }
        return null;
    }

    protected MetaMethod findMixinMethod(String methodName, Class[] arguments) {
        return null;
    }

    protected static MetaMethod findMethodInClassHierarchy(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass) {

        if (metaClass instanceof MetaClassImpl) {
            boolean check = false;
            for (ClassInfo ci : ((MetaClassImpl) metaClass).theCachedClass.getHierarchy()) {
                final MetaClass aClass = ci.getStrongMetaClass();
                if (aClass instanceof MutableMetaClass && ((MutableMetaClass) aClass).isModified()) {
                    check = true;
                    break;
                }
            }

            if (!check)
                return null;
        }

        MetaMethod method = null;

        Class superClass;
        if (metaClass.getTheClass().isArray() && !metaClass.getTheClass().getComponentType().isPrimitive() && metaClass.getTheClass().getComponentType() != Object.class) {
            superClass = Object[].class;
        } else {
            superClass = metaClass.getTheClass().getSuperclass();
        }

        if (superClass != null) {
            MetaClass superMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(superClass);
            method = findMethodInClassHierarchy(instanceKlazz, methodName, arguments, superMetaClass);
        } else {
            if (metaClass.getTheClass().isInterface()) {
                MetaClass superMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(Object.class);
                method = findMethodInClassHierarchy(instanceKlazz, methodName, arguments, superMetaClass);
            }
        }

        method = findSubClassMethod(instanceKlazz, methodName, arguments, metaClass, method);

        method = getMetaMethod(instanceKlazz, methodName, arguments, metaClass, method);

        method = findOwnMethod(instanceKlazz, methodName, arguments, metaClass, method);

        return method;
    }

    private static MetaMethod getMetaMethod(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass, MetaMethod method) {
        MetaMethod infMethod = searchInterfacesForMetaMethod(instanceKlazz, methodName, arguments, metaClass);
        if (infMethod != null) {
            method = (method == null ? infMethod : mostSpecific(method, infMethod, instanceKlazz));
        }
        return method;
    }

    private static MetaMethod findSubClassMethod(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass, MetaMethod method) {
        if (metaClass instanceof MetaClassImpl) {
            Object list = ((MetaClassImpl) metaClass).getSubclassMetaMethods(methodName);
            if (list != null) {
                if (list instanceof MetaMethod) {
                    MetaMethod m = (MetaMethod) list;
                    method = findSubClassMethod(instanceKlazz, arguments, method, m);
                } else {
                    FastArray arr = (FastArray) list;
                    for (int i = 0; i != arr.size(); ++i) {
                        MetaMethod m = (MetaMethod) arr.get(i);
                        method = findSubClassMethod(instanceKlazz, arguments, method, m);
                    }
                }
            }
        }
        return method;
    }

    private static MetaMethod findSubClassMethod(Class instanceKlazz, Class[] arguments, MetaMethod method, MetaMethod m) {
        if (m.getDeclaringClass().getTheClass().isAssignableFrom(instanceKlazz) && m.isValidExactMethod(arguments)) {
            method = (method == null ? m : mostSpecific(method, m, instanceKlazz));
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
        for (Class anInterface : interfaces) {
            MetaClass infMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(anInterface);
            method = getMetaMethod(instanceKlazz, methodName, arguments, infMetaClass, method);
        }

        method = findSubClassMethod(instanceKlazz, methodName, arguments, metaClass, method);

        method = findOwnMethod(instanceKlazz, methodName, arguments, metaClass, method);

        return method;
    }

    protected static MetaMethod findOwnMethod(Class instanceKlazz, String methodName, Class[] arguments, MetaClass metaClass, MetaMethod method) {
        // we trick ourselves here
        if (instanceKlazz != metaClass.getTheClass()) {
            MetaMethod ownMethod = metaClass.pickMethod(methodName, arguments);
            if (ownMethod != null) {
                method = (method == null ? ownMethod : mostSpecific(method, ownMethod, instanceKlazz));
            }
        }
        return method;
    }

    protected Object getSubclassMetaMethods(String methodName) {
        return null;
    }

    private abstract class MethodIndexAction {
        public void iterate() {
            for (Map.Entry<Class<?>, Map<String, MetaMethodIndex.Cache>> classEntry : metaMethodIndex.indexMap.entrySet()) {
                Class<?> clazz = classEntry.getKey();
                if (skipClass(clazz)) continue;
                var header = classEntry.getValue();
                for (MetaMethodIndex.Cache nameEntry : header.values()) {
                    methodNameAction(clazz, nameEntry);
                }
            }
        }

        public abstract void methodNameAction(Class<?> clazz, MetaMethodIndex.Cache methods);

        public boolean skipClass(final Class<?> clazz) {
            return false;
        }
    }

    /**
     * <p>Retrieves a property on the given object for the specified arguments.
     *
     * @param object   The Object which the property is being retrieved from
     * @param property The name of the property
     * @return The properties value
     */
    @Override
    public Object getProperty(Object object, String property) {
        return getProperty(theClass, object, property, false, false);
    }

    /**
     * <p>Sets a property on the given object for the specified arguments.
     *
     * @param object   The Object which the property is being retrieved from
     * @param property The name of the property
     * @param newValue The new value
     */
    @Override
    public void setProperty(Object object, String property, Object newValue) {
        setProperty(theClass, object, property, newValue, false, false);
    }

    /**
     * Retrieves the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
     *
     * @param object    The object to get the attribute from
     * @param attribute The name of the attribute
     * @return The attribute value
     */
    @Override
    public Object getAttribute(final Object object, final String attribute) {
        return getAttribute(theClass, object, attribute, false, false);
    }

    /**
     * Sets the value of an attribute (field). This method is to support the Groovy runtime and not for general client API usage.
     *
     * @param object    The object to get the attribute from
     * @param attribute The name of the attribute
     * @param newValue  The new value of the attribute
     */
    @Override
    public void setAttribute(final Object object, final String attribute, final Object newValue) {
        setAttribute(theClass, object, attribute, newValue, false, false);
    }

    /**
     * Selects a method by name and argument classes. This method
     * does not search for an exact match, it searches for a compatible
     * method. For this the method selection mechanism is used as provided
     * by the implementation of this MetaClass. pickMethod may or may
     * not be used during the method selection process when invoking a method.
     * There is no warranty for that.
     *
     * @param methodName the name of the method to pick
     * @param arguments  the method arguments
     * @return a matching MetaMethod or null
     * @throws GroovyRuntimeException if there is more than one matching method
     */
    @Override
    public MetaMethod pickMethod(String methodName, Class[] arguments) {
        return getMethodWithoutCaching(theClass, methodName, arguments, false);
    }

    /**
     * indicates is the metaclass method invocation for non-static methods is done
     * through a custom invoker object.
     *
     * @return true - if the method invocation is not done by the metaclass itself
     */
    public boolean hasCustomInvokeMethod() {
        return invokeMethodMethod != null;
    }

    /**
     * indicates is the metaclass method invocation for static methods is done
     * through a custom invoker object.
     *
     * @return true - if the method invocation is not done by the metaclass itself
     */
    public boolean hasCustomStaticInvokeMethod() {
        return false;
    }

    /**
     * remove all method call cache entries. This should be done if a
     * method is added during runtime, but not by using a category.
     */
    protected void clearInvocationCaches() {
        metaMethodIndex.clearCaches();
    }

    private static class DummyMetaMethod extends MetaMethod {

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Class getReturnType() {
            return null;
        }

        @Override
        public CachedClass getDeclaringClass() {
            return null;
        }

        public ParameterTypes getParamTypes() {
            return null;
        }

        @Override
        public Object invoke(Object object, Object[] arguments) {
            return null;
        }
    }

    private Tuple2<Object, MetaMethod> invokeMethod(MetaMethod method,
                                                    Object delegate,
                                                    Closure closure,
                                                    String methodName,
                                                    Class[] argClasses,
                                                    Object[] originalArguments,
                                                    Object owner) {
        if (method == null && delegate != closure && delegate != null) {
            MetaClass delegateMetaClass = lookupObjectMetaClass(delegate);
            method = delegateMetaClass.pickMethod(methodName, argClasses);
            if (method != null)
                return tuple(delegateMetaClass.invokeMethod(delegate, methodName, originalArguments), method);
        }
        if (method == null && owner != closure) {
            MetaClass ownerMetaClass = lookupObjectMetaClass(owner);
            method = ownerMetaClass.pickMethod(methodName, argClasses);
            if (method != null)
                return tuple(ownerMetaClass.invokeMethod(owner, methodName, originalArguments), method);
        }

        return tuple(InvokeMethodResult.NONE, method);
    }

    private enum InvokeMethodResult {
        NONE
    }

    public boolean isPermissivePropertyAccess() {
        return permissivePropertyAccess;
    }

    public void setPermissivePropertyAccess(boolean permissivePropertyAccess) {
        this.permissivePropertyAccess = permissivePropertyAccess;
    }
}
