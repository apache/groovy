/*
 * Created on Feb 25, 2004
 *
 */
package groovy.jface.factory;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;
import groovy.swt.InvalidParentException;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;

/**
 * NOTE:	I have seen strnage behavior in combination with a CellModifier
 * 			In this case just use an onEvent Selection.  
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class SelectionChangedListenerFactory extends AbstractSwtFactory
        implements SwtFactory, ISelectionChangedListener, ClosureSupport {

    private Closure closure;

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *           java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {

        if (parent instanceof Viewer) {
            Viewer viewer = (Viewer) parent;
            viewer.addSelectionChangedListener(this);
        } else {
            throw new InvalidParentException("viewer");
        }
        
        return this; 
    }

    /*
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        closure.call(event);
    }

    /*
     * @see groovy.swt.ClosureSupport#getClosure()
     */
    public Closure getClosure() {
        return closure;
    }

    /*
     * @see groovy.swt.ClosureSupport#setClosure(groovy.lang.Closure)
     */
    public void setClosure(Closure closure) {
        this.closure = closure;
    }

}
