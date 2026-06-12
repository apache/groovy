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
package org.codehaus.groovy.reflection;

import groovy.lang.Closure;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.callsite.CallSiteClassLoader;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass;
import org.codehaus.groovy.util.FastArray;
import org.codehaus.groovy.util.LazyReference;
import org.codehaus.groovy.util.ReferenceBundle;

import java.io.Serial;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.reflect.Modifier.isProtected;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Caches reflection information for a class including its fields, methods, constructors, and interfaces.
 * <p>
 * Lazily initializes cached members on first access and provides utility methods for searching methods,
 * checking assignability, and managing MOP methods. Instances are typically obtained via {@link ReflectionCache}.
 */
public class CachedClass {

    /**
     * An empty array constant representing zero cached classes.
     */
    public static final CachedClass[] EMPTY_ARRAY = new CachedClass[0];

    private static ReferenceBundle softBundle = ReferenceBundle.getSoftBundle();

    private static <M extends AccessibleObject & Member> boolean isAccessibleOrCanSetAccessible(M m) {
        final int modifiers = m.getModifiers();
        final Class<?> declaringClass = m.getDeclaringClass();
        if (isPublic(modifiers) && declaringClass.getPackageName().startsWith("sun.")) {
            return false;
        }
        if (isProtected(modifiers) && isPublic(declaringClass.getModifiers())) {
            return true;
        }
        return ReflectionUtils.checkCanSetAccessible(m, CachedClass.class);
    }

    private final LazyReference<CachedField[]> fields = new LazyReference<CachedField[]>(softBundle) {
        @Serial
        private static final long serialVersionUID = 5450437842165410025L;

        @Override
        public CachedField[] initValue() {
            return Arrays.stream(getTheClass().getDeclaredFields())
                .filter(CachedClass::isAccessibleOrCanSetAccessible)
                .map(CachedField::new).toArray(CachedField[]::new);
        }
    };

    private final LazyReference<CachedConstructor[]> constructors = new LazyReference<CachedConstructor[]>(softBundle) {
        @Serial
        private static final long serialVersionUID = -5834446523983631635L;

        @Override
        public CachedConstructor[] initValue() {
            return Arrays.stream(getTheClass().getDeclaredConstructors())
                .filter(c -> !c.isSynthetic()) // GROOVY-9245: exclude inner class ctors
                .filter(CachedClass::isAccessibleOrCanSetAccessible)
                .map(c -> new CachedConstructor(CachedClass.this, c))
                .toArray(CachedConstructor[]::new);
        }
    };

    private final LazyReference<CachedMethod[]> methods = new LazyReference<CachedMethod[]>(softBundle) {
        @Serial
        private static final long serialVersionUID = 6347586066597418308L;

        @Override
        public CachedMethod[] initValue() {
            CachedMethod[] declaredMethods;
            try {
                declaredMethods = Arrays.stream(getTheClass().getDeclaredMethods())
                    .filter(CachedClass::isAccessibleOrCanSetAccessible)
                    .map(m -> new CachedMethod(CachedClass.this, m))
                    .toArray(CachedMethod[]::new);
            } catch (LinkageError e) {
                declaredMethods = CachedMethod.EMPTY_ARRAY;
            }

            List<CachedMethod> methods = new ArrayList<>(declaredMethods.length);
            List<CachedMethod> mopMethods = new ArrayList<>(declaredMethods.length);
            for (CachedMethod method : declaredMethods) {
                String name = method.getName();
                if (name.startsWith("this$") || name.startsWith("super$")) {
                    mopMethods.add(method);
                } else {
                    methods.add(method);
                }
            }
            Collections.sort(methods);

            CachedClass superClass = getCachedSuperClass();
            if (superClass != null) {
                superClass.getMethods(); // populate mopMethods
                Collections.addAll(mopMethods, superClass.mopMethods);
            }
            if (mopMethods.size() > 1)
                mopMethods.sort(CachedMethodComparatorByName.INSTANCE);

            CachedClass.this.mopMethods = mopMethods.toArray(CachedMethod.EMPTY_ARRAY);

            return methods.toArray(CachedMethod.EMPTY_ARRAY);
        }
    };

