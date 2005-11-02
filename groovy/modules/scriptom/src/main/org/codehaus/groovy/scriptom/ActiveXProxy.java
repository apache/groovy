/*
 * $Id$
 *
 * Copyright 2005 (C) Guillaume Laforge. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
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
package org.codehaus.groovy.scriptom;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import com.jacob.com.Dispatch;
import com.jacob.com.DispatchEvents;
import com.jacob.com.ComThread;
import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * <p>Dynamic Groovy proxy around ActiveX COM components.</p>
 *
 * @author Guillaume Laforge
 * @author Dierk Koenig, adapted to Jacob 1.9
 */
public class ActiveXProxy extends GroovyObjectSupport
{
    private ActiveXComponent activex;
    private EventSupport eventSupport;

    /**
     * <p>Build a GroovyObject proxy for an ActiveX component,
     * leveraging Groovy's metaprogramming facilities</p>
     * <p/>
     * <p>Example:</p>
     * <code>
     * excel = new ActiveXProxy("Excel.Application")
     * explorer = new ActiveXProxy("InternetExplorer.Application")
     * // call with Internet Explorer clsid
     * explorer = new ActiveXProxy("clsid:{0002DF01-0000-0000-C000-000000000046}")
     * </code>
     *
     * @param clsId the name of the application or the Class ID of the component.
     */
    public ActiveXProxy(String clsId)
    {
        ComThread.InitMTA();
        activex = new ActiveXComponent(clsId);
        eventSupport = new EventSupport(activex);
    }

    /**
     * Get the property
     *
     * @param propName the name of the property
     * @return the value associated with the property name
     */
    public Object getProperty(String propName)
    {
        if ("events".equals(propName))
            return eventSupport;

        return toReturn(activex.getProperty(propName));
    }

    /**
     * Invoke a method on an ActiveX component
     *
     * @param methodName name of the method to call
     * @param parameters parameters of the method call
     * @return the value returned by the method call
     */
    public Object invokeMethod(String methodName, Object parameters)
    {
        if ("getEvents".equals(methodName))
            return eventSupport;

        Object[] objs = InvokerHelper.getInstance().asArray(parameters);
        Variant[] variants = new Variant[objs.length];
        for (int i = 0; i < variants.length; i++)
        {
            variants[i] = new Variant(VariantProxy.toValue(objs[i]));
        }
        return toReturn(activex.invoke(methodName, variants));
    }

    /**
     * Sets a property
     *
     * @param propertyName name of the property to set
     * @param newValue     new value of the property
     */
    public void setProperty(String propertyName, Object newValue)
    {
        if ("events".equals(propertyName))
            new DispatchEvents(activex, newValue);
        activex.setProperty(propertyName, new Variant(VariantProxy.toValue(newValue)));
    }

    private Object toReturn(Object obj)
    {
        if (obj instanceof Variant)
        {
            return new VariantProxy((Variant) obj);
        }
        else if (obj instanceof Dispatch)
        {
            Variant v = new Variant();
            v.putDispatch(obj);
            return new VariantProxy(v);
        }
        else
        {
            return obj;
        }
    }

    protected void finalize() throws Throwable
    {
        activex.safeRelease();
        ComThread.Release();
        super.finalize();
    }
}
