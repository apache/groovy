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
import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Dynamic groovy proxy for another object.  All property accesses and method
 * invocations get forwarded to actual object, unless the proxy overrides it.
 * The calling order can be set to try the real or proxy first.
 *
 * @author Troy Heninger
 */
public class Proxy extends GroovyObjectSupport {

    private boolean tryRealFirst;
    private Object realObject;
    private MetaClass realMeta, first, second;

    /**
     * Constructor.  Takes real object to be excapsulated and set's the order
     * to the real object first.
     *
     * @param real the real object
     */
    public Proxy(Object real) {
        this(real, true);
    }

    /**
     * Constructor.  Takes real object to be excapsulated and order.
     *
     * @param real the real object
     * @param tryRealFirst call real object first if true
     */
    public Proxy(Object real, boolean tryRealFirst) {
        this.tryRealFirst = tryRealFirst;
        this.realObject = real;
        setMetaClass(InvokerHelper.getMetaClass(real));
    }

    /**
     * Get the property of this proxy, or the real object if property doesn't
     * exist.
     *
     * @param property property to retrieve
     * @return property's value
     */
    public Object getProperty(String property) {
        try {
            return first.getProperty(this, property);
        }
        catch (MissingPropertyException e) {
            return second.getProperty(realObject, property);
        }
    }

    /**
     * Set the property of this proxy, or the real object if property doesn't
     * exist.
     *
     * @param property property to set
     * @param newValue value to store
     */
    public void setProperty(String property, Object newValue) {
        try {
            first.setProperty(this, property, newValue);
        }
        catch (MissingPropertyException e) {
            second.setProperty(realObject, property, newValue);
        }
    }

    /**
     * Returns the MetaClass for the <b>real</b> object.
     *
     * @return MetaClass of real object
     */
    public MetaClass getMetaClass() {
        return realMeta;
    }

    /**
     * Returns the MetaClass for the <b>proxy </b> object.
     *
     * @return MetaClass of proxy object
     */
    public MetaClass getProxyMetaClass() {
        return super.getMetaClass();
    }

    /**
     * Returns the encapsulated object.
     *
     * @return the real object
     */
    public Object getRealObject() {
        return realObject;
    }

    /**
     * Call a method of this proxy, or the real object if method doesn't exist.
     *
     * @param name method to invoke
     * @param args arguments to pass
     * @return
     */
    public Object invokeMethod(String name, Object args) {
        try {
            return first.invokeMethod(this, name, args);
        }
        catch (MissingMethodException e) {
            return second.invokeMethod(this, name, args);
        }
    }

    /**
     * Dynamically change the meta class to use for the <b>real</b> object.
     *
     * @param metaClass substitute real meta class
     */
    public void setMetaClass(MetaClass metaClass) {
        realMeta = metaClass;
        if (tryRealFirst) {
            first = realMeta;
            second = getMetaClass();
        }
        else {
            first = getMetaClass();
            second = realMeta;
        }
    }

    /**
     * Dynamically change the meta class to use for the <b>proxy</b> object.
     *
     * @param metaClass substitute meta class for the proxy object
     */
    public void setProxyMetaClass(MetaClass metaClass) {
        super.setMetaClass(metaClass);
        if (tryRealFirst) {
            first = realMeta;
            second = getMetaClass();
        }
        else {
            first = getMetaClass();
            second = realMeta;
        }
    }
}
