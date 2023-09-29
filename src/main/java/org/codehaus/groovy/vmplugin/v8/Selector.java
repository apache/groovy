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
package org.codehaus.groovy.vmplugin.v8;

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
import groovy.lang.MissingMethodException;
import groovy.transform.Internal;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.ArrayTypeUtils;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.GroovyCategorySupport.CategoryMethod;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MethodMetaProperty;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.ARRAYLIST_CONSTRUCTOR;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.BEAN_CONSTRUCTOR_PROPERTY_SETTER;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.CLASS_FOR_NAME;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.DTT_CAST_TO_TYPE;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.EQUALS;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.GROOVY_CAST_EXCEPTION;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.GROOVY_OBJECT_GET_PROPERTY;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.GROOVY_OBJECT_INVOKER;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.HASHSET_CONSTRUCTOR;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.HAS_CATEGORY_IN_CURRENT_THREAD_GUARD;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.INTERCEPTABLE_INVOKER;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.IS_NULL;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.META_CLASS_INVOKE_STATIC_METHOD;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.META_METHOD_INVOKER;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.META_PROPERTY_GETTER;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.MOP_GET;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.MOP_INVOKE_CONSTRUCTOR;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.MOP_INVOKE_METHOD;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.NON_NULL;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.NULL_REF;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.SAME_CLASS;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.SAME_CLASSES;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.SAME_MC;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.SAM_CONVERSION;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.UNWRAP_EXCEPTION;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.UNWRAP_METHOD;
import static org.codehaus.groovy.vmplugin.v8.IndyGuardsFiltersAndSignatures.unwrap;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.CallType;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.LOG;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.LOG_ENABLED;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.LOOKUP;
import static org.codehaus.groovy.vmplugin.v8.IndyInterface.switchPoint;

public abstract class Selector {
    public Object[] args, originalArguments;
    public MetaMethod method;
    public MethodType targetType, currentType;
    public String name;
    public MethodHandle handle;
    public boolean useMetaClass = false, cache = true;
    public CacheableCallSite callSite;
    public Class<?> sender;
    public boolean isVargs;
    public boolean safeNavigation, safeNavigationOrig, spread;
    public boolean skipSpreadCollector;
    public boolean thisCall;
    public Class<?> selectionBase;
    public boolean catchException = true;
    public CallType callType;

    /**
     * Cache values for read-only access
     */
    private static final CallType[] CALL_TYPE_VALUES = CallType.values();

    /**
     * Returns the Selector
     */
    public static Selector getSelector(CacheableCallSite callSite, Class<?> sender, String methodName, int callID, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
        CallType callType = CALL_TYPE_VALUES[callID];
        switch (callType) {
            case INIT:
                return new InitSelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case METHOD:
                return new MethodSelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case GET:
                return new PropertySelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case SET:
                throw new GroovyBugError("your call tried to do a property set, which is not supported.");
            case CAST:
                return new CastSelector(callSite, arguments);
            case INTERFACE:
                return new InterfaceSelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            default:
                throw new GroovyBugError("unexpected call type");
        }
    }

    /**
     * Returns {@link NullObject#getNullObject()} if the receiver
     * (args[0]) is null.  If it is not null, the receiver itself
     * is returned.
     */
    public Object getCorrectedReceiver() {
        var receiver = args[0];
        if (receiver == null) {
            if (LOG_ENABLED) LOG.info("receiver is null");
            receiver = NullObject.getNullObject();
        }
        return receiver;
    }

    abstract void setCallSiteTarget();

    //--------------------------------------------------------------------------

    private static class CastSelector extends MethodSelector {
        private final Class<?> staticSourceType, staticTargetType;

        public CastSelector(CacheableCallSite callSite, Object[] arguments) {
            super(callSite, Selector.class, "", CallType.CAST, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, arguments);
            this.staticSourceType = callSite.type().parameterType(0);
            this.staticTargetType = callSite.type().returnType();
        }

        @Override
        public void setCallSiteTarget() {
            // target types String, Enum and Class are handled by the compiler

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
            handle = MethodHandles.explicitCastArguments(handle, targetType);
            setGuards(args[0]);
            doCallSiteTargetSet();
        }

