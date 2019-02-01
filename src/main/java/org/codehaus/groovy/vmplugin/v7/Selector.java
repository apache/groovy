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
package org.codehaus.groovy.vmplugin.v7;

import groovy.lang.AdaptingMetaClass;
import groovy.lang.Closure;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassImpl.MetaConstructor;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import groovy.lang.MissingMethodException;
import groovy.transform.Internal;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.GroovyCategorySupport.CategoryMethod;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MethodMetaProperty;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.vmplugin.v7.IndyInterface.CALL_TYPES;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.ARRAYLIST_CONSTRUCTOR;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.BEAN_CONSTRUCTOR_PROPERTY_SETTER;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.BOOLEAN_IDENTITY;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.CLASS_FOR_NAME;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.DTT_CAST_TO_TYPE;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.EQUALS;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.GROOVY_CAST_EXCEPTION;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.GROOVY_OBJECT_GET_PROPERTY;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.GROOVY_OBJECT_INVOKER;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.HASHSET_CONSTRUCTOR;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.HAS_CATEGORY_IN_CURRENT_THREAD_GUARD;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.INTERCEPTABLE_INVOKER;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.IS_NULL;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.META_CLASS_INVOKE_STATIC_METHOD;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.META_METHOD_INVOKER;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.META_PROPERTY_GETTER;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.MOP_GET;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.MOP_INVOKE_CONSTRUCTOR;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.MOP_INVOKE_METHOD;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.NULL_REF;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.SAME_CLASS;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.SAME_MC;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.SAM_CONVERSION;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.UNWRAP_EXCEPTION;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.UNWRAP_METHOD;
import static org.codehaus.groovy.vmplugin.v7.IndyGuardsFiltersAndSignatures.unwrap;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.LOG;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.LOG_ENABLED;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.LOOKUP;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.makeFallBack;
import static org.codehaus.groovy.vmplugin.v7.IndyInterface.switchPoint;

public abstract class Selector {
    public Object[] args, originalArguments;
    public MetaMethod method;
    public MethodType targetType,currentType;
    public String name;
    public MethodHandle handle;
    public boolean useMetaClass = false, cache = true;
    public MutableCallSite callSite;
    public Class sender;
    public boolean isVargs;
    public boolean safeNavigation, safeNavigationOrig, spread;
    public boolean skipSpreadCollector;
    public boolean thisCall;
    public Class selectionBase;
    public boolean catchException = true;
    public CALL_TYPES callType;

    /** Cache values for read-only access */
    private static final CALL_TYPES[] CALL_TYPES_VALUES = CALL_TYPES.values();

    /**
     * Returns the Selector
     */
    public static Selector getSelector(MutableCallSite callSite, Class sender, String methodName, int callID, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
        CALL_TYPES callType = CALL_TYPES_VALUES[callID];
        switch (callType) {
            case INIT: return new InitSelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case METHOD: return new MethodSelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case GET: 
                return new PropertySelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case SET:
                throw new GroovyBugError("your call tried to do a property set, which is not supported.");
            case CAST:  return new CastSelector(callSite, arguments);
            default: throw new GroovyBugError("unexpected call type");
        }
    }
    abstract void setCallSiteTarget();

    /**
     * Helper method to transform the given arguments, consisting of the receiver 
     * and the actual arguments in an Object[], into a new Object[] consisting
     * of the receiver and the arguments directly. Before the size of args was 
     * always 2, the returned Object[] will have a size of 1+n, where n is the
     * number arguments.
     */
    private static Object[] spread(Object[] args, boolean spreadCall) {
        if (!spreadCall) return args;
        Object[] normalArguments = (Object[]) args[1];
        Object[] ret = new Object[normalArguments.length+1];
        ret[0] = args[0];
        System.arraycopy(normalArguments, 0, ret, 1, ret.length-1);
        return ret;
    }

    private static class CastSelector extends MethodSelector {
        private final Class<?> staticSourceType, staticTargetType;

        public CastSelector(MutableCallSite callSite, Object[] arguments) {
            super(callSite, Selector.class, "", CALL_TYPES.CAST, false, false, false, arguments);
            this.staticSourceType = callSite.type().parameterType(0);
            this.staticTargetType = callSite.type().returnType();
        }

        @Override
        public void setCallSiteTarget() {
            // targetTypes String, Enum and Class are handled 
            // by the compiler already
            // Boolean / boolean
            handleBoolean();
            handleNullWithoutBoolean();

            // !! from here on args[0] is always not null !!
            handleInstanceCase();

            // targetType is abstract Collection fitting for HashSet or ArrayList
            // and object is Collection or array
            handleCollections();
            handleSAM();

            // will handle :
            //      * collection case where argument is an array
            //      * array transformation (staticTargetType.isArray())
            //      * constructor invocation
            //      * final GroovyCastException
            castToTypeFallBack();

            if (!handle.type().equals(callSite.type())) castAndSetGuards();
        }

