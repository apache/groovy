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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.codehaus.groovy.reflection.ReflectionUtils.checkCanSetAccessible;

public class CachedClass {

    public static final CachedClass[] EMPTY_ARRAY = new CachedClass[0];

    private static ReferenceBundle softBundle = ReferenceBundle.getSoftBundle();

    private final LazyReference<CachedField[]> fields = new LazyReference<CachedField[]>(softBundle) {
        private static final long serialVersionUID = 5450437842165410025L;

        @Override
        public CachedField[] initValue() {
            PrivilegedAction<CachedField[]> action = () -> Arrays.stream(getTheClass().getDeclaredFields())
                .filter(f -> checkCanSetAccessible(f, CachedClass.class))
                .map(CachedField::new).toArray(CachedField[]::new);
            return AccessController.doPrivileged(action);
        }
    };

    private LazyReference<CachedConstructor[]> constructors = new LazyReference<CachedConstructor[]>(softBundle) {
        private static final long serialVersionUID = -5834446523983631635L;

        @Override
        public CachedConstructor[] initValue() {
            PrivilegedAction<CachedConstructor[]> action = () -> Arrays.stream(getTheClass().getDeclaredConstructors())
                .filter(c -> !c.isSynthetic()) // GROOVY-9245: exclude inner class ctors
                .filter(c -> checkCanSetAccessible(c, CachedClass.class))
                .map(c -> new CachedConstructor(CachedClass.this, c))
                .toArray(CachedConstructor[]::new);
            return AccessController.doPrivileged(action);
        }
    };

    private final LazyReference<CachedMethod[]> methods = new LazyReference<CachedMethod[]>(softBundle) {
        private static final long serialVersionUID = 6347586066597418308L;

        @Override
        public CachedMethod[] initValue() {
            PrivilegedAction<CachedMethod[]> action = () -> {
                try {
                    return Arrays.stream(getTheClass().getDeclaredMethods())
                        // skip synthetic methods inserted by JDK 1.5+ compilers
                        .filter(m -> !m.isBridge() && m.getName().indexOf('+') < 0)
                        .filter(m -> checkCanSetAccessible(m, CachedClass.class))
                        .map(m -> new CachedMethod(CachedClass.this, m))
                        .toArray(CachedMethod[]::new);
                } catch (LinkageError e) {
                    return CachedMethod.EMPTY_ARRAY;
                }
            };
            CachedMethod[] declaredMethods = AccessController.doPrivileged(action);

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
            Collections.sort(mopMethods, CachedMethodComparatorByName.INSTANCE);
            CachedClass.this.mopMethods = mopMethods.toArray(CachedMethod.EMPTY_ARRAY);

            return methods.toArray(CachedMethod.EMPTY_ARRAY);
        }
    };

