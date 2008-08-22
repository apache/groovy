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

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.runtime.Reflector;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author Alex.Tkachman
 */
public class CachedClass {
    private CachedClass cachedSuperClass;

    private static final MetaMethod[] EMPTY = new MetaMethod[0];

    int hashCode;

    private Reflector reflector;

    private volatile Object metaClassForClass; // either MetaClass or SoftReference<MetaClass>

    private CachedField[] fields;
    private CachedConstructor[] constructors;
    private CachedMethod[] methods;
    private final Class cachedClass;
    private MetaMethod[] newMetaMethods = EMPTY;
    public  CachedMethod [] mopMethods;
    public static final CachedClass[] EMPTY_ARRAY = new CachedClass[0];
    private Object staticMetaClassField;
    private static final Object NONE = new Object();

    public Set getInterfaces() {
        if (interfaces == null)  {
            interfaces = new HashSet (0);

            if (getTheClass().isInterface())
              interfaces.add(this);

            Class[] classes = getTheClass().getInterfaces();
            for (int i = 0; i < classes.length; i++) {
                final CachedClass aClass = ReflectionCache.getCachedClass(classes[i]);
                if (!interfaces.contains(aClass))
                  interfaces.addAll(aClass.getInterfaces());
            }

            final CachedClass superClass = getCachedSuperClass();
            if (superClass != null)
              interfaces.addAll(superClass.getInterfaces());
        }
        return interfaces;
    }

    private Set ownInterfaces;

    public Set getOwnInterfaces() {
        if (ownInterfaces == null)  {
            ownInterfaces = new HashSet (0);

            if (getTheClass().isInterface())
              ownInterfaces.add(this);

            Class[] classes = getTheClass().getInterfaces();
            for (int i = 0; i < classes.length; i++) {
                final CachedClass aClass = ReflectionCache.getCachedClass(classes[i]);
                if (!ownInterfaces.contains(aClass))
                  ownInterfaces.addAll(aClass.getInterfaces());
            }

            final CachedClass superClass = getCachedSuperClass();
            if (superClass != null)
              ownInterfaces.addAll(superClass.getInterfaces());
        }
        return ownInterfaces;
    }

    private Set interfaces;

    public final boolean isArray;
    public final boolean isPrimitive;
    public final int modifiers;
    int distance = -1;
    public final boolean isInterface;
    public final boolean isNumber;

    public CachedClass(Class klazz) {
        cachedClass = klazz;
        isArray = klazz.isArray();
        isPrimitive = klazz.isPrimitive();
        modifiers = klazz.getModifiers();
        isInterface = klazz.isInterface();
        isNumber = Number.class.isAssignableFrom(klazz);

    }

    /**
     * Initialization involves making calls back to ReflectionCache to popuplate
     * the "assignable from" structure.
     * Package scoped (like our constructor) because ReflectionCache is really the
     * only place we should be called from.
     *
     * We don't need to be synchronized here because ReflectionCache is careful to
     * make sure we're called exactly once.
     * By the same token we could however safely lock ourself.
     * But the idea here is to take out the bad locks.
     */
    final void initialize() {
        for (Iterator it = getInterfaces().iterator(); it.hasNext(); ) {
            CachedClass inf = (CachedClass) it.next();
            ReflectionCache.isAssignableFrom(cachedClass, inf.cachedClass);
        }

        // If we *were* going to lock the Class/CachedClass of our parents,
        // it would probably be a better idea to climb to the top then do the
        // locking from the top down to here.
        // But that shouldn't really be necessary since this is the wrong place for that.
        // One of the keys is probably that the constructor and initialization need to be
        // separated (like with MetaClassImpl).
        for (CachedClass cur = this; cur != null; cur = cur.getCachedSuperClass()) {
            ReflectionCache.setAssignableFrom(cur.cachedClass, cachedClass);
        }
    }

    /**
     * This can't be final because ReflectionClass has an inner class that extends
     * CachedClass for java.lang.Object (ReflectionClass.OBJECT_CLASS) that returns
     * null for this method.
     */
    public CachedClass getCachedSuperClass() {
        if (cachedSuperClass == null) {
            if (!isArray)
              cachedSuperClass = ReflectionCache.getCachedClass(getTheClass().getSuperclass());
            else
              if (cachedClass.getComponentType().isPrimitive() || cachedClass.getComponentType() == Object.class)
                cachedSuperClass = ReflectionCache.OBJECT_CLASS;
              else
                cachedSuperClass = ReflectionCache.OBJECT_ARRAY_CLASS;
        }

        return cachedSuperClass;
    }