        private void castAndSetGuards() {
            handle =  MethodHandles.explicitCastArguments(handle,targetType);
            setGuards(args[0]);
            doCallSiteTargetSet();
        }

        private void handleNullWithoutBoolean() {
            if (handle!=null || args[0]!=null) return;

            if (staticTargetType.isPrimitive()) {
                handle = MethodHandles.insertArguments(GROOVY_CAST_EXCEPTION,1,staticTargetType);
                // need to call here here because we used the static target type
                // it won't be done otherwise because handle.type() == callSite.type()
                castAndSetGuards(); 
            } else {
               handle = MethodHandles.identity(staticSourceType);
            }
        }

        private void handleInstanceCase() {
            if (handle!=null) return;

            if (staticTargetType.isAssignableFrom(args[0].getClass())) {
                handle = MethodHandles.identity(staticSourceType);
            }
        }

        private static boolean isAbstractClassOf(Class toTest, Class givenOnCallSite) {
            if (!toTest.isAssignableFrom(givenOnCallSite)) return false;
            if (givenOnCallSite.isInterface()) return true;
            return Modifier.isAbstract(givenOnCallSite.getModifiers());
        }

        private void handleCollections() {
            if (handle!=null) return;

            if (!(args[0] instanceof Collection)) return;
            if (isAbstractClassOf(HashSet.class, staticTargetType)) {
                handle = HASHSET_CONSTRUCTOR;
            } else if (isAbstractClassOf(ArrayList.class, staticTargetType)) {
                handle = ARRAYLIST_CONSTRUCTOR;
            }
        }

        private void handleSAM() {
            if (handle!=null) return;

            if (!(args[0] instanceof Closure)) return;
            Method m = CachedSAMClass.getSAMMethod(staticTargetType);
            if (m==null) return;
            //TODO: optimize: add guard based on type Closure
            handle = MethodHandles.insertArguments(SAM_CONVERSION, 1, m, staticTargetType);
        }

        private void castToTypeFallBack() {
            if (handle!=null) return;

            // generic fallback to castToType
            handle = MethodHandles.insertArguments(DTT_CAST_TO_TYPE, 1, staticTargetType);
        }

        private void handleBoolean() {
            if (handle!=null) return;

            // boolean->boolean, Boolean->boolean, boolean->Boolean
            // is handled by compiler
            // that leaves (T)Z and (T)Boolean, where T is the static type
            // but runtime type of T might be Boolean

            boolean primitive = staticTargetType==boolean.class;
            if (!primitive && staticTargetType!=Boolean.class) return;
            if (args[0]==null) {
                if (primitive) {
                    handle = MethodHandles.constant(boolean.class, false);
                    handle = MethodHandles.dropArguments(handle, 0, staticSourceType);
                } else {
                    handle = BOOLEAN_IDENTITY;
                }
            } else if (args[0] instanceof Boolean) {
                // give value through or unbox
                handle = BOOLEAN_IDENTITY;
            } else { 
                //call asBoolean
                name = "asBoolean";
                super.setCallSiteTarget();
            }
        }
    }

    private static class PropertySelector extends MethodSelector {
        private boolean insertName = false;

