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
package org.codehaus.groovy.reflection;

import groovy.lang.*;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.runtime.callsite.CallSiteClassLoader;
import org.codehaus.groovy.util.LazySoftReference;
import org.codehaus.groovy.util.LazyReference;
import org.codehaus.groovy.util.FastArray;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author Alex.Tkachman
 */
public class CachedClass {
    private final Class cachedClass;
    public ClassInfo classInfo;

    private final LazySoftReference<CachedField[]> fields = new LazySoftReference<CachedField[]>() {
        public CachedField[] initValue() {
            final Field[] declaredFields = (Field[])
               AccessController.doPrivileged(new PrivilegedAction/*<Field[]>*/() {
                   public /*Field[]*/ Object run() {
                       final Field[] df = getTheClass().getDeclaredFields();
                       AccessibleObject.setAccessible(df, true);
                       return df;                                                   
                   }
               });
            CachedField [] fields = new CachedField[declaredFields.length];
            for (int i = 0; i != fields.length; ++i)
                fields[i] = new CachedField(declaredFields[i]);
            return fields;
        }
    };

    private LazySoftReference<CachedConstructor[]> constructors = new LazySoftReference<CachedConstructor[]>() {
        public CachedConstructor[] initValue() {
            final Constructor[] declaredConstructors = (Constructor[])
               AccessController.doPrivileged(new PrivilegedAction/*<Constructor[]>*/() {
                   public /*Constructor[]*/ Object run() {
                       return getTheClass().getDeclaredConstructors();
                   }
               });
            CachedConstructor [] constructors = new CachedConstructor[declaredConstructors.length];
            for (int i = 0; i != constructors.length; ++i)
                constructors[i] = new CachedConstructor(CachedClass.this, declaredConstructors[i]);
            return constructors;
        }
    };

    private LazySoftReference<CachedMethod[]> methods = new LazySoftReference<CachedMethod[]>() {
        public CachedMethod[] initValue() {
            final Method[] declaredMethods = (Method[])
               AccessController.doPrivileged(new PrivilegedAction/*<Method[]>*/() {
                   public /*Method[]*/ Object run() {
                       final Method[] dm = getTheClass().getDeclaredMethods();
                       AccessibleObject.setAccessible(dm, true);
                       return dm;
                   }
               });
            ArrayList methods = new ArrayList(declaredMethods.length);
            ArrayList mopMethods = new ArrayList(declaredMethods.length);
            for (int i = 0; i != declaredMethods.length; ++i) {
                final CachedMethod cachedMethod = new CachedMethod(CachedClass.this, declaredMethods[i]);
                final String name = cachedMethod.getName();

                if (name.indexOf('+') >= 0) {
                    // Skip Synthetic methods inserted by JDK 1.5 compilers and later
                    continue;
                } /*else if (Modifier.isAbstract(reflectionMethod.getModifiers())) {
                   continue;
                }*/

                if (name.startsWith("this$") || name.startsWith("super$"))
                  mopMethods.add(cachedMethod);
                else
                  methods.add(cachedMethod);
            }
            CachedMethod [] resMethods = (CachedMethod[]) methods.toArray(new CachedMethod[methods.size()]);
            Arrays.sort(resMethods);

            final CachedClass superClass = getCachedSuperClass();
            if (superClass != null) {
                superClass.getMethods();
                final CachedMethod[] superMopMethods = superClass.mopMethods;
                for (int i = 0; i != superMopMethods.length; ++i)
                  mopMethods.add(superMopMethods[i]);
            }
            CachedClass.this.mopMethods = (CachedMethod[]) mopMethods.toArray(new CachedMethod[mopMethods.size()]);
            Arrays.sort(CachedClass.this.mopMethods, CachedMethodComparatorByName.INSTANCE);

            return resMethods;
        }
    };

    private LazyReference<CachedClass> cachedSuperClass = new LazyReference<CachedClass>() {
        public CachedClass initValue() {
            if (!isArray)
              return ReflectionCache.getCachedClass(getTheClass().getSuperclass());
            else
              if (cachedClass.getComponentType().isPrimitive() || cachedClass.getComponentType() == Object.class)
                return ReflectionCache.OBJECT_CLASS;
              else
                return ReflectionCache.OBJECT_ARRAY_CLASS;
        }
    };

    private final LazySoftReference<CallSiteClassLoader> callSiteClassLoader = new LazySoftReference<CallSiteClassLoader>() {
        public CallSiteClassLoader initValue() {
            return
               AccessController.doPrivileged(new PrivilegedAction<CallSiteClassLoader>() {
                   public CallSiteClassLoader run() {
                       return new CallSiteClassLoader(CachedClass.this.cachedClass);
                   }
               });
        }
    };

    static final MetaMethod[] EMPTY = new MetaMethod[0];

    int hashCode;

    public  CachedMethod [] mopMethods;
    public static final CachedClass[] EMPTY_ARRAY = new CachedClass[0];

    private final LazyReference<Set<CachedClass>> declaredInterfaces = new LazyReference<Set<CachedClass>> () {
        public Set<CachedClass> initValue() {
            HashSet<CachedClass> res = new HashSet<CachedClass> (0);

            Class[] classes = getTheClass().getInterfaces();
            for (int i = 0; i < classes.length; i++) {
                res.add(ReflectionCache.getCachedClass(classes[i]));
            }
            return res;
        }
    };

