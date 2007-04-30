/*
 * Copyright 2004-2006 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang;

import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod;
import org.codehaus.groovy.runtime.metaclass.ClosureStaticMetaMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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
 * // defines a new contructor
 * metaClass.ctor << { String arg -> }
 *
 * // defines or replaces a constructor
 * metaClass.ctor = { String arg -> }
 *
 * // defines a new property with an initial value of "blah"
 * metaClass.getMyProperty = {-> "blah" }
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
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class  ExpandoMetaClass extends MetaClassImpl implements GroovyObject {

	private static final String META_CLASS = "metaClass";
	private static final String CLASS = "class";
	private static final String META_METHODS = "metaMethods";
	private static final String METHODS = "methods";
	private static final String PROPERTIES = "properties";
	public static final String STATIC_QUALIFIER = "static";
	private static final Class[] ZERO_ARGUMENTS = new Class[0];
	private static final String CONSTRUCTOR = "constructor";
	private static final String GROOVY_CONSTRUCTOR = "<init>";
	private static final Map classInheritanceMapping = Collections.synchronizedMap(new HashMap());

	private MetaClass myMetaClass;
	private boolean allowChangesAfterInit = false;
	private boolean initialized;
	private boolean initCalled = false;
	private boolean modified = false;
	private Map beanPropertyCache = new HashMap();
	private boolean inRegistry;
	private boolean hasCreationHandle = false;
	private Set inheritedMetaMethods = new HashSet();
	private List expandoMethods = new LinkedList();
	private List expandoProperties = new LinkedList();

    interface Callable {
		void call();
	}

	/**
	 * @param allowChangesAfterInit the allowChangesAfterInit to set
	 */
	public void setAllowChangesAfterInit(boolean allowChangesAfterInit) {
		this.allowChangesAfterInit = allowChangesAfterInit;
	}


	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassImpl#initialize()
	 */
	public synchronized void initialize() {
		inheritSelfTrackedExpandoMethods();
		super.initialize();
		this.initialized = true;
		this.initCalled = true;
	}


	/* (non-Javadoc)
	 * @see groovy.lang.MetaClassImpl#isInitialized()
	 */
	protected boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * If their is no ExpandoMetaClassCreationHandle the EMC will attempt to track inheritance
	 * methods itself. In this case inherited methods will only work if both the parent and the child
	 * have an EMC. The best way to get method inheritance working properly is to register a
	 * ExpandoMetaClassCreationHandle, which may have a performance drawback (Disclaimer: not measured)
	 */
	private void inheritSelfTrackedExpandoMethods() {
		List superClasses = getSuperClasses();
		for (Iterator i = superClasses.iterator(); i.hasNext();) {
			Class c = (Class) i.next();
			Map methodMap = (Map)classInheritanceMapping.get(c);
			if(methodMap!=null) {
				for (Iterator j = methodMap.values().iterator(); j.hasNext();) {
					List methods = (List) j.next();
					for (Iterator k = methods.iterator(); k.hasNext();) {
						MetaMethod metaMethodFromSuper = (MetaMethod) k.next();
						if(!metaMethodFromSuper.isStatic()) {
							addSuperMethodIfNotOverriden(metaMethodFromSuper);
						}
					}
				}
			}
		}
	}



	private void addSuperMethodIfNotOverriden(final MetaMethod metaMethodFromSuper) {
		performOperationOnMetaClass(new Callable() {
			public void call() {

				MetaMethod existing = pickMethod(metaMethodFromSuper.getName(), metaMethodFromSuper.getParameterTypes());

				if(existing == null) {
					inheritedMetaMethods.add(metaMethodFromSuper);
					addMethodWithKey(metaMethodFromSuper);
				}
				else {
					if(inheritedMetaMethods.contains(existing)) {
						addMethodWithKey(metaMethodFromSuper);
					}
				}

			}

			private void addMethodWithKey(final MetaMethod metaMethodFromSuper) {
				addMetaMethod(metaMethodFromSuper);
				MethodKey key = new DefaultMethodKey(theClass,metaMethodFromSuper.getName(), metaMethodFromSuper.getParameterTypes(),false );
				cacheInstanceMethod(key, metaMethodFromSuper);
			}
		});
	}

	/**
	 * Constructs a new ExpandoMetaClass instance for the given class
	 *
	 * @param theClass The class that the MetaClass applies to
	 */
	public ExpandoMetaClass(Class theClass) {
		super(InvokerHelper.getInstance().getMetaRegistry(), theClass);
		this.myMetaClass = InvokerHelper.getMetaClass(this);

	}

	/**
	 * Constructs a new ExpandoMetaClass instance for the given class optionally placing the MetaClass
	 * in the MetaClassRegistry automatically
	 *
	 * @param theClass The class that the MetaClass applies to
	 * @param register True if the MetaClass should be registered inside the MetaClassRegistry
	 */
	public ExpandoMetaClass(Class theClass, boolean register) {
		this(theClass);

		if(register) {
			super.registry.setMetaClass(theClass, this);
			this.inRegistry = true;
		}
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

		String propertyName;
		boolean isStatic = false;
		protected ExpandoMetaProperty(String name) {
			this(name, false);
		}
		protected ExpandoMetaProperty(String name, boolean isStatic) {
			this.propertyName = name;
			this.isStatic = isStatic;
		}

		public Object leftShift(Object arg) {
			registerIfClosure(arg, false);
			return this;
		}
		private void registerIfClosure(Object arg, boolean replace) {
			if(arg instanceof Closure) {
				Closure callable = (Closure)arg;
				Class[] paramTypes = callable.getParameterTypes();
				if(paramTypes == null)paramTypes = ZERO_ARGUMENTS;
				if(!this.isStatic) {
					Method foundMethod = checkIfMethodExists(theClass, propertyName, paramTypes, false);

					if(foundMethod != null && !replace) throw new GroovyRuntimeException("Cannot add new method ["+propertyName+"] for arguments ["+DefaultGroovyMethods.inspect(paramTypes)+"]. It already exists!");

					registerInstanceMethod(propertyName, callable);
				}
				else {
					Method foundMethod = checkIfMethodExists(theClass, propertyName, paramTypes, true);
					if(foundMethod != null && !replace) throw new GroovyRuntimeException("Cannot add new static method ["+propertyName+"] for arguments ["+DefaultGroovyMethods.inspect(paramTypes)+"]. It already exists!");

					registerStaticMethod(propertyName, callable);
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
	 * Retrieves a list of super classes. Taken from MetaClassImpl. Ideally this method should be protected
	 *
	 * @return A list of super classes
	 */
   protected LinkedList getSuperClasses() {
       LinkedList superClasses = new LinkedList();
       for (Class c = theClass; c!= null; c = c.getSuperclass()) {
           superClasses.addFirst(c);
       }
        if (theClass != null) {
            if (theClass.isArray() && theClass!=Object[].class && !theClass.getComponentType().isPrimitive()) {
                superClasses.addFirst(Object[].class);
            }
        }
        return superClasses;
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
				Class[] paramTypes = c.getParameterTypes();
				if(paramTypes == null)paramTypes = ZERO_ARGUMENTS;

				Constructor ctor = retrieveConstructor(paramTypes);
				if(ctor != null) throw new GroovyRuntimeException("Cannot add new constructor for arguments ["+DefaultGroovyMethods.inspect(paramTypes)+"]. It already exists!");

				registerInstanceMethod(GROOVY_CONSTRUCTOR, c);
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
				return new ExpandoMetaProperty(property);
			}
		}
		else {
			return myMetaClass.getProperty(this, property);
		}
	}

	private boolean isValidExpandoProperty(String property) {
        return !property.equals(META_CLASS) &&
                !property.equals(CLASS) &&
                !property.equals(META_METHODS) &&
                !property.equals(METHODS) &&
                !property.equals(PROPERTIES);
    }

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
	 */
	public Object invokeMethod(String name, Object args) {
		return myMetaClass.invokeMethod(this, name, args);
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
			// here we don't care if the method exists or not we assume the
			// developer is responsible and wants to override methods where necessary
			registerInstanceMethod(property, callable);

		}
		else if(property.equals("allowChangesAfterInit")) {
			this.allowChangesAfterInit = ((Boolean)newValue).booleanValue();
		}
	}


	protected void performOperationOnMetaClass(Callable c) {
		try {
			if(allowChangesAfterInit) {
				this.initialized = false;
			}

			c.call();
		}
		finally {
			if(initCalled)this.initialized = true;
		}
	}


	/**
	 * Registers a new instance method for the given method name and closure on this MetaClass
	 *
	 * @param methodName The method name
	 * @param callable The callable Closure
	 */
	protected void registerInstanceMethod(final String methodName, final Closure callable) {
			final boolean inited = this.initCalled;
			performOperationOnMetaClass(new Callable() {
				public void call() {
					ClosureMetaMethod metaMethod = new ClosureMetaMethod(methodName, theClass,callable);
					MethodKey key = new DefaultMethodKey(theClass,methodName, metaMethod.getParameterTypes(),false );


					addMetaMethod(metaMethod);
					expandoMethods.add(metaMethod);
					cacheInstanceMethod(key, metaMethod);
					if(inited && isGetter(methodName, metaMethod.getParameterTypes())) {
						String propertyName = getPropertyForGetter(methodName);
						registerBeanPropertyForMethod(metaMethod, propertyName, true);

					}
					else if(inited && isSetter(methodName, metaMethod.getParameterTypes())) {
						String propertyName = getPropertyForSetter(methodName);
						registerBeanPropertyForMethod(metaMethod, propertyName, false);
					}
					performRegistryCallbacks();
					if(!hasCreationHandle) {
						registerWithInheritenceManager(theClass, metaMethod);
					}
				}

			});
	}


	private void performRegistryCallbacks() {
		MetaClassRegistry registry =  InvokerHelper.getInstance().getMetaRegistry();
		if(!modified && !inRegistry) {
			modified = true;
			// Implementation note: By default Groovy uses soft references to store MetaClass
			// this insures the registry doesn't grow and get out of hand. By doing this we're
			// saying this this EMC will be a hard reference in the registry. As we're only
			// going have a small number of classes that have modified EMC this is ok
            MetaClass currMetaClass = registry.getMetaClass(theClass);
            if(!(currMetaClass instanceof ExpandoMetaClass) && currMetaClass instanceof AdaptingMetaClass) {
                ((AdaptingMetaClass)currMetaClass).setAdaptee(this);
            } else {
                registry.setMetaClass(theClass, this);
            }

			this.inRegistry = true;
		}
		// Implementation note: EMC handles most cases by itself except for the case where yuou
		// want to call a dynamically injected method registered with a parent on a child class
		// For this to work the MetaClassRegistry needs to have an ExpandoMetaClassCreationHandle
		// What this does is ensure that EVERY class created in the registry uses an EMC
		// Then when an EMC changes it reports back to the EMCCreationHandle which will
		// tell child classes of this class to re-inherit their methods
		if(registry.getMetaClassCreationHandler() instanceof ExpandoMetaClassCreationHandle) {
			ExpandoMetaClassCreationHandle creationHandler = (ExpandoMetaClassCreationHandle)registry.getMetaClassCreationHandler();
			hasCreationHandle  = true;
			if(!creationHandler.hasModifiedMetaClass(this))
				creationHandler.registerModifiedMetaClass(this);
			creationHandler.notifyOfMetaClassChange(this);
		}
	}


	private static void registerWithInheritenceManager(Class theClass, ClosureMetaMethod metaMethod) {
		Map methodMap = (Map)classInheritanceMapping .get(theClass);
		if(methodMap == null) {
			methodMap = new HashMap();
			classInheritanceMapping.put(theClass, methodMap);
		}
		List methodList = (List)methodMap.get(metaMethod.getName());
		if(methodList == null) {
			methodList = new LinkedList();
			methodMap.put(metaMethod.getName(), methodList);
		}
		methodList.add(metaMethod);
	}


	private void registerBeanPropertyForMethod(ClosureMetaMethod metaMethod, String propertyName, boolean getter) {
		MetaBeanProperty beanProperty = (MetaBeanProperty)beanPropertyCache.get(propertyName);
		if(beanProperty == null) {
			if(getter)
				beanProperty = new MetaBeanProperty(propertyName,Object.class,metaMethod,null);
			else
				beanProperty = new MetaBeanProperty(propertyName,Object.class,null,metaMethod);

			beanPropertyCache.put(propertyName, beanProperty);
		}
		else {
			if(getter) {
				MetaMethod setterMethod = beanProperty.getSetter();
				Class type = setterMethod != null ? setterMethod.getParameterTypes()[0] : Object.class;
				beanProperty = new MetaBeanProperty(propertyName,type,metaMethod,setterMethod);
				beanPropertyCache.put(propertyName, beanProperty);
			}else {
				MetaMethod getterMethod = beanProperty.getGetter();
				beanProperty = new MetaBeanProperty(propertyName,metaMethod.getParameterTypes()[0],getterMethod,metaMethod);
				beanPropertyCache .put(propertyName, beanProperty);
			}
		}


		addMetaBeanProperty(beanProperty);
	}

	/**
	 * Registers a new static method for the given method name and closure on this MetaClass
	 *
	 * @param methodName The method name
	 * @param callable The callable Closure
	 */
	protected void registerStaticMethod(final String methodName, final Closure callable) {
		performOperationOnMetaClass(new Callable() {
			public void call() {
				ClosureStaticMetaMethod metaMethod = new ClosureStaticMetaMethod(methodName, theClass,callable);
				MethodKey key = new DefaultMethodKey(theClass,methodName, metaMethod.getParameterTypes(), false );

				addMetaMethod(metaMethod);
				expandoMethods.add(metaMethod);
				cacheStaticMethod(key, metaMethod);
			}

		});
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
	public void refreshInheritedMethods(List modifiedSuperExpandos) {
		for (Iterator i = modifiedSuperExpandos.iterator(); i.hasNext();) {
			ExpandoMetaClass superExpando = (ExpandoMetaClass) i.next();
			List metaMethods = superExpando.getExpandoMethods();
			for (Iterator j = metaMethods.iterator(); j.hasNext();) {
				MetaMethod metaMethod = (MetaMethod) j.next();
				addSuperMethodIfNotOverriden(metaMethod);
			}
		}
	}


	/**
	 * Returns a list of expando MetaMethod instances added to this ExpandoMetaClass
	 *
	 * @return the expandoMethods
	 */
	public List getExpandoMethods() {
		return expandoMethods;
	}


	/**
	 * Returns a list of MetaBeanProperty instances added to this ExpandoMetaClass
	 *
	 * @return the expandoProperties
	 */
	public List getExpandoProperties() {
		return expandoProperties;
	}


    /**
     * Returns true if the name of the method specified and the number of arguments make it a javabean property
     *
     * @param name True if its a Javabean property
     * @param args The arguments
     * @return True if it is a javabean property method
     */
    protected boolean isGetter(String name, Class[] args) {
        if(name == null || name.length() == 0 || args == null)return false;

        if(name.startsWith("get")) {
            if(args.length != 0)return false;
            name = name.substring(3);
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
    protected String getPropertyForGetter(String getterName) {
        if(getterName == null || getterName.length() == 0)return null;

        if(getterName.startsWith("get")) {
            String prop = getterName.substring(3);
            return convertPropertyName(prop);
        }
        return null;
    }

    private String convertPropertyName(String prop) {
        if(Character.isUpperCase(prop.charAt(0)) && Character.isUpperCase(prop.charAt(1))) {
            return prop;
        }
        else if(Character.isDigit(prop.charAt(0))) {
            return prop;
        }
        else {
            return Character.toLowerCase(prop.charAt(0)) + prop.substring(1);
        }
    }


    /**
     * Returns a property name equivalent for the given setter name or null if it is not a getter
     *
     * @param setterName The setter name
     * @return The property name equivalent
     */
    protected String getPropertyForSetter(String setterName) {
        if(setterName == null || setterName.length() == 0)return null;

        if(setterName.startsWith("set")) {
            String prop = setterName.substring(3);
            return convertPropertyName(prop);
        }
        return null;
    }

    protected boolean isSetter(String name, Class[] args) {
        if(name == null || name.length() == 0 || args == null)return false;

        if(name.startsWith("set")) {
            if(args.length != 1) return false;
            name = name.substring(3);
            if(name.length() > 0 && Character.isUpperCase(name.charAt(0))) return true;
        }

        return false;
    }



}

