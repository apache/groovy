/*
 * Created on Feb 28, 2004
 *
 */
package groovy.swt.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;


/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class HyperLinkListenerImpl implements IHyperlinkListener, ClosureSupport {
    private String type;
    private Closure closure;

    public HyperLinkListenerImpl(String type) {
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
     * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
     */
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (closure == null){
            throw new NullPointerException(
            "No closure has been configured for this Listener");
        }
        if ("hyperlinkUpdate".equals(type))
        {
            closure.call(event);
        }
    }

    /*
     * @see org.eclipse.ui.forms.events.HyperlinkListener#linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent)
     */
    public void linkEntered(HyperlinkEvent event) {
        if (closure == null){
            throw new NullPointerException(
            "No closure has been configured for this Listener");
        }
        if ("linkEntered".equals(type))
        {
            closure.call(event);
        }
    }

    /*
     * @see org.eclipse.ui.forms.events.HyperlinkListener#linkExited(org.eclipse.ui.forms.events.HyperlinkEvent)
     */
    public void linkExited(HyperlinkEvent event) {
        if (closure == null){
            throw new NullPointerException(
            "No closure has been configured for this Listener");
        }
        if ("linkExited".equals(type))
        {
            closure.call(event);
        }
    }

    /*
     * @see org.eclipse.ui.forms.events.HyperlinkListener#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
     */
    public void linkActivated(HyperlinkEvent event) {
        if (closure == null){
            throw new NullPointerException(
            "No closure has been configured for this Listener");
        }
        if ("linkActivated".equals(type))
        {
            closure.call(event);
        }
    }
}
