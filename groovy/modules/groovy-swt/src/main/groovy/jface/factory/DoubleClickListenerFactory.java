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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class DoubleClickListenerFactory extends AbstractSwtFactory implements SwtFactory,
        IDoubleClickListener, ClosureSupport {
    
    private Closure closure;

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {
        if (parent instanceof StructuredViewer) {
            StructuredViewer viewer = (StructuredViewer) parent;
            viewer.addDoubleClickListener(this);
        } else {
            throw new InvalidParentException("structuredViewer");
        }
        return this;
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

    /*
     * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     */
    public void doubleClick(DoubleClickEvent event) {
        closure.call(event);
    }
}