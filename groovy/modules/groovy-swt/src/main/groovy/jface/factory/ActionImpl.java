/*
 * Created on Feb 28, 2004
 *
 */
package groovy.jface.factory;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ActionImpl extends Action implements ClosureSupport {

    private Closure closure;

    private Event event;

    public void runWithEvent(Event event) {
        if (closure == null) {
            throw new NullPointerException("No closure has been configured for this Listener");
        }
        this.event = event;

        closure.call(event);
    }

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public Event getEvent() {
        return event;
    }
}