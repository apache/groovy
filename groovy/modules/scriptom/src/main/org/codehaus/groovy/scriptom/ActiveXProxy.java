package org.codehaus.groovy.scriptom;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import com.jacob.com.Dispatch;
import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * <p>Dynamic Groovy proxy around ActiveX COM components.</p>
 *
 * @author Guillaume Laforge
 */
public class ActiveXProxy extends GroovyObjectSupport
{
    private ActiveXComponent activex;

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
        activex = new ActiveXComponent(clsId);
    }

    /**
     * Get the property
     *
     * @param s the name of the property
     * @return the value associated with the property name
     */
    public Object getProperty(String s)
    {
        return toReturn(activex.getProperty(s));
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
        Object[] objs = InvokerHelper.getInstance().asArray(parameters);
        Variant[] variants = new Variant[objs.length];
        for (int i = 0; i < variants.length; i++)
        {
            variants[i] = new Variant(objs[i]);
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
        activex.setProperty(propertyName, new Variant(newValue));
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
        activex.release();
        super.finalize();
    }
}
