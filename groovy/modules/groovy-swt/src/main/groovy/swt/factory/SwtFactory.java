/*
 * Created on Feb 15, 2004
 *
 */
package groovy.swt.factory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public interface SwtFactory {

    /**
     * Create a new instance
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException;

}