    private final LazyReference<CallSiteClassLoader> callSiteClassLoader = new LazyReference<CallSiteClassLoader>(softBundle) {
        @Serial
        private static final long serialVersionUID = 4410385968428074090L;

        @Override
        public CallSiteClassLoader initValue() {
            return new CallSiteClassLoader(CachedClass.this.cachedClass);
        }
    };

    private final LazyReference<Collection<ClassInfo>> hierarchy = new LazyReference<Collection<ClassInfo>>(softBundle) {
        @Serial
        private static final long serialVersionUID = 7166687623678851596L;

        @Override
        public Collection<ClassInfo> initValue() {
            Set<ClassInfo> res = new LinkedHashSet<>();
            res.add(classInfo);

            for (CachedClass iface : getDeclaredInterfaces()) {
                res.addAll(iface.getHierarchy());
            }
            CachedClass superClass = getCachedSuperClass();
            if (superClass != null) {
                res.addAll(superClass.getHierarchy());
            }
            if (isInterface) {
                res.add(ReflectionCache.OBJECT_CLASS.classInfo);
            }
            return res;
        }
    };

    private final LazyReference<Set<CachedClass>> declaredInterfaces = new LazyReference<Set<CachedClass>>(softBundle) {
        @Serial
        private static final long serialVersionUID = 2139190436931329873L;

        @Override
        public Set<CachedClass> initValue() {
            Class[] classes = getTheClass().getInterfaces();
            Set<CachedClass> res = new LinkedHashSet<>(classes.length);
            for (Class cls : classes) {
                res.add(ReflectionCache.getCachedClass(cls));
            }
            return res;
        }
    };

    private final LazyReference<Set<CachedClass>> interfaces = new LazyReference<Set<CachedClass>>(softBundle) {
        @Serial
        private static final long serialVersionUID = 4060471819464086940L;

        @Override
        public Set<CachedClass> initValue() {
            Class<?> theClass = getTheClass();
            Class[] classes = theClass.getInterfaces();
            Set<CachedClass> res = new HashSet<>(classes.length + 8);
            if (theClass.isInterface()) {
                res.add(CachedClass.this);
            }

            for (Class cls : classes) {
                CachedClass aClass = ReflectionCache.getCachedClass(cls);
                if (!res.contains(aClass))
                    res.addAll(aClass.getInterfaces());
            }

            CachedClass superClass = getCachedSuperClass();
            if (superClass != null) {
                res.addAll(superClass.getInterfaces());
            }
            return res;
        }
    };

    private final LazyReference<CachedClass> superClass = new LazyReference<CachedClass>(softBundle) {
        @Serial
        private static final long serialVersionUID = -4663740963306806058L;

        @Override
        public CachedClass initValue() {
            if (!isArray) {
                return ReflectionCache.getCachedClass(getTheClass().getSuperclass());
            } else if (cachedClass.getComponentType().isPrimitive() || cachedClass.getComponentType() == Object.class) {
                return ReflectionCache.OBJECT_CLASS;
            } else {
                return ReflectionCache.OBJECT_ARRAY_CLASS;
            }
        }
    };

    //--------------------------------------------------------------------------

    private final Class<?> cachedClass;
    /**
     * {@code ClassInfo} object for this class, holding metadata about the class.
     */
    public ClassInfo classInfo;
    /**
     * {@code true} if the cached class is an array type, {@code false} otherwise.
     */
    public final boolean isArray;
    /**
     * {@code true} if the cached class is a primitive type, {@code false} otherwise.
     */
    public final boolean isPrimitive;
    /**
     * The modifiers of this class (e.g., public, final, abstract).
     * See {@link java.lang.reflect.Modifier} for constants.
     */
    public final int modifiers;
    /**
     * {@code true} if the cached class is an interface, {@code false} otherwise.
     */
    public final boolean isInterface;
    /**
     * {@code true} if the cached class is assignable from {@link Number}, {@code false} otherwise.
     */
    public final boolean isNumber;
    /**
     * Array of MOP (Meta-Object Protocol) methods, including inherited methods.
     * Updated when methods are added via {@link #setNewMopMethods(List)} or {@link #addNewMopMethods(List)}.
     */
    public CachedMethod[] mopMethods;
    int distance = -1;
    int hashCode;