        private void handleNullWithoutBoolean() {
            if (handle != null || args[0] != null) return;

            if (staticTargetType.isPrimitive()) {
                handle = MethodHandles.insertArguments(GROOVY_CAST_EXCEPTION, 1, staticTargetType);
                // need to call here because we used the static target type
                // it won't be done otherwise because handle.type() == callSite.type()
                castAndSetGuards();
            } else {
                handle = MethodHandles.identity(staticSourceType);
            }
        }

        private void handleInstanceCase() {
            if (handle != null) return;

            if (staticTargetType.isAssignableFrom(args[0].getClass())) {
                handle = MethodHandles.identity(staticSourceType);
            }
        }

        private static boolean isAbstractClassOf(Class<?> toTest, Class<?> givenOnCallSite) {
            if (!toTest.isAssignableFrom(givenOnCallSite)) return false;
            if (givenOnCallSite.isInterface()) return true;
            return Modifier.isAbstract(givenOnCallSite.getModifiers());
        }

        private void handleCollections() {
            if (handle != null) return;

            if (!(args[0] instanceof Collection)) return;
            if (isAbstractClassOf(HashSet.class, staticTargetType)) {
                handle = HASHSET_CONSTRUCTOR;
            } else if (isAbstractClassOf(ArrayList.class, staticTargetType)) {
                handle = ARRAYLIST_CONSTRUCTOR;
            }
        }

        private void handleSAM() {
            if (handle != null) return;

            if (!(args[0] instanceof Closure)) return;
            Method m = CachedSAMClass.getSAMMethod(staticTargetType);
            if (m == null) return;
            //TODO: optimize: add guard based on type Closure
            handle = MethodHandles.insertArguments(SAM_CONVERSION, 1, m, staticTargetType);
        }

        private void castToTypeFallBack() {
            if (handle != null) return;

            // generic fallback to castToType
            handle = MethodHandles.insertArguments(DTT_CAST_TO_TYPE, 1, staticTargetType);
        }

        private void handleBoolean() {
            if (handle != null) return;
            boolean primitive = (staticTargetType == boolean.class);
            if (!primitive && staticTargetType != Boolean.class) return;
            // boolean->boolean, Boolean->boolean, boolean->Boolean are handled by the compiler
            // which leaves (T)Z and (T)Boolean, where T is the static type but runtime type of T might be Boolean

            MethodHandle ifNull = IS_NULL.asType(MethodType.methodType(boolean.class, staticSourceType));

            MethodHandle thenZero;
            if (primitive) { // false
                thenZero = MethodHandles.dropArguments(MethodHandles.constant(boolean.class, Boolean.FALSE), 0, staticSourceType);
            } else { // (Boolean)null
                thenZero = MethodHandles.identity(staticSourceType).asType(MethodType.methodType(Boolean.class, staticSourceType));
            }

            name = "asBoolean";
            super.setCallSiteTarget();
            MethodHandle elseCallAsBoolean = handle;

            handle = MethodHandles.guardWithTest(ifNull, thenZero, elseCallAsBoolean);
        }
    }

    private static class PropertySelector extends MethodSelector {
        private boolean insertName;