    private LazyReference<CachedClass> cachedSuperClass = new LazyReference<CachedClass>(softBundle) {
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

    private final LazyReference<CallSiteClassLoader> callSiteClassLoader = new LazyReference<CallSiteClassLoader>(softBundle) {
        private static final long serialVersionUID = 4410385968428074090L;

        @Override
        public CallSiteClassLoader initValue() {
            return AccessController.doPrivileged((PrivilegedAction<CallSiteClassLoader>) () -> new CallSiteClassLoader(CachedClass.this.cachedClass));
        }
    };

    private final LazyReference<Collection<ClassInfo>> hierarchy = new LazyReference<Collection<ClassInfo>>(softBundle) {
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
        private static final long serialVersionUID = 2139190436931329873L;

        @Override
        public Set<CachedClass> initValue() {
            Set<CachedClass> res = new HashSet<>(0);

            Class[] classes = getTheClass().getInterfaces();
            for (Class cls : classes) {
                res.add(ReflectionCache.getCachedClass(cls));
            }
            return res;
        }
    };

    private final LazyReference<Set<CachedClass>> interfaces = new LazyReference<Set<CachedClass>>(softBundle) {
        private static final long serialVersionUID = 4060471819464086940L;

        @Override
        public Set<CachedClass> initValue() {
            Set<CachedClass> res = new HashSet<>(0);

            if (getTheClass().isInterface()) {
                res.add(CachedClass.this);
            }
            Class[] classes = getTheClass().getInterfaces();
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

    private final Class<?> cachedClass;
    public ClassInfo classInfo;
    public final boolean isArray;
    public final boolean isPrimitive;
    public final int modifiers;
    public final boolean isInterface;
    public final boolean isNumber;
    public CachedMethod[] mopMethods;
    int distance = -1;
    int hashCode;

    public CachedClass(Class<?> klazz, ClassInfo classInfo) {
        cachedClass = klazz;
        this.classInfo = classInfo;
        isArray = klazz.isArray();
        isPrimitive = klazz.isPrimitive();
        modifiers = klazz.getModifiers();
        isInterface = klazz.isInterface();
        isNumber = Number.class.isAssignableFrom(klazz);

        for (CachedClass inf : getInterfaces()) {
            ReflectionCache.isAssignableFrom(klazz, inf.cachedClass);
        }

        for (CachedClass cur = this; cur != null; cur = cur.getCachedSuperClass()) {
            ReflectionCache.setAssignableFrom(cur.cachedClass, klazz);
        }
    }

    public CachedClass getCachedSuperClass() {
        return cachedSuperClass.get();
    }

    public Set<CachedClass> getInterfaces() {
        return interfaces.get();
    }

    public Set<CachedClass> getDeclaredInterfaces() {
        return declaredInterfaces.get();
    }

    public CachedMethod[] getMethods() {
        return methods.get();
    }

    public CachedField[] getFields() {
        return fields.get();
    }

    public CachedConstructor[] getConstructors() {
        return constructors.get();
    }

    public CachedMethod searchMethods(String name, CachedClass[] parameterTypes) {
        CachedMethod[] methods = getMethods();

        CachedMethod res = null;
        for (CachedMethod m : methods) {
            if (m.getName().equals(name)
                    && ReflectionCache.arrayContentsEq(parameterTypes, m.getParameterTypes())
                    && (res == null || res.getReturnType().isAssignableFrom(m.getReturnType())))
                res = m;
        }

        return res;
    }

    public int getModifiers() {
        return modifiers;
    }

    public Object coerceArgument(Object argument) {
        return argument;
    }

    public int getSuperClassDistance() {
        if (distance >= 0) return distance;

        int distance = 0;
        for (Class klazz = getTheClass(); klazz != null; klazz = klazz.getSuperclass()) {
            distance += 1;
        }
        this.distance = distance;
        return distance;
    }

    public int hashCode() {
        if (hashCode == 0) {
          hashCode = super.hashCode();
          if (hashCode == 0)
            hashCode = 0xcafebebe;
        }
        return hashCode;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public boolean isVoid() {
        return getTheClass() == void.class;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public String getName() {
        return getTheClass().getName();
    }

    public String getTypeDescription() {
        return BytecodeHelper.getTypeDescription(getTheClass());
    }

    public final Class getTheClass() {
        return cachedClass;
    }

    public MetaMethod[] getNewMetaMethods() {
        List<MetaMethod> arr = new ArrayList<>(Arrays.asList(classInfo.newMetaMethods));

        final MetaClass metaClass = classInfo.getStrongMetaClass();
        if (metaClass instanceof ExpandoMetaClass) {
            arr.addAll(((ExpandoMetaClass)metaClass).getExpandoMethods());
        }

        if (isInterface) {
            MetaClass mc = ReflectionCache.OBJECT_CLASS.classInfo.getStrongMetaClass();
            addSubclassExpandos(arr, mc);
        }
        else {
            for (CachedClass cls = this; cls != null; cls = cls.getCachedSuperClass()) {
                MetaClass mc = cls.classInfo.getStrongMetaClass();
                addSubclassExpandos(arr, mc);
            }
        }

        for (CachedClass inf : getInterfaces()) {
            MetaClass mc = inf.classInfo.getStrongMetaClass();
            addSubclassExpandos(arr, mc);
        }

        return arr.toArray(MetaMethod.EMPTY_ARRAY);
    }

    private void addSubclassExpandos(List<MetaMethod> arr, MetaClass mc) {
        if (mc instanceof ExpandoMetaClass) {
            ExpandoMetaClass emc = (ExpandoMetaClass) mc;
            for (Object mm : emc.getExpandoSubclassMethods()) {
                if (mm instanceof MetaMethod) {
                    MetaMethod method = (MetaMethod) mm;
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

    public void setNewMopMethods(List<MetaMethod> arr) {
        final MetaClass metaClass = classInfo.getStrongMetaClass();
        if (metaClass != null) {
          if (metaClass.getClass() == MetaClassImpl.class) {
              classInfo.setStrongMetaClass(null);
              updateSetNewMopMethods(arr);
              classInfo.setStrongMetaClass(new MetaClassImpl(metaClass.getTheClass()));
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
        Class theClass = classInfo.getCachedClass().getTheClass();
        if (theClass == Closure.class || theClass == Class.class) {
            ClosureMetaClass.resetCachedMetaClasses();
        }
    }

    public boolean isAssignableFrom(Class argument) {
        return argument == null || ReflectionCache.isAssignableFrom(getTheClass(), argument);
    }

    public boolean isDirectlyAssignable(Object argument) {
        return ReflectionCache.isAssignableFrom(getTheClass(), argument.getClass());
    }

    public CallSiteClassLoader getCallSiteLoader() {
        return callSiteClassLoader.get();
    }

    public Collection<ClassInfo> getHierarchy() {
        return hierarchy.get();
    }

    public static class CachedMethodComparatorByName implements Comparator<CachedMethod> {
        public static final Comparator INSTANCE = new CachedMethodComparatorByName();

        @Override
        public int compare(CachedMethod o1, CachedMethod o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static class CachedMethodComparatorWithString implements Comparator {
        public static final Comparator INSTANCE = new CachedMethodComparatorWithString();

        public int compare(Object o1, Object o2) {
            if (o1 instanceof CachedMethod)
                return ((CachedMethod) o1).getName().compareTo((String) o2);
            else
                return ((String) o1).compareTo(((CachedMethod) o2).getName());
        }
    }

    @Override
    public String toString() {
        return cachedClass.toString();
    }

    /**
     * compatibility method
     * @return this
     */
    public CachedClass getCachedClass () {
        return this;
    }
}
