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
import com.jacob.com.DispatchEvents;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyShell;
import groovy.lang.Binding;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.codehaus.groovy.control.CompilationFailedException;

/**
 * Provides a hooking mechanism to use an "events" property belonging to the ActiveXProxy,
 * containing closures for the event handling.
 * <p/>
 * This "events" is backed by a Map that contains keys representing the event to subscribe to,
 * and closures representing the code to execute when the event is triggered.
 * <p/>
 * Jacob allows only to pass to the <code>DispatchEvents</code> class.
 * But Scriptom pass a dynamically generated class in Groovy through GroovyShell,
 * which delegates calls to the closures stored in the Map.
 * <p/>
 *
 * <p>
 * Event support can be done by adding closures to the event object,
 * then calling listen() method will subscribe to all events:
 * <code>
 * comProxy.events.SomeEvent = { // do something }
 * comProxy.events.OtherEvent = { // do something else }
 * comProxy.events.listen()
 * </code>
 * </p>
 *
 * @author Guillaume Laforge
 */
public class EventSupport extends GroovyObjectSupport
{
    /**
     * Map containing closures for each events which has been subscribed to
     */
    private Map eventHandlers = new HashMap();

    /**
     * Underlying Jacob ActiveXComponent
     */
    private ActiveXComponent activex;

    /**
     * Source code of the class dealing with event support
     */
    private String eventClassSourceCode = "// no event support script generated";

    /**
     * In the constructor, we pass the reference to the <code>ActiveXComponent</code>.
     *
     * @param activex the component
     */
    EventSupport(ActiveXComponent activex)
    {
        this.activex = activex;
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
            try {
                StringBuffer methods = new StringBuffer();
                for (Iterator iterator = eventHandlers.keySet().iterator(); iterator.hasNext();) {
                    String eventName = (String) iterator.next();
                    methods.append("    void ")
                    .append(eventName)
                    .append("(Variant[] variants) {\n")
                    .append("        evtHandlers['")
                    .append(eventName)
                    .append("'].call( VariantProxy.defineArray(variants) )\n    }\n");
                }

                StringBuffer classSource = new StringBuffer();
                classSource.append("import com.jacob.com.*\n")
                .append("import org.codehaus.groovy.scriptom.VariantProxy\n")
                .append("class EventHandler {\n")
                .append("    def evtHandlers\n")
                .append("    EventHandler(scriptBinding) {\n")
                .append("        evtHandlers = scriptBinding\n")
                .append("    }\n")
                .append(methods.toString())
                .append("}\n")
                .append("new EventHandler(binding)\n");

                Map eventHandlersContainer = new HashMap();
                eventHandlersContainer.put("eventHandlers", eventHandlers);
                Binding binding = new Binding(eventHandlers);
                eventClassSourceCode = classSource.toString();
                Object generatedInstance = new GroovyShell(binding).evaluate(eventClassSourceCode);

                new DispatchEvents(this.activex, generatedInstance);
            } catch (CompilationFailedException e) {
                e.printStackTrace();
            }
            return null;

        }
        else
        {
            // call the closure from the eventHandlers Map
            return ((Closure) eventHandlers.get(name)).call(args);
        }
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
        // used to print the source of the generated class dealing with event support for debugging purpose
        if ("eventSourceScript".equals(property)) {
            return eventClassSourceCode;
        }
        return eventHandlers.get(property);
    }
}