    /**
     * Constructs a {@code CachedClass} for the given class with the specified {@code ClassInfo}.
     *
     * @param klazz the class to cache reflection information for
     * @param classInfo the {@code ClassInfo} object associated with this cached class
     */
    public CachedClass(Class<?> klazz, ClassInfo classInfo) {
        cachedClass = klazz;
        this.classInfo = classInfo;
        isArray = klazz.isArray();
        isPrimitive = klazz.isPrimitive();
        modifiers = klazz.getModifiers();
        isInterface = klazz.isInterface();
        isNumber = Number.class.isAssignableFrom(klazz);
    }

    /**
     * Returns the cached superclass of this class, if any.
     *
     * @return the cached superclass, or {@code null} if this is {@code Object} or an interface
     */
    public CachedClass getCachedSuperClass() {
        return superClass.get();
    }

    /**
     * Returns a set of all interfaces implemented or extended by this class, including inherited interfaces.
     * If this class is an interface, it includes itself in the set.
     *
     * @return a set of cached interfaces
     */
    public Set<CachedClass> getInterfaces() {
        return interfaces.get();
    }

    /**
     * Returns a set of interfaces directly declared by this class (not including inherited ones).
     *
     * @return a set of directly declared cached interfaces
     */
    public Set<CachedClass> getDeclaredInterfaces() {
        return declaredInterfaces.get();
    }

    /**
     * Returns all public and protected methods of this class and its superclasses.
     *
     * @return an array of cached methods, sorted by name
     */
    public CachedMethod[] getMethods() {
        return methods.get();
    }

    /**
     * Returns all public and protected fields declared in this class.
     *
     * @return an array of cached fields
     */
    public CachedField[] getFields() {
        return fields.get();
    }

    /**
     * Returns all public and protected constructors declared in this class.
     *
     * @return an array of cached constructors
     */
    public CachedConstructor[] getConstructors() {
        return constructors.get();
    }