        public PropertySelector(MutableCallSite callSite, Class sender, String methodName, CALL_TYPES callType, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
            super(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
        }

        /**
         * We never got the interceptor path with a property get
         */
        @Override
        public boolean setInterceptor() {
            return false;
        }

        /**
         * this method chooses a property from the meta class.
         */
        @Override
        public void chooseMeta(MetaClassImpl mci) {
            Object receiver = getCorrectedReceiver();
            if (receiver instanceof GroovyObject) {
                Class aClass = receiver.getClass();
                Method reflectionMethod = null;
                try {
                    reflectionMethod = aClass.getMethod("getProperty", String.class);
                    if (!reflectionMethod.isSynthetic() && !isMarkedInternal(reflectionMethod)) {
                        handle = MethodHandles.insertArguments(GROOVY_OBJECT_GET_PROPERTY, 1, name);
                        return;
                    }
                } catch (ReflectiveOperationException e)  {}
            } else if (receiver instanceof Class) {
                handle = MOP_GET;
                handle = MethodHandles.insertArguments(handle, 2, name);
                handle = MethodHandles.insertArguments(handle, 0, this.mc);
                return;
            }

            if (method!=null || mci==null) return;
            Class chosenSender = this.sender;
            if (mci.getTheClass()!= chosenSender && GroovyCategorySupport.hasCategoryInCurrentThread()) {
                chosenSender = mci.getTheClass();
            }
            MetaProperty res = mci.getEffectiveGetMetaProperty(chosenSender, receiver, name, false);
            if (res instanceof MethodMetaProperty) {
                MethodMetaProperty mmp = (MethodMetaProperty) res;
                method = mmp.getMetaMethod();
                insertName = true;
            } else if (res instanceof CachedField) {
                CachedField cf = (CachedField) res;
                Field f = cf.field;
                try {
                    handle = LOOKUP.unreflectGetter(f);
                    if (Modifier.isStatic(f.getModifiers())) {
                        // normally we would do the following
                        // handle = MethodHandles.dropArguments(handle,0,Class.class);
                        // but because there is a bug in invokedynamic in all jdk7 versions 
                        // maybe use Unsafe.ensureClassInitialized
                        handle = META_PROPERTY_GETTER.bindTo(res);
                    }
                } catch (IllegalAccessException iae) {
                    throw new GroovyBugError(iae);
                }
            } else {
                handle = META_PROPERTY_GETTER.bindTo(res);
            } 
        }

        private boolean isMarkedInternal(Method reflectionMethod) {
            return reflectionMethod.getAnnotation(Internal.class) != null;
        }

        /**
         * Additionally to the normal {@link MethodSelector#setHandleForMetaMethod()}
         * task we have to also take care of generic getter methods, that depend
         * one the name.
         */
        @Override
        public void setHandleForMetaMethod() {
            if (handle!=null) return;
            super.setHandleForMetaMethod();
            if (handle != null && insertName && handle.type().parameterCount()==2) {
                handle = MethodHandles.insertArguments(handle, 1, name);
            }
        }

        /**
         * The MOP requires all get property operations to go through 
         * {@link GroovyObject#getProperty(String)}. We do this in case 
         * no property was found before.
         */
        @Override
        public void setMetaClassCallHandleIfNedded(boolean standardMetaClass) {
            if (handle!=null) return;
            useMetaClass = true;
            if (LOG_ENABLED) LOG.info("set meta class invocation path for property get.");
            handle = MethodHandles.insertArguments(MOP_GET, 2, this.name);
            handle = MethodHandles.insertArguments(handle, 0, mc);
        }
    }

    private static class InitSelector extends MethodSelector {
        private boolean beanConstructor;