        public PropertySelector(CacheableCallSite callSite, Class<?> sender, String methodName, CallType callType, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
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
         * Chooses a property from the metaclass.
         */
        @Override
        public void chooseMeta(MetaClassImpl mci) {
            var receiver = getCorrectedReceiver();
            if (receiver instanceof GroovyObject) {
                try {
                    var propertyAccessMethod = receiver.getClass().getMethod("getProperty", String.class);
                    if (!propertyAccessMethod.isSynthetic() && !isMarkedInternal(propertyAccessMethod)) {
                        handle = MethodHandles.insertArguments(GROOVY_OBJECT_GET_PROPERTY, 1, name);
                        return;
                    }
                } catch (ReflectiveOperationException ignore) {
                }
            } else if (receiver instanceof Class) {
                handle = MOP_GET;
                handle = MethodHandles.insertArguments(handle, 2, name);
                handle = MethodHandles.insertArguments(handle, 0, this.mc);
                return;
            }

            if (method != null || mci == null) return;

            selectionBase = sender;
            if (sender != mci.getTheClass()) {
                if (GroovyCategorySupport.hasCategoryInCurrentThread()) { // slow path for category property
                    selectionBase = mci.getTheClass();
                } else if (getThisType(sender).isInstance(receiver)) { // GROOVY-5438 for private property
                    selectionBase = getThisType(sender);
                }
            }
            if (LOG_ENABLED) LOG.info("selectionBase set to " + selectionBase);

            var mp = mci.getEffectiveGetMetaProperty(selectionBase, receiver, name, false);
            if (mp instanceof MethodMetaProperty) {
                method = ((MethodMetaProperty) mp).getMetaMethod();
                insertName = true; // pass "name" field as argument
            } else if (mp instanceof CachedField && !mp.isStatic()) {
                try {
                    // GROOVY-9144, GROOVY-9596: get lookup for sender and unreflect before forcing access
                    MethodHandles.Lookup lookup = ((Java8) VMPluginFactory.getPlugin()).newLookup(sender);
                    handle = ((CachedField) mp).asAccessMethod(lookup);
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else {
                handle = META_PROPERTY_GETTER.bindTo(mp);
            }
        }

        private boolean isMarkedInternal(Method reflectionMethod) {
            return reflectionMethod.getAnnotation(Internal.class) != null;
        }

        /**
         * Additionally to the normal {@link MethodSelector#setHandleForMetaMethod()}
         * task we have to also take care of generic getter methods, that depends
         * on the name.
         */
        @Override
        public void setHandleForMetaMethod() {
            if (handle != null) return;
            super.setHandleForMetaMethod();
            if (handle != null && insertName && handle.type().parameterCount() == 2) {
                handle = MethodHandles.insertArguments(handle, 1, name);
            }
        }

        /**
         * The MOP requires all get property operations to go through
         * {@link GroovyObject#getProperty(String)}. We do this in case
         * no property was found before.
         */
        @Override
        public void setMetaClassCallHandleIfNeeded(boolean standardMetaClass) {
            if (handle != null) return;
            useMetaClass = true;
            if (LOG_ENABLED) LOG.info("set meta class invocation path for property get.");
            handle = MethodHandles.insertArguments(MOP_GET, 2, this.name);
            handle = MethodHandles.insertArguments(handle, 0, mc);
        }
    }

    private static class InitSelector extends MethodSelector {
        private static final MethodType MT_OBJECT = MethodType.methodType(Object.class);
        private boolean beanConstructor;

        public InitSelector(CacheableCallSite callSite, Class<?> sender, String methodName, CallType callType, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
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
         * For a constructor call we always use the static metaclass from the registry
         */
        @Override
        public MetaClass getMetaClass() {
            mc = GroovySystem.getMetaClassRegistry().getMetaClass((Class<?>) args[0]);
            if (LOG_ENABLED) LOG.info("meta class is " + mc);
            return mc;
        }

        /**
         * This method chooses a constructor from the metaclass.
         */
        @Override
        public void chooseMeta(MetaClassImpl mci) {
            if (mci == null) return;
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
            if (method == null) return;
            if (method instanceof MetaConstructor) {
                if (LOG_ENABLED) LOG.info("meta method is MetaConstructor instance");
                MetaConstructor mc = (MetaConstructor) method;
                isVargs = mc.isVargsMethod();
                Constructor<?> con = mc.getCachedConstrcutor().getCachedConstructor();
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
                // and the metaclass.

                // to do this we first bind the values to #setBeanProperties
                MethodHandle con = BEAN_CONSTRUCTOR_PROPERTY_SETTER.bindTo(mc);
                // inner class case
                MethodType foldTargetType = MT_OBJECT;
                if (args.length == 3) {
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
         * transformations. Otherwise, we do the same as for {@link MethodSelector#correctParameterLength()}
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
        public void setMetaClassCallHandleIfNeeded(boolean standardMetaClass) {
            if (handle != null) return;
            useMetaClass = true;
            if (LOG_ENABLED) LOG.info("set meta class invocation path");
            handle = MOP_INVOKE_CONSTRUCTOR.bindTo(mc);
            handle = handle.asCollector(Object[].class, targetType.parameterCount() - 1);
            handle = MethodHandles.dropArguments(handle, 0, Class.class);
            if (LOG_ENABLED) LOG.info("create collector for arguments");
        }
    }

    private static class InterfaceSelector extends MethodSelector {
        public InterfaceSelector(CacheableCallSite callSite, Class<?> sender, String methodName, CallType callType, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
            super(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
        }

        @Override
        public MetaClass getMetaClass() {
            mc = GroovySystem.getMetaClassRegistry().getMetaClass(targetType.parameterType(0));
            mc.initialize();
            if (LOG_ENABLED) LOG.info("meta class is " + mc);
            return mc;
        }

        @Override
        public void setSelectionBase() {
            selectionBase = mc.getTheClass();
            if (LOG_ENABLED) LOG.info("selectionBase set to " + selectionBase);
        }

        @Override
        public MethodHandle unreflect(Method cachedMethod) throws IllegalAccessException {
            return this.callSite.getLookup().unreflectSpecial(cachedMethod, this.sender); // throws if sender cannot invoke method
        }
    }

    /**
     * Method invocation based {@link Selector}.
     * This Selector is called for method invocations and is base for constructor
     * calls as well as getProperty calls.
     */
    private static class MethodSelector extends Selector {
        private static final Object[] SINGLE_NULL_ARRAY = {null};
        private boolean isCategoryMethod;
        protected MetaClass mc;

        public MethodSelector(CacheableCallSite callSite, Class<?> sender, String methodName, CallType callType, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object[] arguments) {
            this.callType = callType;
            this.targetType = callSite.type();
            this.name = methodName;
            this.originalArguments = arguments;
            this.args = spread(arguments, spreadCall);
            this.callSite = callSite;
            this.sender = sender;
            this.safeNavigationOrig = safeNavigation;
            this.safeNavigation = safeNavigation && arguments[0] == null;
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
                for (int i = 0; i < arguments.length; i++) {
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
            handle = MethodHandles.dropArguments(NULL_REF, 0, targetType.parameterArray());
            if (LOG_ENABLED) LOG.info("set null returning handle for safe navigation");
            return true;
        }

        /**
         * Gives the metaclass to an Object.
         */
        public MetaClass getMetaClass() {
            var receiver = getCorrectedReceiver();
            if (receiver instanceof GroovyObject) {
                mc = ((GroovyObject) receiver).getMetaClass();
            } else if (receiver instanceof Class) {
                Class<?> c = (Class<?>) receiver;
                mc = GroovySystem.getMetaClassRegistry().getMetaClass(c);
                cache &= !ClassInfo.getClassInfo(c).hasPerInstanceMetaClasses();
            } else {
                mc = ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(receiver);
                cache &= !ClassInfo.getClassInfo(receiver.getClass()).hasPerInstanceMetaClasses();
            }
            mc.initialize();
            if (LOG_ENABLED) LOG.info("meta class is " + mc);
            return mc;
        }

        /**
         * Uses the metaclass to get a meta method for a method call.
         * There will be no meta method selected, if the metaclass is no MetaClassImpl
         * or the metaclass is an AdaptingMetaClass.
         */
        public void chooseMeta(MetaClassImpl mci) {
            if (mci == null) return;
            Object receiver = getCorrectedReceiver();
            Object[] newArgs = removeRealReceiver(args);
            if (receiver instanceof Class) {
                if (LOG_ENABLED) LOG.info("receiver is a class");
                if (!mci.hasCustomStaticInvokeMethod())
                    method = mci.retrieveStaticMethod(name, newArgs);
            }
            else if (!mci.hasCustomInvokeMethod()) {
                String name = this.name;
                if (name.equals("call") && receiver instanceof GeneratedClosure) {
                    name = "doCall";
                }
                method = mci.getMethodWithCaching(selectionBase, name, newArgs, false);
            }
            if (LOG_ENABLED) LOG.info("retrieved method from meta class: " + method);
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
            isCategoryMethod = (method instanceof CategoryMethod);

            if (metaMethod instanceof NumberNumberMetaMethod
                    || (method instanceof GeneratedMetaMethod && (name.equals("next") || name.equals("previous")))) {
                if (LOG_ENABLED) LOG.info("meta method is number method");
                if (IndyMath.chooseMathMethod(this, metaMethod)) {
                    catchException = false;
                    if (LOG_ENABLED) LOG.info("indy math successful");
                    return;
                }
            }

            boolean isCategoryTypeMethod = (metaMethod instanceof NewInstanceMetaMethod);
            if (LOG_ENABLED) LOG.info("meta method is category type method: " + isCategoryTypeMethod);
            boolean isStaticCategoryTypeMethod = (metaMethod instanceof NewStaticMetaMethod);
            if (LOG_ENABLED) LOG.info("meta method is static category type method: " + isCategoryTypeMethod);

            if (metaMethod instanceof ReflectionMetaMethod) {
                if (LOG_ENABLED) LOG.info("meta method is reflective method");
                metaMethod = ((ReflectionMetaMethod) metaMethod).getCachedMethod();
            }

            if (metaMethod instanceof CachedMethod) {
                isVargs = metaMethod.isVargsMethod();
                CachedMethod cm = (CachedMethod) metaMethod;
                VMPlugin vmplugin = VMPluginFactory.getPlugin();
                cm = (CachedMethod) vmplugin.transformMetaMethod(mc, cm, sender);
                try {
                    var declaringClass = cm.getDeclaringClass().getTheClass();
                    int parameterCount = cm.getParamsCount();
                    if (parameterCount == 0 && name.equals("clone") && declaringClass == Object.class) {
                        var receiverClass = getCorrectedReceiver().getClass();
                        if (receiverClass.isArray()) { // GROOVY-10733, et al.
                            handle = MethodHandles.publicLookup().findVirtual(receiverClass, "clone", MethodType.methodType(Object.class));
                        } else { // GROOVY-10319
                            handle = MethodHandles.throwException(Object.class, CloneNotSupportedException.class) // prevent illegal access
                                                                    .bindTo(new CloneNotSupportedException());
                            handle = MethodHandles.dropArguments(handle, 0, Object.class); // discard receiver
                        }
                    } else if (parameterCount == 1 && name.equals("forName") && declaringClass == Class.class) {
                        handle = MethodHandles.insertArguments(CLASS_FOR_NAME, 1, Boolean.TRUE, sender.getClassLoader());
                    } else {
                        handle = unreflect(cm.getCachedMethod());
                    }
                } catch (ReflectiveOperationException e) {
                    throw new GroovyBugError(e);
                }
                if (isStaticCategoryTypeMethod) {
                    handle = MethodHandles.insertArguments(handle, 0, SINGLE_NULL_ARRAY);
                    handle = MethodHandles.dropArguments(handle, 0, targetType.parameterType(0));
                } else if (!isCategoryTypeMethod && cm.isStatic()) {
                    // drop the receiver, which might be a Class (invocation on Class)
                    // or it might be an object (static method invocation on instance)
                    // Object.class handles both cases at once
                    handle = MethodHandles.dropArguments(handle, 0, Object.class);
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
                    handle = handle.asCollector(Object[].class, targetType.parameterCount() - 1);
                }
                currentType = removeWrapper(targetType);
                if (LOG_ENABLED) LOG.info("bound method name to META_METHOD_INVOKER");
            }
        }

        protected MethodHandle unreflect(Method cachedMethod) throws IllegalAccessException {
            return this.callSite.getLookup().unreflect(cachedMethod); // throws if sender cannot invoke method
        }

        /**
         * Helper method to manipulate the given type to replace Wrapper with Object.
         */
        private MethodType removeWrapper(MethodType targetType) {
            Class<?>[] types = targetType.parameterArray();
            for (int i = 0; i < types.length; i++) {
                if (types[i] == Wrapper.class) {
                    targetType = targetType.changeParameterType(i, Object.class);
                }
            }
            return targetType;
        }

        /**
         * Creates a MethodHandle, which will use the metaclass path.
         * This method is called only if no handle has been created before. This
         * is usually the case if the method selection failed.
         */
        public void setMetaClassCallHandleIfNeeded(boolean standardMetaClass) {
            if (handle != null) return;
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
                    // if the metaclass call fails we may still want to fall back to call
                    // GroovyObject#invokeMethod if the receiver is a GroovyObject
                    if (LOG_ENABLED) LOG.info("add MissingMethod handler for GroovyObject#invokeMethod fallback path");
                    handle = MethodHandles.catchException(handle, MissingMethodException.class, GROOVY_OBJECT_INVOKER);
                }
            }
            handle = MethodHandles.insertArguments(handle, 1, name);
            if (!spread) handle = handle.asCollector(Object[].class, targetType.parameterCount() - 1);
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
            Class<?>[] pt = handle.type().parameterArray();
            if (currentType != null) pt = currentType.parameterArray();
            for (int i = 1; i < args.length; i++) {
                if (args[i] instanceof Wrapper) {
                    Class<?> type = pt[i];
                    MethodType mt = MethodType.methodType(type, Wrapper.class);
                    handle = MethodHandles.filterArguments(handle, i, UNWRAP_METHOD.asType(mt));
                    if (LOG_ENABLED) LOG.info("added filter for Wrapper for argument at pos " + i);
                }
            }
        }

        /**
         * Handles cases in which we have to correct the length of arguments
         * using the parameters. This might be needed for vargs and for one
         * parameter calls without arguments (null is used then).
         */
        public void correctParameterLength() {
            if (handle == null) return;

            Class<?>[] params = handle.type().parameterArray();
            if (currentType != null) params = currentType.parameterArray();
            if (!isVargs) {
                if (spread && useMetaClass) return;
                if (params.length == 2 && args.length == 1) {
                    handle = MethodHandles.insertArguments(handle, 1, SINGLE_NULL_ARRAY);
                }
                return;
            }

            int aCount = args.length;
            int pCount = params.length;
            var vaType = params[pCount-1];
            if (aCount == pCount) {
                var lastArg = MetaClassHelper.convertToTypeArray(args)[aCount-1]; // GROOVY-6146
                if (lastArg != null && (!lastArg.isArray() || (ArrayTypeUtils.dimension(lastArg)
                            != ArrayTypeUtils.dimension(vaType) && vaType != Object[].class))) {
                    // we depend on the method selection having done a good job previously
                    // arg is null with cast or not assignment compatible; wrap with array
                    handle = handle.asCollector(vaType, 1);
                    if (LOG_ENABLED) LOG.info("changed last argument to be collected for variadic parameter");
                }
            } else if (aCount < pCount) {
                // we depend on the method selection having done a good
                // job before already, so the only case for this here is, that
                // we have no argument for the array, meaning params.length is
                // args.length+1. In that case we have to fill in an empty array
                handle = MethodHandles.insertArguments(handle, pCount - 1, Array.newInstance(vaType.getComponentType(), 0));
                if (LOG_ENABLED) LOG.info("added empty array for variadic parameter");
            } else { // aCount > pCount
                // we depend on the method selection having done a good
                // job before already, so the only case for this here is, that
                // all trailing arguments belong into the vargs array
                handle = handle.asCollector(vaType, aCount - pCount + 1);
                if (LOG_ENABLED) LOG.info("changed surplus arguments to be collected for variadic parameter");
            }
        }

        /**
         * There are some conversions we have to do explicitly.
         * These are GString to String, Number to Byte and Number to BigInteger
         * conversions.
         */
        public void correctCoerce() {
            if (useMetaClass) return;

            Class<?>[] parameterTypes = handle.type().parameterArray();
            if (currentType != null) parameterTypes = currentType.parameterArray();
            if (args.length != parameterTypes.length) {
                throw new GroovyBugError("At this point argument array length and parameter array length should be the same");
            }
            for (int i = 0; i < args.length; i++) {
                final Class<?> parameterType = parameterTypes[i];
                if (parameterType == Object.class) continue;
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

                if (arg == null) continue;
                Class<?> got = arg.getClass();

                // equal class, nothing to do
                if (got == parameterType) continue;

                Class<?> wrappedPara = TypeHelper.getWrapperClass(parameterType);
                // equal class with one maybe a primitive, the later explicitCastArguments will solve this case
                if (wrappedPara == TypeHelper.getWrapperClass(got)) continue;

                // equal in terms of an assignment in Java. That means according to Java widening rules, or
                // a subclass, interface, superclass relation, this case then handles also
                // primitive to primitive conversion. Those cases are also solved by explicitCastArguments.
                if (parameterType.isAssignableFrom(got)) continue;

                // to aid explicitCastArguments we convert to the wrapper type to let it only unbox
                handle = TypeTransformers.addTransformer(handle, i, arg, wrappedPara);
                if (LOG_ENABLED)
                    LOG.info("added transformer at pos " + i + " for type " + got + " to type " + wrappedPara);
            }
        }

        /**
         * Gives a replacement receiver for null.
         * In case of the receiver being null we want to do the method
         * invocation on NullObject instead.
         */
        public void correctNullReceiver() {
            if (args[0] != null) return;
            handle = handle.bindTo(NullObject.getNullObject());
            handle = MethodHandles.dropArguments(handle, 0, targetType.parameterType(0));
            if (LOG_ENABLED) LOG.info("binding null object receiver and dropping old receiver");
        }

        public void correctSpreading() {
            if (!spread || useMetaClass || skipSpreadCollector) return;
            handle = handle.asSpreader(Object[].class, args.length - 1);
        }

        /**
         * Adds the standard exception handler.
         */
        public void addExceptionHandler() {
            //TODO: if we would know exactly which paths require the exceptions
            //      and which paths not, we can sometimes save this guard
            if (handle == null || !catchException) return;
            Class<?> returnType = handle.type().returnType();
            if (returnType != Object.class) {
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
        public void setGuards(Object receiver) {
            if (handle == null) return;
            if (!cache) return;

            MethodHandle fallback = callSite.getFallbackTarget();

            // special guards for receiver
            if (receiver instanceof GroovyObject) {
                GroovyObject go = (GroovyObject) receiver;
                MetaClass mc = go.getMetaClass();
                MethodHandle test = SAME_MC.bindTo(mc);
                // drop dummy receiver
                test = test.asType(MethodType.methodType(boolean.class, targetType.parameterType(0)));
                handle = MethodHandles.guardWithTest(test, handle, fallback);
                if (LOG_ENABLED) LOG.info("added meta class equality check");
            } else if (receiver instanceof Class) {
                MethodHandle test = EQUALS.bindTo(receiver);
                test = test.asType(MethodType.methodType(boolean.class, targetType.parameterType(0)));
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

            // handle constant metaclass and category changes
            handle = switchPoint.guardWithTest(handle, fallback);
            if (LOG_ENABLED) LOG.info("added switch point guard");

            // guards for receiver and parameter
            Class<?>[] pt = handle.type().parameterArray();
            if (Arrays.stream(args).anyMatch(Objects::isNull)) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    Class<?> paramType = pt[i];
                    MethodHandle test;
                    if (arg == null) {
                        test = IS_NULL.asType(MethodType.methodType(boolean.class, paramType));
                        if (LOG_ENABLED) LOG.info("added null argument check at pos " + i);
                    } else {
                        if (Modifier.isFinal(paramType.getModifiers())) {
                            // primitive types are also `final`
                            continue;
                        }
                        test = SAME_CLASS.bindTo(arg.getClass()).asType(MethodType.methodType(boolean.class, paramType));
                        if (LOG_ENABLED) LOG.info("added same class check at pos " + i);
                    }
                    Class<?>[] drops = new Class[i];
                    System.arraycopy(pt, 0, drops, 0, drops.length);
                    test = MethodHandles.dropArguments(test, 0, drops);
                    handle = MethodHandles.guardWithTest(test, handle, fallback);
                }
            } else if (Arrays.stream(pt).anyMatch(paramType -> !Modifier.isFinal(paramType.getModifiers()))) {
                MethodHandle test = SAME_CLASSES
                        .bindTo(Arrays.stream(args).map(Object::getClass).toArray(Class[]::new))
                        .asCollector(Object[].class, pt.length)
                        .asType(MethodType.methodType(boolean.class, pt));
                handle = MethodHandles.guardWithTest(test, handle, fallback);
            } else if (safeNavigationOrig) { // GROOVY-11126
                MethodHandle test = NON_NULL.asType(MethodType.methodType(boolean.class, pt[0]));
                handle = MethodHandles.guardWithTest(test, handle, fallback);
                if (LOG_ENABLED) LOG.info("added null receiver check");
            }
        }

        /**
         * do the actual call site target set, if the call is supposed to be cached
         */
        public void doCallSiteTargetSet() {
            if (LOG_ENABLED) LOG.info("call site stays uncached");
        }

        /**
         * Chooses the class passed to {@link MetaClassImpl#getMethodWithCaching}.
         *
         * @see #chooseMeta(MetaClassImpl)
         */
        public void setSelectionBase() {
            Class<?> sender = getThisType(this.sender);
            if (thisCall || sender.isInstance(args[0])) { // GROOVY-2433
                selectionBase = sender;
            } else {
                selectionBase = mc.getTheClass();
            }
            if (LOG_ENABLED) LOG.info("selectionBase set to " + selectionBase);
        }

        /**
         * Sets a handle to call {@link GroovyInterceptable#invokeMethod(String, Object)}
         */
        public boolean setInterceptor() {
            if (!(args[0] instanceof GroovyInterceptable)) return false;
            handle = MethodHandles.insertArguments(INTERCEPTABLE_INVOKER, 1, name);
            handle = handle.asCollector(Object[].class, targetType.parameterCount() - 1);
            handle = handle.asType(targetType);
            return true;
        }

        /**
         * setting a call site target consists of the following steps:
         * # get the metaclass
         * # select a method/constructor/property from it, if it is a MetaClassImpl
         * # make a handle out of the selection
         * # if nothing could be selected, select a path through the given MetaClass or the GroovyObject
         * # apply transformations for vargs, implicit null argument, coercion, wrapping, null receiver and spreading
         */
        @Override
        public void setCallSiteTarget() {
            if (!setNullForSafeNavigation() && !setInterceptor()) {
                getMetaClass();
                setSelectionBase();
                MetaClassImpl mci = getMetaClassImpl(mc, callType != CallType.GET);
                chooseMeta(mci);
                setHandleForMetaMethod();
                setMetaClassCallHandleIfNeeded(mci != null);
                correctParameterLength();
                correctCoerce();
                correctWrapping();
                correctNullReceiver();
                correctSpreading();

                if (LOG_ENABLED) LOG.info("casting explicit from " + handle.type() + " to " + targetType);
                handle = MethodHandles.explicitCastArguments(handle, targetType);

                addExceptionHandler();
            }
            setGuards(args[0]);
            doCallSiteTargetSet();
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the MetaClassImpl if the given MetaClass is one of
     * MetaClassImpl, AdaptingMetaClass or ClosureMetaClass. If
     * none of these cases matches, this method returns null.
     */
    private static MetaClassImpl getMetaClassImpl(final MetaClass mc, final boolean includeEMC) {
        Class<?> mcc = mc.getClass();
        boolean valid = mcc == MetaClassImpl.class
                || mcc == ClosureMetaClass.class
                || (includeEMC && mcc == ExpandoMetaClass.class);
        if (!valid) {
            if (LOG_ENABLED) LOG.info("meta class is neither MetaClassImpl, nor ClosureMetaClass, normal method selection path disabled.");
            return null;
        }
        if (LOG_ENABLED) LOG.info("meta class is a recognized MetaClassImpl");
        return (MetaClassImpl) mc;
    }

    /**
     * Helper method to transform the given arguments, consisting of the receiver
     * and the actual arguments in an Object[], into a new Object[] consisting
     * of the receiver and the arguments directly. Before the size of args was
     * always 2, the returned Object[] will have a size of 1+n, where n is the
     * number arguments.
     */
    private static Object[] spread(final Object[] args, final boolean spreadCall) {
        if (!spreadCall) return args;
        Object[] normalArguments = (Object[]) args[1];
        Object[] ret = new Object[normalArguments.length + 1];
        ret[0] = args[0];
        System.arraycopy(normalArguments, 0, ret, 1, ret.length - 1);
        return ret;
    }

    /**
     * Helper method to remove the receiver from the argument array
     * by producing a new array.
     */
    private static Object[] removeRealReceiver(final Object[] args) {
        Object[] ar = new Object[args.length - 1];
        System.arraycopy(args, 1, ar, 0, args.length - 1);
        return ar;
    }

    /**
     * Unwraps the given object from a {@link Wrapper}. If not
     * wrapped, the given object is returned.
     */
    private static Object unwrapIfWrapped(final Object object) {
        return object instanceof Wrapper ? unwrap(object) : object;
    }

    private static Class<?> getThisType(Class<?> sender) {
        while (GeneratedClosure.class.isAssignableFrom(sender)) {
            sender = sender.getEnclosingClass();
        }
        return sender;
    }
}
