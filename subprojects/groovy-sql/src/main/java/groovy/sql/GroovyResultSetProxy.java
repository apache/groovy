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
package groovy.sql;

import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;

/**
 * GroovyResultSetProxy is used to create a proxy for GroovyResultSet.
 * Due to the version incompatibility between java 6 and older versions
 * methods with additional logic were moved into an extension class. When
 * getting properties or calling methods, the runtime will try to first
 * execute these on the extension and then on the ResultSet itself.
 * This way it is possible to replace and add methods. To overload methods
 * from ResultSet all methods have to be implemented on the extension
 * class.
 */
public final class GroovyResultSetProxy implements InvocationHandler {

    private final GroovyResultSetExtension extension;

    /**
     * Creates a new proxy instance.
     * This will create the extension automatically using
     * GroovyResultSetExtension
     *
     * @param set the result set to delegate to
     * @see GroovyResultSetExtension
     */
    public GroovyResultSetProxy(ResultSet set) {
        extension = new GroovyResultSetExtension(set);
    }

    /**
     * Creates a new proxy instance with a custom extension.
     *
     * @param ext the extension
     * @see GroovyResultSetExtension
     */
    public GroovyResultSetProxy(GroovyResultSetExtension ext) {
        extension = ext;
    }

    /**
     * Invokes a method for the GroovyResultSet.
     * This will try to invoke the given method first on the extension
     * and then on the result set given as proxy parameter.
     *
     * @param proxy  the result set
     * @param method the method name of this method will be used
     *               to make a call on the extension. If this fails the call will be
     *               done on the proxy instead
     * @param args   for the call
     * @see ResultSet
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (method.getDeclaringClass() == GroovyObject.class) {
            if (name.equals("getMetaClass")) {
                return getMetaClass();
            } else if (name.equals("setMetaClass")) {
                return setMetaClass((MetaClass) args[0]);
            }
        }

        return InvokerHelper.invokeMethod(extension, method.getName(), args);
    }

    private MetaClass metaClass;

    private MetaClass setMetaClass(MetaClass mc) {
        metaClass = mc;
        return mc;
    }

    /**
     * This class is introduced as a workaround for GROOVY-6187, which failed
     * because if you use a metaclass from an interface, methods defined on
     * Object cannot be called.
     */
    private abstract static class DummyResultSet implements GroovyResultSet {}

    private MetaClass getMetaClass() {
        if (metaClass == null) {
            metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(DummyResultSet.class);
        }
        return metaClass;
    }

    /**
     * Gets a proxy instance that can be used as GroovyResultSet.
     *
     * @return the proxy
     */
    public GroovyResultSet getImpl() {
        return (GroovyResultSet)
                Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[]{GroovyResultSet.class},
                        this
                );
    }
}
