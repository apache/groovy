/*
 * Created on Feb 16, 2004
 *
 */
package groovy.swt;

import groovy.lang.Closure;


/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public interface ClosureSupport {
    public Closure getClosure();

    public void setClosure(Closure closure);
}
