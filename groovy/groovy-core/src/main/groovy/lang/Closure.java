/*
 $Id$

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
package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents any closure object in Groovy.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class Closure extends GroovyObjectSupport implements Cloneable {

    private Object delegate;

    public Closure(Object delegate) {
        this.delegate = delegate;
    }

    public Object invokeMethod(String method, Object arguments) {
        /** @todo optimise me! */
        try {
            return getMetaClass().invokeMethod(this, method, arguments);
        }
        catch (GroovyRuntimeException e) {
            Object delegate = getDelegate();
            if (delegate != this) {
                try {
                    // lets try invoke method on delegate
                    return InvokerHelper.invokeMethod(delegate, method, arguments);
                }
                catch (GroovyRuntimeException e2) {
                    // ignore, we'll throw e
                }
            }
            throw e;
        }
    }
    
    public Object getProperty(String property) {
        /** @todo optimise me! */
        try {
            return getMetaClass().getProperty(this, property);
        }
        catch (GroovyRuntimeException e) {
            Object delegate = getDelegate();
            if (delegate != this) {
                try {
                    // lets try invoke method on delegate
                    return InvokerHelper.getProperty(delegate, property);
                }
                catch (GroovyRuntimeException e2) {
                    // ignore, we'll throw e
                }
            }
            throw e;
        }
    }

    public void setProperty(String property, Object newValue) {
        /** @todo optimise me! */
        try {
            getMetaClass().setProperty(this, property, newValue);
            return;
        }
        catch (GroovyRuntimeException e) {
            Object delegate = getDelegate();
            if (delegate != this) {
                try {
                    // lets try invoke method on delegate
                    InvokerHelper.setProperty(delegate, property, newValue);
                    return;
                }
                catch (GroovyRuntimeException e2) {
                    // ignore, we'll throw e
                }
            }
            throw e;
        }
    }
    
    /**
     * Invokes the closure without any parameters, returning any value if applicable.
     * 
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call() {
        return call(null);
    }

    /**
     * Invokes the closure, returning any value if applicable.
     * 
     * @param arguments could be a single value or a List of values
     * @return the value if applicable or null if there is no return statement in the closure
     */
    public Object call(Object arguments) {
        /** @todo we should customize the exception message better */
        return InvokerHelper.invokeMethod(this, "doCall", arguments);
    }

    /**
     * @return the delegate Object to which method calls will go which is
     * typically the outer class when the closure is constructed
     */
    public Object getDelegate() {
        return delegate;
    }

    /**
     * Allows the delegate to be changed such as when performing markup building
     * @param delegate
     */
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }
    
}
