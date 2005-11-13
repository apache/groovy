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

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GString;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <p>Proxy class for all Variant objects.</p>
 * <p>For the Jacob library, a Variant is a wrapper around an object returned by a COM component:
 * it can be the value of a property, or the return value of a COM method call.</p>
 *
 * @author Guillaume Laforge
 * @author Dierk Koenig, adapted to Jacob 1.9
 */
public class VariantProxy extends GroovyObjectSupport
{
    private Variant variant;

    /**
     * Creates a Variant wrapped in a GroovyObject proxy (Groovy's metaprogramming facilities).
     *
     * @param var the variant to wrap.
     */
    public VariantProxy(Variant var)
    {
        variant = var;
    }

    /**
     * Used internally by the Groovy generated event support class
     * to transform Variant arrays to VariantProxy arrays.
     *
     * @param obj a Variant[] array
     * @return a VariantProxy[] array
     */
    static VariantProxy[] defineArray(Object obj)
    {
        if (obj instanceof Variant[]) {
            Variant[] variants = (Variant[])obj;
            VariantProxy[] array = new VariantProxy[variants.length];
            for (int i = 0; i < array.length; i++)
                array[i] = new VariantProxy(variants[i]);
            return array;
        } else {
            return new VariantProxy[0];
        }
    }

    /**
     * Get the property associated to the property named passed as argument.<b/>
     * The property name <code>value</code> will return the real value of the Variant, not a proxy.
     *
     * @param property the name of the property to retrieve.
     * @return the Variant proxy, or the real value if <code>property.equals("value")</code>
     */
    public Object getProperty(String property)
    {
        if ("value".equals(property))
            return toObject(variant);
        return new VariantProxy(Dispatch.get(variant.toDispatch(), property));
    }

    /**
     * Return the real value wrapped inside the variant.<b/>
     *
     * @param v the variant to retrieve the real value from
     * @return the real value of the variant
     */
    private Object toObject(Variant v)
    {
        switch (v.getvt())
        {
            case Variant.VariantEmpty:
                return null;
            case Variant.VariantNull:
                return null;
            case Variant.VariantShort:
                return new Short(v.toShort());
            case Variant.VariantInt:
                return new Integer(v.toInt());
            case Variant.VariantFloat:
                return new Float(v.toFloat());
            case Variant.VariantDouble:
                return new Double(v.toDouble());
            case Variant.VariantCurrency:
                return new Long(v.toCurrency());
            case Variant.VariantDate:
                return new Double(v.toDate());
            case Variant.VariantString:
                return new String(v.toString());
            case Variant.VariantDispatch:
                return v;
            case Variant.VariantError:
                return new Integer(v.toError());
            case Variant.VariantBoolean:
                return new Boolean(v.toBoolean());
            case Variant.VariantVariant:
                return v;
            case Variant.VariantObject:
                return v.toObject();
            case Variant.VariantByte:
                return new Byte(v.toByte());
            case Variant.VariantTypeMask:
                return v;
            case Variant.VariantArray:
                return v.toVariantArray();
            case Variant.VariantByref:
                return v;
            default:
                return v;
        }
    }

    /**
     * Sets a property of the underlying variant.
     *
     * @param property name of the property to change
     * @param newValue the new value of the property
     */
    public void setProperty(String property, Object newValue)
    {
        Dispatch.put(variant.toDispatch(), property, toValue(newValue));
    }

    static Object toValue(Object newValue)
    {
        // special case for Groovy's arithmetics:
        // BigInteger and BigDecimal aren't recognized by the Jacob library
        if (newValue instanceof BigInteger)
            newValue = new Integer(((BigInteger) newValue).intValue());
        else if (newValue instanceof BigDecimal)
            newValue = new Double(((BigDecimal) newValue).doubleValue());
        else if (newValue instanceof GString)
            newValue = newValue.toString();
        return newValue;
    }

    /**
     * Invoke a method on the variant.
     *
     * @param name name of the method to call
     * @param args arguments passed as parameters to the method call
     * @return the value returned by the method call
     */
    public Object invokeMethod(String name, Object args)
    {
        Object[] objs = InvokerHelper.getInstance().asArray(args);
        // special case for getValue() to retrieve the real value wrapped inside the variant object
        if ("getValue".equals(name) && (objs == null || objs.length == 0))
            return toObject(variant);
        Variant[] variants = new Variant[objs.length];
        for (int i = 0; i < variants.length; i++)
        {
            variants[i] = new Variant(toValue(objs[i]));
        }
        return new VariantProxy(Dispatch.callN(variant.toDispatch(), name, variants));
    }

    protected void finalize() throws Throwable
    {
        variant.safeRelease();
        super.finalize();
    }
}