    private final LazySoftReference<Set<CachedClass>> interfaces = new LazySoftReference<Set<CachedClass>> () {
        public Set<CachedClass> initValue() {
            HashSet<CachedClass> res = new HashSet<CachedClass> (0);

            if (getTheClass().isInterface())
              res.add(CachedClass.this);

            Class[] classes = getTheClass().getInterfaces();
            for (int i = 0; i < classes.length; i++) {
                final CachedClass aClass = ReflectionCache.getCachedClass(classes[i]);
                if (!res.contains(aClass))
                  res.addAll(aClass.getInterfaces());
            }

            final CachedClass superClass = getCachedSuperClass();
            if (superClass != null)
              res.addAll(superClass.getInterfaces());

            return res;
        }
    };

    public final boolean isArray;
    public final boolean isPrimitive;
    public final int modifiers;
    int distance = -1;
    public final boolean isInterface;
    public final boolean isNumber;

    public CachedClass(Class klazz, ClassInfo classInfo) {
        cachedClass = klazz;
        this.classInfo = classInfo;
        isArray = klazz.isArray();
        isPrimitive = klazz.isPrimitive();
        modifiers = klazz.getModifiers();
        isInterface = klazz.isInterface();
        isNumber = Number.class.isAssignableFrom(klazz);

        for (Iterator it = getInterfaces().iterator(); it.hasNext(); ) {
            CachedClass inf = (CachedClass) it.next();
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
        for (int i = 0; i < methods.length; i++) {
            CachedMethod m = methods[i];
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
        synchronized (getTheClass()) {
            if (distance == -1) {
                int distance = 0;
                for (Class klazz= getTheClass(); klazz != null; klazz = klazz.getSuperclass()) {
                    distance++;
                }
                this.distance = distance;
            }
            return distance;
        }
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

    public void box(BytecodeHelper helper) {
        helper.box(getTheClass());
    }

    public void unbox(BytecodeHelper helper) {
        helper.unbox(getTheClass());
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void doCast(BytecodeHelper helper) {
        helper.doCast(getTheClass());
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
        ArrayList<MetaMethod> arr = new ArrayList<MetaMethod>();
        arr.addAll(Arrays.asList(classInfo.newMetaMethods));

        final MetaClass metaClass = classInfo.getStrongMetaClass();
        if (metaClass != null && metaClass instanceof ExpandoMetaClass) {
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

        return arr.toArray(new MetaMethod[arr.size()]);
    }

    private void addSubclassExpandos(ArrayList<MetaMethod> arr, MetaClass mc) {
        if (mc != null && mc instanceof ExpandoMetaClass) {
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

    public void setNewMopMethods(List arr) {
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

    private void updateSetNewMopMethods(List arr) {
        if (arr != null) {
            final MetaMethod[] metaMethods = (MetaMethod[]) arr.toArray(new MetaMethod[arr.size()]);
            classInfo.dgmMetaMethods = metaMethods;
            classInfo.newMetaMethods = metaMethods;
        }
        else
            classInfo.newMetaMethods = classInfo.dgmMetaMethods;
    }

    public void addNewMopMethods(List arr) {
        final MetaClass metaClass = classInfo.getStrongMetaClass();
        if (metaClass != null) {
          if (metaClass.getClass() == MetaClassImpl.class) {
              classInfo.setStrongMetaClass(null);
              updateAddNewMopMethods(arr);
              classInfo.setStrongMetaClass(new MetaClassImpl(metaClass.getTheClass()));
              return;
          }

          if (metaClass.getClass() == ExpandoMetaClass.class) {
              ExpandoMetaClass emc = (ExpandoMetaClass)metaClass;
              classInfo.setStrongMetaClass(null);
              updateAddNewMopMethods(arr);
              ExpandoMetaClass newEmc = new ExpandoMetaClass(metaClass.getTheClass());
              for (Iterator it = emc.getExpandoMethods().iterator(); it.hasNext(); ) {
                  newEmc.registerInstanceMethod((MetaMethod) it.next());
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

    private void updateAddNewMopMethods(List arr) {
        ArrayList res = new ArrayList();
        res.addAll(Arrays.asList(classInfo.newMetaMethods));
        res.addAll(arr);

        final MetaMethod[] metaMethods = (MetaMethod[]) res.toArray(new MetaMethod[res.size()]);
        classInfo.newMetaMethods = metaMethods;
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

    public static class CachedMethodComparatorByName implements Comparator {
        public static final Comparator INSTANCE = new CachedMethodComparatorByName();

        public int compare(Object o1, Object o2) {
              return ((CachedMethod)o1).getName().compareTo(((CachedMethod)o2).getName());
        }
    }

    public static class CachedMethodComparatorWithString implements Comparator {
        public static final Comparator INSTANCE = new CachedMethodComparatorWithString();

        public int compare(Object o1, Object o2) {
            if (o1 instanceof CachedMethod)
              return ((CachedMethod)o1).getName().compareTo((String)o2);
            else
              return ((String)o1).compareTo(((CachedMethod)o2).getName());
        }
    }

    public String toString() {
        return cachedClass.toString();
    }
}
