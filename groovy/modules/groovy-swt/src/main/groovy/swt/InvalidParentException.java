/*
 * Created on Feb 16, 2004
 *
 */
package groovy.swt;

import org.codehaus.groovy.GroovyException;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class InvalidParentException extends GroovyException {

    /**
     * @param message
     */
    public InvalidParentException(String property) {
        super("parent should be: " + property);
    }
}