        public InitSelector(MutableCallSite callSite, Class sender, String methodName, CALL_TYPES callType, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
            super(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
        }

        /**
         * Constructor calls are not intercepted, thus always returns false.
         */
        @Override
        public boolean setInterceptor() {
            return false;
        }

        /**
         * For a constructor call we always use the static meta class from the registry
         */
        @Override
        public void getMetaClass() {
            Object receiver = args[0];
            mc = GroovySystem.getMetaClassRegistry().getMetaClass((Class) receiver);
        }

        /**
         * This method chooses a constructor from the meta class.
         */
        @Override
        public void chooseMeta(MetaClassImpl mci) {
            if (mci==null) return;
            if (LOG_ENABLED) LOG.info("getting constructor");
            Object[] newArgs = removeRealReceiver(args);
            method = mci.retrieveConstructor(newArgs);
            if (method instanceof MetaConstructor) {
                MetaConstructor mcon = (MetaConstructor) method;
                if (mcon.isBeanConstructor()) {
                    if (LOG_ENABLED) LOG.info("do beans constructor");
                    beanConstructor = true;
                }
            }
        }

        /**
         * Adds {@link MetaConstructor} handling.
         */
        @Override
        public void setHandleForMetaMethod() {
            if (method==null) return;
            if (method instanceof MetaConstructor) {
                if (LOG_ENABLED) LOG.info("meta method is MetaConstructor instance");
                MetaConstructor mc = (MetaConstructor) method;
                isVargs = mc.isVargsMethod();
                Constructor con = mc.getCachedConstrcutor().cachedConstructor;
                try {
                    handle = LOOKUP.unreflectConstructor(con);
                    if (LOG_ENABLED) LOG.info("successfully unreflected constructor");
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else {
                super.setHandleForMetaMethod();
            }
            if (beanConstructor) {
                // we have handle that takes no arguments to create the bean, 
                // we have to use its return value to call #setBeanProperties with it
                // and the meta class.

                // to do this we first bind the values to #setBeanProperties
                MethodHandle con = BEAN_CONSTRUCTOR_PROPERTY_SETTER.bindTo(mc);
                // inner class case
                MethodType foldTargetType = MethodType.methodType(Object.class);
                if (args.length==3) {
                    con = MethodHandles.dropArguments(con, 1, targetType.parameterType(1));
                    foldTargetType = foldTargetType.insertParameterTypes(0, targetType.parameterType(1));
                }
                handle = MethodHandles.foldArguments(con, handle.asType(foldTargetType));
            }
            if (method instanceof MetaConstructor) {
                handle = MethodHandles.dropArguments(handle, 0, Class.class);
            }
        }

        /**
         * In case of a bean constructor we don't do any varags or implicit null argument 
         * transformations. Otherwise we do the same as for {@link MethodSelector#correctParameterLength()}
         */
        @Override
        public void correctParameterLength() {
            if (beanConstructor) return;
            super.correctParameterLength();
        }

        /**
         * In case of a bean constructor we don't do any coercion, otherwise
         * we do the same as for {@link MethodSelector#correctCoerce()}
         */
        @Override
        public void correctCoerce() {
            if (beanConstructor) return;
            super.correctCoerce();
        }

        /**
         * Set MOP based constructor invocation path.
         */
        @Override
        public void setMetaClassCallHandleIfNedded(boolean standardMetaClass) {
            if (handle!=null) return;
            useMetaClass = true;
            if (LOG_ENABLED) LOG.info("set meta class invocation path");
            handle = MOP_INVOKE_CONSTRUCTOR.bindTo(mc);
            handle = handle.asCollector(Object[].class, targetType.parameterCount()-1);
            handle = MethodHandles.dropArguments(handle, 0, Class.class);
            if (LOG_ENABLED) LOG.info("create collector for arguments");
        }
    }

    /**
     * Method invocation based {@link Selector}.
     * This Selector is called for method invocations and is base for cosntructor
     * calls as well as getProperty calls.
     */
    private static class MethodSelector extends Selector {
        protected MetaClass mc;
        private boolean isCategoryMethod;
        public MethodSelector(MutableCallSite callSite, Class sender, String methodName, CALL_TYPES callType, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object[] arguments) {
            this.callType = callType;
            this.targetType = callSite.type();
            this.name = methodName;
            this.originalArguments = arguments;
            this.args = spread(arguments, spreadCall);
            this.callSite = callSite;
            this.sender = sender;
            this.safeNavigationOrig = safeNavigation;
            this.safeNavigation = safeNavigation && arguments[0]==null;
            this.thisCall = thisCall;
            this.spread = spreadCall;
            this.cache = !spread;

            if (LOG_ENABLED) {
                StringBuilder msg =
                        new StringBuilder("----------------------------------------------------" +
                                "\n\t\tinvocation of method '" + methodName + "'" +
                                "\n\t\tinvocation type: " + callType +
                                "\n\t\tsender: " + sender +
                                "\n\t\ttargetType: " + targetType +
                                "\n\t\tsafe navigation: " + safeNavigation +
                                "\n\t\tthisCall: " + thisCall +
                                "\n\t\tspreadCall: " + spreadCall +
                                "\n\t\twith " + arguments.length + " arguments");
                for (int i=0; i<arguments.length; i++) {
                    msg.append("\n\t\t\targument[").append(i).append("] = ");
                    if (arguments[i] == null) {
                        msg.append("null");
                    } else {
                        msg.append(arguments[i].getClass().getName()).append("@").append(Integer.toHexString(System.identityHashCode(arguments[i])));
                    }
                }
                LOG.info(msg.toString());
            }
        }

        /**
         * Sets the null constant for safe navigation.
         * In case of foo?.bar() and foo being null, we don't call the method,
         * instead we simply return null. This produces a handle, which will 
         * return the constant.
         */
        public boolean setNullForSafeNavigation() {
            if (!safeNavigation) return false;
            handle = MethodHandles.dropArguments(NULL_REF,0,targetType.parameterArray());
            if (LOG_ENABLED) LOG.info("set null returning handle for safe navigation");
            return true;
        }

        /**
         * Gives the meta class to an Object.
         */
        public void getMetaClass() {
            Object receiver = args[0];
            if (receiver == null) {
                mc = NullObject.getNullObject().getMetaClass();
            } else if (receiver instanceof GroovyObject) {
                mc = ((GroovyObject) receiver).getMetaClass();
            } else if (receiver instanceof Class) {
                Class c = (Class) receiver;
                ClassLoader cl = c.getClassLoader();
                try {
                    Class.forName(c.getName(), true, cl);
                } catch (ClassNotFoundException e) {}
                mc = GroovySystem.getMetaClassRegistry().getMetaClass(c);
                this.cache &= !ClassInfo.getClassInfo(c).hasPerInstanceMetaClasses();
            } else {
                mc = ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(receiver);
                this.cache &= !ClassInfo.getClassInfo(receiver.getClass()).hasPerInstanceMetaClasses();
            }
            mc.initialize();
        }

        /**
         * Uses the meta class to get a meta method for a method call.
         * There will be no meta method selected, if the meta class is no MetaClassImpl
         * or the meta class is an AdaptingMetaClass.
         */
        public void chooseMeta(MetaClassImpl mci) {
            if (mci==null) return;
            Object receiver = getCorrectedReceiver();
            Object[] newArgs = removeRealReceiver(args);
            if (receiver instanceof Class) {
                if (LOG_ENABLED) LOG.info("receiver is a class");
                if (!mci.hasCustomStaticInvokeMethod()) method = mci.retrieveStaticMethod(name, newArgs);
            } else {
                String changedName = name;
                if (receiver instanceof GeneratedClosure && changedName.equals("call")) {changedName = "doCall";}
                if (!mci.hasCustomInvokeMethod()) method = mci.getMethodWithCaching(selectionBase, changedName, newArgs, false);
            }
            if (LOG_ENABLED) LOG.info("retrieved method from meta class: "+method);
        }

        /**
         * Creates a MethodHandle using a before selected MetaMethod.
         * If the MetaMethod has reflective information available, then
         * we will use that information to create the target MethodHandle. 
         * If that is not the case we will produce a handle, which will use the
         * MetaMethod itself for invocation.
         */
        public void setHandleForMetaMethod() {
            MetaMethod metaMethod = method;
            isCategoryMethod = method instanceof CategoryMethod;

            if (
                    metaMethod instanceof NumberNumberMetaMethod ||
                    (method instanceof GeneratedMetaMethod && (name.equals("next") || name.equals("previous")))
            ) {
                if (LOG_ENABLED) LOG.info("meta method is number method");
                if (IndyMath.chooseMathMethod(this, metaMethod)) {
                    catchException = false;
                    if (LOG_ENABLED) LOG.info("indy math successful");
                    return;
                }
            }

            boolean isCategoryTypeMethod = metaMethod instanceof NewInstanceMetaMethod;
            if (LOG_ENABLED) LOG.info("meta method is category type method: "+isCategoryTypeMethod);
            boolean isStaticCategoryTypeMethod = metaMethod instanceof NewStaticMetaMethod;
            if (LOG_ENABLED) LOG.info("meta method is static category type method: "+isCategoryTypeMethod);

            if (metaMethod instanceof ReflectionMetaMethod) {
                if (LOG_ENABLED) LOG.info("meta method is reflective method");
                ReflectionMetaMethod rmm = (ReflectionMetaMethod) metaMethod;
                metaMethod = rmm.getCachedMethod();
            }

            if (metaMethod instanceof CachedMethod) {
                if (LOG_ENABLED) LOG.info("meta method is CachedMethod instance");
                CachedMethod cm = (CachedMethod) metaMethod;
                isVargs = cm.isVargsMethod();
                try {
                    Method m = cm.getCachedMethod();
                    handle = correctClassForNameAndUnReflectOtherwise(m);
                    if (LOG_ENABLED) LOG.info("successfully unreflected method");
                    if (isStaticCategoryTypeMethod) {
                        handle = MethodHandles.insertArguments(handle, 0, new Object[]{null});
                        handle = MethodHandles.dropArguments(handle, 0, targetType.parameterType(0));
                    } else if (!isCategoryTypeMethod && isStatic(m)) {
                        // we drop the receiver, which might be a Class (invocation on Class)
                        // or it might be an object (static method invocation on instance)
                        // Object.class handles both cases at once
                        handle = MethodHandles.dropArguments(handle, 0, Object.class);
                    } 
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else if (method != null) {
                if (LOG_ENABLED) LOG.info("meta method is dgm helper");
                // generic meta method invocation path
                handle = META_METHOD_INVOKER;
                handle = handle.bindTo(method);
                if (spread) {
                    args = originalArguments;
                    skipSpreadCollector = true;
                } else {
                    // wrap arguments from call site in Object[]
                    handle = handle.asCollector(Object[].class, targetType.parameterCount()-1);
                }
                currentType = removeWrapper(targetType);
                if (LOG_ENABLED) LOG.info("bound method name to META_METHOD_INVOKER");
            }
        }

        private MethodHandle correctClassForNameAndUnReflectOtherwise(Method m) throws IllegalAccessException {
            if (m.getDeclaringClass()==Class.class && m.getName().equals("forName") && m.getParameterTypes().length==1) {
                return MethodHandles.insertArguments(CLASS_FOR_NAME, 1, true, sender.getClassLoader());
            } else {
                return LOOKUP.unreflect(m);
            }
        }

        /**
         * Helper method to manipulate the given type to replace Wrapper with Object.
         */
        private MethodType removeWrapper(MethodType targetType) {
            Class[] types = targetType.parameterArray();
            for (int i=0; i<types.length; i++) {
                if (types[i]==Wrapper.class) {
                    targetType = targetType.changeParameterType(i, Object.class);
                }
            }
            return targetType;
        }

        /**
         * Creates a MethodHandle, which will use the meta class path.
         * This method is called only if no handle has been created before. This
         * is usually the case if the method selection failed.
         */
        public void setMetaClassCallHandleIfNedded(boolean standardMetaClass) {
            if (handle!=null) return;
            useMetaClass = true;
            if (LOG_ENABLED) LOG.info("set meta class invocation path");
            Object receiver = getCorrectedReceiver();
            if (receiver instanceof Class) {
                handle = META_CLASS_INVOKE_STATIC_METHOD.bindTo(mc);
                if (LOG_ENABLED) LOG.info("use invokeStaticMethod with bound meta class");
            } else {
                handle = MOP_INVOKE_METHOD.bindTo(mc);
                if (LOG_ENABLED) LOG.info("use invokeMethod with bound meta class");

                if (receiver instanceof GroovyObject) {
                    // if the meta class call fails we may still want to fall back to call
                    // GroovyObject#invokeMethod if the receiver is a GroovyObject
                    if (LOG_ENABLED) LOG.info("add MissingMethod handler for GrooObject#invokeMethod fallback path");
                    handle = MethodHandles.catchException(handle, MissingMethodException.class, GROOVY_OBJECT_INVOKER);
                }
            }
            handle = MethodHandles.insertArguments(handle, 1, name);
            if (!spread) handle = handle.asCollector(Object[].class, targetType.parameterCount()-1);
            if (LOG_ENABLED) LOG.info("bind method name and create collector for arguments");
        }

        /**
         * Corrects method argument wrapping.
         * In cases in which we want to force a certain method selection
         * we use Wrapper classes to transport the static type information.
         * This method will be used to undo the wrapping.
         */
        public void correctWrapping() {
            if (useMetaClass) return;
            Class[] pt = handle.type().parameterArray();
            if (currentType!=null) pt = currentType.parameterArray();
            for (int i=1; i<args.length; i++) {
                if (args[i] instanceof Wrapper) {
                    Class type = pt[i];
                    MethodType mt = MethodType.methodType(type, Wrapper.class);
                    handle = MethodHandles.filterArguments(handle, i, UNWRAP_METHOD.asType(mt));
                    if (LOG_ENABLED) LOG.info("added filter for Wrapper for argument at pos "+i);
                }
            }
        }

        /**
         * Handles cases in which we have to correct the length of arguments
         * using the parameters. This might be needed for vargs and for one 
         * parameter calls without arguments (null is used then).  
         */
        public void correctParameterLength() {
            if (handle==null) return;

            Class[] params = handle.type().parameterArray();
            if (currentType!=null) params = currentType.parameterArray();
            if (!isVargs) {
                if (spread && useMetaClass) return;
                if (params.length==2 && args.length==1) {
                    //TODO: this Object[] can be constant
                    handle = MethodHandles.insertArguments(handle, 1, new Object[]{null});
                }
                return;
            }

            Class lastParam = params[params.length-1];
            Object lastArg = unwrapIfWrapped(args[args.length-1]);
            if (params.length == args.length) {
                // may need rewrap
                if (lastArg == null) return;
                if (lastParam.isInstance(lastArg)) return;
                if (lastArg.getClass().isArray()) return;
                // arg is not null and not assignment compatible
                // so we really need to rewrap
                handle = handle.asCollector(lastParam, 1);
            } else if (params.length > args.length) {
                // we depend on the method selection having done a good 
                // job before already, so the only case for this here is, that
                // we have no argument for the array, meaning params.length is
                // args.length+1. In that case we have to fill in an empty array
                handle = MethodHandles.insertArguments(handle, params.length-1, Array.newInstance(lastParam.getComponentType(), 0));
                if (LOG_ENABLED) LOG.info("added empty array for missing vargs part");
            } else { //params.length < args.length
                // we depend on the method selection having done a good 
                // job before already, so the only case for this here is, that
                // all trailing arguments belong into the vargs array
                handle = handle.asCollector(
                        lastParam,
                        args.length - params.length + 1);
                if (LOG_ENABLED) LOG.info("changed surplus arguments to be collected for vargs call");
            }
        }

        /**
         * There are some conversions we have to do explicitly.
         * These are GString to String, Number to Byte and Number to BigInteger
         * conversions.
         */
        public void correctCoerce() {
            if (useMetaClass) return;

            Class[] parameters = handle.type().parameterArray();
            if (currentType!=null) parameters = currentType.parameterArray();
            if (args.length != parameters.length) {
                throw new GroovyBugError("At this point argument array length and parameter array length should be the same");
            }
            for (int i=0; i<args.length; i++) {
                if (parameters[i]==Object.class) continue; 
                Object arg = unwrapIfWrapped(args[i]);
                // we have to handle here different cases in which we do no
                // transformations. We depend on our method selection to have
                // selected only a compatible method, that means for a null
                // argument we don't have to do anything. Same of course is if
                // the argument is an instance of the parameter type. We also
                // exclude boxing, since the MethodHandles will do that part
                // already for us. Another case is the conversion of a primitive
                // to another primitive or of the wrappers, or a combination of 
                // these. This is also handled already. What is left is the 
                // GString conversion and the number conversions.

                if (arg==null) continue;
                Class got = arg.getClass();

                // equal class, nothing to do
                if (got==parameters[i]) continue;

                Class wrappedPara = TypeHelper.getWrapperClass(parameters[i]);
                // equal class with one maybe a primitive, the later explicitCastArguments will solve this case
                if (wrappedPara==TypeHelper.getWrapperClass(got)) continue;

                // equal in terms of an assignment in Java. That means according to Java widening rules, or
                // a subclass, interface, superclass relation, this case then handles also 
                // primitive to primitive conversion. Those case are also solved by explicitCastArguments.
                if (parameters[i].isAssignableFrom(got)) continue;

                // to aid explicitCastArguments we convert to the wrapper type to let is only unbox
                handle = TypeTransformers.addTransformer(handle, i, arg, wrappedPara);
                if (LOG_ENABLED) LOG.info("added transformer at pos "+i+" for type "+got+" to type "+wrappedPara);
            }
        }

        /**
         * Gives a replacement receiver for null.
         * In case of the receiver being null we want to do the method
         * invocation on NullObject instead.
         */
        public void correctNullReceiver() {
            if (args[0]!=null) return;
            handle = handle.bindTo(NullObject.getNullObject());
            handle = MethodHandles.dropArguments(handle, 0, targetType.parameterType(0));
            if (LOG_ENABLED) LOG.info("binding null object receiver and dropping old receiver");
        }

        public void correctSpreading() {
            if (!spread || useMetaClass || skipSpreadCollector) return;
            handle = handle.asSpreader(Object[].class, args.length-1);
        }

        /**
         * Adds the standard exception handler.  
         */
        public void addExceptionHandler() {
            //TODO: if we would know exactly which paths require the exceptions
            //      and which paths not, we can sometimes save this guard 
            if (handle==null || !catchException) return;
            Class returnType = handle.type().returnType();
            if (returnType!=Object.class) {
                MethodType mtype = MethodType.methodType(returnType, GroovyRuntimeException.class); 
                handle = MethodHandles.catchException(handle, GroovyRuntimeException.class, UNWRAP_EXCEPTION.asType(mtype));
            } else {
                handle = MethodHandles.catchException(handle, GroovyRuntimeException.class, UNWRAP_EXCEPTION);
            }
            if (LOG_ENABLED) LOG.info("added GroovyRuntimeException unwrapper");
        }

        /**
         * Sets all argument and receiver guards.
         */
        public void setGuards (Object receiver) {
            if (handle==null) return;
            if (!cache) return;

            MethodHandle fallback = makeFallBack(callSite, sender, name, callType.ordinal(), targetType, safeNavigationOrig, thisCall, spread);

            // special guards for receiver
            if (receiver instanceof GroovyObject) {
                GroovyObject go = (GroovyObject) receiver;
                MetaClass mc = (MetaClass) go.getMetaClass();
                MethodHandle test = SAME_MC.bindTo(mc); 
                // drop dummy receiver
                test = test.asType(MethodType.methodType(boolean.class,targetType.parameterType(0)));
                handle = MethodHandles.guardWithTest(test, handle, fallback);
                if (LOG_ENABLED) LOG.info("added meta class equality check");
            } else if (receiver instanceof Class) {
                MethodHandle test = EQUALS.bindTo(receiver);
                test = test.asType(MethodType.methodType(boolean.class,targetType.parameterType(0)));
                handle = MethodHandles.guardWithTest(test, handle, fallback);
                if (LOG_ENABLED) LOG.info("added class equality check");
            }

            if (!useMetaClass && isCategoryMethod) {
                // category method needs Thread check
                // cases:
                // (1) method is a category method
                //     We need to check if the category in the current thread is still active.
                //     Since we invalidate on leaving the category checking for it being
                //     active directly is good enough.
                // (2) method is in use scope, but not from category
                //     Since entering/leaving a category will invalidate, there is no need for any special check
                // (3) method is not in use scope /and not from category
                //     Since entering/leaving a category will invalidate, there is no need for any special check
                if (method instanceof NewInstanceMetaMethod) {
                    handle = MethodHandles.guardWithTest(HAS_CATEGORY_IN_CURRENT_THREAD_GUARD, handle, fallback);
                    if (LOG_ENABLED) LOG.info("added category-in-current-thread-guard for category method");
                }
            }

            // handle constant meta class and category changes
            handle = switchPoint.guardWithTest(handle, fallback);
            if (LOG_ENABLED) LOG.info("added switch point guard");

            // guards for receiver and parameter
            Class[] pt = handle.type().parameterArray();
            for (int i=0; i<args.length; i++) {
                Object arg = args[i];
                MethodHandle test = null;
                if (arg==null) {
                    test = IS_NULL.asType(MethodType.methodType(boolean.class, pt[i]));
                    if (LOG_ENABLED) LOG.info("added null argument check at pos "+i);
                } else { 
                    Class argClass = arg.getClass();
                    if (pt[i].isPrimitive()) continue;
                    //if (Modifier.isFinal(argClass.getModifiers()) && TypeHelper.argumentClassIsParameterClass(argClass,pt[i])) continue;
                    test = SAME_CLASS.
                                bindTo(argClass).
                                asType(MethodType.methodType(boolean.class, pt[i]));
                    if (LOG_ENABLED) LOG.info("added same class check at pos "+i);
                }
                Class[] drops = new Class[i];
                System.arraycopy(pt, 0, drops, 0, drops.length);
                test = MethodHandles.dropArguments(test, 0, drops);
                handle = MethodHandles.guardWithTest(test, handle, fallback);
            }
        }

        /**
         * do the actual call site target set, if the call is supposed to be cached
         */
        public void doCallSiteTargetSet() {
            if (!cache) {
                if (LOG_ENABLED) LOG.info("call site stays uncached");
            } else {
                callSite.setTarget(handle);
                if (LOG_ENABLED) LOG.info("call site target set, preparing outside invocation");
            }
        }

        /**
         * Sets the selection base.
         */
        public void setSelectionBase() {
            if (thisCall) {
                selectionBase = sender;
            } else if (args[0]==null) {
                selectionBase = NullObject.class;
            } else {
                selectionBase = mc.getTheClass();
            }
            if (LOG_ENABLED) LOG.info("selection base set to "+selectionBase);
        }

        /**
         * Sets a handle to call {@link GroovyInterceptable#invokeMethod(String, Object)}
         */
        public boolean setInterceptor() {
            if (!(this.args[0] instanceof GroovyInterceptable)) return false;
            handle = MethodHandles.insertArguments(INTERCEPTABLE_INVOKER, 1, this.name);
            handle = handle.asCollector(Object[].class, targetType.parameterCount()-1); 
            handle = handle.asType(targetType);
            return true;
        }

        /**
         * setting a call site target consists of the following steps:
         * # get the meta class
         * # select a method/constructor/property from it, if it is a MetaClassImpl
         * # make a handle out of the selection
         * # if nothing could be selected select a path through the given MetaClass or the GroovyObject
         * # apply transformations for vargs, implicit null argument, coercion, wrapping, null receiver and spreading
         */
        @Override
        public void setCallSiteTarget() {
            if (!setNullForSafeNavigation() && !setInterceptor()) {
                getMetaClass();
                if (LOG_ENABLED) LOG.info("meta class is "+mc);
                setSelectionBase();
                MetaClassImpl mci = getMetaClassImpl(mc, callType != CALL_TYPES.GET);
                chooseMeta(mci);
                setHandleForMetaMethod();
                setMetaClassCallHandleIfNedded(mci!=null);
                correctParameterLength();
                correctCoerce();
                correctWrapping();
                correctNullReceiver();
                correctSpreading();

                if (LOG_ENABLED) LOG.info("casting explicit from "+handle.type()+" to "+targetType);
                handle =  MethodHandles.explicitCastArguments(handle,targetType);

                addExceptionHandler();
            }
            setGuards(args[0]);
            doCallSiteTargetSet();
        }
    }

    /**
     * Unwraps the given object from a {@link Wrapper}. If not
     * wrapped, the given object is returned.
     */
    private static Object unwrapIfWrapped(Object object) {
        if (object instanceof Wrapper) return unwrap(object);
        return object;
    }

    /**
     * Returns {@link NullObject#getNullObject()} if the receiver
     * (args[0]) is null. If it is not null, the recevier itself
     * is returned.
     */
    public Object getCorrectedReceiver() {
        Object receiver = args[0];
        if (receiver==null) {
            if (LOG_ENABLED) LOG.info("receiver is null");
            receiver = NullObject.getNullObject();
        }
        return receiver;
    }

    /**
     * Returns if a method is static
     */
    private static boolean isStatic(Method m) {
        int mods = m.getModifiers();
        return (mods & Modifier.STATIC) != 0;
    }

    /**
     * Returns the MetaClassImpl if the given MetaClass is one of
     * MetaClassImpl, AdaptingMetaClass or ClosureMetaClass. If
     * none of these cases matches, this method returns null.
     */
    private static MetaClassImpl getMetaClassImpl(MetaClass mc, boolean includeEMC) {
        Class mcc = mc.getClass();
        boolean valid = mcc == MetaClassImpl.class ||
                         mcc == AdaptingMetaClass.class ||
                         mcc == ClosureMetaClass.class ||
                         (includeEMC && mcc == ExpandoMetaClass.class);
        if (!valid) {
            if (LOG_ENABLED) LOG.info("meta class is neither MetaClassImpl, nor AdoptingMetaClass, nor ClosureMetaClass, normal method selection path disabled.");
            return null;
        }
        if (LOG_ENABLED) LOG.info("meta class is a recognized MetaClassImpl");
        return (MetaClassImpl) mc;
    }

    /**
     * Helper method to remove the receiver from the argument array
     * by producing a new array.
     */
    private static Object[] removeRealReceiver(Object[] args) {
        Object[] ar = new Object[args.length-1];
        System.arraycopy(args, 1, ar, 0, args.length - 1);
        return ar;
    }
}
