/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.commons.spring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springmodules.beans.factory.drivers.Bean;
import org.springmodules.beans.factory.drivers.BeanReference;
import org.springmodules.beans.factory.drivers.Instance;
import org.springmodules.beans.factory.drivers.LiteralValue;

/**
 * <p>Helper class to create Spring configuration objects for loading the application context.
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class SpringConfigUtils {

	private SpringConfigUtils() {
		super();
	}

	public static Bean createSingletonBean() {
		return createSingletonBean(null);
	}
	
	public static Bean createSingletonBean(Class clazz) {
		return createSingletonBean(clazz, null, null);
	}
	
	public static Bean createSingletonBean(Class clazz, Collection constructorArguments) {
		return createSingletonBean(clazz, null, constructorArguments);
	}
	
	public static Bean createSingletonBean(Class clazz, BeanReference parent) {
		return createSingletonBean(clazz, parent, null);
	}
	
	public static Bean createSingletonBean(Class clazz, BeanReference parent, Collection constructorArguments) {
		return createSingletonBean(clazz, parent, false, constructorArguments);
	}
	
	public static Bean createAbstractSingletonBean(Class clazz) {
		return createAbstractSingletonBean(clazz, null);
	}
	
	public static Bean createAbstractSingletonBean(Class clazz, Collection constructorArguments) {
		return createSingletonBean(clazz, null, true, constructorArguments);
	}
	
	public static Bean createSingletonBean(final Class clazz, final BeanReference parent, final boolean _abstract, final Collection constructorArguments) {
		return new Bean() {
			private String autowire = null;
			private Collection dependsOn = null;
			private String description = null;
			private String destroyMethod = null;
			private String initMethod = null;
			private Map properties = new HashMap();
			private BeanReference factoryBean = null;
			private String factoryMethod = null;
			
			public String getAutowire() {
				return autowire;
			}
			
			public Class getClazz() {
				return clazz;
			}
			
			public Collection getConstructorArguments() {
				return constructorArguments;
			}
			
			public Collection getDependsOn() {
				return dependsOn;
			}
			
			public String getDescription() {
				return description;
			}
			
			public String getDestroyMethod() {
				return destroyMethod;
			}
			
			public String getInitMethod() {
				return initMethod;
			}
			
			public BeanReference getParent() {
				return parent;
			}
			
			public Map getProperties() {
				return properties;
			}
			
			public boolean isAbstract() {
				return _abstract;
			}
			
			public boolean isLazy() {
				return false;
			}
			
			public boolean isSingleton() {
				return true;
			}
			
			public void setAutowire(String autowire) {
				this.autowire = autowire;
			}
			
			public void setDependsOn(Collection dependsOn) {
				this.dependsOn = dependsOn;
			}
			
			public void setDescription(String description) {
				this.description = description;
			}
			
			public void setDestroyMethod(String destroyMethod) {
				this.destroyMethod = destroyMethod;
			}
			
			public void setInitMethod(String initMethod) {
				this.initMethod = initMethod;
			}
			
			public void setProperty(String name, Instance value) {
				this.properties.put(name, value);
			}
			
			public BeanReference getFactoryBean() {
				return this.factoryBean;
			}
			
			public void setFactoryBean(BeanReference factoryBean) {
				this.factoryBean = factoryBean;
			}
			
			public String getFactoryMethod() {
				return this.factoryMethod;
			}
			
			public void setFactoryMethod(String factoryMethod) {
				this.factoryMethod = factoryMethod;
			}
		};
	}
	
	public static BeanReference createBeanReference(final String beanName, final Bean bean) {
		return new BeanReference() {
			public Bean getBean() {
				return bean;
			}
			
			public String getBeanName() {
				return beanName;
			}
		};
	}

	public static BeanReference createBeanReference(String beanName) {
		return createBeanReference(beanName, null);
	}
	
	public static LiteralValue createLiteralValue(final String value) {
		return new LiteralValue() {
			public String getValue() {
				return value;
			}
		};
	}
	
}
