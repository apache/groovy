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

package org.codehaus.groovy.runtime.wrappers;

//import java.lang.reflect.Constructor;
//import java.lang.reflect.Method;
//import java.util.List;
//import java.util.Map;
//
//import org.codehaus.groovy.ast.ClassNode;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
//import groovy.lang.MetaMethod;

/**
 * @author John Wilson
 *
 */

public abstract class Wrapper implements GroovyObject {
    protected MetaClass delegatingMetaClass;
    
//  protected MetaClass delegatingMetaClass = new MetaClass(Object.class) {
//    /**
//     * @param obj
//     * @see java.lang.Object#equals(java.lang.Object)
//     */
//    public boolean equals(Object obj) {
//      return Wrapper.this.getDelegatedMetaClass().equals(obj);
//    }
//
//    /**
//     * @param object
//     * @param attribute
//     * @see groovy.lang.MetaClass#getAttribute(java.lang.Object, java.lang.String)
//     */
//    public Object getAttribute(Object object, String attribute) {
//      return Wrapper.this.getDelegatedMetaClass().getAttribute(Wrapper.this.getWrapped(), attribute);
//    }
//
//    /**
//     * @see groovy.lang.MetaClass#getClassNode()
//     */
//    public ClassNode getClassNode() {
//      return Wrapper.this.getDelegatedMetaClass().getClassNode();
//    }
//
//    /**
//     * @see groovy.lang.MetaClass#getMetaMethods()
//     */
//    public List getMetaMethods() {
//      return Wrapper.this.getDelegatedMetaClass().getMetaMethods();
//    }
//
//    /**
//     * @see groovy.lang.MetaClass#getMethods()
//     */
//    public List getMethods() {
//      return Wrapper.this.getDelegatedMetaClass().getMethods();
//    }
//
//    /**
//     * @see groovy.lang.MetaClass#getProperties()
//     */
//    public List getProperties() {
//      return Wrapper.this.getDelegatedMetaClass().getProperties();
//    }
//
//    /**
//     * @param object
//     * @param property
//     * @see groovy.lang.MetaClass#getProperty(java.lang.Object, java.lang.String)
//     */
//    public Object getProperty(Object object, String property) {
//      return Wrapper.this.getDelegatedMetaClass().getProperty(Wrapper.this.getWrapped(), property);
//    }
//
//    /**
//     * @see java.lang.Object#hashCode()
//     */
//    public int hashCode() {
//      return Wrapper.this.getDelegatedMetaClass().hashCode();
//    }
//
//    /**
//     * @param arguments
//     * @see groovy.lang.MetaClass#invokeConstructor(java.lang.Object[])
//     */
//    public Object invokeConstructor(Object[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().invokeConstructor(arguments);
//    }
//
//    /**
//     * @param at
//     * @param arguments
//     * @see groovy.lang.MetaClass#invokeConstructorAt(java.lang.Class, java.lang.Object[])
//     */
//    public Object invokeConstructorAt(Class at, Object[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().invokeConstructorAt(at, arguments);
//    }
//
//    /**
//     * @param object
//     * @param methodName
//     * @param arguments
//     * @see groovy.lang.MetaClass#invokeMethod(java.lang.Object, java.lang.String, java.lang.Object)
//     */
//    public Object invokeMethod(Object object, String methodName, Object arguments) {
//      return Wrapper.this.getDelegatedMetaClass().invokeMethod(Wrapper.this.getWrapped(), methodName, arguments);
//    }
//
//    /**
//     * @param object
//     * @param methodName
//     * @param arguments
//     * @see groovy.lang.MetaClass#invokeMethod(java.lang.Object, java.lang.String, java.lang.Object[])
//     */
//    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().invokeMethod(Wrapper.this.getWrapped(), methodName, arguments);
//    }
//
//    /**
//     * @param object
//     * @param methodName
//     * @param arguments
//     * @see groovy.lang.MetaClass#invokeStaticMethod(java.lang.Object, java.lang.String, java.lang.Object[])
//     */
//    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().invokeStaticMethod(Wrapper.this.getWrapped(), methodName, arguments);
//    }
//
//    /**
//     * @param arguments
//     * @see groovy.lang.MetaClass#retrieveConstructor(java.lang.Class[])
//     */
//    public Constructor retrieveConstructor(Class[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().retrieveConstructor(arguments);
//    }
//
//    /**
//     * @param owner
//     * @param methodName
//     * @param arguments
//     * @see groovy.lang.MetaClass#retrieveMethod(java.lang.Object, java.lang.String, java.lang.Object[])
//     */
//    public MetaMethod retrieveMethod(Object owner, String methodName, Object[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().retrieveMethod(owner, methodName, arguments);
//    }
//
//    /**
//     * @param methodName
//     * @param arguments
//     * @see groovy.lang.MetaClass#retrieveMethod(java.lang.String, java.lang.Class[])
//     */
//    public MetaMethod retrieveMethod(String methodName, Class[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().retrieveMethod(methodName, arguments);
//    }
//
//    /**
//     * @param methodName
//     * @param arguments
//     * @see groovy.lang.MetaClass#retrieveStaticMethod(java.lang.String, java.lang.Class[])
//     */
//    public MetaMethod retrieveStaticMethod(String methodName, Class[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().retrieveStaticMethod(methodName, arguments);
//    }
//
//    /**
//     * @param object
//     * @param attribute
//     * @param newValue
//     * @see groovy.lang.MetaClass#setAttribute(java.lang.Object, java.lang.String, java.lang.Object)
//     */
//    public void setAttribute(Object object, String attribute, Object newValue) {
//      Wrapper.this.getDelegatedMetaClass().setAttribute(Wrapper.this.getWrapped(), attribute, newValue);
//    }
//
//    /**
//     * @param bean
//     * @param map
//     * @see groovy.lang.MetaClass#setProperties(java.lang.Object, java.util.Map)
//     */
//    public void setProperties(Object bean, Map map) {
//      Wrapper.this.getDelegatedMetaClass().setProperties(Wrapper.this.getWrapped(), map);
//    }
//
//    /**
//     * @param object
//     * @param property
//     * @param newValue
//     * @see groovy.lang.MetaClass#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
//     */
//    public void setProperty(Object object, String property, Object newValue) {
//      Wrapper.this.getDelegatedMetaClass().setProperty(Wrapper.this.getWrapped(), property, newValue);
//    }
//
//    /**
//     * @see java.lang.Object#toString()
//     */
//    public String toString() {
//      return Wrapper.this.getDelegatedMetaClass().toString();
//    }
//
//    /* (non-Javadoc)
//     * @see groovy.lang.MetaClass#addNewInstanceMethod(java.lang.reflect.Method)
//     */
//    public void addNewInstanceMethod(Method method) {
//      Wrapper.this.getDelegatedMetaClass().addNewInstanceMethod(method);
//    }
//
//    /* (non-Javadoc)
//     * @see groovy.lang.MetaClass#addNewStaticMethod(java.lang.reflect.Method)
//     */
//    public void addNewStaticMethod(Method method) {
//      Wrapper.this.getDelegatedMetaClass().addNewStaticMethod(method);
//    }
//
//    /* (non-Javadoc)
//     * @see groovy.lang.MetaClass#checkInitialised()
//     */
//    public void checkInitialised() {
//      Wrapper.this.getDelegatedMetaClass().checkInitialised();
//    }
//
//    /* (non-Javadoc)
//     * @see groovy.lang.MetaClass#pickMethod(java.lang.Object, java.lang.String, java.lang.Object[])
//     */
//    public MetaMethod pickMethod(Object object, String methodName, Object[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().pickMethod(object, methodName, arguments);
//    }
//
//    /* (non-Javadoc)
//     * @see groovy.lang.MetaClass#pickMethod(java.lang.String, java.lang.Class[])
//     */
//    public MetaMethod pickMethod(String methodName, Class[] arguments) {
//      return Wrapper.this.getDelegatedMetaClass().pickMethod(methodName, arguments);
//    }
//  };
  
  protected final Class constrainedType;
  
  public Wrapper(final Class constrainedType) {
    this.constrainedType = constrainedType;
  }

  /* (non-Javadoc)
   * @see groovy.lang.GroovyObject#getMetaClass()
   * 
   * This will only be useful post 1.0
   */
  public MetaClass getMetaClass() {
    return this.delegatingMetaClass;
  }
  
  public abstract Object unwrap();
  
  public Class getType() {
    return this.constrainedType;
  }
  
  protected abstract Object getWrapped();
  protected abstract MetaClass getDelegatedMetaClass();
}
