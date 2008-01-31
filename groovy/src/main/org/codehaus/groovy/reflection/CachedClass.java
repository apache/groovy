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
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.runtime.Reflector;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
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

        for (Iterator it = getInterfaces().iterator(); it.hasNext(); ) {
            CachedClass inf = (CachedClass) it.next();
            ReflectionCache.isAssignableFrom(klazz, inf.cachedClass);
        }

        for (CachedClass cur = this; cur != null; cur = cur.getCachedSuperClass()) {
            ReflectionCache.setAssignableFrom(cur.cachedClass, klazz);
        }
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

    public synchronized CachedMethod[] getMethods() {
        if (methods == null) {
            final Method[] declaredMethods = (Method[])
               AccessController.doPrivileged(new PrivilegedAction/*<Method[]>*/() {
                   public /*Method[]*/ Object run() {
                       return getCachedClass().getDeclaredMethods();
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
                       return getCachedClass().getDeclaredFields();
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
                       return getCachedClass().getDeclaredConstructors();
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

    public int getModifiers() {
        return modifiers;
    }

    public Object coerceArgument(Object argument) {
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
        /*if (reflector == null) {
            final MetaClassRegistry metaClassRegistry = MetaClassRegistryImpl.getInstance(MetaClassRegistryImpl.LOAD_DEFAULT);
            reflector = ((MetaClassRegistryImpl)metaClassRegistry).loadReflector(getCachedClass(), Arrays.asList(getMethods()));
        }*/
        return reflector;
    }

    public final Class getCachedClass() {
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
        return argument == null || ReflectionCache.isAssignableFrom(getCachedClass(), argument);
    }

    boolean isDirectlyAssignable(Object argument) {
        return ReflectionCache.isAssignableFrom(getCachedClass(), argument.getClass());
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

    public String toString() {
        return cachedClass.toString();
    }

    public static class NumberCachedClass extends CachedClass {

        NumberCachedClass(Class klazz) {
            super(klazz);
        }

        public Object coerceArgument(Object argument) {
            if (argument instanceof Number) {
                return coerceNumber(argument);
            }
            return argument;

        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return classToTransformFrom == null
                    || Number.class.isAssignableFrom(classToTransformFrom)
                    || classToTransformFrom == Byte.TYPE
                    || classToTransformFrom == Short.TYPE
                    || classToTransformFrom == Integer.TYPE
                    || classToTransformFrom == Long.TYPE
                    || classToTransformFrom == Float.TYPE
                    || classToTransformFrom == Double.TYPE
                        ;
        }

        private Object coerceNumber(Object argument) {
            Class param = getCachedClass();
            if (param == Byte.class /*|| param == Byte.TYPE*/) {
                argument = new Byte(((Number) argument).byteValue());
            } else if (param == BigInteger.class) {
                argument = new BigInteger(String.valueOf((Number) argument));
            }

            return argument;
        }
    }

    public static class IntegerCachedClass extends NumberCachedClass {  // int, Integer
        IntegerCachedClass(Class klazz) {
            super(klazz);
        }

        public Object coerceArgument(Object argument) {
            if (argument instanceof Integer) {
                return argument;
            }

            return new Integer(((Number) argument).intValue());
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument == null || argument instanceof Integer;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return classToTransformFrom == null
                || classToTransformFrom == Integer.class
                || classToTransformFrom == Short.class
                || classToTransformFrom == Byte.class
                || classToTransformFrom == BigInteger.class
                || classToTransformFrom == Integer.TYPE
                || classToTransformFrom == Short.TYPE
                || classToTransformFrom == Byte.TYPE;
        }
    }

    public static class BigIntegerCachedClass extends NumberCachedClass {
        BigIntegerCachedClass(Class klazz) {
            super(klazz);
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument instanceof BigInteger;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return classToTransformFrom == null
                || classToTransformFrom == Integer.class
                || classToTransformFrom == Short.class
                || classToTransformFrom == Byte.class
                || classToTransformFrom == BigInteger.class
                || classToTransformFrom == Long.class
                || classToTransformFrom == Integer.TYPE
                || classToTransformFrom == Short.TYPE
                || classToTransformFrom == Byte.TYPE
                || classToTransformFrom == Long.TYPE;
        }
    }

    public static class ByteCachedClass extends NumberCachedClass {
        ByteCachedClass(Class klazz) {
            super(klazz);
        }

        public Object coerceArgument(Object argument) {
            if (argument instanceof Byte) {
                return argument;
            }

            return new Byte(((Number) argument).byteValue());
        }

        public boolean isDirectlyAssignable(Object argument) {
            return argument instanceof Short;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return classToTransformFrom == null
                || classToTransformFrom == Byte.class
                || classToTransformFrom == Byte.TYPE;
        }
    }

    public static class ShortCachedClass extends NumberCachedClass {
        ShortCachedClass(Class klazz) {
            super(klazz);
        }

        public Object coerceArgument(Object argument) {
            if (argument instanceof Short) {
                return argument;
            }

            return new Short(((Number) argument).shortValue());
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument instanceof Short;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return classToTransformFrom == null
                || classToTransformFrom == Short.class
                || classToTransformFrom == Byte.class
                || classToTransformFrom == Short.TYPE
                || classToTransformFrom == Byte.TYPE;
        }
    }

    public static class LongCachedClass extends NumberCachedClass {
        LongCachedClass(Class klazz) {
            super(klazz);
        }


        public Object coerceArgument(Object argument) {
            if (argument instanceof Long) {
                return argument;
            }

            return new Long(((Number) argument).longValue());
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument instanceof Long;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return  classToTransformFrom == null
                    || classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == Integer.TYPE
                    || classToTransformFrom == Long.TYPE
                    || classToTransformFrom == Short.TYPE
                    || classToTransformFrom == Byte.TYPE;
        }
    }

    public static class FloatCachedClass extends NumberCachedClass {
        FloatCachedClass(Class klazz) {
            super(klazz);
        }

        public Object coerceArgument(Object argument) {
            if (argument instanceof Float) {
                return argument;
            }

            Float res = new Float(((Number) argument).floatValue());
            if (argument instanceof BigDecimal && res.isInfinite()) {
                throw new IllegalArgumentException(Float.class + " out of range while converting from BigDecimal");
            }
            return res;
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument instanceof Float;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return  classToTransformFrom == null
                    || classToTransformFrom == Float.class
                    || classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == Float.TYPE
                    || classToTransformFrom == Integer.TYPE
                    || classToTransformFrom == Long.TYPE
                    || classToTransformFrom == Short.TYPE
                    || classToTransformFrom == Byte.TYPE;
        }
    }

    public static class DoubleCachedClass extends NumberCachedClass { // Double, double
        DoubleCachedClass(Class klazz) {
            super(klazz);
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument instanceof Double;
        }

        public Object coerceArgument(Object argument) {
            if (argument instanceof Double) {
                return argument;
            }

            Double res = new Double(((Number) argument).doubleValue());
            if (argument instanceof BigDecimal && res.isInfinite()) {
                throw new IllegalArgumentException(Double.class + " out of range while converting from BigDecimal");
            }
            return res;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return  classToTransformFrom == null
                    || classToTransformFrom == Double.class
                    || classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == Float.class
                    || classToTransformFrom == Double.TYPE
                    || classToTransformFrom == Integer.TYPE
                    || classToTransformFrom == Long.TYPE
                    || classToTransformFrom == Short.TYPE
                    || classToTransformFrom == Byte.TYPE
                    || classToTransformFrom == Float.TYPE
                    || classToTransformFrom == BigDecimal.class
                    || classToTransformFrom == BigInteger.class;
        }
    }

    public static class BigDecimalCachedClass extends DoubleCachedClass {
        BigDecimalCachedClass(Class klazz) {
            super(klazz);
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument instanceof BigDecimal;
        }

        public Object coerceArgument(Object argument) {
            if (argument instanceof BigDecimal) {
                return argument;
            }

            return new BigDecimal(((Number) argument).doubleValue());
        }
    }

    public static class StringCachedClass extends CachedClass {
        private static final Class STRING_CLASS = String.class;
        private static final Class GSTRING_CLASS = GString.class;

        StringCachedClass() {
            super(STRING_CLASS);
        }

        boolean isDirectlyAssignable(Object argument) {
            return argument instanceof String;
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return  classToTransformFrom == null
                  || classToTransformFrom == STRING_CLASS
                  || ReflectionCache.isAssignableFrom(GSTRING_CLASS,classToTransformFrom);
        }

        public Object coerceArgument(Object argument) {
            return argument instanceof GString ? argument.toString() : argument;
        }
    }

    public static class BooleanCachedClass extends CachedClass {
        BooleanCachedClass(Class klazz) {
            super(klazz);
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return classToTransformFrom == null
                  || classToTransformFrom == Boolean.class
                  || classToTransformFrom == Boolean.TYPE;
        }
    }

    public static class CharacterCachedClass extends CachedClass {
        public CharacterCachedClass(Class klazz) {
            super(klazz);
        }

        public boolean isAssignableFrom(Class classToTransformFrom) {
            return  classToTransformFrom == null
                  ||classToTransformFrom == Character.class
                  ||classToTransformFrom == Character.TYPE;
        }
    }

    public static class ArrayCachedClass extends CachedClass {
        ArrayCachedClass(Class klazz) {
            super(klazz);
        }

        public Object coerceArgument(Object argument) {
            Class argumentClass = argument.getClass();
            if (argumentClass.getName().charAt(0) != '[') return argument;
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

    }
}
