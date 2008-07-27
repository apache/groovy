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
package groovy.lang;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.MixinInMetaClass;
import org.codehaus.groovy.runtime.metaclass.MixinInstanceMetaMethod;
import org.codehaus.groovy.util.FastArray;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.callsite.*;
import org.codehaus.groovy.runtime.metaclass.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A MetaClass that implements GroovyObject and behaves like an Expando, allowing the addition of new methods on the fly
 *
 * <code><pre>
 * // defines or replaces instance method:
 * metaClass.myMethod = { args -> }
 *
 * // defines a new instance method
 * metaClass.myMethod << { args -> }
 *
 * // creates multiple overloaded methods of the same name
 * metaClass.myMethod << { String s -> } << { Integer i -> }
 *
 * // defines or replaces a static method with the 'static' qualifier
 * metaClass.'static'.myMethod = { args ->  }
 *
 * // defines a new static method with the 'static' qualifier
 * metaClass.'static'.myMethod << { args ->  }
 *
 * // defines a new constructor
 * metaClass.constructor << { String arg -> }
 *
 * // defines or replaces a constructor
 * metaClass.constructor = { String arg -> }
 *
 * // defines a new property with an initial value of "blah"
 * metaClass.myProperty = "blah"
 *
 * </code></pre>
 *
 * By default methods are only allowed to be added before initialize() is called. In other words you create a new
 * ExpandoMetaClass, add some methods and then call initialize(). If you attempt to add new methods after initialize()
 * has been called an error will be thrown.
 *
 * This is to ensure that the MetaClass can operate appropriately in multi threaded environments as it forces you
 * to do all method additions at the beginning, before using the MetaClass.
 *
 * If you need more fine grained control of how a method is matched you can use DynamicMethodsMetaClass
 *
 * WARNING: This MetaClass uses a thread-bound ThreadLocal instance to store and retrieve properties.
 * In addition properties stored use soft references so they are both bound by the life of the Thread and by the soft
 * references. The implication here is you should NEVER use dynamic properties if you want their values to stick around
 * for long periods because as soon as the JVM is running low on memory or the thread dies they will be garbage collected.
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class ExpandoMetaClass extends MetaClassImpl implements GroovyObject {

	private static final String META_CLASS = "metaClass";
	private static final String CLASS = "class";
	private static final String META_METHODS = "metaMethods";
	private static final String METHODS = "methods";
	private static final String PROPERTIES = "properties";
	public static final String STATIC_QUALIFIER = "static";
	private static final Class[] ZERO_ARGUMENTS = new Class[0];
	public static final String CONSTRUCTOR = "constructor";

    private static final String CLASS_PROPERTY = "class";
    private static final String META_CLASS_PROPERTY = "metaClass";
    private static final String GROOVY_CONSTRUCTOR = "<init>";

    // These two properties are used when no ExpandoMetaClassCreationHandle is present

    private MetaClass myMetaClass;
    private boolean allowChangesAfterInit;

    private boolean initialized;
    private boolean initCalled;
    private boolean modified;
    private boolean inRegistry;
    private final Set inheritedMetaMethods = new HashSet();
    private final Map beanPropertyCache = new ConcurrentHashMap();
    private final Map staticBeanPropertyCache = new ConcurrentHashMap();
    private final Map expandoMethods = new ConcurrentHashMap();

    public Collection getExpandoSubclassMethods() {
        return expandoSubclassMethods.values();
    }

    private final ConcurrentHashMap expandoSubclassMethods = new ConcurrentHashMap();
    private final Map expandoProperties = new ConcurrentHashMap();
    private ClosureStaticMetaMethod invokeStaticMethodMethod;
    private final LinkedHashSet<MixinInMetaClass> mixinClasses = new LinkedHashSet<MixinInMetaClass>();

    /**
	 * Constructs a new ExpandoMetaClass instance for the given class
	 *
	 * @param theClass The class that the MetaClass applies to
	 */
	public ExpandoMetaClass(Class theClass) {
		super(GroovySystem.getMetaClassRegistry(), theClass);
		this.myMetaClass = InvokerHelper.getMetaClass(getClass());
	}

    public ExpandoMetaClass(Class theClass, MetaMethod [] add) {
        super(GroovySystem.getMetaClassRegistry(), theClass, add);
        this.myMetaClass = InvokerHelper.getMetaClass(getClass());
    }

	/**
	 * Constructs a new ExpandoMetaClass instance for the given class optionally placing the MetaClass
	 * in the MetaClassRegistry automatically
	 *
	 * @param theClass The class that the MetaClass applies to
	 * @param register True if the MetaClass should be registered inside the MetaClassRegistry. This defaults to true and ExpandoMetaClass will effect all instances if changed
	 */
	public ExpandoMetaClass(Class theClass, boolean register) {
		this(theClass);
	    this.inRegistry = register;
	}

    public ExpandoMetaClass(Class theClass, boolean register, MetaMethod [] add) {
        this(theClass, add);
        this.inRegistry = register;
    }

	/**
	 * Constructs a new ExpandoMetaClass instance for the given class optionally placing the MetaClass
	 * in the MetaClassRegistry automatically
	 *
	 * @param theClass The class that the MetaClass applies to
	 * @param register True if the MetaClass should be registered inside the MetaClassRegistry. This defaults to true and ExpandoMetaClass will effect all instances if changed
     * @param allowChangesAfterInit Should the meta class be modifiable after initialization. Default is false.
	 */
	public ExpandoMetaClass(Class theClass, boolean register, boolean allowChangesAfterInit) {
		this(theClass);
	    this.inRegistry = register;
        this.allowChangesAfterInit = allowChangesAfterInit;
    }

    public MetaMethod findMixinMethod(String methodName, Class[] arguments) {
        for (MixinInMetaClass mixin : mixinClasses) {
            final CachedClass mixinClass = mixin.getMixinClass();
            MetaClass metaClass = mixinClass.classInfo.getMetaClassForClass();
            if (metaClass == null) {
                metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(mixinClass.getTheClass());
            }

            final MetaMethod metaMethod = metaClass.pickMethod(methodName, arguments);
            if (metaMethod != null) {
              MetaMethod method = new MixinInstanceMetaMethod(metaMethod, mixin);
              registerInstanceMethod(method);
              return method;
            }
        }
        return null;
    }

    protected void onInvokeMethodFoundInHierarchy(MetaMethod method) {
        this.invokeMethodMethod = method;
    }

    protected void onSuperMethodFoundInHierarchy(MetaMethod method) {
        addSuperMethodIfNotOverriden(method);
    }

    protected void onSuperPropertyFoundInHierarchy(MetaBeanProperty property) {
        addMetaBeanProperty(property);
    }

    protected void onSetPropertyFoundInHierarchy(MetaMethod method) {
        this.setPropertyMethod = method;
    }

    protected void onGetPropertyFoundInHierarchy(MetaMethod method) {
        this.getPropertyMethod = method;
    }

    public synchronized boolean isModified() {
        return this.modified;
    }

    public void registerSubclassInstanceMethod(String name, Class klazz, Closure closure) {
        final List<ClosureMetaMethod> list = ClosureMetaMethod.createMethodList(name, klazz, closure);
        for (ClosureMetaMethod metaMethod : list) {
            registerSubclassInstanceMethod(metaMethod);
        }
    }

    public void registerSubclassInstanceMethod(MetaMethod metaMethod) {
        final String name = metaMethod.getName();
        Object methodOrList = expandoSubclassMethods.get(name);
        if (methodOrList == null) {
            expandoSubclassMethods.put(name, metaMethod);
        }
        else {
            if (methodOrList instanceof MetaMethod) {
                FastArray arr = new FastArray(2);
                arr.add(methodOrList);
                arr.add(metaMethod);
                expandoSubclassMethods.put(name, arr);
            }
            else {
                ((FastArray)methodOrList).add(metaMethod);
            }
        }
    }

    public void addMixinClass(MixinInMetaClass mixin) {
        mixinClasses.add (mixin);
    }

    public Object castToMixedType(Object obj, Class type) {
        for (MixinInMetaClass mixin : mixinClasses) {
           if (type.isAssignableFrom(mixin.getMixinClass().getTheClass()))
             return mixin.getMixinInstance(obj);
        }
        return null;
    }

    /**
     * For simulating closures in Java
     */
    private interface Callable {
		void call();
	}

    /**
     * Call to enable global use of global use of ExpandoMetaClass within the registry. This has the advantage that
     * inheritance will function correctly, but has a higher memory usage on the JVM than normal Groovy
     */
    public static void enableGlobally() {
        ExpandoMetaClassCreationHandle.enable();
    }

    /**
     * Call to disable the global use of ExpandoMetaClass
     */
    public static void disableGlobally() {
        ExpandoMetaClassCreationHandle.disable();
    }



    /* (non-Javadoc)
	 * @see groovy.lang.MetaClassImpl#initialize()
	 */
	public synchronized void initialize() {
        if (!isInitialized()) {
            super.initialize();
            setInitialized(true);
            this.initCalled = true;
        }
    }


	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassImpl#isInitialized()
	 */
	protected synchronized boolean isInitialized() {
		return this.initialized;
	}

    protected synchronized void setInitialized(boolean b) {
        this.initialized = b;
    }


    private void addSuperMethodIfNotOverriden(final MetaMethod metaMethodFromSuper) {
		performOperationOnMetaClass(new Callable() {
			public void call() {

                final MetaMethodIndex.Header header = metaMethodIndex.getHeader(theClass);
                final MetaMethodIndex.Entry methods = metaMethodIndex.getOrPutMethods(metaMethodFromSuper.getName(), header);

                MetaMethod existing = null;
				try {
					existing = pickMethod(metaMethodFromSuper.getName(), metaMethodFromSuper.getNativeParameterTypes());}
				catch ( GroovyRuntimeException e) {
					// ignore, this happens with overlapping method definitions
				}

				if(existing == null) {
                        addMethodWithKey(metaMethodFromSuper);
				}
				else {
                    boolean isGroovyMethod = getMetaMethods().contains(existing);


                    if(isGroovyMethod) {
                        addMethodWithKey(metaMethodFromSuper);
                    }
                    else if(inheritedMetaMethods.contains(existing)) {
                        inheritedMetaMethods.remove(existing);

                        addMethodWithKey(metaMethodFromSuper);
                    }
                }

			}

			private void addMethodWithKey(final MetaMethod metaMethodFromSuper) {
                inheritedMetaMethods.add(metaMethodFromSuper);
                if(metaMethodFromSuper instanceof ClosureMetaMethod) {
                    ClosureMetaMethod closureMethod = (ClosureMetaMethod)metaMethodFromSuper;
                    Closure cloned = (Closure)closureMethod.getClosure().clone();
                    String name = metaMethodFromSuper.getName();
                    final Class declaringClass = metaMethodFromSuper.getDeclaringClass().getTheClass();
                    ClosureMetaMethod localMethod = ClosureMetaMethod.copy(closureMethod);
                    addMetaMethod(localMethod);

                    MethodKey key = new DefaultCachedMethodKey(declaringClass, name, localMethod.getParameterTypes(),false );
//                    cacheInstanceMethod(key, localMethod);

                    checkIfGroovyObjectMethod(localMethod);
                    expandoMethods.put(key,localMethod);

                }
            }
		});
	}



	/**
	 * Instances of this class are returned when using the << left shift operator.
	 *
	 * Example:
	 *
	 * metaClass.myMethod << { String args -> }
	 *
	 * This allows callbacks to the ExpandoMetaClass for registering appending methods
	 *
	 * @author Graeme Rocher
	 *
	 */
	protected class ExpandoMetaProperty extends GroovyObjectSupport {

		protected String propertyName;
		protected boolean isStatic;

        protected ExpandoMetaProperty(String name) {
			this(name, false);
		}

		protected ExpandoMetaProperty(String name, boolean isStatic) {
			this.propertyName = name;
			this.isStatic = isStatic;
		}

        public String getPropertyName() { return this.propertyName; }
        public boolean isStatic() { return this.isStatic; }

        public Object leftShift(Object arg) {
			registerIfClosure(arg, false);
			return this;
		}

		private void registerIfClosure(Object arg, boolean replace) {
			if(arg instanceof Closure) {
                Closure callable = (Closure)arg;
                final List<ClosureMetaMethod> list = ClosureMetaMethod.createMethodList(propertyName, theClass, callable);
                for (MetaMethod method : list) {
                    Class[] paramTypes = method.getNativeParameterTypes();
                    if(!this.isStatic) {
                        Method foundMethod = checkIfMethodExists(theClass, propertyName, paramTypes, false);

                        if(foundMethod != null && !replace) throw new GroovyRuntimeException("Cannot add new method ["+propertyName+"] for arguments ["+DefaultGroovyMethods.inspect(paramTypes)+"]. It already exists!");

                        registerInstanceMethod(method);
                    }
                    else {
                        Method foundMethod = checkIfMethodExists(theClass, propertyName, paramTypes, true);
                        if(foundMethod != null && !replace) throw new GroovyRuntimeException("Cannot add new static method ["+propertyName+"] for arguments ["+DefaultGroovyMethods.inspect(paramTypes)+"]. It already exists!");

                        registerStaticMethod(propertyName, callable);
                    }
                }
			}
		}

		private Method checkIfMethodExists(Class methodClass, String methodName, Class[] paramTypes, boolean staticMethod) {
			Method foundMethod = null;
			Method[] methods = methodClass.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if(methods[i].getName().equals(methodName) && Modifier.isStatic(methods[i].getModifiers()) == staticMethod) {
					if(MetaClassHelper.parametersAreCompatible( paramTypes, methods[i].getParameterTypes() )) {
						foundMethod = methods[i];
						break;
					}
				}
			}
			return foundMethod;
		}

		/* (non-Javadoc)
		 * @see groovy.lang.GroovyObjectSupport#getProperty(java.lang.String)
		 */
		public Object getProperty(String property) {
			this.propertyName = property;
			return this;
		}
		/* (non-Javadoc)
		 * @see groovy.lang.GroovyObjectSupport#setProperty(java.lang.String, java.lang.Object)
		 */
		public void setProperty(String property, Object newValue) {
			this.propertyName = property;
			registerIfClosure(newValue, true);
		}
	}


	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassImpl#invokeConstructor(java.lang.Object[])
	 */
	public Object invokeConstructor(Object[] arguments) {

		// TODO This is the only area where this MetaClass needs to do some interception because Groovy's current
		// MetaClass uses hard coded references to the java.lang.reflect.Constructor class so you can't simply
		// inject Constructor like you can do properties, methods and fields. When Groovy's MetaClassImpl is
		// refactored we can fix this
		Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);
		MetaMethod method = pickMethod(GROOVY_CONSTRUCTOR, argClasses);
		if(method!=null && method.getParameterTypes().length == arguments.length) {
			return method.invoke(theClass, arguments);
		}
		return super.invokeConstructor(arguments);
	}

	/**
	 * Handles the ability to use the left shift operator to append new constructors
	 *
	 * @author Graeme Rocher
	 *
	 */
	protected class ExpandoMetaConstructor extends GroovyObjectSupport {
		public Object leftShift(Closure c) {
			if(c != null) {
                final List<ClosureMetaMethod> list = ClosureMetaMethod.createMethodList(GROOVY_CONSTRUCTOR, theClass, c);
                for (MetaMethod method : list) {
                    Class[] paramTypes = method.getNativeParameterTypes();
                    Constructor ctor = retrieveConstructor(paramTypes);
                    if(ctor != null) throw new GroovyRuntimeException("Cannot add new constructor for arguments ["+DefaultGroovyMethods.inspect(paramTypes)+"]. It already exists!");

                    registerInstanceMethod(method);
                }
			}

			return this;
		}
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#getMetaClass()
	 */
	public MetaClass getMetaClass() {
		return myMetaClass;
	}



	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
	 */
	public Object getProperty(String property) {
		if(isValidExpandoProperty(property)) {
			if(property.equals(STATIC_QUALIFIER)) {
				return new ExpandoMetaProperty(property, true);
			}
			else if(property.equals(CONSTRUCTOR)) {
				return new ExpandoMetaConstructor();
			}
			else {
                if (myMetaClass.hasProperty(this, property) == null)
                  return new ExpandoMetaProperty(property);
                else
                  return myMetaClass.getProperty(this, property);
            }
		}
		else {
			return myMetaClass.getProperty(this, property);
		}
	}

	public static boolean isValidExpandoProperty(String property) {
        return !(property.equals(META_CLASS) || property.equals(CLASS) || property.equals(META_METHODS) || property.equals(METHODS) || property.equals(PROPERTIES));
    }

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
	 */
	public Object invokeMethod(String name, Object args) {
        final Object[] argsArr = args instanceof Object[] ? (Object[]) args : new Object[]{args};
        MetaMethod metaMethod = myMetaClass.getMetaMethod(name, argsArr);
        if (metaMethod != null)
          return metaMethod.invoke(this, argsArr);

        if (argsArr.length == 2 && argsArr[0] instanceof Class && argsArr[1] instanceof Closure) {
            if (argsArr[0] == theClass)
              registerInstanceMethod(name, (Closure) argsArr[1]);
            else {
              registerSubclassInstanceMethod(name, (Class) argsArr[0], (Closure) argsArr[1]);
            }
            return null;
        }

        if (argsArr.length == 1 && argsArr[0] instanceof Closure) {
           registerInstanceMethod(name, (Closure) argsArr[0]);
           return null;
        }

        throw new MissingMethodException(name, getClass(), argsArr);
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#setMetaClass(groovy.lang.MetaClass)
	 */
	public void setMetaClass(MetaClass metaClass) {
		this.myMetaClass = metaClass;
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String property, Object newValue) {
		if(newValue instanceof Closure) {
			if(property.equals(CONSTRUCTOR)) {
				property = GROOVY_CONSTRUCTOR;
			}
            Closure callable = (Closure)newValue;
            final List<ClosureMetaMethod> list = ClosureMetaMethod.createMethodList(property, theClass, callable);
            for (MetaMethod method : list) {
                // here we don't care if the method exists or not we assume the
                // developer is responsible and wants to override methods where necessary
                registerInstanceMethod(method);
            }
		}
		else {
			registerBeanProperty(property, newValue);
		}
	}

    public ExpandoMetaClass define (Closure closure) {
        final DefiningClosure definer = new DefiningClosure();
        Object delegate = closure.getDelegate();
        closure.setDelegate(definer);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call(null);
        closure.setDelegate(delegate);
        definer.definition = false;
        return this;
    }
    
    protected synchronized void performOperationOnMetaClass(Callable c) {
            try {
                if(allowChangesAfterInit) {
                    setInitialized(false);
                }

                c.call();
            }
            finally {
                if(initCalled){
                    setInitialized(true);
                }
            }
	}

	/**
	 * Registers a new bean property
	 *
	 * @param property The property name
	 * @param newValue The properties initial value
	 */
	public void registerBeanProperty(final String property, final Object newValue) {
			performOperationOnMetaClass(new Callable() {
				public void call() {
					Class type = newValue == null ? Object.class : newValue.getClass();

					MetaBeanProperty mbp = newValue instanceof MetaBeanProperty ? (MetaBeanProperty)newValue : new ThreadManagedMetaBeanProperty(theClass,property,type,newValue);

                    final MetaMethod getter = mbp.getGetter();
                    final MethodKey getterKey = new DefaultCachedMethodKey(theClass,getter.getName(), CachedClass.EMPTY_ARRAY,false );
                    final MetaMethod setter = mbp.getSetter();
                    final MethodKey setterKey = new DefaultCachedMethodKey(theClass,setter.getName(), setter.getParameterTypes(),false );
                    addMetaMethod(getter);
                    addMetaMethod(setter);

                    expandoMethods.put(setterKey,setter);
                    expandoMethods.put(getterKey,getter);
                    expandoProperties.put(mbp.getName(),mbp);

					addMetaBeanProperty(mbp);
                    performRegistryCallbacks();
                }

			});
	}

	/**
	 * Registers a new instance method for the given method name and closure on this MetaClass
	 *
	 * @param metaMethod
	 */
	public void registerInstanceMethod(final MetaMethod metaMethod) {
			final boolean inited = this.initCalled;
			performOperationOnMetaClass(new Callable() {
				public void call() {
                    String methodName = metaMethod.getName();
                    checkIfGroovyObjectMethod(metaMethod);
                    MethodKey key = new DefaultCachedMethodKey(theClass,methodName, metaMethod.getParameterTypes(),false );


					addMetaMethod(metaMethod);
                    dropMethodCache(methodName);
                    expandoMethods.put(key,metaMethod);

					if(inited && isGetter(methodName, metaMethod.getParameterTypes())) {
						String propertyName = getPropertyForGetter(methodName);
						registerBeanPropertyForMethod(metaMethod, propertyName, true, false);

					}
					else if(inited && isSetter(methodName, metaMethod.getParameterTypes())) {
						String propertyName = getPropertyForSetter(methodName);
						registerBeanPropertyForMethod(metaMethod, propertyName, false, false);
					}
					performRegistryCallbacks();
				}

			});
	}

    public void registerInstanceMethod(String name, Closure closure) {
        final List<ClosureMetaMethod> list = ClosureMetaMethod.createMethodList(name, theClass, closure);
        for (MetaMethod method : list) {
            registerInstanceMethod(method);
        }
    }

    /**
     * Overrides the behaviour of parent getMethods() method to make MetaClass aware of added Expando methods
     *
     * @see MetaObjectProtocol#getMethods()
     *
     * @return A list of MetaMethods
     */
    public List getMethods() {
        List methodList =  new ArrayList();
        methodList.addAll(this.expandoMethods.values());
        methodList.addAll(super.getMethods());
        return methodList;
    }

    public List getProperties() {
        List propertyList = new ArrayList();
        propertyList.addAll(super.getProperties());
        return propertyList;
    }


    private void performRegistryCallbacks() {
		MetaClassRegistry registry =  GroovySystem.getMetaClassRegistry();
		if(!modified) {
			modified = true;
            // Implementation note: By default Groovy uses soft references to store MetaClass
            // this insures the registry doesn't grow and get out of hand. By doing this we're
            // saying this this EMC will be a hard reference in the registry. As we're only
            // going have a small number of classes that have modified EMC this is ok
            if(inRegistry) {
                MetaClass currMetaClass = registry.getMetaClass(theClass);
                if(!(currMetaClass instanceof ExpandoMetaClass) && currMetaClass instanceof AdaptingMetaClass) {
                    ((AdaptingMetaClass)currMetaClass).setAdaptee(this);
                } else {
                    registry.setMetaClass(theClass, this);
                }
            }

		}
		// Implementation note: EMC handles most cases by itself except for the case where yuou
		// want to call a dynamically injected method registered with a parent on a child class
		// For this to work the MetaClassRegistry needs to have an ExpandoMetaClassCreationHandle
		// What this does is ensure that EVERY class created in the registry uses an EMC
		// Then when an EMC changes it reports back to the EMCCreationHandle which will
		// tell child classes of this class to re-inherit their methods
		if(registry.getMetaClassCreationHandler() instanceof ExpandoMetaClassCreationHandle) {
			ExpandoMetaClassCreationHandle creationHandler = (ExpandoMetaClassCreationHandle)registry.getMetaClassCreationHandler();
			if(!creationHandler.hasModifiedMetaClass(this))
				creationHandler.registerModifiedMetaClass(this);

        }
	}


	private void registerBeanPropertyForMethod(MetaMethod metaMethod, String propertyName, boolean getter, boolean isStatic) {
        Map propertyCache = isStatic ? staticBeanPropertyCache : beanPropertyCache;
        MetaBeanProperty beanProperty = (MetaBeanProperty)propertyCache.get(propertyName);
        if(beanProperty == null) {
            if(getter)
                beanProperty = new MetaBeanProperty(propertyName,Object.class,metaMethod,null);
            else
                beanProperty = new MetaBeanProperty(propertyName,Object.class,null,metaMethod);

            propertyCache.put(propertyName, beanProperty);
        }
        else {
            if(getter) {
                MetaMethod setterMethod = beanProperty.getSetter();
                Class type = setterMethod != null ? setterMethod.getParameterTypes()[0].getTheClass() : Object.class;
                beanProperty = new MetaBeanProperty(propertyName,type,metaMethod,setterMethod);
                propertyCache.put(propertyName, beanProperty);
            }else {
                MetaMethod getterMethod = beanProperty.getGetter();
                beanProperty = new MetaBeanProperty(propertyName, metaMethod.getParameterTypes()[0].getTheClass(),getterMethod,metaMethod);
                propertyCache .put(propertyName, beanProperty);
            }
        }
         expandoProperties.put(beanProperty.getName(),beanProperty);
        addMetaBeanProperty(beanProperty);
	}

	/**
	 * Registers a new static method for the given method name and closure on this MetaClass
	 *
	 * @param name The method name
	 * @param callable The callable Closure
	 */
	protected void registerStaticMethod(final String name, final Closure callable) {
		performOperationOnMetaClass(new Callable() {
			public void call() {
                String methodName;
                if(name.equals(METHOD_MISSING))
                    methodName = STATIC_METHOD_MISSING;
                else if(name.equals(PROPERTY_MISSING))
                    methodName = STATIC_PROPERTY_MISSING;
                else
                    methodName = name;

                ClosureStaticMetaMethod metaMethod = new ClosureStaticMetaMethod(methodName, theClass,callable);
                if(methodName.equals(INVOKE_METHOD_METHOD) && callable.getParameterTypes().length == 2) {
                    invokeStaticMethodMethod = metaMethod;
                }
                else {
                    if(methodName.equals(METHOD_MISSING)) {
                        methodName = STATIC_METHOD_MISSING;
                    }
                    MethodKey key = new DefaultCachedMethodKey(theClass,methodName, metaMethod.getParameterTypes(), false );

                    addMetaMethod(metaMethod);
                    dropStaticMethodCache (methodName);
//                    cacheStaticMethod(key,metaMethod);

                    if(isGetter(methodName, metaMethod.getParameterTypes())) {
                        String propertyName = getPropertyForGetter(methodName);
                        registerBeanPropertyForMethod(metaMethod, propertyName, true, true);

                    }
                    else if(isSetter(methodName, metaMethod.getParameterTypes())) {
                        String propertyName = getPropertyForSetter(methodName);
                        registerBeanPropertyForMethod(metaMethod, propertyName, false, true);
                    }
                    performRegistryCallbacks();
                    expandoMethods.put(key,metaMethod);
                }
			}

		});
	}

    protected Object getSubclassMetaMethods(String methodName) {
        if (!isModified())
            return null;

        return expandoSubclassMethods.get(methodName);
    }

    /**
	 * @return The Java class enhanced by this MetaClass
	 */
	public Class getJavaClass() {
		return theClass;
	}

	/**
	 * Called from ExpandoMetaClassCreationHandle in the registry if it exists to setup inheritance
	 * handling
	 *
	 * @param modifiedSuperExpandos A list of modified super ExpandoMetaClass
	 */
	public void refreshInheritedMethods(Set modifiedSuperExpandos) {
		for (Iterator i = modifiedSuperExpandos.iterator(); i.hasNext();) {

			ExpandoMetaClass superExpando = (ExpandoMetaClass) i.next();
            if(superExpando != this) {
                List metaMethods = superExpando.getExpandoMethods();
                for (Iterator j = metaMethods.iterator(); j.hasNext();) {
                    MetaMethod metaMethod = (MetaMethod) j.next();
                    if(metaMethod.isStatic()) {
                        if (superExpando.getTheClass() != getTheClass())
                          continue; // don't inherit static methods except our own

                        registerStaticMethod(metaMethod.getName(), (Closure) ((ClosureStaticMetaMethod)metaMethod).getClosure().clone());
                    }
                    else
                      addSuperMethodIfNotOverriden(metaMethod);
                }
                Collection metaProperties = superExpando.getExpandoProperties();
                for (Iterator j = metaProperties.iterator(); j.hasNext();) {
                    MetaBeanProperty property = (MetaBeanProperty) j.next();
                    expandoProperties.put(property.getName(),property);
                    addMetaBeanProperty(property);
                }
            }

        }
	}


	/**
	 * Returns a list of expando MetaMethod instances added to this ExpandoMetaClass
	 *
	 * @return the expandoMethods
	 */
	public List getExpandoMethods() {
       return Collections.unmodifiableList(DefaultGroovyMethods.toList(expandoMethods.values()));
    }


	/**
	 * Returns a list of MetaBeanProperty instances added to this ExpandoMetaClass
	 *
	 * @return the expandoProperties
	 */
	public Collection getExpandoProperties() {
        return Collections.unmodifiableCollection(expandoProperties.values());
    }

    /**
     * Overrides default implementation just in case invokeMethod has been overriden by ExpandoMetaClass
     *
     * @see groovy.lang.MetaClassImpl#invokeMethod(Class, Object, String, Object[], boolean, boolean)
     */
    public Object invokeMethod(Class sender, Object object, String methodName, Object[] originalArguments, boolean isCallToSuper, boolean fromInsideClass) {
        if(invokeMethodMethod!=null) {
            return invokeMethodMethod.invoke(object, new Object[]{methodName, originalArguments});
        }
        return super.invokeMethod(sender, object, methodName, originalArguments, isCallToSuper, fromInsideClass);
    }

    /**
     * Overrides default implementation just in case a static invoke method has been set on ExpandoMetaClass
     * @see MetaClassImpl#invokeStaticMethod(Object, String, Object[])
     */
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        if(invokeStaticMethodMethod != null) {
            return invokeStaticMethodMethod.invoke(object, new Object[]{methodName, arguments});
        }
        return super.invokeStaticMethod(object, methodName, arguments);
    }

    /**
     * Overrides default implementation just in case getProperty method has been overriden by ExpandoMetaClass
     *
     * @see MetaClassImpl#getProperty(Class, Object, String, boolean, boolean)
     */
    public Object getProperty(Class sender, Object object, String name, boolean useSuper, boolean fromInsideClass) {
        if(hasOverrideGetProperty(name) && getJavaClass().isInstance(object)) {
            return getPropertyMethod.invoke(object, new Object[]{name});
        }

        if ("mixedIn".equals(name)) {
            return new MixedInAccessor(object, mixinClasses);
        }

        return super.getProperty(sender, object, name, useSuper, fromInsideClass);
    }


    /**
     * Overrides default implementation just in case getProperty method has been overriden by ExpandoMetaClass
     *
     * @see MetaClassImpl#getProperty(Object, String)
     */
    public Object getProperty(Object object, String name) {
        if(hasOverrideGetProperty(name) && getJavaClass().isInstance(object)) {
            return getPropertyMethod.invoke(object, new Object[]{name});
        }
        return super.getProperty(object,name);
    }

    private boolean hasOverrideGetProperty(String name) {
        return getPropertyMethod != null && !name.equals(META_CLASS_PROPERTY)&& !name.equals(CLASS_PROPERTY);
    }

    /**
     * Overrides default implementation just in case setProperty method has been overriden by ExpandoMetaClass
     *
     * @see MetaClassImpl#setProperty(Class, Object, String, Object, boolean, boolean)
     */

    public void setProperty(Class sender, Object object, String name, Object newValue, boolean useSuper, boolean fromInsideClass) {
        if(setPropertyMethod!=null  && !name.equals(META_CLASS_PROPERTY) && getJavaClass().isInstance(object)) {
            setPropertyMethod.invoke(object, new Object[]{name, newValue});
            return;
        }
        super.setProperty(sender, object, name, newValue, useSuper, fromInsideClass);
    }

    /**
     * Looks up an existing MetaProperty by name
     *
     * @param name The name of the MetaProperty
     * @return The MetaProperty or null if it doesn't exist
     */
    public MetaProperty getMetaProperty(String name) {
        MetaProperty mp = (MetaProperty) this.expandoProperties.get(name);
        if (mp != null) return mp;
        return super.getMetaProperty(name);
    }

    /**
     * Returns true if the MetaClass has the given property
     *
     * @param name The name of the MetaProperty
     * @return True it exists as a MetaProperty
     */
    public boolean hasMetaProperty(String name) {
        return getMetaProperty(name) != null;
    }

    /**
     * Checks whether a MetaMethod for the given name and arguments exists
     *
     * @param name The name of the MetaMethod
     * @param args The arguments to the meta method
     * @return True if the method exists otherwise null
     */
    public boolean hasMetaMethod(String name, Class[] args) {
        return super.pickMethod(name, args) != null;
    }


    /**
     * Returns true if the name of the method specified and the number of arguments make it a javabean property
     *
     * @param name True if its a Javabean property
     * @param args The arguments
     * @return True if it is a javabean property method
     */
    private boolean isGetter(String name, CachedClass[] args) {
        if(name == null || name.length() == 0 || args == null)return false;
        if(args.length != 0)return false;

        if(name.startsWith("get")) {
            name = name.substring(3);
            if(name.length() > 0 && Character.isUpperCase(name.charAt(0))) return true;
        }
        else if(name.startsWith("is")) {
            name = name.substring(2);
            if(name.length() > 0 && Character.isUpperCase(name.charAt(0))) return true;
        }
        return false;
    }

    /**
     * Returns a property name equivalent for the given getter name or null if it is not a getter
     *
     * @param getterName The getter name
     * @return The property name equivalent
     */
    private String getPropertyForGetter(String getterName) {
        if(getterName == null || getterName.length() == 0)return null;

        if(getterName.startsWith("get")) {
            String prop = getterName.substring(3);
            return convertPropertyName(prop);
        }
        else if(getterName.startsWith("is")) {
            String prop = getterName.substring(2);
            return convertPropertyName(prop);
        }
        return null;
    }

	private String convertPropertyName(String prop) {
		if(Character.isUpperCase(prop.charAt(0)) && (prop.length() > 1 && Character.isUpperCase(prop.charAt(1)))) {
			return prop;
		}
		else if(Character.isDigit(prop.charAt(0))) {
			return prop;
		}
		else {
			return Character.toLowerCase(prop.charAt(0)) + (prop.length() > 1 ? prop.substring(1) : "");
		}
	}

    /**
     * Returns a property name equivalent for the given setter name or null if it is not a getter
     *
     * @param setterName The setter name
     * @return The property name equivalent
     */
    public String getPropertyForSetter(String setterName) {
        if(setterName == null || setterName.length() == 0)return null;

        if(setterName.startsWith("set")) {
            String prop = setterName.substring(3);
            return convertPropertyName(prop);
        }
        return null;
    }

    public boolean isSetter(String name, CachedClass[] args) {
        if(name == null || name.length() == 0 || args == null)return false;

        if(name.startsWith("set")) {
            if(args.length != 1) return false;
            name = name.substring(3);
            if(name.length() > 0 && Character.isUpperCase(name.charAt(0))) return true;
        }

        return false;
    }

    public CallSite createPojoCallSite(CallSite site, Object receiver, Object[] args) {
        if (invokeMethodMethod != null)
          return new PojoMetaClassSite(site, this);

        return super.createPojoCallSite(site, receiver, args);
    }

    public CallSite createStaticSite(CallSite site, Object[] args) {
        if(invokeStaticMethodMethod != null)
            return new StaticMetaClassSite(site, this);

        return super.createStaticSite(site, args);
    }

    public CallSite createPogoCallSite(CallSite site, Object[] args) {
        if (invokeMethodMethod != null)
            return new PogoMetaClassSite(site, this);
        return super.createPogoCallSite(site, args);
    }

    public CallSite createPogoCallCurrentSite(CallSite site, Class sender, String name, Object[] args) {
        if (invokeMethodMethod != null)
            return new PogoMetaClassSite(site, this);
        return super.createPogoCallCurrentSite(site, sender, args);
    }

    public CallSite createConstructorSite(CallSite site, Object[] args) {
        Class[] params = MetaClassHelper.convertToTypeArray(args);
        MetaMethod method = pickMethod(GROOVY_CONSTRUCTOR, params);
        if(method!=null && method.getParameterTypes().length == args.length) {
           return new ConstructorMetaMethodSite(site, this, method, params);
        }

        return super.createConstructorSite(site, args);
    }

    private class SubClassDefiningClosure extends GroovyObjectSupport {
        private final Class klazz;

        public SubClassDefiningClosure(Class klazz) {
            this.klazz = klazz;
        }

        public Object invokeMethod(String name, Object obj) {
            if (obj instanceof Object[]) {
                Object args [] = (Object[]) obj;
                if (args.length == 1 && args[0] instanceof Closure) {
                    registerSubclassInstanceMethod(name, klazz, (Closure)args[0]);
                    return null;
                }
            }

            throw new MissingMethodException(name, getClass(), new Object[] {obj});
        }
    }

    private class DefiningClosure extends GroovyObjectSupport {
        boolean definition = true;

        public void mixin (Class category) {
            mixin(Collections.singletonList(category));
        }

        public void mixin (List categories) {
            DefaultGroovyMethods.mixin(ExpandoMetaClass.this, categories);
        }

        public void mixin (Class [] categories) {
            DefaultGroovyMethods.mixin(ExpandoMetaClass.this, categories);
        }

        public void define (Class subClass, Closure closure) {
            final SubClassDefiningClosure definer = new SubClassDefiningClosure(subClass);
            closure.setDelegate(definer);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            closure.call(null);
        }

        public Object invokeMethod(String name, Object obj) {
            try {
                return getMetaClass().invokeMethod(this, name, obj);
            }
            catch (MissingMethodException mme) {
                if (obj instanceof Object[]) {
                    if (STATIC_QUALIFIER.equals(name)) {
                        final StaticDefiningClosure staticDef = new StaticDefiningClosure();
                        Closure c = (Closure)((Object[])obj)[0];
                        c.setDelegate(staticDef);
                        c.setResolveStrategy(Closure.DELEGATE_ONLY);
                        c.call(null);
                        return null;
                    }
                    Object args [] = (Object[]) obj;
                    if (args.length == 1 && args[0] instanceof Closure) {
                        registerInstanceMethod(name, (Closure)args[0]);
                    }
                    else
                        if (args.length == 2 && args[0] instanceof Class && args [1] instanceof Closure)
                          registerSubclassInstanceMethod(name, (Class)args[0], (Closure)args[1]);
                        else
                          ExpandoMetaClass.this.setProperty(name, ((Object[])obj)[0]);

                    return null;
                }

                throw mme;
            }
        }

        public void setProperty(String property, Object newValue) {
            ExpandoMetaClass.this.setProperty(property, newValue);
        }

        public Object getProperty(String property) {
            if (STATIC_QUALIFIER.equals(property))
              return new StaticDefiningClosure();

            if (definition)
              return new ExpandoMetaProperty(property);
            else
              throw new MissingPropertyException(property, getClass());
        }
    }

    private class StaticDefiningClosure extends ExpandoMetaProperty {
        protected StaticDefiningClosure() {
            super(STATIC_QUALIFIER, true);
        }

        public Object invokeMethod(String name, Object obj) {
            if (obj instanceof Object[]) {
                final Object[] args = (Object[]) obj;
                if (args.length == 1 && args[0] instanceof Closure) {
                    registerStaticMethod(name, (Closure)args[0]);
                    return null;
                }
            }

            throw new MissingMethodException(name, getClass(), obj instanceof Object[] ? (Object[])obj : new Object[] {obj} );
        }
    }

    private static class MixedInAccessor {
        private final Object object;
        private final LinkedHashSet<MixinInMetaClass> mixinClasses;

        public MixedInAccessor(Object object, LinkedHashSet<MixinInMetaClass> mixinClasses) {
            this.object = object;
            this.mixinClasses = mixinClasses;
        }

        public Object getAt (Class key) {
            if (key.isAssignableFrom(object.getClass())) {
                return new GroovyObjectSupport() {
                    {
                        final MetaClass ownMetaClass = InvokerHelper.getMetaClass(object.getClass());
                        setMetaClass(new OwnedMetaClass(ownMetaClass){
                            protected Object getOwner() {
                                return object;
                            }

                            protected MetaClass getOwnerMetaClass(Object owner) {
                                return getAdaptee();
                            }
                        });
                    }
                };
            }

            for (final MixinInMetaClass mixin : mixinClasses) {
                if (key.isAssignableFrom(mixin.getMixinClass().getTheClass())) {
                    return new GroovyObjectSupport() {
                        {
                            final Object mixedInInstance = mixin.getMixinInstance(object);
                            setMetaClass(new OwnedMetaClass(InvokerHelper.getMetaClass(mixedInInstance)){
                                protected Object getOwner() {
                                    return mixedInInstance;
                                }

                                protected MetaClass getOwnerMetaClass(Object owner) {
                                    return ((MixedInMetaClass)getAdaptee()).getAdaptee();
                                }
                            });
                        }
                    };
                }
            }

            throw new RuntimeException("Class " + key + " isn't mixed in " + object.getClass());
        }

        public void putAt (Class key, Object value) {
            for (MixinInMetaClass mixin : mixinClasses)
              if (mixin.getMixinClass().getTheClass() == key) {
                mixin.setMixinInstance(object, value);
                return;
              }

            throw new RuntimeException("Class " + key + " isn't mixed in " + object.getClass());
        }
    }
}
