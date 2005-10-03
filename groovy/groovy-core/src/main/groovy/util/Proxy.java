/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package groovy.util;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Dynamic groovy proxy for another object.  All method
 * invocations get forwarded to actual object, unless the proxy overrides it.
 * See groovy/util/ProxyTest.groovy for usage details.
 *
 * @author Troy Heninger
 * @author Dierk Koenig
 */
public class Proxy extends GroovyObjectSupport {

    private Object adaptee = null;

    /**
     * This method is for convenience.
     * It allows to get around the need for defining dump ctors is subclasses.
     * See unit tests for details.
     */
    public Proxy wrap(Object adaptee){
        setAdaptee(adaptee);
        return this;
    }

    public Object getAdaptee() {
        return adaptee;
    }

    public void setAdaptee(Object adaptee) {
        this.adaptee = adaptee;
    }

    public Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args);
        }
        catch (MissingMethodException e) {
            return InvokerHelper.getMetaClass(adaptee).invokeMethod(adaptee, name, args);
        }
    }

    public Object invokeMethodAt(Class at, String name, Object args) {
        try {
            return super.invokeMethodAt(at, name, args);
        }
        catch (MissingMethodException e) {
            return InvokerHelper.getMetaClass(adaptee).invokeMethodAt(at, adaptee, name, args);
        }
    }

}
