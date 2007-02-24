/*
 $Id: GroovyResultSet.java 4831 2007-01-12 13:59:29Z tug $

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.sql;

import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * GroovyResultSetProxy is used to create a proxy for GroovyResultSet.
 * Due to the version incompatibility between java 6 and older versions 
 * methods with additional logic were moved in an extension class. When
 * getting properties or calling methods the runtime will try to first
 * execute these on the extension and then on the ResultSet itself.
 * This way it is possible to replace and add methods. To overload methods
 * from ResultSet all methods have to be implemented on the extension
 * class.
 * 
 * @author Jochen Theodorou
 */
public final class GroovyResultSetProxy implements InvocationHandler {
    
    private GroovyResultSetExtension extension;
    
    /**
     * Creates a new procy instance.
     * This will create the extension automatically using
     * GroovyResultSetExtension
     * @see GroovyResultSetExtension
     * @param set the result set to delegate to
     */
    public GroovyResultSetProxy(ResultSet set) {
        extension = new GroovyResultSetExtension(set);
    }
    
    /**
     * Creates a new procy instance with a custom extension.
     * @param ext the extension
     * @see GroovyResultSetExtension
     */
    public GroovyResultSetProxy(GroovyResultSetExtension ext) {
        extension = ext;
    }

    /**
     * Invokes a method for the GroovyResultSet.
     * This will try to invoke the given method first on the extension
     * and then on the result set given as procy parameter.
     * @param proxy the result set
     * @param method the method name of this method will be used
     * to make a call on the extension. If this fails the call will be
     * done on proxy instead
     * @params arguments for the call
     * @see ResultSet
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (method.getDeclaringClass()==GroovyObject.class) {
            if (name.equals("getMetaClass")) {
                return getMetaClass();
            } else if (name.equals("setMetaClass")) {
                return setMetaClass((MetaClass) args[0]);
            }
        }
        
        if (method.getDeclaringClass()!=Object.class) {
            try {
                return InvokerHelper.invokeMethod(extension,method.getName(),args);
            } catch (MissingMethodException mme) {}
        }
        
        try {
            return method.invoke(proxy,args);
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }
    
    private MetaClass metaClass;
    
    private MetaClass setMetaClass(MetaClass mc) {
        metaClass = mc;
        return mc;
    }

    private MetaClass getMetaClass() {
        if (metaClass==null) {
            metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(GroovyResultSet.class);
        } 
        return metaClass;
    }

    /**
     * Gets an procy instance that can be used as GroovyResultSet.
     * @return the proxy
     */
    public GroovyResultSet getImpl() {
        return (GroovyResultSet) 
            Proxy.newProxyInstance (
                this.getClass().getClassLoader(),
                new Class[]{GroovyResultSet.class},
                this
            );
    }
}