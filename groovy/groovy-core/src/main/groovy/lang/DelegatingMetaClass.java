/*
 * Copyright 2005 John G. Wilson
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
 *
 */

package groovy.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;

/**
 * @author John Wilson
 *
 */

public class DelegatingMetaClass extends MetaClass {
    private final MetaClass delegate;
    public DelegatingMetaClass(final MetaClass delegate) {
        super(delegate.getClass());
        
        this.delegate = delegate;
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#addNewInstanceMethod(java.lang.reflect.Method)
     */
    protected void addNewInstanceMethod(Method method) {
        delegate.addNewInstanceMethod(method);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#addNewStaticMethod(java.lang.reflect.Method)
     */
    protected void addNewStaticMethod(Method method) {
        delegate.addNewStaticMethod(method);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#checkInitialised()
     */
    protected void checkInitialised() {
        delegate.checkInitialised();
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#pickMethod(java.lang.Object, java.lang.String, java.lang.Object[])
     */
    protected MetaMethod pickMethod(Object object, String methodName, Object[] arguments) {
        return delegate.pickMethod(object, methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#pickMethod(java.lang.String, java.lang.Class[])
     */
    protected MetaMethod pickMethod(String methodName, Class[] arguments) {
        return delegate.pickMethod(methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getAttribute(java.lang.Object, java.lang.String)
     */
    public Object getAttribute(Object object, String attribute) {
        return delegate.getAttribute(object, attribute);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getClassNode()
     */
    public ClassNode getClassNode() {
         return delegate.getClassNode();
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getMetaMethods()
     */
    public List getMetaMethods() {
        return delegate.getMetaMethods();
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getMethods()
     */
    public List getMethods() {
        return delegate.getMethods();
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getProperties()
     */
    public List getProperties() {
        return delegate.getProperties();
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#getProperty(java.lang.Object, java.lang.String)
     */
    public Object getProperty(Object object, String property) {
        return delegate.getProperty(object, property);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeConstructor(java.lang.Object[])
     */
    public Object invokeConstructor(Object[] arguments) {
        return delegate.invokeConstructor(arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeConstructorAt(java.lang.Class, java.lang.Object[])
     */
    public Object invokeConstructorAt(Class at, Object[] arguments) {
        return delegate.invokeConstructorAt(at, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeMethod(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        return delegate.invokeMethod(object, methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeMethod(java.lang.Object, java.lang.String, java.lang.Object[])
     */
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        return delegate.invokeMethod(object, methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#invokeStaticMethod(java.lang.Object, java.lang.String, java.lang.Object[])
     */
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        return delegate.invokeStaticMethod(object, methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#retrieveConstructor(java.lang.Class[])
     */
    public Constructor retrieveConstructor(Class[] arguments) {
        return delegate.retrieveConstructor(arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#retrieveMethod(java.lang.Object, java.lang.String, java.lang.Object[])
     */
    public MetaMethod retrieveMethod(Object owner, String methodName, Object[] arguments) {
        return delegate.retrieveMethod(owner, methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#retrieveMethod(java.lang.String, java.lang.Class[])
     */
    public MetaMethod retrieveMethod(String methodName, Class[] arguments) {
        return delegate.retrieveMethod(methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#retrieveStaticMethod(java.lang.String, java.lang.Class[])
     */
    public MetaMethod retrieveStaticMethod(String methodName, Class[] arguments) {
        return delegate.retrieveStaticMethod(methodName, arguments);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#setAttribute(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void setAttribute(Object object, String attribute, Object newValue) {
        delegate.setAttribute(object, attribute, newValue);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#setProperties(java.lang.Object, java.util.Map)
     */
    public void setProperties(Object bean, Map map) {
        delegate.setProperties(bean, map);
    }
    /* (non-Javadoc)
     * @see groovy.lang.MetaClass#setProperty(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void setProperty(Object object, String property, Object newValue) {
        delegate.setProperty(object, property, newValue);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }
}
