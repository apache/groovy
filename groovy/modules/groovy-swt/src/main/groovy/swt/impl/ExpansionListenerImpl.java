/*
 * Created on Feb 28, 2004
 *
 */
package groovy.swt.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ExpansionListenerImpl implements IExpansionListener, ClosureSupport{
    private String type;
    private Closure closure;

    public ExpansionListenerImpl(String type) {
        this.type = type;
    }

    public Closure getClosure()
    {
        return closure;
    }

    public void setClosure(Closure closure)
    {
        this.closure = closure;
    }

    /*
     * @see org.eclipse.ui.forms.events.ExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
     */
    public void expansionStateChanging(ExpansionEvent event) {
        if (closure == null){
            throw new NullPointerException(
            "No closure has been configured for this Listener");
        }
        if ("expansionStateChanging".equals(type))
        {
            closure.call(event);
        }
    }

    /*
     * @see org.eclipse.ui.forms.events.ExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
     */
    public void expansionStateChanged(ExpansionEvent event) {
        if (closure == null){
            throw new NullPointerException(
            "No closure has been configured for this Listener");
        }
        if ("expansionStateChanged".equals(type))
        {
            closure.call(event);
        }
    }
}