    /**
     * Searches for a method with the specified name and parameter types in this class.
     *
     * @param name the name of the method to search for
     * @param parameterTypes the parameter types of the method
     * @return the matching cached method, or {@code null} if not found. If multiple matches exist,
     *         returns the one with the most specific return type
     */
    public CachedMethod searchMethods(String name, CachedClass[] parameterTypes) {
        CachedMethod[] methods = getMethods();

        CachedMethod res = null;
        for (CachedMethod m : methods) {
            if (m.getName().equals(name)
                    && arrayContentsEq(parameterTypes, m.getParameterTypes())
                    && (res == null || res.getReturnType().isAssignableFrom(m.getReturnType())))
                res = m;
        }

        return res;
    }

    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null) {
            return a1.length == 0;
        }

        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the access modifier flags for this class.
     *
     * @return the modifiers as an integer, see {@link java.lang.reflect.Modifier}
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * Coerces an argument to a form suitable for this class.
     * <p>
     * By default, returns the argument unchanged. Subclasses may override to provide
     * type-specific coercion logic.
     *
     * @param argument the argument to coerce
     * @return the coerced argument
     */
    public Object coerceArgument(Object argument) {
        return argument;
    }

    /**
     * Computes the distance from this class to the superclass hierarchy, used for type matching.
     * The distance is the number of steps up the inheritance chain from this class to {@code Object}.
     *
     * @return the superclass distance (1 for Object, 2 for direct subclasses of Object, etc.)
     */
    public int getSuperClassDistance() {
        if (distance >= 0) return distance;

        int distance = 0;
        for (Class klazz = getTheClass(); klazz != null; klazz = klazz.getSuperclass()) {
            distance += 1;
        }
        this.distance = distance;
        return distance;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
          hashCode = super.hashCode();
          if (hashCode == 0)
            hashCode = 0xcafebebe;
        }
        return hashCode;
    }

    /**
     * Returns whether this class represents a primitive type.
     *
     * @return {@code true} if this is a primitive type (int, double, etc.), {@code false} otherwise
     */
    public boolean isPrimitive() {
        return isPrimitive;
    }

    /**
     * Returns whether this class represents the void type.
     *
     * @return {@code true} if this class is void, {@code false} otherwise
     */
    public boolean isVoid() {
        return getTheClass() == void.class;
    }

    /**
     * Returns whether this class represents an interface type.
     *
     * @return {@code true} if this is an interface, {@code false} otherwise
     */
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Returns the fully qualified name of this class.
     *
     * @return the class name
     */
    public String getName() {
        return getTheClass().getName();
    }

    /**
     * Returns the bytecode type descriptor for this class.
     *
     * @return the type descriptor string
     */
    public String getTypeDescription() {
        return BytecodeHelper.getTypeDescription(getTheClass());
    }

    /**
     * Returns the underlying Java {@code Class} object wrapped by this cached class.
     *
     * @return the class object
     */
    public final Class<?> getTheClass() {
        return cachedClass;
    }

    /**
     * Returns a list of new meta-methods added to this class's meta-class.
     * Includes expando methods and methods from all superclasses and interfaces.
     *
     * @return an array of meta-methods
     */
    public MetaMethod[] getNewMetaMethods() {
        List<MetaMethod> metaMethods = new ArrayList<>();

        Collections.addAll(metaMethods, classInfo.newMetaMethods);

        if (classInfo.getStrongMetaClass() instanceof ExpandoMetaClass emc) {
            List<MetaMethod> expandoMethods = emc.getExpandoMethods();
            metaMethods.addAll(expandoMethods);
        }

        if (isInterface) {
            MetaClass mc = ReflectionCache.OBJECT_CLASS.classInfo.getStrongMetaClass();
            addSubclassExpandos(metaMethods, mc);
        } else {
            for (CachedClass cc = this; cc != null; cc = cc.getCachedSuperClass()) {
                MetaClass mc = cc.classInfo.getStrongMetaClass();
                addSubclassExpandos(metaMethods, mc);
            }
        }

        for (CachedClass cc : getInterfaces()) { // includes this if interface
            MetaClass mc = cc.classInfo.getStrongMetaClass();
            addSubclassExpandos(metaMethods, mc);
        }

        return metaMethods.toArray(MetaMethod[]::new);
    }

    private void addSubclassExpandos(List<MetaMethod> arr, MetaClass mc) {
        if (mc instanceof ExpandoMetaClass emc) {
            for (Object mm : emc.getExpandoSubclassMethods()) {
                if (mm instanceof MetaMethod method) {
                    if (method.getDeclaringClass() == this)
                      arr.add(method);
                }
                else {
                    FastArray farr = (FastArray) mm;
                    for (int i = 0; i != farr.size; ++i) {
                        MetaMethod method = (MetaMethod) farr.get(i);
                        if (method.getDeclaringClass() == this)
                          arr.add(method);
                    }
                }
            }
        }
    }

    /**
     * Replaces the current MOP methods with the specified list of meta-methods.
     * Reinitializes the meta-class to reflect the new methods.
     *
     * @param arr the new list of meta-methods to use, or {@code null} to revert to default
     * @throws GroovyRuntimeException if a strong custom meta-class is already set
     */
    public void setNewMopMethods(List<MetaMethod> arr) {
        final MetaClass metaClass = classInfo.getStrongMetaClass();
        if (metaClass != null) {
          if (metaClass.getClass() == MetaClassImpl.class) {
              classInfo.setStrongMetaClass(null);
              updateSetNewMopMethods(arr);
              MetaClassImpl mci = new MetaClassImpl(metaClass.getTheClass());
              mci.initialize();
              classInfo.setStrongMetaClass(mci);
              return;
          }

          if (metaClass.getClass() == ExpandoMetaClass.class) {
              classInfo.setStrongMetaClass(null);
              updateSetNewMopMethods(arr);
              ExpandoMetaClass newEmc = new ExpandoMetaClass(metaClass.getTheClass());
              newEmc.initialize();
              classInfo.setStrongMetaClass(newEmc);
              return;
          }

          throw new GroovyRuntimeException("Can't add methods to class " + getTheClass().getName() + ". Strong custom meta class already set.");
        }

        classInfo.setWeakMetaClass(null);
        updateSetNewMopMethods(arr);
    }

    private void updateSetNewMopMethods(List<MetaMethod> arr) {
        if (arr != null) {
            final MetaMethod[] metaMethods = arr.toArray(MetaMethod.EMPTY_ARRAY);
            classInfo.dgmMetaMethods = metaMethods;
            classInfo.newMetaMethods = metaMethods;
        }
        else
            classInfo.newMetaMethods = classInfo.dgmMetaMethods;
    }

    /**
     * Adds a list of new meta-methods to this class's existing MOP methods.
     * Reinitializes the meta-class to incorporate the new methods alongside existing ones.
     *
     * @param arr the list of meta-methods to add
     * @throws GroovyRuntimeException if a strong custom meta-class is already set
     */
    public void addNewMopMethods(List<MetaMethod> arr) {
        final MetaClass metaClass = classInfo.getStrongMetaClass();
        if (metaClass != null) {
          if (metaClass.getClass() == MetaClassImpl.class) {
              classInfo.setStrongMetaClass(null);
              List<MetaMethod> res = new ArrayList<>();
              Collections.addAll(res, classInfo.newMetaMethods);
              res.addAll(arr);
              updateSetNewMopMethods(res);
              MetaClassImpl answer = new MetaClassImpl(((MetaClassImpl)metaClass).getRegistry(),metaClass.getTheClass());
              answer.initialize();
              classInfo.setStrongMetaClass(answer);
              return;
          }

          if (metaClass.getClass() == ExpandoMetaClass.class) {
              ExpandoMetaClass emc = (ExpandoMetaClass)metaClass;
              classInfo.setStrongMetaClass(null);
              updateAddNewMopMethods(arr);
              ExpandoMetaClass newEmc = new ExpandoMetaClass(metaClass.getTheClass());
              for (MetaMethod mm : emc.getExpandoMethods()) {
                  newEmc.registerInstanceMethod(mm);
              }
              newEmc.initialize();
              classInfo.setStrongMetaClass(newEmc);
              return;
          }

          throw new GroovyRuntimeException("Can't add methods to class " + getTheClass().getName() + ". Strong custom meta class already set.");
        }

        classInfo.setWeakMetaClass(null);

        updateAddNewMopMethods(arr);
    }

    private void updateAddNewMopMethods(List<MetaMethod> arr) {
        List<MetaMethod> res = new ArrayList<>();
        res.addAll(Arrays.asList(classInfo.newMetaMethods));
        res.addAll(arr);
        classInfo.newMetaMethods = res.toArray(MetaMethod.EMPTY_ARRAY);
        var theClass = classInfo.getCachedClass().getTheClass();
        if (theClass == Closure.class || theClass == Class.class) {
            ClosureMetaClass.resetCachedMetaClasses();
        }
    }

    /**
     * Returns whether the argument class is assignable to this class.
     * Returns {@code true} for {@code null} arguments.
     *
     * @param argument the class to check
     * @return {@code true} if {@code argument} is {@code null} or assignable to this class
     */
    public boolean isAssignableFrom(Class<?> argument) {
        return argument == null || getTheClass().isAssignableFrom(argument);
    }

    /**
     * Returns whether an object instance is directly assignable to this class.
     *
     * @param argument the object to check
     * @return {@code true} if the object's class is assignable to this class
     */
    public boolean isDirectlyAssignable(Object argument) {
        return getTheClass().isAssignableFrom(argument.getClass());
    }

    /**
     * Returns the class loader used for generating call site classes for this cached class.
     *
     * @return a call site class loader
     */
    public CallSiteClassLoader getCallSiteLoader() {
        return callSiteClassLoader.get();
    }

    /**
     * Returns the complete type hierarchy for this class, including superclasses and interfaces.
     *
     * @return a collection of {@code ClassInfo} objects in the hierarchy
     */
    public Collection<ClassInfo> getHierarchy() {
        return hierarchy.get();
    }

    /**
     * Returns this cached class (for compatibility).
     *
     * @return this {@code CachedClass}
     */
    public CachedClass getCachedClass() {
        return this;
    }

    @Override
    public String toString() {
        return cachedClass.toString();
    }

    //--------------------------------------------------------------------------

    /**
     * Comparator for ordering cached methods by name.
     */
    public static class CachedMethodComparatorByName implements Comparator<CachedMethod> {
        /**
         * Singleton instance of this comparator.
         */
        public static final Comparator INSTANCE = new CachedMethodComparatorByName();

        @Override
        public int compare(CachedMethod o1, CachedMethod o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /**
     * Comparator for ordering cached methods against string names.
     * Allows mixed comparisons between CachedMethod and String instances.
     */
    public static class CachedMethodComparatorWithString implements Comparator {
        /**
         * Singleton instance of this comparator.
         */
        public static final Comparator INSTANCE = new CachedMethodComparatorWithString();

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof CachedMethod)
                return ((CachedMethod) o1).getName().compareTo((String) o2);
            else
                return ((String) o1).compareTo(((CachedMethod) o2).getName());
        }
    }
}
