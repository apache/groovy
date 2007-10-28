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

import groovy.lang.GString;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.runtime.Reflector;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Alex.Tkachman
 */
public class CachedClass {
    private CachedClass cachedSuperClass;

    int hashCode;

    private Reflector reflector;

    private CachedField[] fields;
    private CachedConstructor[] constructors;
    private CachedMethod[] methods;
    private final Class cachedClass;
    private MetaMethod[] newMetaMethods;
    public  CachedMethod [] mopMethods;

    public Set getInterfaces() {
        if (interfaces == null)  {
            interfaces = new HashSet (0);

            if (getCachedClass().isInterface())
              interfaces.add(this);

            Class[] classes = getCachedClass().getInterfaces();
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

            if (getCachedClass().isInterface())
              ownInterfaces.add(this);

            Class[] classes = getCachedClass().getInterfaces();
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

    CachedClass(Class klazz) {
        cachedClass = klazz;
        isArray = klazz.isArray();
        isPrimitive = klazz.isPrimitive();
        modifiers = klazz.getModifiers();
        isInterface = klazz.isInterface();
        isNumber = Number.class.isAssignableFrom(klazz);
    }

    public synchronized CachedClass getCachedSuperClass() {
        if (cachedSuperClass == null) {
            if (!isArray)
              cachedSuperClass = ReflectionCache.getCachedClass(getCachedClass().getSuperclass());
            else
              if (cachedClass.getComponentType().isPrimitive() || cachedClass.getComponentType() == Object.class)
                cachedSuperClass = ReflectionCache.OBJECT_CLASS;
              else
                cachedSuperClass = ReflectionCache.OBJECT_ARRAY_CLASS;
        }

        return cachedSuperClass;
    }

    public CachedMethod[] getMethods() {
        if (methods == null) {
            synchronized (this) {
                final Method[] declaredMethods = getCachedClass().getDeclaredMethods();
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
        }
        return methods;
    }

    public synchronized CachedField[] getFields() {
        if (fields == null) {
            synchronized (this) {
                final Field[] declaredFields = getCachedClass().getDeclaredFields();
                fields = new CachedField[declaredFields.length];
                for (int i = 0; i != fields.length; ++i)
                    fields[i] = new CachedField(this, declaredFields[i]);
            }
        }
        return fields;
    }

    public CachedConstructor[] getConstructors() {
        if (constructors == null) {
            final Constructor[] declaredConstructors = getCachedClass().getDeclaredConstructors();
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
                    && (res == null || res.getReturnType().isAssignableFrom(m.cachedMethod.getReturnType())))
                res = m;
        }

        return res;
    }

    public int getModifiers() {
        return modifiers;
    }

    /**
     * Coerces a GString instance into String if needed
     *
     * @return the coerced argument
     */
    protected Object coerceGString(Object argument) {
        if (getCachedClass() != String.class) return argument;
        if (!(argument instanceof GString)) return argument;
        return argument.toString();
    }

    // PRECONDITION:
    //   !ReflectionCache.isAssignableFrom(parameterType, argument.getClass())
    protected Object coerceNumber(Object argument) {
        if (argument instanceof Number && (isNumber || isPrimitive)) { // Number types
            Object oldArgument = argument;
            boolean wasDouble = false;
            boolean wasFloat = false;
            Class param = getCachedClass();
            if (param == Byte.class || param == Byte.TYPE) {
                argument = new Byte(((Number) argument).byteValue());
            } else if (param == Double.class || param == Double.TYPE) {
                wasDouble = true;
                argument = new Double(((Number) argument).doubleValue());
            } else if (param == Float.class || param == Float.TYPE) {
                wasFloat = true;
                argument = new Float(((Number) argument).floatValue());
            } else if (param == Integer.class || param == Integer.TYPE) {
                argument = new Integer(((Number) argument).intValue());
            } else if (param == Long.class || param == Long.TYPE) {
                argument = new Long(((Number) argument).longValue());
            } else if (param == Short.class || param == Short.TYPE) {
                argument = new Short(((Number) argument).shortValue());
            } else if (param == BigDecimal.class) {
                argument = new BigDecimal(String.valueOf((Number) argument));
            } else if (param == BigInteger.class) {
                argument = new BigInteger(String.valueOf((Number) argument));
            }

            if (oldArgument instanceof BigDecimal) {
                BigDecimal oldbd = (BigDecimal) oldArgument;
                boolean throwException = false;
                if (wasDouble) {
                    Double d = (Double) argument;
                    if (d.isInfinite()) throwException = true;
                } else if (wasFloat) {
                    Float f = (Float) argument;
                    if (f.isInfinite()) throwException = true;
                } else {
                    BigDecimal newbd = new BigDecimal(String.valueOf((Number) argument));
                    throwException = !oldArgument.equals(newbd);
                }

                if (throwException)
                    throw new IllegalArgumentException(param + " out of range while converting from BigDecimal");
            }

        }
        return argument;
    }

    protected Object coerceArray(Object argument) {
        if (!isArray) return argument;
        Class argumentClass = argument.getClass();
        if (!argumentClass.isArray()) return argument;
        Class argumentComponent = argumentClass.getComponentType();

        Class paramComponent = getCachedClass().getComponentType();
        if (paramComponent.isPrimitive()) {
            if (paramComponent == boolean.class && argumentClass == Boolean[].class) {
                argument = DefaultTypeTransformation.convertToBooleanArray(argument);
            } else if (paramComponent == byte.class && argumentClass == Byte[].class) {
                argument = DefaultTypeTransformation.convertToByteArray(argument);
            } else if (paramComponent == char.class && argumentClass == Character[].class) {
                argument = DefaultTypeTransformation.convertToCharArray(argument);
            } else if (paramComponent == short.class && argumentClass == Short[].class) {
                argument = DefaultTypeTransformation.convertToShortArray(argument);
            } else if (paramComponent == int.class && argumentClass == Integer[].class) {
                argument = DefaultTypeTransformation.convertToIntArray(argument);
            } else if (paramComponent == long.class &&
                    (argumentClass == Long[].class || argumentClass == Integer[].class)) {
                argument = DefaultTypeTransformation.convertToLongArray(argument);
            } else if (paramComponent == float.class &&
                    (argumentClass == Float[].class || argumentClass == Integer[].class)) {
                argument = DefaultTypeTransformation.convertToFloatArray(argument);
            } else if (paramComponent == double.class &&
                    (argumentClass == Double[].class || argumentClass == Float[].class
                            || BigDecimal[].class.isAssignableFrom(argumentClass))) {
                argument = DefaultTypeTransformation.convertToDoubleArray(argument);
            }
        } else if (paramComponent == String.class && argument instanceof GString[]) {
            GString[] strings = (GString[]) argument;
            String[] ret = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                ret[i] = strings[i].toString();
            }
            argument = ret;
        } else if (paramComponent==Object.class && argumentComponent.isPrimitive()){
            argument = DefaultTypeTransformation.primitiveArrayBox(argument);
        }
        return argument;
    }
    
    public int getSuperClassDistance() {
        synchronized (getCachedClass()) {
            if (distance == -1) {
                int distance = 0;
                for (Class klazz= getCachedClass(); klazz != null; klazz = klazz.getSuperclass()) {
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
        return getCachedClass() == void.class;
    }

    public void box(BytecodeHelper helper) {
        helper.box(getCachedClass());
    }

    public void unbox(BytecodeHelper helper) {
        helper.unbox(getCachedClass());
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void doCast(BytecodeHelper helper) {
        helper.doCast(getCachedClass());
    }

    public String getName() {
        return getCachedClass().getName();
    }

    public String getTypeDescription() {
        return BytecodeHelper.getTypeDescription(getCachedClass());
    }

    public synchronized Reflector getReflector() {
        if (reflector == null) {
            final MetaClassRegistry metaClassRegistry = MetaClassRegistryImpl.getInstance(MetaClassRegistryImpl.LOAD_DEFAULT);
            reflector = ((MetaClassRegistryImpl)metaClassRegistry).loadReflector(getCachedClass(), Arrays.asList(getMethods()));
        }
        return reflector;
    }

    public Class getCachedClass() {
        return cachedClass;
    }

    public MetaMethod[] getNewMetaMethods() {
        if (newMetaMethods == null) {
            synchronized (this) {
                ArrayList arr = new ArrayList();

                // add methods declared by DGM
                final MetaClassRegistryImpl metaClassRegistry = (MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry();
                FastArray methods;
                methods = metaClassRegistry.getInstanceMethods();
                for (int i = 0; i != methods.size; ++i) {
                    MetaMethod element = (MetaMethod) methods.get(i);
                    if (element.getDeclaringClass() != this)
                        continue;
                    arr.add(element);
                }

                // add static methods declared by DGM
                methods = metaClassRegistry.getStaticMethods();
                for (int i = 0; i != methods.size; ++i) {
                    MetaMethod element = (MetaMethod) methods.get(i);
                    if (element.getDeclaringClass() != this)
                        continue;
                    arr.add(element);
                }
                newMetaMethods = (MetaMethod[]) arr.toArray(new MetaMethod[arr.size()]);
            }
        }
        return newMetaMethods;
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
            return ((CachedMethod)o1).getName().compareTo((String)o2);
        }
    }
}
