package org.codehaus.groovy.scriptom;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.DispatchEvents;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a hooking mechanism to use an "events" property belonging to the ActiveXProxy,
 * containing closures for the event handling.
 * <p/>
 * This "events" is backed by a Map that contains keys representing the event to subscribe to,
 * and closures representing the code to execute when the event is triggered.
 * <p/>
 * Jacob allows only to pass to the <code>DispatchEvents</code> class a class of the form:
 * <pre>
 * public class MyEvents {
 *    public void Quit(Variant[] variants) { }
 * }
 * </pre>
 * <p/>
 * To circumvent this, and to allow the users to use closures for event handling,
 * I'm building with ASM an interface, with event methods forged after the name of the keys in the map.
 * I'm then creating a <code>java.lang.reflect.Proxy</code> that delegates all calls to the Proxy
 * to my own <code>InvocationHandler</code> which is implemented by <code>EventSupport</code>.
 * All invocations gets then routed to the relevant closure in the <code>eventHandlers</code> Map.
 *
 * @author Guillaume Laforge
 */
public class EventSupport extends GroovyObjectSupport implements InvocationHandler
{
    private Map eventHandlers = new HashMap();
    private ActiveXComponent activex;
    private ScriptomClassLoader scriptomCL;
    private Object reflectionProxy;

    /**
     * In the constructor, we pass the reference to the <code>ActiveXComponent</code>,
     * and a <code>ScriptomClassLoader</code> in charge of building a custom <code>EventHandler</code> interface is created.
     *
     * @param activex the component
     */
    EventSupport(ActiveXComponent activex)
    {
        this.activex = activex;
        this.scriptomCL = new ScriptomClassLoader(activex.getClass().getClassLoader(), eventHandlers);
    }

    /**
     * Invokes directly a closure in the <code>eventHandlers</code> Map,
     * or call the <code>listen()</code> pseudo-method that triggers the creation of the <code>EventHandler</code>
     * and registers it with <code>DispatchEvents</code>.
     *
     * @param name name of the closure to call, or the "listen" pseudo-method.
     * @param args arguments to be passed to the closure
     * @return result returned by the closre
     */
    public Object invokeMethod(String name, Object args)
    {
        if ("listen".equals(name))
        {
            Class genInterface = null;
            try
            {
                // build and load the EventHandler interface
                genInterface = scriptomCL.loadClass("EventHandler");
                // create a java.lang.reflect.Proxy of the generated interface
                this.reflectionProxy = Proxy.newProxyInstance(scriptomCL, new Class[]{genInterface}, this);
                // hook up the dispatch mechanism of Jacob
                new DispatchEvents(this.activex, this.reflectionProxy);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            return null;
        }
        // call the closure from the eventHandlers Map
        return ((Closure) eventHandlers.get(name)).call(args);
    }

    /**
     * Invocation method of the <code>InvocationHandler</code> passed to the Proxy.
     *
     * @param proxy the proxy
     * @param method the name of the method to invoke
     * @param args the arguments to pass to the method
     * @return the return of the method
     * @throws Throwable thrown if the invocation fails
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        // delegates to the GroovyObjectSupport metamethod
        return invokeMethod(method.getName(), args);
    }

    /**
     * Sets the property only if a <code>Closure</code> for event handling is passed as value.
     * The name of the property represents the name of the events triggered by the ActiveX/COM component.
     * The closure is the code to be executed upon the event being triggered.
     *
     * @param property the name of the event
     * @param newValue the closure to execute
     */
    public void setProperty(String property, Object newValue)
    {
        if (newValue instanceof Closure)
            eventHandlers.put(property, newValue);
    }

    /**
     * Retrieves the closure associated with a given event.
     *
     * @param property the name of the event
     * @return the closure associated with the handling of the given event
     */
    public Object getProperty(String property)
    {
        return eventHandlers.get(property);
    }
}
