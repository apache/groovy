/*
 * Created on Feb 16, 2004
 *
 */
package groovy.swt.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ListenerImpl implements Listener, ClosureSupport {
    private Closure closure;

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public void handleEvent(Event event) {
        if (closure == null){
            throw new NullPointerException(
            "No closure has been configured for this Listener");
        }
        closure.call(event);
    }
}