    public synchronized CachedMethod[] getMethods() {
        if (methods == null) {
            final Method[] declaredMethods = (Method[])
               AccessController.doPrivileged(new PrivilegedAction/*<Method[]>*/() {
                   public /*Method[]*/ Object run() {
                       return getTheClass().getDeclaredMethods();
                   }
               });
            ArrayList methods = new ArrayList(declaredMethods.length);
            ArrayList mopMethods = new ArrayList(declaredMethods.length);
            for (int i = 0; i != declaredMethods.length; ++i) {
                final CachedMethod cachedMethod = new CachedMethod(this, declaredMethods[i]);
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
            this.methods = (CachedMethod[]) methods.toArray(new CachedMethod[methods.size()]);
            Arrays.sort(this.methods);

            final CachedClass superClass = getCachedSuperClass();
            if (superClass != null) {
                superClass.getMethods();
                final CachedMethod[] superMopMethods = superClass.mopMethods;
                for (int i = 0; i != superMopMethods.length; ++i)
                  mopMethods.add(superMopMethods[i]);
            }
            this.mopMethods = (CachedMethod[]) mopMethods.toArray(new CachedMethod[mopMethods.size()]);
            Arrays.sort(this.mopMethods, CachedMethodComparatorByName.INSTANCE);
        }
        return methods;
    }

    public synchronized CachedField[] getFields() {
        if (fields == null) {

            final Field[] declaredFields = (Field[])
               AccessController.doPrivileged(new PrivilegedAction/*<Field[]>*/() {
                   public /*Field[]*/ Object run() {
                       return getTheClass().getDeclaredFields();
                   }
               });
            fields = new CachedField[declaredFields.length];
            for (int i = 0; i != fields.length; ++i)
                fields[i] = new CachedField(this, declaredFields[i]);
        }
        return fields;
    }

    public synchronized CachedConstructor[] getConstructors() {
        if (constructors == null) {
            final Constructor[] declaredConstructors = (Constructor[])
               AccessController.doPrivileged(new PrivilegedAction/*<Constructor[]>*/() {
                   public /*Constructor[]*/ Object run() {
                       return getTheClass().getDeclaredConstructors();
                   }
               });
            constructors = new CachedConstructor[declaredConstructors.length];
            for (int i = 0; i != constructors.length; ++i)
                constructors[i] = new CachedConstructor(this, declaredConstructors[i]);
        }
        return constructors;
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

    public final int getModifiers() {
        return modifiers;
    }

    public Object coerceArgument(Object argument) {
        return argument;
    }

    public int getSuperClassDistance() {
     // This method is idempotent.  Don't put a dangerous lock here!
     // synchronized (getCachedClass()) {
            if (distance == -1) {
                int distance = 0;
                for (Class klazz= getTheClass(); klazz != null; klazz = klazz.getSuperclass()) {
                    distance++;
                }
                this.distance = distance;
            }
            return distance;
      //  }
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

    public Reflector getReflector() {
        /*if (reflector == null) {
            final MetaClassRegistry metaClassRegistry = MetaClassRegistryImpl.getInstance(MetaClassRegistryImpl.LOAD_DEFAULT);
            reflector = ((MetaClassRegistryImpl)metaClassRegistry).loadReflector(getCachedClass(), Arrays.asList(getMethods()));
        }*/
        return reflector;
    }

    public final Class getTheClass() {
        return cachedClass;
    }

    public MetaMethod[] getNewMetaMethods() {
        return newMetaMethods;
    }

    public void setNewMopMethods(ArrayList arr) {
        newMetaMethods = (MetaMethod[]) arr.toArray(new MetaMethod[arr.size()]);
    }

    public synchronized void setStaticMetaClassField(MetaClass mc) {
        if (staticMetaClassField == NONE)
          return;
        if (staticMetaClassField == null) {
            final CachedField[] cachedFields = getFields();
            for (int i = 0; i < cachedFields.length; i++) {
                CachedField cachedField = cachedFields[i];
                if (cachedField.getName().startsWith("$staticMetaClass") && cachedField.getType() == MetaClass.class && cachedField.isStatic()) {
                    staticMetaClassField = cachedField;
                    break;
                }
            }
        }
        if (staticMetaClassField == null) {
          staticMetaClassField = NONE;
          return;
        }

        ((CachedField)staticMetaClassField).setProperty(null,mc);
    }

    public MetaClass getMetaClassForClass() {
        Object cur = metaClassForClass;
        if (cur == null)
            return null;
        if (cur instanceof SoftReference) {
            SoftReference softReference = (SoftReference) cur;
            return (MetaClass) softReference.get();
        }
        return (MetaClass) metaClassForClass;
    }

    public void setMetaClassForClass(MetaClass metaClassForClass, boolean isConst) {
        if (isConst || metaClassForClass == null)
            this.metaClassForClass = metaClassForClass;
        else
            this.metaClassForClass = new SoftReference(metaClassForClass);
        setStaticMetaClassField(metaClassForClass);
    }

    public boolean isAssignableFrom(Class argument) {
        return argument == null || ReflectionCache.isAssignableFrom(getTheClass(), argument);
    }

    boolean isDirectlyAssignable(Object argument) {
        return ReflectionCache.isAssignableFrom(getTheClass(), argument.getClass());
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
