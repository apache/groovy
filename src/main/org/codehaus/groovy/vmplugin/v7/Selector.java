/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.vmplugin.v7;

import groovy.lang.AdaptingMetaClass;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import groovy.lang.MetaObjectProtocol;
import groovy.lang.MetaProperty;
import groovy.lang.MissingMethodException;
import groovy.lang.MetaClassImpl.MetaConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberMetaMethod;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.MethodMetaProperty;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.vmplugin.v7.IndyInterface.CALL_TYPES;

import static org.codehaus.groovy.vmplugin.v7.IndyInterface.*;

public abstract class Selector {
    public Object[] args, originalArguments;
    public MetaMethod method;
    public MethodType targetType,currentType;
    public String name;
    public MethodHandle handle;
    public boolean useMetaClass = false;
    public MutableCallSite callSite;
    public Class sender;
    public boolean isVargs;
    public boolean safeNavigation, safeNavigationOrig, spread, genericInvoker;
    public boolean skipSpreadCollector;
    public boolean thisCall;
    public Class selectionBase;
    public boolean catchException = true;
    public CALL_TYPES callType;
    
    public static Selector getSelector(MutableCallSite callSite, Class sender, String methodName, int callID, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
        CALL_TYPES callType = CALL_TYPES.values()[callID];
        switch (callType) {
            case INIT: return new InitSelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case METHOD: return new MethodSelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
            case GET: case SET:
                return new PropertySelector(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
        }
        return null;
    }
    abstract void setCallSiteTarget();

    private static Object[] spread(Object[] args, boolean spreadCall) {
        if (!spreadCall) return args;
        Object[] normalArguments = (Object[]) args[1];
        Object[] ret = new Object[normalArguments.length+1];
        ret[0] = args[0];
        System.arraycopy(normalArguments, 0, ret, 1, ret.length-1);
        return ret;
    }

    private static class PropertySelector extends MethodSelector {

        public PropertySelector(MutableCallSite callSite, Class sender, String methodName, CALL_TYPES callType, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
            super(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
        }

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
                    if (!reflectionMethod.isSynthetic()) {
                        handle = LOOKUP.unreflect(reflectionMethod);
                        handle = MethodHandles.insertArguments(handle, 1, name);
                        genericInvoker = true;
                        return;
                    }
                } catch (ReflectiveOperationException e)  {}
            }

            if (method!=null || mci==null) return;
            MetaProperty res = mci.getEffectiveGetMetaProperty(mci.getTheClass(), receiver, name, false);
            if (res instanceof MethodMetaProperty) {
                MethodMetaProperty mmp = (MethodMetaProperty) res;
                method = mmp.getMetaMethod();
            } else if (res instanceof CachedField) {
                CachedField cf = (CachedField) res;
                Field f = cf.field;
                try {
                    handle = LOOKUP.unreflectGetter(f);
                    if (Modifier.isStatic(f.getModifiers())) {
                        // normally we would do the following
                        // handle = MethodHandles.dropArguments(handle,0,Class.class);
                        // but because there is a bug in invokedynamic in all jdk7 versions up to update 7
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

        @Override
        public void setHandleForMetaMethod() {
            if (handle!=null) return;
            super.setHandleForMetaMethod();
        }

        @Override
        public void setMetaClassCallHandleIfNedded() {
            if (handle!=null) return;
            useMetaClass = true;
            try {
                if (LOG_ENABLED) LOG.info("set meta class invocation path for property get.");
                handle = LOOKUP.findVirtual(MetaClass.class, "getProperty", MethodType.methodType(Object.class, Class.class, Object.class, String.class, boolean.class, boolean.class));
                handle = MethodHandles.insertArguments(handle, 0, mc, selectionBase);
                handle = MethodHandles.insertArguments(handle, 1, this.name, false, false);
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
        }
    }

    private static class InitSelector extends MethodSelector {
        private boolean beanConstructor;
        
        public InitSelector(MutableCallSite callSite, Class sender, String methodName, CALL_TYPES callType, boolean safeNavigation, boolean thisCall, boolean spreadCall, Object[] arguments) {
            super(callSite, sender, methodName, callType, safeNavigation, thisCall, spreadCall, arguments);
        }
        
        @Override
        public boolean setInterceptor() {
            return false;
        }

        @Override
        public void getMetaClass() {
            Object receiver = args[0];
            mc = GroovySystem.getMetaClassRegistry().getMetaClass((Class) receiver);
        }

        /**
         * this method chooses a constructor from the meta class
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

        @Override
        public void setHandleForMetaMethod() {
            super.setHandleForMetaMethod();
            if (handle==null) return;
            if (beanConstructor) {
                // we have handle that takes no arguments to create the bean, 
                // we have to use its return value to call #setBeanProperties with it
                // and the meta class.

                // to do this we first bind the values to #setBeanProperties
                MethodHandle con = BEAN_CONSTRUCTOR_PROPERTY_SETTER.bindTo(mc);
                handle = MethodHandles.foldArguments(con, handle.asType(MethodType.methodType(Object.class)));
            }
            handle = MethodHandles.dropArguments(handle, 0, Class.class);
        }

        @Override
        public void correctParameterLength() {
            if (beanConstructor) return;
            super.correctParameterLength();
        }

        @Override
        public void correctCoerce() {
            if (beanConstructor) return;
            super.correctCoerce();
        }
        
        @Override
        public void setMetaClassCallHandleIfNedded() {
            if (handle!=null) return;
            try {
                useMetaClass = true;
                if (LOG_ENABLED) LOG.info("set meta class invocation path");
                handle = LOOKUP.findVirtual(MetaObjectProtocol.class, "invokeConstructor", MethodType.methodType(Object.class, Object[].class));
                handle = handle.bindTo(mc);
                handle = handle.asCollector(Object[].class, targetType.parameterCount()-1);
                handle = MethodHandles.dropArguments(handle, 0, Class.class);
                if (LOG_ENABLED) LOG.info("create collector for arguments");
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }

        }
    }

    private static class MethodSelector extends Selector {
        protected MetaClass mc;
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

            if (LOG_ENABLED) {
                String msg =
                    "----------------------------------------------------"+
                    "\n\t\tinvocation of method '"+methodName+"'"+
                    "\n\t\tinvocation type: "+callType+
                    "\n\t\tsender: "+sender+
                    "\n\t\ttargetType: "+targetType+
                    "\n\t\tsafe navigation: "+safeNavigation+
                    "\n\t\tthisCall: "+thisCall+
                    "\n\t\tspreadCall: "+spreadCall+
                    "\n\t\twith "+arguments.length+" arguments";
                for (int i=0; i<arguments.length; i++) {
                    msg += "\n\t\t\targument["+i+"] = "+arguments[i];
                }
                LOG.info(msg);
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
                mc = GroovySystem.getMetaClassRegistry().getMetaClass((Class)receiver);
            } else {
                mc = ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(receiver);
            }
        }

        /**
         * Uses the meta class to get a meta method for a method call.
         * There will be no meta method selected, if the meta class is no MetaClassImpl
         * or the meta class is an AdaptingMetaClass.
         */
        public void chooseMeta(MetaClassImpl mci) {
            if (mci==null) return;
            Object receiver = getCorrectedReceiver();
            if (receiver instanceof Class) {
                if (LOG_ENABLED) LOG.info("receiver is a class");
                method = mci.retrieveStaticMethod(name, removeRealReceiver(args));
            } else {
                method = mci.getMethodWithCaching(selectionBase, name, removeRealReceiver(args), false);
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

            if (metaMethod instanceof NumberNumberMetaMethod) {
                if (LOG_ENABLED) LOG.info("meta method is number method");
                catchException = false;
                if (IndyMath.chooseMathMethod(this, metaMethod)) {
                    if (LOG_ENABLED) LOG.info("indy math successfull");
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
                    handle = LOOKUP.unreflect(m);
                    if (LOG_ENABLED) LOG.info("successfully unreflected method");
                    if (isStaticCategoryTypeMethod) {
                        handle = MethodHandles.insertArguments(handle, 0, new Object[]{null});
                        handle = MethodHandles.dropArguments(handle, 0, targetType.parameterType(0));
                    } else if (!isCategoryTypeMethod && isStatic(m)) {
                        handle = MethodHandles.dropArguments(handle, 0, Class.class);
                    } 
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else if (metaMethod instanceof MetaConstructor) {
                //TODO: move this to init selector
                if (LOG_ENABLED) LOG.info("meta method is MetaConstructor instance");
                MetaConstructor mc = (MetaConstructor) metaMethod;
                isVargs = mc.isVargsMethod();
                Constructor con = mc.getCachedConstrcutor().cachedConstructor;
                try {
                    handle = LOOKUP.unreflectConstructor(con);
                    if (LOG_ENABLED) LOG.info("successfully unreflected constructor");
                } catch (IllegalAccessException e) {
                    throw new GroovyBugError(e);
                }
            } else if (method != null) {
                if (LOG_ENABLED) LOG.info("meta method is dgm helper");
                // dgm method helper path
                handle = META_METHOD_INVOKER;
                handle = handle.bindTo(method);
                if (LOG_ENABLED) LOG.info("bound method name to META_METHOD_INVOKER");

                if (
                        method.getNativeParameterTypes().length==1 && 
                        args.length==1 
                ) {
                    // the method expects a parameter but we don't provide an 
                    // argument for that. So we give in a Object[], containing 
                    // a null value
                    // since MethodHandles.insertArguments is a vargs method giving
                    // only the array would be like just giving a null value, so
                    // we need to wrap the array that represents our argument in
                    // another one for the vargs call
                    handle = MethodHandles.insertArguments(handle, 1, new Object[]{new Object[]{null}});
                    if (LOG_ENABLED) LOG.info("null argument expansion");
                } else if (method.isVargsMethod() && !spread) {
                    // the method expects the arguments as Object[] in the target[]
                    // in case of the methods given as single arguments we need to create
                    // the target array, in case the array is given already we do not. The
                    // creation of the target array we will leave to #correctParameterLength
                    // Thus we create only the wrapping Object[] and use currentType to hint 
                    // the signature we would like to have

                    // create Object[] wrapper, set vargs to true and set override type
                    handle = handle.asCollector(Object[].class, 1);
                    isVargs = true;
                    currentType = MethodType.methodType(method.getReturnType(), method.getNativeParameterTypes());
                    currentType = currentType.insertParameterTypes(0, method.getDeclaringClass().getTheClass());
                    if (LOG_ENABLED) LOG.info("wrapping vargs for dgm helper");
                } else {
                    handle = handle.asCollector(Object[].class, targetType.parameterCount()-1);
                    currentType = MethodType.methodType(method.getReturnType(), method.getNativeParameterTypes());
                    currentType = currentType.insertParameterTypes(0, method.getDeclaringClass().getTheClass());
                    if (spread) {
                        args = originalArguments;
                        skipSpreadCollector = true;
                    }
                    if (LOG_ENABLED) LOG.info("normal dgm helper wrapping");
                }
            }
        }

        /**
         * Creates a MethodHandle, which will use the meta class path.
         * This method is called only if no handle has been created before. This
         * is usually the case if the method selection failed.
         */
        public void setMetaClassCallHandleIfNedded() {
            if (handle!=null) return;
            try {
                useMetaClass = true;
                if (LOG_ENABLED) LOG.info("set meta class invocation path");
                Object receiver = args[0];
                if (receiver instanceof Class) {
                    handle = LOOKUP.findVirtual(MetaClass.class, "invokeStaticMethod", MethodType.methodType(Object.class, Object.class, String.class, Object[].class));
                    handle = handle.bindTo(mc);
                    if (LOG_ENABLED) LOG.info("use invokeStaticMethod with bound meta class");
                } else {
                    boolean useShortForm = mc instanceof AdaptingMetaClass;
                    if (useShortForm) {
                        handle = LOOKUP.findVirtual(MetaObjectProtocol.class, "invokeMethod", MethodType.methodType(Object.class, Object.class, String.class, Object[].class));
                    } else {
                        handle = LOOKUP.findVirtual(MetaClass.class, "invokeMethod", INVOKE_METHOD_SIGNATURE);
                        handle = MethodHandles.insertArguments(handle, handle.type().parameterCount()-2, false, true);
                    }

                    handle = handle.bindTo(mc);
                    if (!useShortForm) {
                        handle = handle.bindTo(selectionBase);
                    }
                    if (LOG_ENABLED) LOG.info("use invokeMethod with bound meta class");

                    if (receiver instanceof GroovyObject) {
                        // if the meta class call fails we may still want to fall back to call
                        // GroovyObject#invokeMethod if the receiver is a GroovyObject
                        if (LOG_ENABLED) LOG.info("add MissingMethod handler for GrooObject#invokeMethod fallback path");
                        handle = MethodHandles.catchException(handle, MissingMethodException.class, GROOVY_OBJECT_INVOKER);
                    }
                }
                handle = MethodHandles.insertArguments(handle, 1, name);
                handle = handle.asCollector(Object[].class, targetType.parameterCount()-1);
                if (LOG_ENABLED) LOG.info("bind method name and create collector for arguments");
            } catch (Exception e) {
                throw new GroovyBugError(e);
            }
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
                if (params.length != args.length) {
                  //TODO: add null argument
                }
                return;
            }

            Class lastParam = params[params.length-1];
            Object lastArg = unwrapIfWrapped(args[args.length-1]);
            if (params.length == args.length) {
                // may need rewrap
                if (lastParam == lastArg || lastArg == null) return;
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
                if (got==parameters[i]) continue;
                Class wrappedPara = TypeHelper.getWrapperClass(parameters[i]);
                if (wrappedPara==got) continue;
                if (parameters[i].isAssignableFrom(got)) continue;
                if (isPrimitiveOrWrapper(parameters[i]) && isPrimitiveOrWrapper(got)) continue;
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
            if (handle==null || catchException==false) return;
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
            if (spread) return;

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
            }

            if (!useMetaClass) {
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
                    if (Modifier.isFinal(argClass.getModifiers()) && TypeHelper.argumentClassIsParameterClass(argClass,pt[i])) continue;
                    test = SAME_CLASS.
                                bindTo(argClass).
                                asType(MethodType.methodType(boolean.class, pt[i]));
                    if (LOG_ENABLED) LOG.info("added same class check at pos "+i);
                }
                Class[] drops = new Class[i];
                for (int j=0; j<drops.length; j++) drops[j] = pt[j];
                test = MethodHandles.dropArguments(test, 0, drops);
                handle = MethodHandles.guardWithTest(test, handle, fallback);
            }
        }

        public void doCallSiteTargetSet() {
            if (spread) {
                if (LOG_ENABLED) LOG.info("call site target kept for spread call");
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
        
        public boolean setInterceptor() {
            if (!(this.args[0] instanceof GroovyInterceptable)) return false;
            try {
                handle = LOOKUP.findVirtual(GroovyInterceptable.class, "invokeMethod", MethodType.methodType(Object.class, String.class, Object.class));
            } catch (ReflectiveOperationException e) {
                throw new GroovyBugError(e);
            }
            handle = MethodHandles.insertArguments(handle, 1, this.name);
            handle = handle.asCollector(Object[].class, targetType.parameterCount()-1); 
            handle = handle.asType(targetType);
            return true;
        }

        @Override
        public void setCallSiteTarget() {
            if (!setNullForSafeNavigation() && !setInterceptor()) {
                getMetaClass();
                if (LOG_ENABLED) LOG.info("meta class is "+mc);
                setSelectionBase();
                MetaClassImpl mci = getMetaClassImpl(mc);
                chooseMeta(mci);
                setHandleForMetaMethod();
                setMetaClassCallHandleIfNedded();
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
     * Return true if the given argument is either a primitive or
     * one of its wrapper. 
     */
    private static boolean isPrimitiveOrWrapper(Class c) {
        return c == byte.class    || c == Byte.class      ||
                c == int.class     || c == Integer.class   ||
                c == long.class    || c == Long.class      ||
                c == float.class   || c == Float.class     ||
                c == double.class  || c == Double.class    ||
                c == short.class   || c == Short.class     ||
                c == boolean.class || c == Boolean.class   ||
                c == char.class    || c == Character.class;
    }

    private static Object unwrapIfWrapped(Object object) {
        if (object instanceof Wrapper) return unwrap(object);
        return object;
    }
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

    private static MetaClassImpl getMetaClassImpl(MetaClass mc) {
        if (!(mc instanceof MetaClassImpl) || mc instanceof AdaptingMetaClass) {
            if (LOG_ENABLED) LOG.info("meta class is neither MetaClassImpl nor AdoptingMetaClass, normal method selection path disabled.");
            return null;
        }
        if (LOG_ENABLED) LOG.info("meta class is a MetaClassImpl");
        return (MetaClassImpl) mc;
    }

    /**
     * Helper method to remove the receiver from the argument array
     * by producing a new array.
     */
    private static Object[] removeRealReceiver(Object[] args) {
        Object[] ar = new Object[args.length-1];
        for (int i=1; i<args.length; i++) {
            ar[i-1] = args[i];
        }
        return ar;
    }
}